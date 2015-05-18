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
    public static String CurrentAlarmsFmt;
    public static String AcknowledgedAlarmsFmt;
    
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
