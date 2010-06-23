package org.csstudio.trends.databrowser.propsheet;

import java.util.ArrayList;

import org.csstudio.apputil.ui.dialog.ErrorDialog;
import org.csstudio.apputil.ui.formula.FormulaDialog;
import org.csstudio.apputil.ui.formula.InputItem;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.trends.databrowser.Messages;
import org.csstudio.trends.databrowser.model.FormulaInput;
import org.csstudio.trends.databrowser.model.FormulaItem;
import org.csstudio.trends.databrowser.model.Model;
import org.csstudio.trends.databrowser.model.ModelItem;
import org.eclipse.swt.widgets.Shell;

/** Editor for editing a {@link FormulaItem}
 * 
 *  Technically this is not a Dialog, but code that calls the
 *  FormulaDialog with information from a FormulaItem,
 *  updating the item on success.
 *  
 *  @author Kay Kasemir
 */
public class EditFormulaDialog
{
    final OperationsManager operations_manager;
    final private Shell shell;
    final private FormulaItem formula;

    /** Create action
     *  @param operations_manager Manager used to 'undo' changes. May be <code>null</code>.
     *  @param shell Parent shell for formula edit dialog
     *  @param formula FormulaItem to edit
     */
    public EditFormulaDialog(final OperationsManager operations_manager,
            final Shell shell, final FormulaItem formula)
    {
        this.operations_manager = operations_manager;
        this.shell = shell;
        this.formula = formula;
    }

    /** Open, i.e. display the dialog.
     *  @return <code>true</code> when the item was updated,
     *          <code>false</code> for 'cancel'
     */
    public boolean open()
    {
        try
        {
            // Edit
            final FormulaDialog dialog = new FormulaDialog(shell,
                                    formula.getExpression(), determineInputs());
            if (dialog.open() != FormulaDialog.OK)
                return false;

            // Update model item with new formula from dialog
            final Model model = formula.getModel();
            final ArrayList<FormulaInput> new_inputs = new ArrayList<FormulaInput>();
            for (InputItem input : dialog.getInputs())
            {
                final ModelItem item = model.getItem(input.getInputName());
                if (item == null)
                    throw new Exception("Cannot locate formula input " + input.getInputName()); //$NON-NLS-1$
                new_inputs.add(new FormulaInput(item, input.getVariableName()));
            }
            // Update formula via undo-able command
            new ChangeFormulaCommand(shell, operations_manager, formula,
                    dialog.getFormula(),
                    new_inputs.toArray(new FormulaInput[new_inputs.size()]));
        }
        catch (Exception ex)
        {
            ErrorDialog.open(shell, Messages.Error, ex.getMessage());
            return false;
        }
        return true;
    }
    
    /** @return List of inputs for formula: Each model item is a possible input,
     *          mapped to a variable name that's either already used in the
     *          formula for that model item, or a simple "x1", "x2", ... when
     *          not already used
     */
    @SuppressWarnings("nls")
    private InputItem[] determineInputs()
    {
        final Model model = formula.getModel();
        // Create list of inputs.
        final ArrayList<InputItem> inputs = new ArrayList<InputItem>();
        // Every model item is a possible input.
        for (int i=0; i<model.getItemCount(); ++i)
        {
            final ModelItem model_item = model.getItem(i);
            // Formula cannot be an input to itself
            if (model_item == formula)
                continue;
            // Create InputItem for that ModelItem
            InputItem input = null;
            // See if model item is already used in the formula
            for (FormulaInput existing_input : formula.getInputs())
            {
                if (existing_input.getItem() == model_item)
                {   // Yes, use the existing variable name
                    input = new InputItem(model_item.getName(),
                                          existing_input.getVariableName());
                    break;
                }
            }
            // If input is unused, assign variable name x1, x2, ...
            if (input == null)
            {
                // Try "x1", then "xx1", "xxx1" until an unused name is found
                String var_name = Integer.toString(inputs.size()+1);
                boolean name_in_use;
                do
                {
                    name_in_use = false;
                    var_name = "x" + var_name;
                    for (FormulaInput existing_input : formula.getInputs())
                    {
                        if (existing_input.getVariableName().equals(var_name))
                        { 
                            name_in_use = true;
                            break;
                        }
                    }
                }
                while (name_in_use);
                input = new InputItem(model_item.getName(), var_name);
            }
            inputs.add(input);
        }
        return inputs.toArray(new InputItem[inputs.size()]);
    }
}