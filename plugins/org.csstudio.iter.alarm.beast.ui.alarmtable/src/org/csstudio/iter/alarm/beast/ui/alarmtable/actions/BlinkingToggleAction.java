package org.csstudio.iter.alarm.beast.ui.alarmtable.actions;

import org.csstudio.iter.alarm.beast.ui.alarmtable.AlarmTableView;
import org.csstudio.iter.alarm.beast.ui.alarmtable.Messages;
import org.eclipse.jface.action.Action;

/**
 * 
 * <code>BlinkToggleAction</code> turns the blinking of severity icons on or off.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class BlinkingToggleAction extends Action {
    private final AlarmTableView view;
    
    /**
     * Construct a new action.
     * 
     * @param view the view that owns this action
     * @param checked initial state for the button
     */
    public BlinkingToggleAction(final AlarmTableView view, boolean checked) {
        super(Messages.ToggleBlinking,Action.AS_CHECK_BOX);
        this.view = view;
        setChecked(checked);
    }
    
    @Override
    public void run() {
        view.setBlinkingIcons(isChecked());
    }
}
