/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.FontProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.swt.graphics.RGB;

/**
 * An extension of the text input model, which provides some iter specific settings. Among these are the label with
 * label settings and focus color. This model is not intended to be used as a standalone model. It is wrapped into
 * the {@link LabeledTextInputModel}.
 *
 * It must be used together with LabeledTextInputEditpart and IterTextEditManager. The TypeID is the same as for the
 * TextInputModel (and defined with the same id in plugin.xml) so that this widget implementation replaces the default
 * TextInput widget.
 *
 * @author Boris Versic
 *
 */
public class LabeledTextInputModelDelegate extends TextInputModel {

    /** The background color when this control has focus */
    public static final String PROP_COLOR_BACKGROUND_FOCUS = "background_focus_color";//$NON-NLS-1$

    /** Confirm (store value) at focus lost */
    public static final String PROP_CONFIRM_FOCUS_LOST = "confirm_focus_lost"; //$NON-NLS-1$

    /** Text of the (optional) label for this input field */
    public static final String PROP_LABEL_TEXT = "label_text"; //$NON-NLS-1$

    /** Specifies label position: above or to the left of the input field */
    public static final String PROP_LABEL_POSITION = "label_position"; //$NON-NLS-1$
    /** The label font */
    public static final String PROP_LABEL_FONT = "label_font"; //$NON-NLS-1$
    /** The label font color */
    public static final String PROP_LABEL_COLOR = "label_color"; //$NON-NLS-1$
    /** The gap in pixels between the label and input field */
    public static final String PROP_LABEL_GAP = "label_gap"; //$NON-NLS-1$
    /** Alignment of the label with respect to the parent widget */
    public static final String PROP_LABEL_ALIGNMENT = "label_alignment"; //$NON-NLS-1$


    static enum LabelPosition {
        ABOVE("Above"), LEFT("Left");

        private String description;

        private LabelPosition(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            String[] result = new String[values().length];
            int i = 0;
            for (LabelPosition f : values()) {
                result[i++] = f.toString();
            }
            return result;
        }
    }

    static enum LabelAlignment {
        BEGINNING("Top/Left"), CENTER("Center"), END("Bottom/Right");

        private String description;

        private LabelAlignment(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            String[] result = new String[values().length];
            int i = 0;
            for (LabelAlignment f : values()) {
                result[i++] = f.toString();
            }
            return result;
        }
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();
        addProperty(new ColorProperty(PROP_COLOR_BACKGROUND_FOCUS, "Background Focus Color",
            WidgetPropertyCategory.Display, "Minor"));
        addProperty(new BooleanProperty(PROP_CONFIRM_FOCUS_LOST, "Confirm on Focus Lost",
            WidgetPropertyCategory.Behavior, true));
        addProperty(new StringProperty(PROP_LABEL_TEXT, "Label Text", WidgetPropertyCategory.Display, "", true));
        addProperty(new ComboProperty(PROP_LABEL_POSITION, "Label Position", WidgetPropertyCategory.Display,
            LabelPosition.stringValues(), LabelPosition.ABOVE.ordinal()));
        addProperty(new ComboProperty(PROP_LABEL_ALIGNMENT, "Label Alignment", WidgetPropertyCategory.Display,
            LabelAlignment.stringValues(), LabelAlignment.CENTER.ordinal()));
        addProperty(new IntegerProperty(PROP_LABEL_GAP, "Label Gap", WidgetPropertyCategory.Display, 2));
        addProperty(
            new ColorProperty(PROP_LABEL_COLOR, "Label Color", WidgetPropertyCategory.Display, new RGB(0, 0, 0)));
        addProperty(
            new FontProperty(PROP_LABEL_FONT, "Label Font", WidgetPropertyCategory.Display, MediaService.DEFAULT_FONT));
    }

    public LabelAlignment getLabelAlignment() {
        return LabelAlignment.values()[(int) getPropertyValue(PROP_LABEL_ALIGNMENT)];
    }

    public int getLabelGap() {
        return getCastedPropertyValue(PROP_LABEL_GAP);
    }

    public OPIFont getLabelFont() {
        return (OPIFont) getPropertyValue(PROP_LABEL_FONT);
    }

    public RGB getLabelColor() {
        return getRGBFromColorProperty(PROP_LABEL_COLOR);
    }

    public RGB getBackgroundFocusColor() {
        return getRGBFromColorProperty(PROP_COLOR_BACKGROUND_FOCUS);
    }

    public void setBackgroundFocusColor(RGB color) {
        setPropertyValue(PROP_COLOR_BACKGROUND_FOCUS, color);
    }

    public void setBackgroundFocusColor(String colorName) {
        setBackgroundFocusColor(MediaService.getInstance().getOPIColor(colorName).getRGBValue());
    }

    public boolean isConfirmOnFocusLost() {
        return (Boolean) getPropertyValue(PROP_CONFIRM_FOCUS_LOST);
    }

    public void setConfirmOnFocusLost(boolean confirm) {
        setPropertyValue(PROP_CONFIRM_FOCUS_LOST, confirm);
    }

    public String getLabelText() {
        return (String) getCastedPropertyValue(PROP_LABEL_TEXT);
    }

    public void setLabelText(String labelText) {
        setPropertyValue(PROP_LABEL_TEXT, labelText);
    }

    public void setText(String labelText, boolean fire) {
        getProperty(PROP_LABEL_TEXT).setPropertyValue(labelText, fire);
    }

    public LabelPosition getLabelPosition() {
        return LabelPosition.values()[(int) getPropertyValue(PROP_LABEL_POSITION)];
    }

    @Override
    public String getTypeID() {
        return "org.csstudio.opibuilder.widgets.TextInput";
    }
}
