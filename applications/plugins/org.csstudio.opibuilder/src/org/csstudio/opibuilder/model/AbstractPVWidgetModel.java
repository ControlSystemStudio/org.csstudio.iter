package org.csstudio.opibuilder.model;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;

/**The abstract widget model for all PV related widgets. 
 * @author Xihui Chen
 *
 */
public abstract class AbstractPVWidgetModel extends AbstractWidgetModel {


	public static final String PROP_BORDER_ALARMSENSITIVE= "border_alarm_sensitive"; //$NON-NLS-1$
	public static final String PROP_FORECOLOR_ALARMSENSITIVE= "forecolor_alarm_sensitive"; //$NON-NLS-1$
	public static final String PROP_BACKCOLOR_ALARMSENSITIVE= "backcolor_alarm_sensitive"; //$NON-NLS-1$
	/**
	 * The ID of the pv value property.
	 */
	public static final String PROP_PVVALUE= "pv_value"; //$NON-NLS-1$
	
	/**
	 * The ID of the pv name property.
	 */
	public static final String PROP_PVNAME= "pv_name"; //$NON-NLS-1$
	
	@Override
	protected void configureBaseProperties() {
		super.configureBaseProperties();		
		addPVProperty(new StringProperty(PROP_PVNAME, "PV Name", WidgetPropertyCategory.Basic,
				""), new PVValueProperty(PROP_PVVALUE, null));
		
		addProperty(new BooleanProperty(PROP_BORDER_ALARMSENSITIVE, 
				"Alarm Sensitive", WidgetPropertyCategory.Border, true));
		addProperty(new BooleanProperty(PROP_FORECOLOR_ALARMSENSITIVE, 
				"ForeColor Alarm Sensitive", WidgetPropertyCategory.Display, false));
		addProperty(new BooleanProperty(PROP_BACKCOLOR_ALARMSENSITIVE, 
				"BackColor Alarm Sensitive", WidgetPropertyCategory.Display, false));
		
		setTooltip("$(" + PROP_PVNAME + ")\n" + "$(" + PROP_PVVALUE + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public boolean isBorderAlarmSensitve(){
		if(getProperty(PROP_BORDER_ALARMSENSITIVE) == null)
			return false;
		return (Boolean)getCastedPropertyValue(PROP_BORDER_ALARMSENSITIVE);
	}
	
	public boolean isForeColorAlarmSensitve(){
		if(getProperty(PROP_FORECOLOR_ALARMSENSITIVE) == null)
			return false;
		return (Boolean)getCastedPropertyValue(PROP_FORECOLOR_ALARMSENSITIVE);
	}
	
	public boolean isBackColorAlarmSensitve(){
		if(getProperty(PROP_BACKCOLOR_ALARMSENSITIVE) == null)
			return false;
		return (Boolean)getCastedPropertyValue(PROP_BACKCOLOR_ALARMSENSITIVE);
	}
	
	public String getPVName(){
		return (String)getCastedPropertyValue(PROP_PVNAME);
	}
}