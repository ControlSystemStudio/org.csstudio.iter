package org.csstudio.opibuilder.widgets.editparts;


import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.ProgressBarModel;
import org.csstudio.opibuilder.widgets.model.ScaledSliderModel;
import org.csstudio.swt.widgets.figures.ProgressBarFigure;
import org.csstudio.swt.widgets.figures.ScaledSliderFigure;
import org.eclipse.draw2d.IFigure;

/**
 * EditPart controller for the scaled slider widget. The controller mediates between
 * {@link ScaledSliderModel} and {@link ScaledSliderFigure}.
 * 
 * @author Xihui Chen
 * 
 */
public final class ProgressBarEditPart extends AbstractMarkedWidgetEditPart {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IFigure doCreateFigure() {
		final ProgressBarModel model = getWidgetModel();

		ProgressBarFigure bar = new ProgressBarFigure();
		
		initializeCommonFigureProperties(bar, model);		
		bar.setFillColor(model.getFillColor());
		bar.setEffect3D(model.isEffect3D());	
		bar.setFillBackgroundColor(model.getFillbackgroundColor());
		bar.setHorizontal(model.isHorizontal());
		bar.setShowLabel(model.isShowLabel());
		bar.setOrigin(model.getOrigin());
		bar.setOriginIgnored(model.isOriginIgnored());
		bar.setIndicatorMode(model.isIndicatorMode());
		return bar;

	}

	@Override
	public ProgressBarModel getWidgetModel() {
		return (ProgressBarModel)getModel();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPropertyChangeHandlers() {
		registerCommonPropertyChangeHandlers();
		
		IWidgetPropertyChangeHandler originHandler = new IWidgetPropertyChangeHandler() {
			
			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				((ProgressBarFigure)figure).setOrigin((Double)newValue);
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_ORIGIN, originHandler);
		
	IWidgetPropertyChangeHandler originIgnoredHandler = new IWidgetPropertyChangeHandler() {
			
			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				((ProgressBarFigure)figure).setOriginIgnored((Boolean)newValue);
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_ORIGIN_IGNORED, originIgnoredHandler);
		
		//fillColor
		IWidgetPropertyChangeHandler fillColorHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setFillColor(((OPIColor) newValue).getSWTColor());
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_FILL_COLOR, fillColorHandler);	
		
		//fillBackgroundColor
		IWidgetPropertyChangeHandler fillBackColorHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setFillBackgroundColor(((OPIColor) newValue).getSWTColor());
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_FILLBACKGROUND_COLOR, fillBackColorHandler);	
		
	
		
		//effect 3D
		IWidgetPropertyChangeHandler effect3DHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setEffect3D((Boolean) newValue);
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_EFFECT3D, effect3DHandler);	
		
		//effect 3D
		IWidgetPropertyChangeHandler showLabelHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setShowLabel((Boolean) newValue);
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_SHOW_LABEL, showLabelHandler);	
		
		IWidgetPropertyChangeHandler indicatorHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setIndicatorMode((Boolean) newValue);
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_INDICATOR_MODE, indicatorHandler);	
		
		//horizontal
		IWidgetPropertyChangeHandler horizontalHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;
				slider.setHorizontal((Boolean) newValue);
				ProgressBarModel model = (ProgressBarModel)getModel();
				
				if((Boolean) newValue) //from vertical to horizontal
					model.setLocation(model.getLocation().x - model.getSize().height/2 + model.getSize().width/2,
						model.getLocation().y + model.getSize().height/2 - model.getSize().width/2);
				else  //from horizontal to vertical
					model.setLocation(model.getLocation().x + model.getSize().width/2 - model.getSize().height/2,
						model.getLocation().y - model.getSize().width/2 + model.getSize().height/2);					
				
				model.setSize(model.getSize().height, model.getSize().width);
				
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_HORIZONTAL, horizontalHandler);	
		
		
		//enabled. WidgetBaseEditPart will force the widget as disabled in edit model,
		//which is not the case for the scaled slider		
		IWidgetPropertyChangeHandler enableHandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue,
					final IFigure refreshableFigure) {
				ProgressBarFigure slider = (ProgressBarFigure) refreshableFigure;				
				slider.setEnabled((Boolean) newValue);				
				return false;
			}
		};
		setPropertyChangeHandler(ProgressBarModel.PROP_ENABLED, enableHandler);	
		
		

		
	}

}