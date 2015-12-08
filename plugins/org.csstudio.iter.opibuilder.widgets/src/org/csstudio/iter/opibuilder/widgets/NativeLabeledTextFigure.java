/*******************************************************************************
 * Copyright (c) 2010-2015 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.widgets.figures.AbstractSWTWidgetFigure;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NativeLabeledTextFigure extends AbstractSWTWidgetFigure<Composite> implements ITextFigure {

    private Composite base;
    private Text text;
    private boolean readOnly, verticalStacking;
    private Label label;
    private final byte LABEL_SPACING = 2;
    private int textStyle;

    /**
     * Constructs a new figure for a LabeledTextInput.
     *
     * @param editPart
     * @param style
     */
    public NativeLabeledTextFigure(AbstractBaseEditPart editPart, int style) {
        super(editPart, style);
    }

/*    @Override
    protected void dispose() {
        if (label != null) label.dispose();
        if (text != null) text.dispose();

        super.dispose();
    }
*/
    @Override
    public Color getForegroundColor() {
        if (text != null)
            return text.getForeground();
        return super.getForegroundColor();
    }

    @Override
    public void setForegroundColor(Color fg) {
        if(!runmode)
            super.setForegroundColor(fg);
        if (text != null)
            text.setForeground(fg);
        if (label != null)
            label.setForeground(fg);
    }

    @Override
    public Color getBackgroundColor() {
        if (text != null)
            return text.getBackground();
        return super.getBackgroundColor();
    }

    @Override
    public void setBackgroundColor(Color bg) {
//        if(!runmode)
//            super.setBackgroundColor(bg);
        if (text != null)
            text.setBackground(bg);
//        if (getSWTWidget() != null) // base
//            getSWTWidget().setBackground(bg);
    };

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        if (text != null)
            text.setFont(f);
        if (label != null)
            label.setFont(f);
    }

    private int getLabelStyle(int textStyle, boolean isStackingVertical) {
        int style = SWT.NONE | SWT.WRAP;
        if (isStackingVertical) {
            style |= SWT.BOTTOM | (textStyle & (SWT.LEFT|SWT.CENTER|SWT.RIGHT));
        }
        else {
            style |= SWT.RIGHT;
        }

        return style;
    }

    @Override
    protected Composite createSWTWidget(Composite parent, int textStyle) {
        this.textStyle = textStyle;
        LabeledTextInputModel model = ((LabeledTextInputEditpart) editPart).getWidgetModel();

        base = new Composite(parent, SWT.NONE);
        base.setBackgroundMode(SWT.INHERIT_DEFAULT);

        verticalStacking = model.getInputLabelStacking() == LabeledTextInputModel.INPUT_LABEL_STACKING.VERTICAL;
        GridLayout layout = new GridLayout(verticalStacking ? 1 : 2, false);
        layout.horizontalSpacing = LABEL_SPACING;
        layout.verticalSpacing = LABEL_SPACING;
        layout.marginHeight = layout.marginWidth = 0;
        base.setLayout(layout);
        layoutLabeledInput();

        return base;
    }

    public void layoutLabeledInput() {
        if (label != null) label.dispose();
        if (text != null) text.dispose();

        if (base == null) return;

        GridData labelGridData, textGridData;
        LabeledTextInputModel model = ((LabeledTextInputEditpart) editPart).getWidgetModel();
        boolean hasLabel = !model.getInputLabelText().isEmpty();

        if (hasLabel)
        {
            label = new Label(base, getLabelStyle(textStyle, verticalStacking));
            label.setText(model.getInputLabelText());
        } else {
            label = null;
        }

        text= new Text(base, textStyle);
        readOnly = (textStyle & SWT.READ_ONLY)!=0;

        if (verticalStacking)
        {
            labelGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
            textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        }
        else
        {    // horizontal stacking
            // if the Input is multiline, v.align the label to top; otherwise center it
            labelGridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
            if (model.isMultilineInput())
                labelGridData.verticalAlignment = SWT.TOP;
            else
                labelGridData.verticalAlignment = SWT.CENTER;
            textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        }
        if (hasLabel) label.setLayoutData(labelGridData);
        text.setLayoutData(textGridData);
    }

    public Dimension getAutoSizeDimension(){
        Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (label == null)
            return new Dimension(textSize.x + this.getInsets().getWidth(), textSize.y + this.getInsets().getHeight());

        Point labelSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        return new Dimension(textSize.x + labelSize.x + (verticalStacking ? 0 : LABEL_SPACING) + this.getInsets().getWidth(),
                             textSize.y + labelSize.y + (verticalStacking ? LABEL_SPACING : 0) + this.getInsets().getHeight());
    }

    @Override
    public void setEnabled(boolean value) {
            super.setEnabled(value);
            if(runmode && getSWTWidget() != null && !getSWTWidget().isDisposed()){
                //the parent should be always enabled so the text can be enabled.
                base.getParent().setEnabled(true);
                base.setEnabled(true);
                if (label != null) label.setEnabled(true);
                text.setEnabled(true);
                if(!readOnly)
                    text.setEditable(value);
            }

    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
    }

    @Override
    public String getText() {
        return text.getText();
    }

    public Text getTextSWTWidget() {
        return text;
    }

    public Label getLabelSWTWidget() {
        return label;
    }
}
