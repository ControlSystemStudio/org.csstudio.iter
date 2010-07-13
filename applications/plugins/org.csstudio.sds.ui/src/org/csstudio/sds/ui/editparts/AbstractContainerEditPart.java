/* 
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton, 
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
package org.csstudio.sds.ui.editparts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.sds.model.AbstractWidgetModel;
import org.csstudio.sds.model.ContainerModel;
import org.csstudio.sds.model.layers.ILayerModelListener;
import org.csstudio.sds.model.layers.Layer;
import org.csstudio.sds.model.layers.LayerSupport;
import org.csstudio.sds.ui.CheckedUiRunnable;
import org.csstudio.sds.ui.internal.commands.CreateWidgetFromDroppedPvCommand;
import org.csstudio.sds.ui.internal.commands.DeleteElementCommand;
import org.csstudio.sds.ui.internal.commands.OrphanChildCommand;
import org.csstudio.sds.ui.internal.editor.DropPvRequest;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.rulers.RulerProvider;

/**
 * EditPart implementation with container semantics. Can be used as controller
 * base class for widgets that are derived from {@link ContainerModel}.
 * 
 * @author Sven Wende
 * 
 */
public abstract class AbstractContainerEditPart extends AbstractBaseEditPart
		implements IAdaptable, ILayerModelListener {

	/**
	 * Flag for the selection behaviour of children.
	 */
	private boolean _childrenSelectable;

	/**
	 * Flag which indicates that the layers are already initialized on the
	 * content pane.
	 */
	private boolean _layersInitialized = false;

	public AbstractContainerEditPart() {
		_childrenSelectable = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activate() {
		super.activate();
		// listen to the layer model
		getContainerModel().getLayerSupport().addLayerModelListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deactivate() {
		// listen to the layer model
		getContainerModel().getLayerSupport().removeLayerModelListener(this);
		super.deactivate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPropertyChangeHandlers() {
		// do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFigure getContentPane() {
		IFigure f = super.getContentPane();

		// initialize the layers
		if (f instanceof LayeredWidgetPane && !_layersInitialized) {
			LayeredWidgetPane layeredWidgetPane = (LayeredWidgetPane) f;
			int i = 0;
			for (Layer layer : getContainerModel().getLayerSupport()
					.getLayers()) {
				layeredWidgetPane.addLayer(layer.getId(), i);
				i++;
			}
			_layersInitialized = true;
		}

		return f;
	}

	/**
	 * Returns the container model, which is managed by this controller. This is
	 * for convinience only. The method returns the same object as
	 * {@link #getModel()} or {@link #getWidgetModel()}.
	 * 
	 * @return the container model
	 */
	public final ContainerModel getContainerModel() {
		return (ContainerModel) getModel();
	}

	/**
	 * Overidden to suppport layers. Layers are only supported if the figure for
	 * this editpart used a {@link LayeredWidgetPane} as its content pane.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected final void addChildVisual(final EditPart childEditPart,
			final int index) {
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		IFigure contentPane = getContentPane();

		AbstractWidgetModel widgetModel = (AbstractWidgetModel) childEditPart
				.getModel();

		if (contentPane instanceof LayeredWidgetPane) {
			Layer layer = getContainerModel().getLayerSupport().findLayer(
					widgetModel.getLayer());
			((LayeredWidgetPane) contentPane).addWidget(layer.getId(), child,
					index);
		} else {
			super.addChildVisual(childEditPart, index);
		}
	}

	/**
	 * Overidden to suppport layers. Layers are only supported if the figure for
	 * this editpart used a {@link LayeredWidgetPane} as its content pane.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected final void removeChildVisual(final EditPart childEditPart) {
		IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
		IFigure contentPane = getContentPane();
		if (contentPane instanceof LayeredWidgetPane) {
			((LayeredWidgetPane) contentPane).removeWidget(child);
		} else {
			super.removeChildVisual(childEditPart);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			@Override
			protected Command createDeleteCommand(
					final GroupRequest deleteRequest) {
				ContainerModel model = (ContainerModel) getHost().getParent()
						.getModel();
				AbstractWidgetModel widgetModel = (AbstractWidgetModel) getHost()
						.getModel();
				return new DeleteElementCommand(model, widgetModel);
			}

			@Override
			public Command getCommand(final Request request) {
				return super.getCommand(request);
			}

			@Override
			public EditPart getTargetEditPart(final Request request) {
				return super.getTargetEditPart(request);
			}
		});
		installEditPolicy(EditPolicy.NODE_ROLE, null);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);

		// to prevent selection and manipulation of widgets in the the Run Mode,
		// the layout role is only installed for the Edit Mode
		installEditPolicy(
				EditPolicy.LAYOUT_ROLE,
				getExecutionMode() == ExecutionMode.EDIT_MODE ? new ModelXYLayoutEditPolicy()
						: null);

		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ContainerEditPolicy() {
			@Override
			public Command getCommand(final Request request) {
				if (DropPvRequest.REQ_DROP_PV.equals(request.getType())) {
					return new CreateWidgetFromDroppedPvCommand(
							(DropPvRequest) request, (ContainerModel) getHost()
									.getModel());
				}
				return super.getCommand(request);
			}

			@Override
			protected Command getCreateCommand(final CreateRequest request) {
				return null;
			}

			@Override
			public Command getOrphanChildrenCommand(final GroupRequest request) {
				List parts = request.getEditParts();
				CompoundCommand result = new CompoundCommand(""); //$NON-NLS-1$
				for (int i = 0; i < parts.size(); i++) {
					ContainerModel container = (ContainerModel) getHost()
							.getModel();
					AbstractWidgetModel widget = (AbstractWidgetModel) ((EditPart) parts
							.get(i)).getModel();
					OrphanChildCommand orphan = new OrphanChildCommand(
							container, widget);
					result.add(orphan);
				}
				return result.unwrap();
			}

			@Override
			public EditPart getTargetEditPart(final Request request) {
				if (DropPvRequest.REQ_DROP_PV.equals(request.getType())) {
					return getHost();
				}
				return super.getTargetEditPart(request);
			}

		});

		installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final synchronized List getModelChildren() {
		List<AbstractWidgetModel> modelChildren = getContainerModel()
				.getWidgets();
		return modelChildren;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final Object getAdapter(final Class adapter) {
		if (adapter == SnapToHelper.class) {
			List snapStrategies = new ArrayList();
			Boolean val = (Boolean) getViewer().getProperty(
					RulerProvider.PROPERTY_RULER_VISIBILITY);
			if ((val != null) && val.booleanValue()) {
				snapStrategies.add(new SnapToGuides(this));
			}
			val = (Boolean) getViewer().getProperty(
					SnapToGeometry.PROPERTY_SNAP_ENABLED);
			if ((val != null) && val.booleanValue()) {
				snapStrategies.add(new SnapToGeometry(this));
			}
			val = (Boolean) getViewer().getProperty(
					SnapToGrid.PROPERTY_GRID_ENABLED);
			if ((val != null) && val.booleanValue()) {
				snapStrategies.add(new SnapToGrid(this));
			}

			if (snapStrategies.size() == 0) {
				return null;
			}
			if (snapStrategies.size() == 1) {
				return snapStrategies.get(0);
			}

			SnapToHelper[] ss = new SnapToHelper[snapStrategies.size()];
			for (int i = 0; i < snapStrategies.size(); i++) {
				ss[i] = (SnapToHelper) snapStrategies.get(i);
			}
			return new CompoundSnapToHelper(ss);
		}

		// else if (adapter == AutoexposeHelper.class) {
		// return new ViewportAutoexposeHelper(this);
		// }

		return super.getAdapter(adapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final synchronized void propertyChange(final PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		new CheckedUiRunnable() {
			@Override
			protected void doRunInUi() {
				String prop = evt.getPropertyName();

				if (prop.equals(ContainerModel.PROP_CHILD_ADDED)) {
					AbstractWidgetModel model = (AbstractWidgetModel) evt
							.getNewValue();
					addChild(createChild(model), getChildren().size());
				} else if (prop.equals(ContainerModel.PROP_CHILD_REMOVED)) {
					refreshChildren();
				} else if (prop.equals(ContainerModel.PROP_ORDER_CHANGED)) {
					refreshChildren();
				}
			}

		};

	}

	/**
	 * {@inheritDoc}
	 */
	public final void layerChanged(
			final org.csstudio.sds.model.layers.Layer layer,
			final String property) {
		if (getContentPane() instanceof LayeredWidgetPane) {
			final LayeredWidgetPane layeredWidgetPane = (LayeredWidgetPane) getContentPane();

			if (property.equals(LayerSupport.PROP_LAYER_MOVED)) {
				layeredWidgetPane.moveLayer(layer.getId(), getContainerModel()
						.getLayerSupport().getLayerIndex(layer));
			} else if (property.equals(LayerSupport.PROP_LAYER_ADDED)) {
				new CheckedUiRunnable() {

					@Override
					protected void doRunInUi() {
						layeredWidgetPane.addLayer(layer.getId(), getContainerModel()
								.getLayerSupport().getLayerIndex(layer));
						layeredWidgetPane.setVisibility(layer.getId(), layer
								.isVisible());		
					}
					
				};
				
			} else if (property.equals(LayerSupport.PROP_LAYER_REMOVED)) {
				layeredWidgetPane.removeLayer(layer.getId(),
						getContainerModel().getLayerSupport().getDefaultLayer()
								.getId());
			} else if (property
					.equals(org.csstudio.sds.model.layers.Layer.PROP_VISIBLE)) {
				layeredWidgetPane.setVisibility(layer.getId(), layer
						.isVisible());
			}
		}
	}

	/**
	 * Sets the selection behaviour for children of this container.
	 * 
	 * @param childrenSelectable
	 *            true, if children of this container should be selectable,
	 *            false otherwise
	 */
	public final void setChildrenSelectable(final boolean childrenSelectable) {
		_childrenSelectable = childrenSelectable;
	}

	/**
	 * {@inheritDoc} Overidden, to set the selection behaviour of child
	 * EditParts.
	 */
	@Override
	protected final EditPart createChild(final Object model) {
		EditPart result = super.createChild(model);
		// setup selection behaviour for the new child
		if (result instanceof AbstractBaseEditPart) {
			((AbstractBaseEditPart) result).setSelectable(_childrenSelectable);
		}

		return result;
	}

	/**
	 * Called by child editparts when their current layer has changed.
	 * 
	 * Note: Layers are only supported if the figure for this editpart used a
	 * {@link LayeredWidgetPane} as its content pane. Otherwise this call won�t
	 * have any effect.
	 * 
	 * @param childEditPart
	 *            the child editpart
	 * @param oldLayerName
	 *            the old name of the layer
	 * @param newLayerName
	 *            the new name of the layer
	 * 
	 * FIXME: Auf Layer typisieren, sobald Backend entsprechend umgestellt
	 */
	protected final void handleLayerChanged(
			final AbstractGraphicalEditPart childEditPart,
			final String oldLayerName, final String newLayerName) {
		if (getContentPane() instanceof LayeredWidgetPane) {

			// FIXME: Layers: Bitte keine Layer implizit anlegen lassen! Und
			// erst recht nicht hier!
			LayeredWidgetPane contentPane = (LayeredWidgetPane) getContentPane();
			if (!contentPane.hasLayer(newLayerName)) {
				this.getContainerModel().getLayerSupport().addLayer(
						new Layer(newLayerName, newLayerName));
			}
			contentPane.moveWidget(childEditPart.getFigure(), oldLayerName,
					newLayerName);
		}
	}

}