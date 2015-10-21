package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.widgets.editparts.NativeTextEditpartDelegate;
import org.csstudio.opibuilder.widgets.editparts.TextInputEditpart;
import org.csstudio.opibuilder.widgets.figures.NativeTextFigure;
import org.csstudio.opibuilder.widgets.model.TextInputModel;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class NativeTextStyledEditpartDelegate extends NativeTextEditpartDelegate {
	private Color backgroundFocusColor;
	private Color originalBackgroundColor = null;

	public NativeTextStyledEditpartDelegate(TextInputStyledEditpart editpart, TextInputStyledModel model) {
		super(editpart, model);
		
		this.backgroundFocusColor = new Color(Display.getDefault(), model.getBackgroundFocusColor());
	}

	@Override
    protected FocusAdapter getTextFocusListener(NativeTextFigure figure){
    	return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            	// This listener will also be triggered when ENTER is pressed to store the value (even when FOCUS_TRAVERSE is set to KEEP).
            	// When ConfirmOnFocusLost is TRUE, this will cause a bug: the value will be reset to the old value. 
            	// This is because at this point the value of text.getText() will be the old value, set in NativeTextEditpartDelegate.outputText().
            	// Only after the value is successfully set on the PV will the model (and thus text) be updated to the new value.
            	// In such a case, focusLost must not call outputText with the value in text.getText().
            	if (((TextInputStyledModel)model).isConfirmOnFocusLost()) {
            		if (text.getText().equals(model.getText())) {
            			// either there is no change or ENTER/CTRL+ENTER was pressed to store it but the figure & model were not yet updated.
            			figure.setBackgroundColor(originalBackgroundColor);
                        originalBackgroundColor = null;
            			return;
        			}
            	}
            	
                //On mobile, lost focus should output text since there is not enter hit or ctrl key.
            	//If ConfirmOnFocusLost is set, lost focus should also output text.
                if(editpart.getPV() != null && !OPIBuilderPlugin.isMobile(text.getDisplay()) && ((TextInputStyledModel)model).isConfirmOnFocusLost() == false)
                    text.setText(model.getText());
                else if(figure.isEnabled())	
                    outputText(text.getText());
                
                figure.setBackgroundColor(originalBackgroundColor);
                originalBackgroundColor = null;
            }
            
            @Override
            public void focusGained(FocusEvent e) {
            	if (originalBackgroundColor == null) originalBackgroundColor = figure.getBackgroundColor();
            	figure.setBackgroundColor(backgroundFocusColor);
            }
        };
    }
	
	@Override
	protected void traverseLoseFocus() {
    	// setFocus() gave the focus to the 'lowest first' child control that could accept it, which can be the same text,
		// making LOSE and KEEP 'Next focus' behave the same way
		text.getShell().forceFocus();
	}
}
