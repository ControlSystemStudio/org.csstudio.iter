package org.csstudio.opibuilder.widgets.editparts;

import org.csstudio.opibuilder.editparts.AbstractWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgetActions.ActionsInput;
import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.csstudio.swt.widgets.figures.LabelFigure;
import org.csstudio.swt.widgets.figures.LabelFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.LabelFigure.V_ALIGN;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.widgets.Display;

/**The editpart for Label widget.
 * @author jbercic (class of same name in SDS)
 * @author Xihui Chen
 *
 */
public class LabelEditPart extends AbstractWidgetEditPart {

	
	@Override
	protected IFigure doCreateFigure() {
		LabelFigure labelFigure = new LabelFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
		labelFigure.setFont(CustomMediaFactory.getInstance().getFont(
				getWidgetModel().getFont().getFontData()));
		labelFigure.setOpaque(!getWidgetModel().isTransparent());
		labelFigure.setHorizontalAlignment(getWidgetModel().getHorizontalAlignment());
		labelFigure.setVerticalAlignment(getWidgetModel().getVerticalAlignment());
		labelFigure.setSelectable(
				!getWidgetModel().getActionsInput().getActionsList().isEmpty() ||
				getWidgetModel().getTooltip().trim().length() > 0);
		return labelFigure;
	}
	
	@Override
	public void activate() {
		super.activate();
		((LabelFigure)getFigure()).setText(getWidgetModel().getText());		
		if(getWidgetModel().isAutoSize()){
			getWidgetModel().setSize(((LabelFigure)figure).getAutoSizeDimension());
			figure.revalidate();
		}
	}
	
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		if(getExecutionMode() == ExecutionMode.EDIT_MODE)
			installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new LabelDirectEditPolicy());
		
	}
	
	@Override
	protected void registerPropertyChangeHandlers() {
		IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					final IFigure figure) {
				((LabelFigure)figure).setText((String)newValue);
				Display.getCurrent().timerExec(10, new Runnable() {					
					public void run() {
						if(getWidgetModel().isAutoSize())
							getWidgetModel().setSize(((LabelFigure)figure).getAutoSizeDimension());
					}
				});
				
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_TEXT, handler);
		
		
		IWidgetPropertyChangeHandler fontHandler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				figure.setFont(CustomMediaFactory.getInstance().getFont(
						((OPIFont)newValue).getFontData()));
				return true;
			}
		};		
		setPropertyChangeHandler(LabelModel.PROP_FONT, fontHandler);
	
		IWidgetPropertyChangeHandler clickableHandler = new IWidgetPropertyChangeHandler() {
			
			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				((LabelFigure)figure).setSelectable(
						!((ActionsInput)newValue).getActionsList().isEmpty() || 
						getWidgetModel().getTooltip().trim().length() > 0);
				return false;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_ACTIONS, clickableHandler);
		setPropertyChangeHandler(LabelModel.PROP_TOOLTIP, clickableHandler);

		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					final IFigure figure) {
				Display.getCurrent().timerExec(10, new Runnable() {					
					public void run() {
						if(getWidgetModel().isAutoSize()){
							getWidgetModel().setSize(((LabelFigure)figure).getAutoSizeDimension());
							figure.revalidate();
						}
					}
				});
				
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_FONT, handler);		
		setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handler);
		setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((LabelFigure)figure).setOpaque(!(Boolean)newValue);
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_TRANSPARENT, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {				
				if((Boolean)newValue){
					getWidgetModel().setSize(((LabelFigure)figure).getAutoSizeDimension());
					figure.revalidate();
				}
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_AUTOSIZE, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((LabelFigure)figure).setHorizontalAlignment(H_ALIGN.values()[(Integer)newValue]);
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_ALIGN_H, handler);
		
		handler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((LabelFigure)figure).setVerticalAlignment(V_ALIGN.values()[(Integer)newValue]);
				return true;
			}
		};
		setPropertyChangeHandler(LabelModel.PROP_ALIGN_V, handler);
		
		

		
	}

	private void performDirectEdit(){
		new LabelEditManager(this, new LabelCellEditorLocator((LabelFigure)getFigure())).show();
	}
	
	@Override
	public void performRequest(Request request){
		if (getExecutionMode() == ExecutionMode.EDIT_MODE &&( 
				request.getType() == RequestConstants.REQ_DIRECT_EDIT || 
				request.getType() == RequestConstants.REQ_OPEN))
			performDirectEdit();
	}
	
	
	@Override
	public LabelModel getWidgetModel() {
		return (LabelModel)getModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class key) {
		if(key == LabelFigure.class)
			return ((LabelFigure)getFigure());

		return super.getAdapter(key);
	}

}