package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.swt.graphics.RGB;

/**
 * An override for the default Model for the text input widget.
 * Differences with the default TextInputModel:
 * 	- it provides a property for a different background color when the text input has focus (used to paint the CellEditor's background)
 *  - it provides a property to enable confirmation (saving) of value to the PV on focus loss (without pressing ENTER)
 *  
 *  It must be used together with TextInputEditpart and IterTextEditManager.
 *  The TypeID is the same as for the TextInputModel (and defined with the same id in fragment.xml) so that this widget implementation
 *  replaces the default TextInput widget.
 *
 * @author Boris Versic
 *
 */
public class TextInputStyledModel extends TextInputModel {
    
	/** The background color when this control has focus */
    public static final String PROP_COLOR_BACKGROUND_FOCUS = "background_focus_color";//$NON-NLS-1$

    /** Confirm (store value) at focus lost */
    public static final String PROP_CONFIRM_FOCUS_LOST = "confirm_focus_lost"; //$NON-NLS-1$
    
	public TextInputStyledModel() {
	}

    @Override
    protected void configureProperties() {
        super.configureProperties();

        addProperty(new ColorProperty(PROP_COLOR_BACKGROUND_FOCUS, "Background Focus Color",
                WidgetPropertyCategory.Display, "Minor"));

        addProperty(new BooleanProperty(PROP_CONFIRM_FOCUS_LOST, "Confirm on Focus Lost",
                WidgetPropertyCategory.Behavior, true));
    }
    
    public RGB getBackgroundFocusColor(){
        return getRGBFromColorProperty(PROP_COLOR_BACKGROUND_FOCUS);
    }
    
    public void setBackgroundFocusColor(RGB color){
        setPropertyValue(PROP_COLOR_BACKGROUND_FOCUS, color);
    }

    public void setBackgroundFocusColor(String colorName) {
    	setBackgroundFocusColor(MediaService.getInstance().getOPIColor(colorName).getRGBValue());
    }
    
    public boolean isConfirmOnFocusLost(){
        return (Boolean)getPropertyValue(PROP_CONFIRM_FOCUS_LOST);
    }
    
    public void setConfirmOnFocusLost(boolean confirm){
        setPropertyValue(PROP_CONFIRM_FOCUS_LOST, confirm);
    }
}
