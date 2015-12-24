/*******************************************************************************
 * Copyright (c) 2010-2015 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.swt.graphics.RGB;

/**
 * An override for the default Model for the text input widget.
 * Differences with the default TextInputModel:
 *     - it provides a property for a different background color when the text input has focus (used to paint the CellEditor's background)
 *  - it provides a property to enable confirmation (saving) of value to the PV on focus loss (without pressing ENTER)
 *
 *  It must be used together with LabeledTextInputEditpart and IterTextEditManager.
 *  The TypeID is the same as for the TextInputModel (and defined with the same id in plugin.xml) so that this widget implementation
 *  replaces the default TextInput widget.
 *
 * @author Boris Versic
 *
 */
public class LabeledTextInputModel extends TextInputModel {

    /** The background color when this control has focus */
    public static final String PROP_COLOR_BACKGROUND_FOCUS = "background_focus_color";//$NON-NLS-1$

    /** Confirm (store value) at focus lost */
    public static final String PROP_CONFIRM_FOCUS_LOST = "confirm_focus_lost"; //$NON-NLS-1$

    /** Text of the (optional) label for this input field */
    public static final String PROP_INPUT_LABEL_TEXT = "input_label_text"; //$NON-NLS-1$

    /** Setting for the label's placement.
     *  Determines whether the label should be stacked vertically (above) or horizontally (on the left) */
    public static final String PROP_INPUT_LABEL_STACKING = "input_label_stacking"; //$NON-NLS-1$

    public enum INPUT_LABEL_STACKING {
        VERTICAL("Vertical (above)"),
        HORIZONTAL("Horizontal (beside on left)");

        private String description;
        private INPUT_LABEL_STACKING(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues(){
            String[] result = new String[values().length];
            int i =0 ;
            for(INPUT_LABEL_STACKING f : values()){
                result[i++] = f.toString();
            }
            return result;
        }

        public static int getDefault() {
            return VERTICAL.ordinal();
        };
    }

    public LabeledTextInputModel() {
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();

        addProperty(new ColorProperty(PROP_COLOR_BACKGROUND_FOCUS, "Background Focus Color",
                WidgetPropertyCategory.Display, "Minor"));

        addProperty(new BooleanProperty(PROP_CONFIRM_FOCUS_LOST, "Confirm on Focus Lost",
                WidgetPropertyCategory.Behavior, true));

        addProperty(new StringProperty(PROP_INPUT_LABEL_TEXT, "Label Text",
                WidgetPropertyCategory.Display, "", true));

        addProperty(new ComboProperty(PROP_INPUT_LABEL_STACKING, "Label Stacking mode",
                WidgetPropertyCategory.Position, INPUT_LABEL_STACKING.stringValues(), INPUT_LABEL_STACKING.getDefault()));

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

    public String getInputLabelText(){
        return (String)getCastedPropertyValue(PROP_INPUT_LABEL_TEXT);
    }

    public void setInputLabelText(String labelText){
        setPropertyValue(PROP_INPUT_LABEL_TEXT, labelText);
    }

    public void setText(String labelText, boolean fire){
        getProperty(PROP_INPUT_LABEL_TEXT).setPropertyValue(labelText, fire);
    }

    public INPUT_LABEL_STACKING getInputLabelStacking(){
        return INPUT_LABEL_STACKING.values()[(int)getPropertyValue(PROP_INPUT_LABEL_STACKING)];
    }
}
