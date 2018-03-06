/*******************************************************************************
* Copyright (c) 2010-2018 ITER Organization.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/
package org.csstudio.opibuilder.util;

import org.diirt.vtype.AlarmSeverity;

/**
 * BEAST alarm info for PV Widgets.
 * Holds the BeastDataSource channel name, blinking state, current & latched BEAST severity.
 *
 * @author Boris Versic
 */
public final class BeastAlarmInfo {
    private String alarmPVChannelName;
    private boolean isBeastChannelConnected;
    private BeastAlarmSeverityLevel latchedSeverity;
    private BeastAlarmSeverityLevel currentSeverity;
    private int alarmPVsCount;

    /**
     * The blinking "state" for the Beast Alarm alert: 0 = default color, 1 = severity color.
     */
    private int beastAlertBlinkState = 0;

    public BeastAlarmInfo() {
        alarmPVChannelName = "";
        isBeastChannelConnected = false;
        latchedSeverity = BeastAlarmSeverityLevel.OK;
        currentSeverity = BeastAlarmSeverityLevel.OK;
    }

    /**
     * Get the Current severity of the BEAST alarm.
     * @return the Current Severity of the BEAST alarm, see {@link BeastAlarmSeverityLevel}.
     */
    public BeastAlarmSeverityLevel getCurrentSeverity() {
        return currentSeverity;
    }

    /**
     * Get the AlarmSeverity of the Current BEAST alarm severity.
     * Returns the {@link AlarmSeverity} of the Current {@link BeastAlarmSeverityLevel},
     * i.e. without the ack/unack distinction.
     *
     * @return the AlarmSeverity of the BEAST alarm Current Severity.
     */
    public AlarmSeverity getCurrentAlarmSeverity() {
        return currentSeverity.getAlarmSeverity();
    }

    /**
     * Set the Current severity of the BEAST alarm.
     * @param currentSeverity the Current Severity of the BEAST alarm, see {@link BeastAlarmSeverityLevel}.
     */
    public void setCurrentSeverity(BeastAlarmSeverityLevel currentSeverity) {
        this.currentSeverity = currentSeverity;
    }

    /**
     * Get the Latched severity of the BEAST alarm.
     * @return the Latched Severity of the BEAST alarm, see {@link BeastAlarmSeverityLevel}.
     */
    public BeastAlarmSeverityLevel getLatchedSeverity() {
        return latchedSeverity;
    }

    /**
     * Get the AlarmSeverity of the Latched BEAST alarm severity.
     * Returns the {@link AlarmSeverity} of the Latched {@link BeastAlarmSeverityLevel},
     * i.e. without the ack/unack distinction.
     *
     * @return the AlarmSeverity of the BEAST alarm Latched Severity.
     */
    public AlarmSeverity getLatchedAlarmSeverity() {
        return latchedSeverity.getAlarmSeverity();
    }

    /**
     * Set the Latched severity of the BEAST alarm.
     * @param latchedSeverity the Latched Severity of the BEAST alarm, see {@link BeastAlarmSeverityLevel}.
     */
    public void setLatchedSeverity(BeastAlarmSeverityLevel latchedSeverity) {
        this.latchedSeverity = latchedSeverity;
    }

    /**
     * Get the next color to be used for blinking the Beast Alarm alert:
     * {@code 0} = default color (used for {@link AlarmSeverity#OK}),
     * {@code 1} = current severity color
     *
     * @return the blinking "state" for the Beast Alarm alert
     */
    public int getBeastAlertBlinkState() {
        return beastAlertBlinkState;
    }

    /**
     * Set the blinking "state" for the Beast Alarm alert:
     * passing {@code 0} will use the default color ({@link AlarmSeverity#OK}) for the next Blink change,
     * {@code 1} will use the current severity's color.
     *
     * @param beastAlertBlinkState the beastAlertBlinkState to set
     */
    public void setBeastAlertBlinkState(int beastAlertBlinkState) {
        this.beastAlertBlinkState = beastAlertBlinkState;
    }

    /**
     * Set the BeastDataSource Channel name for this BEAST Alarm.
     * @param channelName the BeastDataSource Channel name to set
     */
    public void setBeastChannelName(String channelName) {
        alarmPVChannelName = channelName;
    }

    /**
     * Get the BeastDataSource Channel name of this BEAST Alarm.
     * @return the BeastDataSource Channel name of this BEAST Alarm
     */
    public String getBeastChannelName() {
        return alarmPVChannelName;
    }

    /**
     * Get the BeastDataSource Channel name of this BEAST Alarm without the scheme/channel protocol (the initial "beast://").
     * @return the BeastDataSource Channel name of this BEAST Alarm without the scheme
     */
    public String getBeastChannelNameWithoutScheme() {
        if (alarmPVChannelName.length() > 8)
            return alarmPVChannelName.substring(8);
        return "";
    }

    /**
     * Is the Latched BEAST alarm Acknowledged ?
     * See {@link BeastAlarmSeverityLevel#isActive}.
     *
     * @return {@code true} if (latched) severity indicates an acknowledged alarm state,
     *          {@code false} for unacknowledged alarm or OK
     */
    public boolean isAcknowledged() {
        return !latchedSeverity.isActive() && latchedSeverity != BeastAlarmSeverityLevel.OK;
    }

    /**
     * Is the Latched BEAST alarm Active (not OK or Acknowledged) ?
     * See {@link BeastAlarmSeverityLevel#isActive}.
     *
     * @return {@code true} if latched severity indicates an active alarm,
     *          {@code false} for acknowledged or OK state
     */
    public boolean isLatchedAlarmActive() {
        return latchedSeverity.isActive();
    }

    /**
     * Is the Latched BEAST alarm OK ?
     * @return {@code true} if the Latched Severity is OK, {@code false} for any other state.
     */
    public boolean isLatchedAlarmOK() {
        return latchedSeverity == BeastAlarmSeverityLevel.OK;
    }

    /**
     * Is the Current BEAST alarm Active (not OK or Acknowledged) ?
     * See {@link BeastAlarmSeverityLevel#isActive}.
     *
     * @return {@code true} if current severity indicates an active alarm,
     *          {@code false} for acknowledged or OK state
     */
    public boolean isCurrentAlarmActive() {
        return currentSeverity.isActive();
    }

    /**
     * Reset the Current and Latched severities for this BEAST alarm and set the beastAlertBlinkState to 0.
     */
    public void reset() {
        latchedSeverity = BeastAlarmSeverityLevel.OK;
        currentSeverity = BeastAlarmSeverityLevel.OK;
        beastAlertBlinkState = 0;
    }

    /**
     * Is the BeastDataSource channel for this PV connected ?
     * @return {@code true} if the PVReader reported it successfully connected or we received at least
     * one ValueChanged event from it, {@code false} otherwise.
     */
    public boolean isBeastChannelConnected() {
        return isBeastChannelConnected;
    }

    /**
     * Set the current BeastDataSource connection status.
     * @param connected {@code true} if the PVReader reported it successfully connected or we received at least
     * one ValueChanged event from it, {@code false} otherwise.
     */
    public void setBeastChannelConnected(boolean connected) {
        isBeastChannelConnected = connected;
    }

    /**
     * Get the number of PVs in alarm state.
     * @return The number of PVs whose Latched severity is not {@code SeverityLevel.OK}.
     */
    public int getAlarmPVsCount() {
        return alarmPVsCount;
    }

    /**
     * Set the number of PVs in alarm state.
     * @param count The number of PVs whose Latched severity is not {@code SeverityLevel.OK}.
     */
    public void setAlarmPVsCount(int count) {
        alarmPVsCount = count;
    }
}
