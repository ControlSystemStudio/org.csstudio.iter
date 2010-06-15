/* 
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.csstudio.opibuilder.editpolicies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.csstudio.opibuilder.commands.AddWidgetCommand;
import org.csstudio.opibuilder.commands.ChangeGuideCommand;
import org.csstudio.opibuilder.commands.CloneCommand;
import org.csstudio.opibuilder.commands.SetBoundsCommand;
import org.csstudio.opibuilder.commands.WidgetCreateCommand;
import org.csstudio.opibuilder.commands.WidgetSetConstraintCommand;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.feedback.IGraphicalFeedbackFactory;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.GuideModel;
import org.csstudio.opibuilder.util.GuideUtil;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * The EditPolicy for create/move/resize a widget.
 *
 * @author Xihui Chen, Sven Wende, Kai Meyer (part of the code is copied from SDS)
 *
 */
public class WidgetXYLayoutEditPolicy extends XYLayoutEditPolicy {

	
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		IGraphicalFeedbackFactory feedbackFactory = 
			WidgetsService.getInstance().getWidgetFeedbackFactory(
					((AbstractWidgetModel)child.getModel()).getTypeID());
		if(feedbackFactory != null && child instanceof AbstractBaseEditPart){
			return new GraphicalFeedbackChildEditPolicy(
					(AbstractBaseEditPart) child, feedbackFactory);
		}else
			return super.createChildEditPolicy(child);
	}
	
	@Override
	protected Command createChangeConstraintCommand(
			ChangeBoundsRequest request, EditPart child, Object constraint) {
		if(!(child instanceof AbstractBaseEditPart) || !(constraint instanceof Rectangle))
			return super.createChangeConstraintCommand(request, child, constraint);
		AbstractBaseEditPart part = (AbstractBaseEditPart) child;
		AbstractWidgetModel widgetModel = part.getWidgetModel();

		IGraphicalFeedbackFactory feedbackFactory = 
			WidgetsService.getInstance().getWidgetFeedbackFactory(widgetModel.getTypeID());
		Command cmd;
		if(feedbackFactory != null){
			cmd = feedbackFactory.createChangeBoundsCommand(
					widgetModel, request, (Rectangle)constraint);
		}else		
			cmd = new WidgetSetConstraintCommand(
					widgetModel, request, (Rectangle)constraint);
		
		// for guide support
			
			if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
				Integer guidePos = (Integer) request.getExtendedData().get(
						SnapToGuides.KEY_HORIZONTAL_GUIDE);
				if (guidePos != null) {
					cmd = chainGuideAttachmentCommand(request, part, cmd,
							true);
				} else if (GuideUtil.getInstance().getGuide(
						widgetModel, true) != null) {
					// SnapToGuides didn't provide a horizontal guide, but
					// this part is attached
					// to a horizontal guide. Now we check to see if the
					// part is attached to
					// the guide along the edge being resized. If that is
					// the case, we need to
					// detach the part from the guide; otherwise, we leave
					// it alone.
					int alignment = GuideUtil.getInstance().getGuide(
							widgetModel, true).getAlignment(widgetModel);
					int edgeBeingResized = 0;
					if ((request.getResizeDirection() & PositionConstants.NORTH) != 0) {
						edgeBeingResized = -1;
					} else {
						edgeBeingResized = 1;
					}
					if (alignment == edgeBeingResized) {
						cmd = cmd.chain(new ChangeGuideCommand(widgetModel, true));
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
						widgetModel, false) != null) {
					int alignment = GuideUtil.getInstance().getGuide(
							widgetModel, false).getAlignment(widgetModel);
					int edgeBeingResized = 0;
					if ((request.getResizeDirection() & PositionConstants.WEST) != 0) {
						edgeBeingResized = -1;
					} else {
						edgeBeingResized = 1;
					}
					if (alignment == edgeBeingResized) {
						cmd = cmd.chain(new ChangeGuideCommand(widgetModel, false));
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
		
	return cmd;
	
	}
	
	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		return null;
	}

	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		if(!(child instanceof AbstractBaseEditPart) || !(constraint instanceof Rectangle))
			return super.createAddCommand(child, constraint);
		
		AbstractContainerModel container = (AbstractContainerModel)getHost().getModel();
		AbstractWidgetModel widget = (AbstractWidgetModel)child.getModel();
		CompoundCommand result = new CompoundCommand("Adding widgets to container");
		
		result.add(new AddWidgetCommand(container, widget));
		result.add(new SetBoundsCommand(widget, (Rectangle) constraint));
		return result;		
	}
	
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		String typeId = determineTypeIdFromRequest(request);

		IGraphicalFeedbackFactory feedbackFactory = 
			WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);
		
		WidgetCreateCommand widgetCreateCommand = new WidgetCreateCommand((AbstractWidgetModel)request.getNewObject(), 
					(AbstractContainerModel)getHost().getModel(), 
					(Rectangle)getConstraintFor(request), false);
		if(feedbackFactory != null){
			CompoundCommand compoundCommand = new CompoundCommand();
			compoundCommand.add(widgetCreateCommand);
			Command initialBoundsCommand = feedbackFactory.createInitialBoundsCommand(
					(AbstractWidgetModel)request.getNewObject(), request, (Rectangle)getConstraintFor(request));
			if(initialBoundsCommand != null)
				compoundCommand.add(initialBoundsCommand);
			return compoundCommand;
		}else
			return widgetCreateCommand;
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

		IGraphicalFeedbackFactory feedbackFactory = 
			WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);

		if(feedbackFactory != null){
			Shape feedbackFigure = feedbackFactory
				.createSizeOnDropFeedback(request);
			if(feedbackFigure != null)
				addFeedback(feedbackFigure);
			return feedbackFigure;
		}else{
			return super.createSizeOnDropFeedback(request);
		}	
	}
	
	@Override
	protected void showSizeOnDropFeedback(CreateRequest request) {
		String typeId = determineTypeIdFromRequest(request);

		IGraphicalFeedbackFactory feedbackFactory =
			WidgetsService.getInstance().getWidgetFeedbackFactory(typeId);

		if(feedbackFactory != null){
			IFigure feedbackFigure = getSizeOnDropFeedback(request);

			feedbackFactory.showSizeOnDropFeedback(request, feedbackFigure,
				getCreationFeedbackOffset(request));

			feedbackFigure.repaint();
		}else{
			super.showSizeOnDropFeedback(request);
		}

		
	}
	
	/**
	 * Creates a prototype object to determine the type identification of the
	 * widget model, that is about to be created.
	 * 
	 * @param request
	 *            the create request
	 * @return the type identification
	 */
	@SuppressWarnings("unchecked")
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
			ChangeGuideCommand cgm = new ChangeGuideCommand(
					part.getWidgetModel(), horizontal);
			cgm.setNewGuide(findGuideAt(guidePos.intValue(), horizontal),
					alignment);
			result = result.chain(cgm);
		}

		return result;
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

	
	
	@Override
	protected Command getCloneCommand(ChangeBoundsRequest request) {
		CloneCommand clone = new CloneCommand((AbstractContainerModel)getHost().getModel());
		
		for (AbstractBaseEditPart part : sortSelectedWidgets(request.getEditParts())) {	
			clone.addPart((AbstractWidgetModel)part.getModel(), (Rectangle)getConstraintForClone(part, request));
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
	 * Sort the selected widget as they were in their parents
	 * 
	 * @return a list with all widget editpart that are currently selected
	 */
	@SuppressWarnings("unchecked")
	private final List<AbstractBaseEditPart> sortSelectedWidgets(List selection) {	
		List<AbstractBaseEditPart> sameParentWidgets = new ArrayList<AbstractBaseEditPart>();
		List<AbstractBaseEditPart> differentParentWidgets = new ArrayList<AbstractBaseEditPart>();
		List<AbstractBaseEditPart> result = new ArrayList<AbstractBaseEditPart>();
		AbstractContainerModel parent = null;
		for (Object o : selection) {
			if (o instanceof AbstractBaseEditPart && !(o instanceof DisplayEditpart)) {
				AbstractWidgetModel widgetModel = 
					((AbstractBaseEditPart) o).getWidgetModel();
				if(parent == null)
					parent = widgetModel.getParent();
				if(widgetModel.getParent() == parent)
					sameParentWidgets.add((AbstractBaseEditPart) o);
				else 
					differentParentWidgets.add((AbstractBaseEditPart) o);
			}
		}
		//sort widgets to its original order
		if(sameParentWidgets.size() > 1){
			AbstractBaseEditPart[] modelArray = sameParentWidgets.toArray(new AbstractBaseEditPart[0]);
		
			Arrays.sort(modelArray, new Comparator<AbstractBaseEditPart>(){

				public int compare(AbstractBaseEditPart o1,
						AbstractBaseEditPart o2) {
					if(o1.getWidgetModel().getParent().getChildren().indexOf(o1.getWidgetModel()) > 
						o2.getWidgetModel().getParent().getChildren().indexOf(o2.getWidgetModel()))
						return 1;
					else
						return -1;					
				}
				
			});
			result.addAll(Arrays.asList(modelArray));
			if(differentParentWidgets.size() > 0)
				result.addAll(differentParentWidgets);
			return result;
		}
		if(differentParentWidgets.size() > 0)
			sameParentWidgets.addAll(differentParentWidgets);
		
		return sameParentWidgets;
	}

}