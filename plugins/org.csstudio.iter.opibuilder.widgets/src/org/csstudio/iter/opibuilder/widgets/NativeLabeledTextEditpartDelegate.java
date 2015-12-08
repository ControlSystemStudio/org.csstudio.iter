/*******************************************************************************
 * Copyright (c) 2010-2015 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.editparts.NativeTextEditpartDelegate;
import org.csstudio.opibuilder.widgets.util.SingleSourceHelper;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class NativeLabeledTextEditpartDelegate extends NativeTextEditpartDelegate {
	private Color backgroundFocusColor = null;
	private Color originalBackgroundColor = null;

	public NativeLabeledTextEditpartDelegate(LabeledTextInputEditpart editpart, LabeledTextInputModel model) {
		super(editpart, model);

		this.backgroundFocusColor = new Color(Display.getDefault(), model.getBackgroundFocusColor());
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.backgroundFocusColor != null) this.backgroundFocusColor.dispose();
		super.finalize();
	}

    protected FocusAdapter getTextFocusListener(NativeLabeledTextFigure figure){
    	return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            	// This listener will also be triggered when ENTER is pressed to store the value (even when FOCUS_TRAVERSE is set to KEEP).
            	// When ConfirmOnFocusLost is TRUE, this will cause a bug: the value will be reset to the old value.
            	// This is because at this point the value of text.getText() will be the old value, set in NativeTextEditpartDelegate.outputText().
            	// Only after the value is successfully set on the PV will the model (and thus text) be updated to the new value.
            	// In such a case, focusLost must not call outputText with the value in text.getText().
            	if (((LabeledTextInputModel)model).isConfirmOnFocusLost()) {
            		if (text.getText().equals(model.getText())) {
            			// either there is no change or ENTER/CTRL+ENTER was pressed to store it but the figure & model were not yet updated.
            			text.setBackground(originalBackgroundColor);
                        originalBackgroundColor = null;
            			return;
        			}
            	}

                //On mobile, lost focus should output text since there is not enter hit or ctrl key.
            	//If ConfirmOnFocusLost is set, lost focus should also output text.
                if(editpart.getPV() != null && !OPIBuilderPlugin.isMobile(text.getDisplay()) && ((LabeledTextInputModel)model).isConfirmOnFocusLost() == false)
                    text.setText(model.getText());
                else if(text.isEnabled())
                    outputText(text.getText());

//                figure.setBackgroundColor(originalBackgroundColor);
                text.setBackground(originalBackgroundColor);
                originalBackgroundColor = null;
            }

            @Override
            public void focusGained(FocusEvent e) {
//            	if (originalBackgroundColor == null) originalBackgroundColor = figure.getBackgroundColor();
//            	figure.setBackgroundColor(backgroundFocusColor);
            	if (originalBackgroundColor == null) originalBackgroundColor = text.getBackground();
            	text.setBackground(backgroundFocusColor);
            }
        };
    }

    @Override
    public IFigure doCreateFigure() {
        int textStyle = getTextFigureStyle();

        final NativeLabeledTextFigure figure = new NativeLabeledTextFigure(editpart, textStyle);
        text = figure.getTextSWTWidget();

        if(!model.isReadOnly()){
            if(model.isMultilineInput()){
                text.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent keyEvent) {
                        if (keyEvent.character == '\r') { // Return key
                            if (text != null && !text.isDisposed()
                                    && (text.getStyle() & SWT.MULTI) != 0) {
                                if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                                	outputText(text.getText());
	                                keyEvent.doit=false;
	                                //force focus to parent (base composite) so that the Text widget will lose it
	                                text.getParent().forceFocus();
                                }
                            }

                        }
                    }
                });
            }else {
                text.addListener (SWT.DefaultSelection, new Listener () {
                    public void handleEvent (Event e) {
                        outputText(text.getText());
                        switch (model.getFocusTraverse()) {
                        case LOSE:
                        	// setFocus() gave the focus to the 'lowest first' child control that could accept it, which can be the same text,
                    		// making LOSE and KEEP 'Next focus' behave the same way
                    		text.getShell().forceFocus();
                            break;
                        case NEXT:
                            SingleSourceHelper.swtControlTraverse(text, SWT.TRAVERSE_TAB_NEXT);
                            break;
                        case PREVIOUS:
                            SingleSourceHelper.swtControlTraverse(text, SWT.TRAVERSE_TAB_PREVIOUS);
                            break;
                        case KEEP:
                        default:
                            break;
                        }
                    }
                });
            }
            //Recover text if editing aborted.
            text.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    if(keyEvent.character == SWT.ESC){
                        text.setText(model.getText());
                    }
                }
            });
            text.addFocusListener(getTextFocusListener(figure));
        }

        Label label = figure.getLabelSWTWidget();
        if (label != null)
	        label.addMouseListener(new MouseAdapter() {
	        	@Override
	        	   public void mouseUp(MouseEvent event) {
	        	      super.mouseUp(event);

	        	      if (event.getSource() instanceof Label) {
	        	         Label label = (Label)event.getSource();
	        	         label.forceFocus();
	        	      }
	        	   }
	        });

        return figure;
    }

    @Override
	public void registerPropertyChangeHandlers() {
    	super.registerPropertyChangeHandlers();

/*    	PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                ((NativeLabeledTextFigure) editpart.getFigure()).layoutLabeledInput();
            }
        };
        model.getProperty(LabeledTextInputModel.PROP_INPUT_LABEL_STACKING).addPropertyChangeListener(listener);
        model.getProperty(LabeledTextInputModel.PROP_INPUT_LABEL_TEXT).addPropertyChangeListener(listener);
*/
        IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {

            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
                AbstractContainerModel parent = model.getParent();
                parent.removeChild(model);
                parent.addChild(model);
                parent.selectWidget(model, true);
                return false;
            }
        };
        editpart.setPropertyChangeHandler(LabeledTextInputModel.PROP_INPUT_LABEL_STACKING, handler);
        editpart.setPropertyChangeHandler(LabeledTextInputModel.PROP_INPUT_LABEL_TEXT, handler);

    }

    @Override
    public void performAutoSize() {
        model.setSize(((NativeLabeledTextFigure)editpart.getFigure()).getAutoSizeDimension());
    }
}
