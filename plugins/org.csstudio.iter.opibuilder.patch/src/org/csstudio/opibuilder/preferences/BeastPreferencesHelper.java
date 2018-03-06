/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.csstudio.opibuilder.OPIBuilderPlugin;

public class BeastPreferencesHelper {

    // BEAST Alarm functionality in BOY: enabled/disabled & blinking period (in ms)
    public static final String OPI_BEAST_ALARMS_ENABLED = "opi_beast_alarms_enabled"; //$NON-NLS-1$
    public static final String OPI_BEAST_ALARMS_BLINK_PERIOD = "opi_beast_alarms_blink_period"; //$NON-NLS-1$

    /**
     * Property controlling whether BEAST Alarms functionality should be enabled for BOY OPIs.
     * @return true if BEAST Alarms functionality should be enabled for BOY OPIs
     */
    public static boolean isOpiBeastAlarmsEnabled(){
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getBoolean(OPIBuilderPlugin.PLUGIN_ID, OPI_BEAST_ALARMS_ENABLED, false, null);
    }

    /**
     * Blinking period (in ms) for BEAST Alarm visual feedback in BOY.
     * Default is 500ms (blinks at 2Hz).
     * @return Blinking period in ms for BEAST Alarm visual feedback in BOY
     */
    public static Integer getOpiBeastAlarmsBlinkPeriod(){
        final IPreferencesService service = Platform.getPreferencesService();
        return service.getInt(OPIBuilderPlugin.PLUGIN_ID, OPI_BEAST_ALARMS_BLINK_PERIOD, 500, null);
    }
}
