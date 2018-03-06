/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgets.model.ActionButtonModel.Style;
import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.csstudio.swt.widgets.figures.LabelFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.LabelFigure.V_ALIGN;
import org.eclipse.swt.graphics.RGB;

/**
 *
 * <code>LabeledTextInputModel</code> is the a text input model extension which in addition to the input field also
 * displays a label. The label can be positioned to the left or above the input field. There are other properties that
 * can be set for the label, such as font color and size, text alignment and gap.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class LabeledTextInputModel extends LabeledTextInputModelDelegate {

    static final List<String> PROPERTIES_TO_REHANDLE = Arrays.asList(TextInputModel.PROP_SHOW_NATIVE_BORDER,
        TextInputModel.PROP_MULTILINE_INPUT, TextInputModel.PROP_WRAP_WORDS, TextInputModel.PROP_SHOW_H_SCROLL,
        TextInputModel.PROP_SHOW_V_SCROLL, TextInputModel.PROP_PASSWORD_INPUT, TextInputModel.PROP_ALIGN_H,
        TextInputModel.PROP_STYLE);

    private LabeledTextInputModelDelegate textModel;
    private LabelModel labelModel;

    /**
     * Constructs and returns the model for the text input field.
     *
     * @return the text input field
     */
    LabeledTextInputModelDelegate getTextModel() {
        if (textModel == null) {
            textModel = new LabeledTextInputModelDelegate();
            textModel.getProperty(TextInputModel.PROP_STYLE).setPropertyValue(Style.NATIVE.ordinal());
            textModel.setX(0);
            textModel.setY(30);
            textModel.setWidth(30);
            textModel.setHeight(30);
        }
        return textModel;
    }

    /**
     * Constructs a new returns the model for the label.
     *
     * @return the label model
     */
    LabelModel getLabelModel() {
        if (labelModel == null) {
            labelModel = new LabelModel();
            labelModel.setX(0);
            labelModel.setY(0);
            labelModel.setWidth(30);
            labelModel.setHeight(30);
            labelModel.setPropertyValue(LabelModel.PROP_ALIGN_H, H_ALIGN.CENTER);
            labelModel.setPropertyValue(LabelModel.PROP_ALIGN_V, V_ALIGN.BOTTOM);
            labelModel.setPropertyValue(LabelModel.PROP_BORDER_ALARMSENSITIVE, false);
            labelModel.setPropertyValue(LabelModel.PROP_BACKCOLOR_ALARMSENSITIVE,false);
            labelModel.setPropertyValue(LabelModel.PROP_FORECOLOR_ALARMSENSITIVE, false);
            labelModel.setPropertyValue(LabelModel.PROP_ALARM_PULSING, false);
            labelModel.setBorderStyle(BorderStyle.NONE);
            labelModel.setTooltip("");
            labelModel.setName("");
        }
        return labelModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.iter.opibuilder.widgets.LabeledTextInputModelDelegate#configureProperties()
     */
    @Override
    protected void configureProperties() {
        super.configureProperties();
        setUpPropertyListeners();
    }

    @Override
    public void setParent(AbstractContainerModel parent) {
        getLabelModel().setParent(parent);
        getTextModel().setParent(parent);
        super.setParent(parent);
    }

    /**
     * Binds the text input and label models to the properties of this model. This method has to be called every time
     * when the edit part is recreated. It also has to be called when the model is created in order to create the
     * initial figure correctly.
     */
    void setUpPropertyListeners() {
        final LabeledTextInputModelDelegate textModel = getTextModel();
        final LabelModel labelModel = getLabelModel();
        for (String s : getAllPropertyIDs()) {
            if (AbstractWidgetModel.PROP_XPOS.equals(s) || AbstractWidgetModel.PROP_YPOS.equals(s)
                || AbstractWidgetModel.PROP_WIDTH.equals(s) || AbstractWidgetModel.PROP_HEIGHT.equals(s)) {
                // this guys are managed by the edit part when doing layout
                continue;
            }
            final String propId = s;
            AbstractWidgetProperty thisProperty = getProperty(propId);
            if (LabeledTextInputModelDelegate.PROP_LABEL_TEXT.equals(s)) {
                thisProperty.addPropertyChangeListener(evt -> labelModel.setText(String.valueOf(evt.getNewValue())));
                labelModel.setText(String.valueOf(thisProperty.getRawPropertyValue()));
            } else if (LabeledTextInputModelDelegate.PROP_LABEL_COLOR.equals(s)) {
                thisProperty.addPropertyChangeListener(
                    evt -> labelModel.setForegroundColor(((OPIColor) evt.getNewValue()).getRGBValue()));
                labelModel.setForegroundColor(((OPIColor) thisProperty.getRawPropertyValue()).getRGBValue());
            } else if (LabeledTextInputModelDelegate.PROP_LABEL_FONT.equals(s)) {
                thisProperty.addPropertyChangeListener(evt -> labelModel.setFont((OPIFont) evt.getNewValue()));
                labelModel.setFont((OPIFont) thisProperty.getRawPropertyValue());
            } else {
                if (AbstractWidgetModel.PROP_NAME.equals(s) || AbstractPVWidgetModel.PROP_PVNAME.equals(s)
                    || AbstractPVWidgetModel.PROP_PVVALUE.equals(s)) {
                    thisProperty.addPropertyChangeListener(evt -> labelModel.setPropertyValue(propId, evt.getNewValue()));
                    labelModel.setPropertyValue(propId, thisProperty.getRawPropertyValue());
                }
                // not all properties are relevant to the text model, but who cares - they're ignored
                thisProperty.addPropertyChangeListener(evt -> textModel.setPropertyValue(propId, evt.getNewValue()));
                textModel.setPropertyValue(propId, thisProperty.getRawPropertyValue());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.model.AbstractWidgetModel#setPropertyValue(java.lang.Object, java.lang.Object,
     * boolean)
     */
    @Override
    public void setPropertyValue(Object id, Object value, boolean forceFire) {
        super.setPropertyValue(id, value, forceFire);
        if (!forceFire) {
            getTextModel().setPropertyValue(id, value, forceFire);
            if (LabeledTextInputModelDelegate.PROP_LABEL_TEXT.equals(id)) {
                getLabelModel().setPropertyValue(PROP_TEXT, value, forceFire);
            } else if (LabeledTextInputModelDelegate.PROP_LABEL_COLOR.equals(id)) {
                RGB rgb = value instanceof RGB ? (RGB) value
                    : value instanceof OPIColor ? ((OPIColor) value).getRGBValue() : null;
                getLabelModel().setPropertyValue(PROP_COLOR_FOREGROUND, rgb, forceFire);
            } else if (LabeledTextInputModelDelegate.PROP_LABEL_FONT.equals(id)) {
                getLabelModel().setPropertyValue(PROP_FONT, value, forceFire);
            } else if (AbstractWidgetModel.PROP_NAME.equals(id) || AbstractPVWidgetModel.PROP_PVNAME.equals(id)
                || AbstractPVWidgetModel.PROP_PVVALUE.equals(id)) {
                getLabelModel().setPropertyValue(id, value, forceFire);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.widgets.model.TextInputModel#getTypeID()
     */
    @Override
    public String getTypeID() {
        return "org.csstudio.opibuilder.widgets.TextInput";
    }
}
