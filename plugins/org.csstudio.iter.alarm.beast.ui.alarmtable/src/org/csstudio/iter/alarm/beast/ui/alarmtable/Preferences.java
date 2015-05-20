/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.alarm.beast.ui.alarmtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/** Read preference settings.
 *  Defaults for the application are provided in preferences.ini.
 *  Final product can override in plugin_preferences.ini.
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class Preferences
{
    /** Preference and memento tag for combined vs. separate alarm tables */
    final public static String ALARM_TABLE_COMBINED_TABLES = "combined_alarm_table";
    
    /** Preference and memento tag for column names */
    final public static String ALARM_TABLE_COLUMN_SETTING = "alarm_table_columns";
    
    /** Preference and memento tag for synchronise alarms with the tree selection */
    final public static String ALARM_TABLE_SYNC_WITH_TREE = "synchronize_with_tree";
    
    /** Preference and memento tag for lock filter to the selected tree item */ 
    final public static String ALARM_TABLE_LOCK_SELECTION = "lock_tree_selection";    
    
    /** Preference tag for blinking of unacknowledged alarms icons */
    final public static String ALARM_TABLE_BLINK_UNACKNOWLEDGED = "blink_unacknowledged";
    
    /** Preference tag for blinking period */
    final public static String ALARM_TABLE_BLINK_PERIOD = "blinking_period";
    
    /** @return Alarm table row limit */
	public static int getAlarmTableRowLimit()
    {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(Activator.ID, "alarm_table_row_limit", 2500, null);
    }
	
	/** @return true if acknowledged an unacknowledged alarms are shown in a single table or false otherwise */
	public static boolean isCombinedAlarmTable()
	{
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(Activator.ID, ALARM_TABLE_COMBINED_TABLES, false, null);
	}
	
	/** @return true if the alarm table content shows only what is selected in the alarm tree */
	public static boolean isSynchronizeWithTree()
    {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(Activator.ID, ALARM_TABLE_SYNC_WITH_TREE, false, null);
    }
	
	/** @return true if the table content is locked on the selected item in the alarm tree or if it is changed
	 *         when the selection changes */
    public static boolean isLockTreeSelection()
    {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(Activator.ID, ALARM_TABLE_LOCK_SELECTION, false, null);
    }

    /** @return list of default columns for the table and their order */
    public static String[] getColumns()
    {
        final IPreferencesService service = Platform.getPreferencesService();
        final String pref = service.getString(Activator.ID, ALARM_TABLE_COLUMN_SETTING, 
                "ICON,35,0| PV,80,50| DESCRIPTION,80,100| TIME,80,70| CURRENT_SEVERITY,50,30|"+
                "CURRENT_STATUS,45,30| SEVERITY,50,30| STATUS,45,30| VALUE,45,30", null);
        return pref.split(" *\\| *"); // Vertical line-separated, allowing for spaces
    }
    
    /** @return blinking icons of unacknowledged alarms */
    public static boolean isBlinkUnacknowledged() 
    {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(Activator.ID, ALARM_TABLE_BLINK_UNACKNOWLEDGED, false, null);
    }
    
    /** @return the blinking period in milliseconds */
    public static int getBlinkingPeriod() 
    {
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(Activator.ID, ALARM_TABLE_BLINK_PERIOD, 500, null); 
    }

}
