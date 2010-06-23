package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.visualparts.BorderStyle;

/**
 *The model of spinner widget.
 * @author Xihui Chen
 */
public class SpinnerModel extends LabelModel {
	
	public final String ID = "org.csstudio.opibuilder.widgets.spinner";

	
	/** The ID of the minimum property. */
	public static final String PROP_MIN = "minimum"; //$NON-NLS-1$		
	
	/** The ID of the maximum property. */
	public static final String PROP_MAX = "maximum"; //$NON-NLS-1$	
	
	/**the amount the scrollbar will move when the up or down arrow buttons are
	pressed.*/	
	public static final String PROP_STEP_INCREMENT = "step_increment"; //$NON-NLS-1$		
	
	/** The amount the scrollbar will move when the page up or page down areas are
	pressed.*/
	public static final String PROP_PAGE_INCREMENT = "page_increment"; //$NON-NLS-1$	

	public static final String PROP_LIMITS_FROM_PV = "limits_from_pv"; //$NON-NLS-1$		

	
	/** The default value of the minimum property. */
	private static final double DEFAULT_MIN = Double.NEGATIVE_INFINITY;
	
	/** The default value of the maximum property. */
	private static final double DEFAULT_MAX = Double.POSITIVE_INFINITY;	
	
	private static final double DEFAULT_STEP_INCREMENT = 1;	
	
	private static final double DEFAULT_PAGE_INCREMENT = 10;
	
	public SpinnerModel() {		
		setSize(85, 25);
		setBorderStyle(BorderStyle.LOWERED);
	}
	
	
	@Override
	protected void configureProperties() {			
		pvModel = true;
		super.configureProperties();		
		removeProperty(LabelModel.PROP_AUTOSIZE);
		setPropertyVisible(LabelModel.PROP_TEXT, false);
		addProperty(new DoubleProperty(PROP_MIN, "Minimum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MIN));
		
		addProperty(new DoubleProperty(PROP_MAX, "Maximum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MAX));			
		
		addProperty(new DoubleProperty(PROP_STEP_INCREMENT, "Step Increment", 
				WidgetPropertyCategory.Behavior, DEFAULT_STEP_INCREMENT), true);		
	
		addProperty(new DoubleProperty(PROP_PAGE_INCREMENT, "Page_Increment",
				WidgetPropertyCategory.Behavior, DEFAULT_PAGE_INCREMENT), true);
		
		addProperty(new BooleanProperty(PROP_LIMITS_FROM_PV, "Limits From PV",
				WidgetPropertyCategory.Behavior, true));
		
	}	


	/**
	 * @return the minimum value
	 */
	public Double getMinimum() {
		return (Double) getProperty(PROP_MIN).getPropertyValue();
	}


	/**
	 * @return the maximum value
	 */
	public Double getMaximum() {
		return (Double) getProperty(PROP_MAX).getPropertyValue();
	}

	
	

	
	/**
	 * @return the step increment
	 */
	public Double getStepIncrement() {
		return (Double) getProperty(PROP_STEP_INCREMENT).getPropertyValue();
	}
	
	public double getPageIncrement() {
		return (Double) getProperty(PROP_PAGE_INCREMENT).getPropertyValue();
	}
	
	/**
	 * @return true if limits will be load from DB, false otherwise
	 */
	public boolean isLimitsFromPV() {
		return (Boolean) getProperty(PROP_LIMITS_FROM_PV).getPropertyValue();
	}
		
	@Override
	public String getTypeID() {
		return ID;
	}		
}