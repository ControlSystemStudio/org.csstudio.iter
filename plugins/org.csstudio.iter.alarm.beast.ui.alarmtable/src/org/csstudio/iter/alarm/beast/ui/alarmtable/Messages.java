package org.csstudio.iter.alarm.beast.ui.alarmtable;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.csstudio.iter.alarm.beast.ui.alarmtable.messages"; //$NON-NLS-1$
	public static String AlarmTableCombined;
	public static String AlarmTableOpenErrorTitle;
	public static String AlarmTableOpenErrorMessage;
	public static String AlarmTablePartName;
	public static String AlarmTableRootName;
	public static String AlarmTableRowLimitInfoFmt;
	public static String AlarmTableRowLimitMessage;
    public static String AlarmTableSeparate;
    public static String AlarmTableTitleTT;
    public static String ColumnConfigDescription;
    public static String ColumnConfigTitle;
    public static String ConfigureColumns;
    public static String ResetColumns;
    public static String ToggleBlinking;
    public static String SynchronizeWithTreeCheckedTT;
    public static String SynchronizeWithTreeUncheckedTT;
    public static String LockFilterCheckedTT;
    public static String LockFilterUncheckedTT;
    public static String NewTableView;
    public static String SelectFilter;
    public static String SelectFilterDialogTitle;
    public static String SelectFilterDialogMessage;
    public static String SelectFilterItemNonExisting;
    public static String CurrentAlarmsFmt;
    public static String AcknowledgedAlarmsFmt;
    
    
    public static String AcknowledgeColumnHeader;
    public static String AcknowledgeColumnHeaderTooltip;
    public static String AlarmIconColumnHeader;
    public static String AlarmIconColumnHeaderTooltip;
    public static String AlarmID;
    public static String AlarmIDTooltip;
    public static String AlarmPV;
    public static String AlarmPVTooltip;
    public static String AlarmDescription;
    public static String AlarmDescriptionTooltip;
    public static String AlarmTime;
    public static String AlarmTimeTooltip;
    public static String AlarmCurrentMessage;
    public static String AlarmCurrentMessageTooltip; 
    public static String AlarmCurrentSeverity;
    public static String AlarmCurrentSeverityTooltip;
    public static String AlarmSeverity;
    public static String AlarmSeverityTooltip;
    public static String AlarmMessage;
    public static String AlarmMessageTooltip;
    public static String AlarmValue;
    public static String AlarmValueTooltip;
    public static String AlarmAction;
    public static String AlarmActionTooltip;
    
    
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
