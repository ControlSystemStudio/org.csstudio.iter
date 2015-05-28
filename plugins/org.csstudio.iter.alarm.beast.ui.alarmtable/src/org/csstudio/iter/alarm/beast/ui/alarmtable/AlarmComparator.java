/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.alarm.beast.ui.alarmtable;

import java.util.Comparator;

import org.csstudio.alarm.beast.AnnunciationFormatter;
import org.csstudio.alarm.beast.SeverityLevel;
import org.csstudio.alarm.beast.client.AlarmTreePV;
import org.csstudio.alarm.beast.client.GDCDataStructure;
import org.epics.util.time.Timestamp;

/** Comparator (= table sorter) that compares one column of an alarm.
 *  @author Kay Kasemir
 */
public class AlarmComparator implements Comparator<AlarmTreePV>
{
	/** Create comparator for AlarmTreePV entries
	 * 
	 *  @param col_info What to use for the comparison
	 *  @param up Up or downward sort?
	 *  @return Comparator<AlarmTreePV>
	 */
    public static Comparator<AlarmTreePV> getComparator(final ColumnInfo col_info, final boolean up)
    {
    	switch (col_info)
        {
    	case PV:
            return new AlarmComparator(up)
            {
                @Override
                protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
                {
                    final String prop1 = pv1.getName();
                    final String prop2 = pv2.getName();
                    final int cmp = prop1.compareTo(prop2);
                    return cmp != 0  ?  cmp  :  super.doCompare(pv1, pv2);
                }
            };
        case CURRENT_SEVERITY:
        	return new AlarmComparator(up)
			{
				@Override
				protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
				{
					final int level1 = pv1.getCurrentSeverity().ordinal();
					final int level2 = pv2.getCurrentSeverity().ordinal();
		            if (level1 == level2)
		                return super.doCompare(pv1, pv2);
		            return level1 - level2;
				}
			};
        case SEVERITY:
        	return new AlarmComparator(up)
			{
				@Override
				protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
				{
					final int level1 = pv1.getSeverity().ordinal();
					final int level2 = pv2.getSeverity().ordinal();
		            if (level1 == level2)
		                return super.doCompare(pv1, pv2);
		            return level1 - level2;
				}
			};
        case ICON:
//            1. Invalid / Disconnected alarm
//            2. Major unacknowledged alarm
//            3. Minor unacknowledged alarm
//            4. Alarm cleared but unacknowledged
//            5. Major acknowledged alarm
//            6. Minor acknowledged alarm
            return new AlarmComparator(up)
            {                
                @Override
                protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
                {
                    final SeverityLevel sv1 = pv1.getSeverity();
                    final SeverityLevel sv2 = pv2.getSeverity();
                    
                    final boolean pv1ClrNotAcked = pv1.getCurrentSeverity() == SeverityLevel.OK && sv1.isActive();
                    final boolean pv2ClrNotAcked = pv2.getCurrentSeverity() == SeverityLevel.OK && sv2.isActive();
                    
                    if (pv1ClrNotAcked ^ pv2ClrNotAcked) 
                        return pv1ClrNotAcked ? (sv2.isActive() ? -1 : 1) : (sv1.isActive() ? 1 : -1);
                    else 
                        return (sv1 == sv2) ? super.doCompare(pv1, pv2) : sv1.ordinal() - sv2.ordinal();
                }
            };
        case STATUS:
        	return new AlarmComparator(up)
			{
				@Override
				protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
				{
		            final int cmp = pv1.getMessage().compareTo(pv2.getMessage());
		            if (cmp != 0)
		            	return cmp;
	                return super.doCompare(pv1, pv2);
				}
			};
        case CURRENT_STATUS:
            return new AlarmComparator(up)
            {
                @Override
                protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
                {
                    final int cmp = pv1.getCurrentMessage().compareTo(pv2.getCurrentMessage());
                    if (cmp != 0)
                        return cmp;
                    return super.doCompare(pv1, pv2);
                }
            };
        case DESCRIPTION:
        	return new AlarmComparator(up)
			{
				@Override
				protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
				{
				    final String desc1 = AnnunciationFormatter.format(pv1.getDescription(),
				            pv1.getSeverity().getDisplayName(), pv1.getValue());
                    final String desc2 = AnnunciationFormatter.format(pv2.getDescription(),
                            pv2.getSeverity().getDisplayName(), pv2.getValue());
		            final int cmp = desc1.compareTo(desc2);
		            return cmp != 0  ?  cmp  :  super.doCompare(pv1, pv2);
				}
			};
        case ACK:
            return new AlarmComparator(up) 
            {
                @Override
                protected int doCompare(AlarmTreePV pv1, AlarmTreePV pv2)
                {
                    boolean active1 = pv1.getSeverity().isActive();
                    boolean active2 = pv2.getSeverity().isActive();
                    if (active1 == active2)
                        return super.doCompare(pv1, pv2);
                    return active1 ? -1 : 1;
                }
            };
        case VALUE:
            return new AlarmComparator(up) 
            {
                @Override
                protected int doCompare(AlarmTreePV pv1, AlarmTreePV pv2)
                {
                    final int cmp = pv1.getValue().compareTo(pv2.getValue());
                    if (cmp != 0)
                        return cmp;
                    return super.doCompare(pv1, pv2);
                }
            };
        case ACTION:
            return new AlarmComparator(up) 
            {
                @Override
                protected int doCompare(AlarmTreePV pv1, AlarmTreePV pv2)
                {
                    final int cmp = compareGuidance(pv1, pv2, false);
                    if (cmp != 0) 
                        return cmp;
                    return super.doCompare(pv1, pv2);
                }
            };
        case ID:
            return new AlarmComparator(up) 
            {
                @Override
                protected int doCompare(AlarmTreePV pv1, AlarmTreePV pv2)
                {
                    final int cmp = compareGuidance(pv1, pv2, true);
                    if (cmp != 0) 
                        return cmp;
                    return super.doCompare(pv1, pv2);
                }
            };
        case TIME:
        default:
        	return new AlarmComparator(up);
        }
    }
    
    private static int compareGuidance(AlarmTreePV pv1, AlarmTreePV pv2, boolean title) 
    {
        GDCDataStructure[] g1 = pv1.getGuidance();
        GDCDataStructure[] g2 = pv2.getGuidance();
        if (g1.length != 0 && g2.length != 0)
        {   
            int cmp = 0;
            if (title) 
                cmp = g1[0].getTitle().toLowerCase().compareTo(g2[0].getTitle().toLowerCase());
            else 
                cmp = g1[0].getDetails().toLowerCase().compareTo(g2[0].getDetails().toLowerCase());
            
            if (cmp != 0) 
                return cmp;
        } 
        else if (g1.length == 0 && g2.length != 0)
            return 1;
        else if (g1.length != 0 && g2.length == 0)
            return -1;
        return 0;
    }

    final private boolean up;
    
    /** Initialize
     *  @param up Sort 'up' or 'down'?
     */
    private AlarmComparator(final boolean up)
    {
        this.up = up;
    }

    /** {@inhericDoc} */
    @Override
    public int compare(final AlarmTreePV pv1, final AlarmTreePV pv2)
    {
    	if (up)
    		return doCompare(pv1, pv2);
    	else
    		return -doCompare(pv1, pv2);
    }

    /** Compare PVs in 'up' order
     * 
     *  Default compares by name, derived class can override.
     *  @param pv1
     *  @param pv2
     *  @return comparison -1, 0, 1
     */
    protected int doCompare(final AlarmTreePV pv1, final AlarmTreePV pv2)
    {
        Timestamp time1 = pv1.getTimestamp();
        Timestamp time2 = pv2.getTimestamp();
        if (time1 == null)
            time1 = Timestamp.of(0, 0);
        if (time2 == null)
            time2 = Timestamp.of(0, 0);
        final int cmp = time1.compareTo(time2);
        if (cmp != 0)
            return cmp;
    	final String prop1 = pv1.getName();
        final String prop2 = pv2.getName();
        return prop1.compareTo(prop2);
    }
}