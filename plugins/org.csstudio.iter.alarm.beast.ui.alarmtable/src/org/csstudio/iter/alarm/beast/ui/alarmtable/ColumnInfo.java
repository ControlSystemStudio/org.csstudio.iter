package org.csstudio.iter.alarm.beast.ui.alarmtable;

/** Description of one column in the alarm table
 *
 *  @author Kay Kasemir
 *  @author Jaka Bobnar - Extracted inner class of AlarmTableLabelProvider
 */
public enum ColumnInfo
{
    ACK(Messages.AcknowledgeColumnHeader, Messages.AcknowledgeColumnHeaderTooltip ,35, 0),
    ICON(Messages.AlarmIconColumnHeader, Messages.AlarmIconColumnHeaderTooltip, 20, 0),
    PV(Messages.AlarmPV, Messages.AlarmPVTooltip, 80, 50),
    DESCRIPTION(Messages.AlarmDescription, Messages.AlarmDescriptionTooltip, 80, 100),
    TIME(Messages.AlarmTime, Messages.AlarmTimeTooltip, 80, 70),
    CURRENT_SEVERITY(Messages.AlarmCurrentSeverity, Messages.AlarmCurrentSeverityTooltip, 50, 30),
    CURRENT_STATUS(Messages.AlarmCurrentMessage, Messages.AlarmCurrentMessageTooltip, 45, 30),
    SEVERITY(Messages.AlarmSeverity, Messages.AlarmSeverityTooltip, 50, 30),
    STATUS(Messages.AlarmMessage, Messages.AlarmMessageTooltip, 45, 30),
    VALUE(Messages.AlarmValue, Messages.AlarmValueTooltip, 45, 30),
    ACTION(Messages.AlarmAction, Messages.AlarmActionTooltip, 45, 30),
    ID(Messages.AlarmID, Messages.AlarmIDTooltip, 45, 30);

    final private String title;
    final private String tooltip;

    final private int width, weight;

    /** Initialize Column
     *  @param title Column title
     */
    ColumnInfo(final String title, final String tooltip, final int widths, final int weight)
    {
        this.title = title;
        this.tooltip = tooltip;
        this.width = widths;
        this.weight = weight;
    }

    /** @return Column title */
    public String getTitle()
    {
        return title;
    }

    /** @return Minimum column width */
    public int getMinWidth()
    {
        return width;
    }

    /** @return Column resize weight */
    public int getWeight()
    {
        return weight;
    }
    
    /** @return column header tooltip */
    public String getTooltip()
    {
        return tooltip;
    }
}
