package org.csstudio.sds.ui.editparts;

import java.util.List;

import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.ContainerModel;
import org.csstudio.sds.model.DisplayModel;
import org.csstudio.sds.model.GuideModel;
import org.csstudio.sds.ui.feedback.IGraphicalFeedbackFactory;
import org.csstudio.sds.ui.internal.commands.AddWidgetCommand;
import org.csstudio.sds.ui.internal.commands.ChangeGuideCommand;
import org.csstudio.sds.ui.internal.commands.CloneCommand;
import org.csstudio.sds.ui.internal.commands.CreateElementCommand;
import org.csstudio.sds.ui.internal.commands.SetBoundsCommand;
import org.csstudio.sds.ui.internal.feedback.GraphicalFeedbackContributionsService;
import org.csstudio.sds.util.GuideUtil;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * The EditPolicy for {@link DisplayModel}. It can be used with
 * <code>Figures</code> in {@link XYLayout}. The constraint for XYLayout is a
 * {@link org.eclipse.draw2d.geometry.Rectangle}.
 * 
 * @author Sven Wende, Kai Meyer
 */
final class ModelXYLayoutEditPolicy extends XYLayoutEditPolicy {

	/**
	 * Overriden, to provide a generic EditPolicy for children, which is aware
	 * of different feedback and selection handles. {@inheritDoc}
	 */
	@Override
	protected EditPolicy createChildEditPolicy(final EditPart child) {
		return new GenericChildEditPolicy(child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command createAddCommand(final EditPart child,
			final Object constraint) {
		assert child != null : "child!=null"; //$NON-NLS-1$
		assert constraint instanceof Rectangle : "constraint instanceof Rectangle"; //$NON-NLS-1$

		ContainerModel container = (ContainerModel) getHost().getModel();
		AbstractWidgetModel widget = (AbstractWidgetModel) child.getModel();
		CompoundCommand compoundCommand = new CompoundCommand();
		Command cmd = new AddWidgetCommand(container, widget);
		compoundCommand.add(cmd);

		cmd = new SetBoundsCommand(widget, (Rectangle) constraint);
		compoundCommand.add(cmd);

		return compoundCommand;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command createChangeConstraintCommand(
			final ChangeBoundsRequest request, final EditPart child,
			final Object constraint) {
		assert child != null : "child!=null"; //$NON-NLS-1$
		assert request != null : "request!=null"; //$NON-NLS-1$
		assert constraint != null : "constraint!=null"; //$NON-NLS-1$
		assert constraint instanceof Rectangle : "constraint instanceof Rectangle"; //$NON-NLS-1$
		AbstractWidgetModel childModel = (AbstractWidgetModel) child.getModel();
		Command cmd = null;
		IGraphicalFeedbackFactory factory = determineFeedbackFactory(childModel
				.getTypeID());

		if (factory != null) {
			cmd = factory.createChangeBoundsCommand(childModel, request,
					(Rectangle) constraint);
			// for guide support
			if (child instanceof AbstractBaseEditPart) {
				AbstractBaseEditPart part = (AbstractBaseEditPart) child;
				if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
					Integer guidePos = (Integer) request.getExtendedData().get(
							SnapToGuides.KEY_HORIZONTAL_GUIDE);
					if (guidePos != null) {
						cmd = chainGuideAttachmentCommand(request, part, cmd,
								true);
					} else if (GuideUtil.getInstance().getGuide(
							part.getWidgetModel(), true) != null) {
						// SnapToGuides didn't provide a horizontal guide, but
						// this part is attached
						// to a horizontal guide. Now we check to see if the
						// part is attached to
						// the guide along the edge being resized. If that is
						// the case, we need to
						// detach the part from the guide; otherwise, we leave
						// it alone.
						int alignment = GuideUtil.getInstance().getGuide(
								part.getWidgetModel(), true).getAlignment(
								part.getWidgetModel());
						int edgeBeingResized = 0;
						if ((request.getResizeDirection() & PositionConstants.NORTH) != 0) {
							edgeBeingResized = -1;
						} else {
							edgeBeingResized = 1;
						}
						if (alignment == edgeBeingResized) {
							cmd = cmd.chain(new ChangeGuideCommand(part
									.getWidgetModel(), true));
						}
					}
				}

				if ((request.getResizeDirection() & PositionConstants.EAST_WEST) != 0) {
					Integer guidePos = (Integer) request.getExtendedData().get(
							SnapToGuides.KEY_VERTICAL_GUIDE);
					if (guidePos != null) {
						cmd = chainGuideAttachmentCommand(request, part, cmd,
								false);
					} else if (GuideUtil.getInstance().getGuide(
							part.getWidgetModel(), false) != null) {
						int alignment = GuideUtil.getInstance().getGuide(
								part.getWidgetModel(), false).getAlignment(
								part.getWidgetModel());
						int edgeBeingResized = 0;
						if ((request.getResizeDirection() & PositionConstants.WEST) != 0) {
							edgeBeingResized = -1;
						} else {
							edgeBeingResized = 1;
						}
						if (alignment == edgeBeingResized) {
							cmd = cmd.chain(new ChangeGuideCommand(part
									.getWidgetModel(), false));
						}
					}
				}

				if (request.getType().equals(REQ_MOVE_CHILDREN)
						|| request.getType().equals(REQ_ALIGN_CHILDREN)) {
					cmd = chainGuideAttachmentCommand(request, part, cmd, true);
					cmd = chainGuideAttachmentCommand(request, part, cmd, false);
					cmd = chainGuideDetachmentCommand(request, part, cmd, true);
					cmd = chainGuideDetachmentCommand(request, part, cmd, false);
				}
			}
		}

		return cmd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command createChangeConstraintCommand(final EditPart child,
			final Object constraint) {
		assert false : "This method should never get called."; //$NON-NLS-1$
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command getCreateCommand(final CreateRequest request) {
		ContainerModel container = (ContainerModel) getHost().getModel();
		Rectangle bounds = (Rectangle) getConstraintFor(request);
		Command cmd = new CreateElementCommand(container, request, bounds);

		return cmd;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Command getCloneCommand(final ChangeBoundsRequest request) {
		CloneCommand clone = new CloneCommand((DisplayModel)getHost().getModel());
		
		GraphicalEditPart currPart = null;
		for (Object part : request.getEditParts()) {
			currPart = (GraphicalEditPart)part;
			clone.addPart((AbstractWidgetModel)currPart.getModel(), (Rectangle)getConstraintForClone(currPart, request));
		}
		
		// Attach to horizontal guide, if one is given
		Integer guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.KEY_HORIZONTAL_GUIDE);
		if (guidePos != null) {
			int hAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.KEY_HORIZONTAL_ANCHOR)).intValue();
			clone.setGuide(findGuideAt(guidePos.intValue(), true), hAlignment, true);
		}
		
		// Attach to vertical guide, if one is given
		guidePos = (Integer)request.getExtendedData()
				.get(SnapToGuides.KEY_VERTICAL_GUIDE);
		if (guidePos != null) {
			int vAlignment = ((Integer)request.getExtendedData()
					.get(SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
			clone.setGuide(findGuideAt(guidePos.intValue(), false), vAlignment, false);
		}
		return clone;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void showTargetFeedback(final Request request) {
		if (REQ_ADD.equals(request.getType())
				|| REQ_CLONE.equals(request.getType())
				|| REQ_MOVE.equals(request.getType())
				|| REQ_RESIZE_CHILDREN.equals(request.getType())
				|| REQ_CREATE.equals(request.getType())) {
			showLayoutTargetFeedback(request);
		}

		if (REQ_CREATE.equals(request.getType())) {
			CreateRequest createReq = (CreateRequest) request;
			if (createReq.getSize() != null) {
				showSizeOnDropFeedback(createReq);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void showSizeOnDropFeedback(final CreateRequest request) {
		String typeId = determineTypeIdFromRequest(request);

		IGraphicalFeedbackFactory feedbackFactory = determineFeedbackFactory(typeId);

		assert feedbackFactory != null;

		IFigure feedbackFigure = getSizeOnDropFeedback(request);

		feedbackFactory.showSizeOnDropFeedback(request, feedbackFigure,
				getCreationFeedbackOffset(request));

		feedbackFigure.repaint();
	}

	/**
	 * Creates a prototype object to determine the type identification of the
	 * widget model, that is about to be created.
	 * 
	 * @param request
	 *            the create request
	 * @return the type identification
	 */
	private String determineTypeIdFromRequest(final CreateRequest request) {
		Class newObject = (Class) request.getNewObjectType();
		AbstractWidgetModel instance;
		String typeId = ""; //$NON-NLS-1$
		try {
			instance = (AbstractWidgetModel) newObject.newInstance();
			typeId = instance.getTypeID();
		} catch (InstantiationException e) {
			CentralLogger.getInstance().error(this, e);
		} catch (IllegalAccessException e) {
			CentralLogger.getInstance().error(this, e);
		}

		return typeId;
	}

	/**
	 * Override to provide custom feedback figure for the given create request.
	 * 
	 * @param request
	 *            the create request
	 * @return custom feedback figure
	 */
	@Override
	protected IFigure createSizeOnDropFeedback(final CreateRequest request) {
		String typeId = determineTypeIdFromRequest(request);

		IGraphicalFeedbackFactory feedbackFactory = determineFeedbackFactory(typeId);

		Shape feedbackFigure = feedbackFactory
				.createSizeOnDropFeedback(request);

		addFeedback(feedbackFigure);

		return feedbackFigure;
	}

	/**
	 * Gets the determining FeedbackFactory for the given typeID.
	 * 
	 * @param typeId
	 *            The identifier for the FeedbackFactory
	 * @return IGraphicalFeedbackFactory The requested IGraphicalFeedbackFactory
	 */
	protected static IGraphicalFeedbackFactory determineFeedbackFactory(
			final String typeId) {

		IGraphicalFeedbackFactory feedbackFactory = GraphicalFeedbackContributionsService
				.getInstance().getGraphicalFeedbackFactory(typeId);
		return feedbackFactory;
	}

	/**
	 * Adds a ChangeGuideCommand to the given Command.
	 * 
	 * @param request
	 *            The Request
	 * @param part
	 *            The AbstractWidgetEditPart, which model should be detached
	 *            from a guide
	 * @param cmd
	 *            The Command
	 * @param horizontal
	 *            A boolean, true if the guide is horizontal, false otherwise
	 * @return Command The given command
	 */
	private Command chainGuideAttachmentCommand(final Request request,
			final AbstractBaseEditPart part, final Command cmd,
			final boolean horizontal) {
		Command result = cmd;

		// Attach to guide, if one is given
		Integer guidePos = (Integer) request.getExtendedData().get(
				horizontal ? SnapToGuides.KEY_HORIZONTAL_GUIDE
						: SnapToGuides.KEY_VERTICAL_GUIDE);
		if (guidePos != null) {
			int alignment = ((Integer) request.getExtendedData().get(
					horizontal ? SnapToGuides.KEY_HORIZONTAL_ANCHOR
							: SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
			ChangeGuideCommand cgm = new ChangeGuideCommand(part
					.getWidgetModel(), horizontal);
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), horizontal),
					alignment);
			result = result.chain(cgm);
		}

		return result;
	}

	/**
	 * Returns the guide at the given position and with the given orientation.
	 * 
	 * @param pos
	 *            The Position of the guide
	 * @param horizontal
	 *            The orientation of the guide
	 * @return GuideModel The GuideModel
	 */
	private GuideModel findGuideAt(final int pos, final boolean horizontal) {
		RulerProvider provider = ((RulerProvider) getHost().getViewer()
				.getProperty(
						horizontal ? RulerProvider.PROPERTY_VERTICAL_RULER
								: RulerProvider.PROPERTY_HORIZONTAL_RULER));
		return (GuideModel) provider.getGuideAt(pos);
	}

	/**
	 * Adds a ChangeGuideCommand to the given Command.
	 * 
	 * @param request
	 *            The request
	 * @param part
	 *            The AbstractWidgetEditPart, which model should be detached
	 *            from a guide
	 * @param cmd
	 *            The Command
	 * @param horizontal
	 *            A boolean, true if the guide is horizontal, false otherwise
	 * @return Command The given command
	 */
	private Command chainGuideDetachmentCommand(final Request request,
			final AbstractBaseEditPart part, final Command cmd,
			final boolean horizontal) {
		Command result = cmd;

		// Detach from guide, if none is given
		Integer guidePos = (Integer) request.getExtendedData().get(
				horizontal ? SnapToGuides.KEY_HORIZONTAL_GUIDE
						: SnapToGuides.KEY_VERTICAL_GUIDE);
		if (guidePos == null) {
			result = result.chain(new ChangeGuideCommand(part.getWidgetModel(),
					horizontal));
		}

		return result;
	}

	/**
	 * Provides support for selecting, positioning, and resizing an editpart. By
	 * default, selection is indicated via eight square handles along the
	 * editpart's figure, and a rectangular handle that outlines the editpart
	 * with a 1-pixel black line. The eight square handles will resize the
	 * current selection in the eight primary directions. The rectangular handle
	 * will drag the current selection using a {@link
	 * org.eclipse.gef.tools.DragEditPartsTracker}.
	 * <P>
	 * By default, during feedback, a rectangle filled using XOR and outlined
	 * with dashes is drawn. This feedback can be tailored by contributing a
	 * {@link IGraphicalFeedbackFactory} via the extension point
	 * org.csstudio.sds.graphicalFeedbackFactories.
	 * 
	 * @author Sven Wende
	 * 
	 */
	protected final class GenericChildEditPolicy extends ResizableEditPolicy {
		/**
		 * The edit part.
		 */
		private final EditPart _child;

		/**
		 * Standard constructor.
		 * 
		 * @param child
		 *            An edit part.
		 */
		protected GenericChildEditPolicy(final EditPart child) {
			_child = child;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected IFigure createDragSourceFeedbackFigure() {
			assert _child.getModel() instanceof AbstractWidgetModel : "widget models must be derived from AbstractWidgetModel"; //$NON-NLS-1$"

			String typeId = ((AbstractWidgetModel) _child.getModel())
					.getTypeID();

			IGraphicalFeedbackFactory feedbackFactory = determineFeedbackFactory(typeId);

			IFigure feedbackFigure = feedbackFactory
					.createDragSourceFeedbackFigure(
							(AbstractWidgetModel) _child.getModel(),
							getInitialFeedbackBounds());

			addFeedback(feedbackFigure);

			return feedbackFigure;
		}

		/**
		 * Shows or updates feedback for a change bounds request.
		 * 
		 * @param request
		 *            the request
		 */
		@Override
		protected void showChangeBoundsFeedback(
				final ChangeBoundsRequest request) {
			assert _child.getModel() instanceof AbstractWidgetModel : "widget models must be derived from AbstractWidgetModel"; //$NON-NLS-1$"

			String typeId = ((AbstractWidgetModel) _child.getModel())
					.getTypeID();

			IGraphicalFeedbackFactory feedbackFactory = determineFeedbackFactory(typeId);

			IFigure feedbackFigure = getDragSourceFeedbackFigure();

			PrecisionRectangle rect = new PrecisionRectangle(
					getInitialFeedbackBounds().getCopy());
			getHostFigure().translateToAbsolute(rect);

			Point moveDelta = request.getMoveDelta();
			rect.translate(moveDelta);

			Dimension sizeDelta = request.getSizeDelta();
			rect.resize(sizeDelta);

			feedbackFactory.showChangeBoundsFeedback(
					(AbstractWidgetModel) getHost().getModel(), rect,
					feedbackFigure, request);

			feedbackFigure.repaint();
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected List createSelectionHandles() {
			// get default handles
			List handleList = super.createSelectionHandles();

			// add contributed handles
			assert _child.getModel() instanceof AbstractWidgetModel : "widget models must be derived from AbstractWidgetModel"; //$NON-NLS-1$"

			String typeId = ((AbstractWidgetModel) _child.getModel())
					.getTypeID();

			IGraphicalFeedbackFactory feedbackFactory = determineFeedbackFactory(typeId);

			GraphicalEditPart hostEP = (GraphicalEditPart) getHost();

			List contributedHandles = feedbackFactory
					.createCustomHandles(hostEP);

			if (contributedHandles != null) {
				handleList.addAll(contributedHandles);
			}

			return handleList;

		}
	}
}