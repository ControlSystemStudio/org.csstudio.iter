/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.widgets.editparts.NativeTextEditpartDelegate;
import org.csstudio.opibuilder.widgets.editparts.TextInputEditpart;
import org.csstudio.opibuilder.widgets.figures.NativeTextFigure;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 *
 * <code>NativeLabeledTextEditpartDelegate</code> adds additional focus lost functionality to the native widget.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class NativeLabeledTextEditpartDelegate extends NativeTextEditpartDelegate {
    private Color backgroundFocusColor = null;
    private Color originalBackgroundColor = null;
    private TextInputEditpart editpart;
    private TextInputModel model;
    private Text text;

    NativeLabeledTextEditpartDelegate(LabeledTextInputEditPartDelegate editpart,
        LabeledTextInputModelDelegate model) {
        super(editpart, model);
        this.editpart = editpart;
        this.model = model;
        this.backgroundFocusColor = new Color(Display.getDefault(), model.getBackgroundFocusColor());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (this.backgroundFocusColor != null)
            this.backgroundFocusColor.dispose();
        super.finalize();
    }

    private FocusAdapter getTextFocusListener(NativeTextFigure figure) {
        return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // This listener will also be triggered when ENTER is pressed to store the value (even when
                // FOCUS_TRAVERSE is set to KEEP).
                // When ConfirmOnFocusLost is TRUE, this will cause a bug: the value will be reset to the old value.
                // This is because at this point the value of text.getText() will be the old value, set in
                // NativeTextEditpartDelegate.outputText().
                // Only after the value is successfully set on the PV will the model (and thus text) be updated to the
                // new value.
                // In such a case, focusLost must not call outputText with the value in text.getText().
                if (((LabeledTextInputModelDelegate) model).isConfirmOnFocusLost()) {
                    if (text.getText().equals(model.getText())) {
                        // either there is no change or ENTER/CTRL+ENTER was pressed to store it but the figure & model
                        // were not yet updated.
                        text.setBackground(originalBackgroundColor);
                        originalBackgroundColor = null;
                        return;
                    }
                }

                // On mobile, lost focus should output text since there is not enter hit or ctrl key.
                // If ConfirmOnFocusLost is set, lost focus should also output text.
                if (editpart.getPV() != null && !OPIBuilderPlugin.isMobile(text.getDisplay())
                    && ((LabeledTextInputModelDelegate) model).isConfirmOnFocusLost() == false) {
                    text.setText(model.getText());
                } else if (text.isEnabled()) {
                    outputText(text.getText());
                }

                text.setBackground(originalBackgroundColor);
                originalBackgroundColor = null;
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (originalBackgroundColor == null) {
                    originalBackgroundColor = text.getBackground();
                }
                text.setBackground(backgroundFocusColor);
            }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.widgets.editparts.NativeTextEditpartDelegate#doCreateFigure()
     */
    @Override
    public IFigure doCreateFigure() {
        NativeTextFigure figure = (NativeTextFigure)super.doCreateFigure();
        text = figure.getSWTWidget();

        if (!model.isReadOnly()) {
            Listener[] listeners1 = text.getListeners(SWT.FocusIn);
            Listener[] listeners2 = text.getListeners(SWT.FocusOut);
            for (Listener l : listeners1) {
                text.removeListener(SWT.FocusIn, l);
            }
            for (Listener l : listeners2) {
                text.removeListener(SWT.FocusOut, l);
            }
            text.addFocusListener(getTextFocusListener(figure));
        }
        return figure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.csstudio.opibuilder.widgets.editparts.NativeTextEditpartDelegate#registerPropertyChangeHandlers()
     */
    @Override
    public void registerPropertyChangeHandlers() {
        super.registerPropertyChangeHandlers();
        for (String s : LabeledTextInputModel.PROPERTIES_TO_REHANDLE) {
            editpart.removeAllPropertyChangeHandlers(s);
        }
    }
}
