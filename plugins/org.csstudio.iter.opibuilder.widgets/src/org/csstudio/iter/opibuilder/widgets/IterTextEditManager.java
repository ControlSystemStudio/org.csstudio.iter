/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.editparts.IPVWidgetEditpart;
import org.csstudio.opibuilder.widgets.editparts.TextEditManager;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

/**
 * An subclass of BOY's TextEditManager with support for ITER-specific behaviour and appearance.
 * Differences with the default TextEditManager:
 * <ul>
 * <li>it can set a configurable background color (for the direct edit CellEditor) when the text input has focus</li>
 * <li>it can confirm (save) the value to the PV on focus loss (without pressing ENTER)</li>
 * </ul>
 * It must be used together with LabeledTextInputEditpart and LabeledTextInputModel.
 *
 * @author Boris Versic
 *
 */
public class IterTextEditManager extends TextEditManager {

    private Color backgroundFocusColor;
    private boolean confirmOnFocusLost;
    private boolean multiLine;

    /**
     * @param backgroundFocusColor The color to be used for the CellEditor's background. If null, the Figure's background color is used.
     * @param confirmOnFocusLost If true, the PV value will be saved on FocusLost, even without pressing ENTER.
     */
    public IterTextEditManager(AbstractBaseEditPart source, CellEditorLocator locator, boolean multiline, Color backgroundFocusColor, boolean confirmOnFocusLost) {
        super(source, locator, multiline);
        this.multiLine = multiline;
        this.backgroundFocusColor = backgroundFocusColor;
        this.confirmOnFocusLost = confirmOnFocusLost;
    }

    public IterTextEditManager(AbstractBaseEditPart source, CellEditorLocator locator, boolean multiline) {
        super(source, locator, multiline);

        this.backgroundFocusColor = null;
        this.confirmOnFocusLost = false;
    }

    public IterTextEditManager(AbstractBaseEditPart source, CellEditorLocator locator) {
        super(source, locator);

        this.backgroundFocusColor = null;
        this.confirmOnFocusLost = false;
    }

    @Override
    protected CellEditor createCellEditorOn(Composite composite) {
        CellEditor editor =  new TextCellEditor(composite, (multiLine ? SWT.MULTI : SWT.SINGLE) | SWT.WRAP){
            @Override
            protected void focusLost() {
                //in run mode, if the widget has a PV attached,
                //lose focus should cancel the editing except mobile or when confirmOnFocusLost is set to true.
                    if (((AbstractBaseEditPart) getEditPart()).getExecutionMode() == ExecutionMode.RUN_MODE
                            && !OPIBuilderPlugin.isMobile(getControl().getDisplay())
                            && !confirmOnFocusLost
                            && getEditPart() instanceof IPVWidgetEditpart
                            && ((IPVWidgetEditpart) getEditPart()).getPV() != null) {
                        if (isActivated()) {
                            fireCancelEditor();
                            deactivate();
                        }
                        getEditPart().getFigure().requestFocus();
                    } else
                        super.focusLost();
            }

            @Override
            protected void handleDefaultSelection(SelectionEvent event) {
                //In run mode, hit ENTER should force to write the new value even it doesn't change.
                if(((AbstractBaseEditPart) getEditPart()).getExecutionMode() == ExecutionMode.RUN_MODE) {
                    setDirty(true);
                }
                super.handleDefaultSelection(event);
            }

            @Override
            protected void keyReleaseOccured(KeyEvent keyEvent) {
                //In run mode, CTRL+ENTER will always perform a write if it is multiline text input
                if (keyEvent.character == '\r' &&
                        ((AbstractBaseEditPart) getEditPart()).getExecutionMode() == ExecutionMode.RUN_MODE) { // Return key
                    if (text != null && !text.isDisposed()
                            && (text.getStyle() & SWT.MULTI) != 0) {
                        if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                          setDirty(true);
                        }
                    }
                }
                super.keyReleaseOccured(keyEvent);
            }
        };
        editor.getControl().moveAbove(null);
        return editor;
    }

    @Override
    protected void initCellEditor() {
        super.initCellEditor();

        // override background color setting, but only in runMode
        if (((AbstractBaseEditPart) getEditPart()).getExecutionMode() == ExecutionMode.RUN_MODE && backgroundFocusColor != null)
            getCellEditor().getControl().setBackground(backgroundFocusColor);
    }

}
