/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.help.generation.preferences;

import org.csstudio.help.generation.Activator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class Preferences {

    public final static String TOC_FILTER = "toc.filters";
    public final static String LOG_ENABLED = "log.enabled";
    public final static String TOC_DISABLE = "toc.disable";
    public final static String GENERATION_DIRECTORY = "generation.directory";
    /**
     * @param setting Preference identifier
     * @return String from preference system, or <code>null</code>
     */
    private static String getString(final String setting) {
        return getString(setting, null);
    }

    /**
     * @param setting Preference identifier
     * @param default_value Default value when preferences unavailable
     * @return String from preference system, or <code>null</code>
     */
    private static String getString(final String setting,
            final String default_value) {
        final IPreferencesService service = Platform.getPreferencesService();
        if (service == null)
            return default_value;
        return service.getString(Activator.ID, setting, default_value, null);
    }

    /** @return toc filter */
    public static String[] getTocFilter() {
        String tocFilter = getString(TOC_FILTER);
        if (tocFilter != null) {
            return tocFilter.split(",");
        }
        return new String[]{};
    }
    /** @return toc filter */
    public static String[] getTocDisable() {
        String tocFilter = getString(TOC_DISABLE);
        tocFilter = tocFilter.replaceAll("\\\\,", "{tmpComa}");
        if (tocFilter != null) {
            String[] tocFilters = tocFilter.split(",");
            for (int i = 0; i < tocFilters.length; i++) {
                if (tocFilters[i].contains("{tmpComa}")) {
                    tocFilters[i] = tocFilters[i].replace("{tmpComa}", ",");
                }
            }
            return tocFilters;
        }
        return new String[]{};
    }

    public static Boolean isLogEnabled() {
        return Boolean.valueOf(getString(LOG_ENABLED));
    }

    public static String getGeneratedDocumentationPath() {
        return getString(GENERATION_DIRECTORY);
    }
}
