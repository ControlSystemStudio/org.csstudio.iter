package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.figures.AbstractBoolFigure;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.ValueUtil;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.FontData;

/**
 * Base editPart controller for a widget based on {@link AbstractBoolWidgetModel}.
 * 
 * @author Xihui Chen
 * 
 */
public abstract class AbstractBoolEditPart extends AbstractPVWidgetEditPart {

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
			final AbstractBoolFigure figure, final AbstractBoolWidgetModel model) {
		if(model.getDataType() == 0)
			figure.setBit(model.getBit());
		else
			figure.setBit(-1);
		updatePropSheet(model.getDataType());
		figure.setShowBooleanLabel(model.isShowBoolLabel());
		figure.setOnLabel(model.getOnLabel());
		figure.setOffLabel(model.getOffLabel());
		figure.setOnColor(model.getOnColor());
		figure.setOffColor(model.getOffColor());
		figure.setFont(CustomMediaFactory.getInstance().getFont(
				model.getFont().getFontData()));
		
	}	
	
	@Override
	public AbstractBoolWidgetModel getWidgetModel() {
		return (AbstractBoolWidgetModel)getModel();
	}
	
	/**
	 * Registers property change handlers for the properties defined in
	 * {@link AbstractBoolWidgetModel}. This method is provided for the convenience
	 * of subclasses, which can call this method in their implementation of
	 * {@link #registerPropertyChangeHandlers()}.
	 */
	protected void registerCommonPropertyChangeHandlers() {
		// value
		IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				if(newValue == null)
					return false;
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				updateFromValue(newValue, figure);					
				return true;
			}

			
		};
		setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVVALUE, handler);
		
		// bit
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				if(getWidgetModel().getDataType() != 0)
					return false;
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setBit((Integer) newValue);
				updateFromValue(getPVValue(AbstractPVWidgetModel.PROP_PVNAME), figure);					
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_BIT, handler);
		
		//data type
	    final IWidgetPropertyChangeHandler	dataTypeHandler = new IWidgetPropertyChangeHandler() {
			
			public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				if((Integer)newValue == 0)
					figure.setBit(getWidgetModel().getBit());
				else
					figure.setBit(-1);		
				updateFromValue(getPVValue(AbstractPVWidgetModel.PROP_PVNAME), figure);		
				updatePropSheet((Integer)newValue);
				return true;
			}
		};
		getWidgetModel().getProperty(AbstractBoolWidgetModel.PROP_DATA_TYPE).
			addPropertyChangeListener(new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					dataTypeHandler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
				}
			});

		//on state
		handler = new IWidgetPropertyChangeHandler() {
			
			public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;					
				updateFromValue(getPVValue(AbstractPVWidgetModel.PROP_PVNAME), figure);					
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_ON_STATE, handler);
		
		
		// show bool label
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setShowBooleanLabel((Boolean) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_SHOW_BOOL_LABEL, handler);
		
		// on label
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setOnLabel((String) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_ON_LABEL, handler);
		
		// off label
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setOffLabel((String) newValue);
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_OFF_LABEL, handler);
		
		// on color
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setOnColor(((OPIColor) newValue).getRGBValue());
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_ON_COLOR, handler);
		
		// off color
		handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				figure.setOffColor(((OPIColor) newValue).getRGBValue());
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_OFF_COLOR, handler);
		
		
		// font
		IWidgetPropertyChangeHandler fontHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue, final IFigure refreshableFigure) {
				AbstractBoolFigure figure = (AbstractBoolFigure) refreshableFigure;
				FontData fontData = ((OPIFont) newValue).getFontData();
				figure.setFont(CustomMediaFactory.getInstance().getFont(fontData));
				return true;
			}
		};
		setPropertyChangeHandler(AbstractBoolWidgetModel.PROP_FONT, fontHandler);
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		if(value instanceof Double)
			((AbstractBoolFigure)getFigure()).setValue((Double)value);
		else if (value instanceof Boolean)
			((AbstractBoolFigure)getFigure()).setBooleanValue((Boolean)value);
	}

	@Override
	public Boolean getValue() {
		return ((AbstractBoolFigure)getFigure()).getBoolValue();
	}
	/**
	 * @param newValue
	 * @param figure
	 */
	private void updateFromValue(final Object newValue,
			AbstractBoolFigure figure) {
		if(newValue == null || !(newValue instanceof IValue))
			return;
		if(getWidgetModel().getDataType() == 0)
			figure.setValue(ValueUtil.getDouble((IValue)newValue));
		else {
			if(newValue !=null && newValue instanceof IValue && ValueUtil.getString((IValue)newValue).equals(
					getWidgetModel().getOnState()))
				figure.setValue(1);
			else
				figure.setValue(0);
		}
	}
	
	private void updatePropSheet(final int dataType) {
		getWidgetModel().setPropertyVisible(
				AbstractBoolWidgetModel.PROP_BIT, dataType == 0);
		getWidgetModel().setPropertyVisible(
				AbstractBoolWidgetModel.PROP_ON_STATE, dataType == 1);
		getWidgetModel().setPropertyVisible(
				AbstractBoolWidgetModel.PROP_OFF_STATE, dataType == 1);
		
	}
}