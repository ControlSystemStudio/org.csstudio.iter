package org.csstudio.iter.alarm.beast.ui.alarmtable.actions;

import org.csstudio.iter.alarm.beast.ui.alarmtable.Activator;
import org.csstudio.iter.alarm.beast.ui.alarmtable.AlarmTableView;
import org.csstudio.iter.alarm.beast.ui.alarmtable.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * <code>LockTreeSelectionAction</code> toggles the locking of the selected alarm tree item. When checked the alarm
 * table will keep the currently selected alarm tree item as the base for the alarms. When unchecked the alarm table
 * content will change according to the tree selection.  
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class LockTreeSelectionAction extends Action implements IPropertyChangeListener {
    private final AlarmTableView view;
    
    /**
     * Construct a new lock tree selection action.
     * 
     * @param view the view to act on
     * @param lock the default state of this action 
     */
    public LockTreeSelectionAction(final AlarmTableView view, final boolean lock) {
        super(null, AS_CHECK_BOX);
        this.view = view;
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.ID, "icons/lock.gif"));
        setChecked(lock);
        addPropertyChangeListener(this);
        update();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
    }
    
    private void update() {
        if (isChecked()) {
            setToolTipText(Messages.LockFilterCheckedTT);
        } else {
            setToolTipText(Messages.LockFilterUncheckedTT);
        }
    }
    
    @Override
    public void run() {
        view.setLockTreeSelection(isChecked());
    }
}
