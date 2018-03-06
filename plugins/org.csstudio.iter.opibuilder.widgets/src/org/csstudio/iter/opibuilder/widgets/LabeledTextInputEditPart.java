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

import org.csstudio.iter.opibuilder.widgets.LabeledTextInputModelDelegate.LabelAlignment;
import org.csstudio.iter.opibuilder.widgets.LabeledTextInputModelDelegate.LabelPosition;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.editparts.LabelEditPart;
import org.csstudio.opibuilder.widgets.model.LabelModel;
import org.csstudio.swt.widgets.figures.GroupingContainerFigure;
import org.csstudio.swt.widgets.figures.TextFigure;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Font;

/**
 * <code>LabeledTextInputEditPart</code> is an edit part for the textinput field with a label.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class LabeledTextInputEditPart extends AbstractPVWidgetEditPart {

    private boolean initialized = false;
    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.editparts.AbstractBaseEditPart#doCreateFigure()
     */
    @Override
    protected IFigure doCreateFigure() {
        GroupingContainerFigure f = new GroupingContainerFigure() {
            @Override
            public void setBorder(Border border) {
                // no border on the parent widget. only the text input field has border
            }
        };
        f.setOpaque(false);
        f.setShowScrollBar(false);
        return f;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.editparts.AbstractBaseEditPart#getWidgetModel()
     */
    @Override
    public LabeledTextInputModel getWidgetModel() {
        return (LabeledTextInputModel) getModel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
     */
    @Override
    public IFigure getContentPane() {
        return ((GroupingContainerFigure) getFigure()).getContentPane();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#createChild(java.lang.Object)
     */
    @Override
    protected EditPart createChild(Object model) {
        EditPart part = null;
        if (model instanceof LabeledTextInputModelDelegate) {
            part = new LabeledTextInputEditPartDelegate();
            part.setModel(model);
            ((LabeledTextInputEditPartDelegate) part).setExecutionMode(getExecutionMode());
        } else {
            part = super.createChild(model);
        }
        if (part instanceof AbstractBaseEditPart && getExecutionMode() == ExecutionMode.EDIT_MODE) {
            ((AbstractBaseEditPart)part).setSelectable(false);
        }
        return part;
    }

    @Override
    protected void addChild(EditPart child, int index) {
        super.addChild(child, index);
        child.setParent(getParent());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.editparts.AbstractBaseEditPart#registerPropertyChangeHandlers()
     */
    @Override
    protected void registerPropertyChangeHandlers() {
        // redirect property changes to sub widgets
        getWidgetModel().getProperty(AbstractWidgetModel.PROP_WIDTH).addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(AbstractWidgetModel.PROP_HEIGHT)
            .addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(LabeledTextInputModelDelegate.PROP_LABEL_POSITION)
            .addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(LabeledTextInputModelDelegate.PROP_LABEL_FONT)
            .addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(LabeledTextInputModelDelegate.PROP_LABEL_GAP)
            .addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(LabeledTextInputModelDelegate.PROP_LABEL_ALIGNMENT)
            .addPropertyChangeListener(evt -> resizeChildren());
        getWidgetModel().getProperty(LabeledTextInputModelDelegate.PROP_LABEL_TEXT)
            .addPropertyChangeListener(evt -> resizeChildren());
        resizeChildren();
        for (String s : LabeledTextInputModel.PROPERTIES_TO_REHANDLE) {
            setPropertyChangeHandler(s, (o,a,n) -> recreateWidget());
        }
        for (Object part : getChildren()) {
            ((EditPart)part).removeEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE);
        }
        initialized = true;
    }

    private boolean recreateWidget() {
        if (initialized) {
            LabeledTextInputModel model = getWidgetModel();
            AbstractContainerModel parent = model.getParent();
            parent.removeChild(model);
            parent.addChild(model);
            parent.selectWidget(model, true);
            model.setUpPropertyListeners();
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected List getModelChildren() {
        return Arrays.asList(getWidgetModel().getLabelModel(), getWidgetModel().getTextModel());
    }

    private void resizeChildren() {
        String text = getWidgetModel().getLabelText();
        TextFigure labelFigure = (TextFigure) ((LabelEditPart) getChildren().get(0)).getFigure();
        Font font = labelFigure.getFont();
        Dimension dim = FigureUtilities.getTextExtents(text, font);
        LabelPosition position = getWidgetModel().getLabelPosition();
        LabelAlignment alignment = getWidgetModel().getLabelAlignment();
        int gap = getWidgetModel().getLabelGap();
        LabelModel labelModel = getWidgetModel().getLabelModel();
        LabeledTextInputModelDelegate inputModel = getWidgetModel().getTextModel();

        // label has the optimum size for the text it contains, the input field takes up the rest of the available space
        if (position == LabelPosition.LEFT) {
            labelFigure.setVerticalAlignment(V_ALIGN.MIDDLE);
            labelFigure.setHorizontalAlignment(H_ALIGN.LEFT);
            if (text.isEmpty()) {
                labelModel.setWidth(0);
                labelModel.setHeight(0);
                inputModel.setX(0);
            } else {
                labelModel.setX(0);
                labelModel.setY(0);
                labelModel.setWidth(dim.width + gap);
                if (alignment == LabelAlignment.BEGINNING) {
                    labelModel.setHeight(dim.height);
                } else if (alignment == LabelAlignment.CENTER) {
                    labelModel.setHeight(getWidgetModel().getHeight());
                } else if (alignment == LabelAlignment.END) {
                    labelModel.setHeight(dim.height);
                    labelModel.setY(getWidgetModel().getHeight() - dim.height);
                }
                inputModel.setX(dim.width + gap);
            }
            inputModel.setWidth(getWidgetModel().getWidth() - inputModel.getX());
            inputModel.setY(0);
            inputModel.setHeight(getWidgetModel().getHeight());
        } else if (position == LabelPosition.ABOVE) {
            labelFigure.setVerticalAlignment(V_ALIGN.TOP);
            labelFigure.setHorizontalAlignment(H_ALIGN.CENTER);
            if (text.isEmpty()) {
                labelModel.setWidth(0);
                labelModel.setHeight(0);
                inputModel.setY(0);
            } else {
                labelModel.setX(0);
                labelModel.setY(0);
                labelModel.setHeight(dim.height + gap);
                if (alignment == LabelAlignment.BEGINNING) {
                    labelModel.setWidth(dim.width);
                } else if (alignment == LabelAlignment.CENTER) {
                    labelModel.setWidth(getWidgetModel().getWidth());
                } else if (alignment == LabelAlignment.END) {
                    labelModel.setWidth(dim.width);
                    labelModel.setX(getWidgetModel().getWidth() - dim.width);
                }
                inputModel.setY(dim.height + gap);
            }
            inputModel.setX(0);
            inputModel.setWidth(getWidgetModel().getWidth());
            inputModel.setHeight(getWidgetModel().getHeight() - inputModel.getY());
        }
    }
}
