package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.AbstractBoolControlFigure;
import org.csstudio.opibuilder.widgets.figures.AbstractBoolFigure;
import org.csstudio.opibuilder.widgets.figures.AbstractBoolControlFigure.IBoolControlListener;
import org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.eclipse.draw2d.IFigure;

/**
 * Base editPart controller for a widget based on {@link AbstractBoolControlModel}.
 * 
 * @author Xihui Chen
 * 
 */
public abstract class AbstractBoolControlEditPart extends AbstractBoolEditPart {

	/**
	 * Sets those properties on the figure that are defined in the
	 * {@link AbstractBoolFigure} base class. This method is provided for the
	 * convenience of subclasses, which can call this method in their
	 * implementation of {@link AbstractBaseEditPart#doCreateFigure()}.
	 * 
	 * @param figure
	 *            the figure.
	 * @param model
	 *            the model.
	 */
	protected void initializeCommonFigureProperties(
			final AbstractBoolControlFigure figure, final AbstractBoolControlModel model) {
		super.initializeCommonFigureProperties(figure, model);
		figure.setToggle(model.isToggleButton());
		figure.setShowConfirmDialog(model.isShowConfirmDialog());
		figure.setConfirmTip(model.getConfirmTip());
		figure.setPassword(model.getPassword());
		figure.setRunMode(getExecutionMode().equals(
				ExecutionMode.RUN_MODE));
		figure.addBoolControlListener(new IBoolControlListener() {
			public void valueChanged(final double newValue) {
				if (getExecutionMode() == ExecutionMode.RUN_MODE){
					if(getWidgetModel().getDataType() == 0)
						setPVValue(AbstractBoolControlModel.PROP_PVNAME, newValue);
					else
						setPVValue(AbstractBoolWidgetModel.PROP_PVNAME, newValue<=0.01 ? 
								getWidgetModel().getOffState() : getWidgetModel().getOnState());
				}
			}
		});		
		markAsControlPV(AbstractBoolControlModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);
	}	
	
	/**
	 * Registers property change handlers for the properties defined in
	 * {@link AbstractBoolWidgetModel}. This method is provided for the convenience
	 * of subclasses, which can call this method in their implementation of
	 * {@link #registerPropertyChangeHandlers()}.
	 */
	protected void registerCommonPropertyChangeHandlers() {
		
		configureButtonListener((AbstractBoolControlFigure) getFigure());
		
		super.registerCommonPropertyChangeHandlers();
		
		// toggle button
		final IWidgetPropertyChangeHandler toggleHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolControlFigure figure = (AbstractBoolControlFigure) refreshableFigure;
				figure.setToggle((Boolean) newValue);
				return true;
			}
		};
		getWidgetModel().getProperty(AbstractBoolControlModel.PROP_TOGGLE_BUTTON).
		addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				toggleHandler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
			}
		});
		
		
		//setPropertyChangeHandler(AbstractBoolControlModel.PROP_TOGGLE_BUTTON, handler);
		
		// show confirm dialog
		IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolControlFigure figure = (AbstractBoolControlFigure) refreshableFigure;
				figure.setShowConfirmDialog((Boolean) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolControlModel.PROP_CONFIRM_DIALOG, handler);
				
		// confirm tip
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolControlFigure figure = (AbstractBoolControlFigure) refreshableFigure;
				figure.setConfirmTip((String) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolControlModel.PROP_CONFIRM_TIP, handler);
		
		// password
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolControlFigure figure = (AbstractBoolControlFigure) refreshableFigure;
				figure.setPassword((String) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolControlModel.PROP_PASSWORD, handler);		
		
		//enabled. WidgetBaseEditPart will force the widget as disabled in edit model,
		//which is not the case for the bool control widget
		IWidgetPropertyChangeHandler enableHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolControlFigure figure = (AbstractBoolControlFigure) refreshableFigure;
				figure.setEnabled((Boolean) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolControlModel.PROP_ENABLED, enableHandler);
		
	}
	
	@Override
	public AbstractBoolControlModel getWidgetModel() {
		return (AbstractBoolControlModel)getModel();
	}
	
	/**
	 * Configures a listener for performing a {@link AbstractWidgetActionModel}.
	 * 
	 * @param figure
	 *            The figure of the widget
	 */
	private void configureButtonListener(
			final AbstractBoolControlFigure figure) {
		figure.addBoolControlListener(new IBoolControlListener() {
			public void valueChanged(double newValue) {		
				// If the display is not in run mode, don't do anything.
				if (getExecutionMode() != ExecutionMode.RUN_MODE)
					return;				
				
				int actionIndex;

				if(figure.getBoolValue()){
					actionIndex = getWidgetModel().getPushActionIndex();
				}else
				actionIndex = getWidgetModel().getReleasedActionIndex();				
				
				if(actionIndex >= 0 && getWidgetModel().getActionsInput().getActionsList().size() > 
					actionIndex)
					getWidgetModel().getActionsInput().getActionsList().get(
						actionIndex).run();				
			}
			
		});
	}
}