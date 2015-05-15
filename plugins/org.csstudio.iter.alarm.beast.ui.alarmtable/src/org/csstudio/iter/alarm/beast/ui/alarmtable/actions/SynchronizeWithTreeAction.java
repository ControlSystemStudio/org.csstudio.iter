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
 * <code>SynchronizeWithTreeAction</code> toggles the <code>synchronize the alarm table with the alarm tree 
 * selection</code> option. When checked the alarm table content will be synchronized with the selected item in the
 * alarm tree. When unchecked the alarm table will show the alarms from the root.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class SynchronizeWithTreeAction extends Action implements IPropertyChangeListener {
    private final AlarmTableView view;
    
    /**
     * Construct a new action.
     * 
     * @param view the view to act on
     * @param synchronize default state of this action 
     */
    public SynchronizeWithTreeAction(final AlarmTableView view, final boolean synchronize) {
        super(null, AS_CHECK_BOX);
        this.view = view;
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.ID, "icons/synced.gif"));
        setChecked(synchronize);
        addPropertyChangeListener(this);
        update();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        update();
    }
    
    private void update() {
        if (isChecked()) {
            setToolTipText(Messages.SynchronizeWithTreeCheckedTT);
        } else {
            setToolTipText(Messages.SynchronizeWithTreeUncheckedTT);
        }
    }
    
    @Override
    public void run() {
        view.setSyncAlarmsWithTreeSelection(isChecked());
    }
}
