package org.csstudio.iter.opibuilder.widgets;

import org.csstudio.iter.opibuilder.widgets.TextInputStyledModel;
import org.csstudio.opibuilder.widgets.editparts.LabelCellEditorLocator;
import org.csstudio.opibuilder.widgets.editparts.TextInputEditpart;
import org.eclipse.draw2d.Figure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * An override for the default EditPart for the text input widget.
 * Differences with the default TextInputEditPart: it uses an IterTextEditManager instance for DirectEdit,
 * giving the TextInput widget the option of setting a different background color when the control has focus,
 * and saving the value to the PV without pressing ENTER (on loss of focus).
 *  
 * It must be used together with TextInputStyledModel and IterTextEditManager.
 *
 * @author Boris Versic
 */
public class TextInputStyledEditpart extends TextInputEditpart {
	
	public TextInputStyledEditpart() {
		super();
	}

    @Override
    public TextInputStyledModel getWidgetModel() {
        return (TextInputStyledModel) getModel();
    }
    
	@Override
    protected void performDirectEdit() {
    	final TextInputStyledModel model = getWidgetModel();
        new IterTextEditManager(this, new LabelCellEditorLocator(
                (Figure) getFigure()), getWidgetModel().isMultilineInput(),
        		new Color(Display.getDefault(), model.getBackgroundFocusColor()), model.isConfirmOnFocusLost()).show();
    }
}
