/*
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.utility.ldapUpdater.preferences;

import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.IOC_DBL_DUMP_PATH;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.LDAP_AUTO_INTERVAL;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.LDAP_AUTO_START;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.LDAP_CONT_ROOT;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.LDAP_HIST_PATH;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.XMPP_PASSWD;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.XMPP_SERVER;
import static org.csstudio.utility.ldapUpdater.preferences.LdapUpdaterPreferenceKey.XMPP_USER;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.csstudio.utility.ldapUpdater.Activator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Default initialization values, if there isn't any plugin_custimization.ini present.
 *
 * @author bknerr 25.03.2010
 */
public class LdapUpdaterPreferences extends AbstractPreferenceInitializer {

    @Override
    public final void initializeDefaultPreferences() {
        final IEclipsePreferences prefs =
            new DefaultScope().getNode(Activator.getDefault().getPluginId());

        prefs.put(IOC_DBL_DUMP_PATH.getDescription(), "Y:\\directoryServer\\");  // unix : "/applic/directoryServer/";
        prefs.put(LDAP_CONT_ROOT.getDescription(), "de.desy.epicsControls.");
        prefs.put(LDAP_HIST_PATH.getDescription(), "Y:\\scripts\\ldap-tests\\");
        prefs.put(XMPP_USER.getDescription(), "LDAP_Updater");
        prefs.put(XMPP_PASSWD.getDescription(), "LDAP_Updater");
        prefs.put(XMPP_SERVER.getDescription(), "krynfs.desy.de");
        prefs.put(LDAP_AUTO_START.getDescription(), "46800");
        prefs.put(LDAP_AUTO_INTERVAL.getDescription(), "43200");

    }

    /**
     * Retrieves value from preferences.
     * @param prefKey the preference key
     * @return the value
     */
    @CheckForNull
    public static String getValueFromPreferences(@Nonnull final LdapUpdaterPreferenceKey prefKey) {
        return getValueFromPreferences(Activator.getDefault(), prefKey, "", null);
    }

    /**
     * Retrieves value from preferences.
     * @param prefKey the preference key
     * @param defaultValue the default value, if there isn't any value set.
     * @return the value
     */
    @CheckForNull
    public static String getValueFromPreferences(@Nonnull final LdapUpdaterPreferenceKey prefKey,
                                                 @Nonnull final String defaultValue) {
        return getValueFromPreferences(Activator.getDefault(), prefKey, defaultValue, null);
    }

    /**
     * Retrieves value from preferences.
     * @param activator .
     * @param prefKey the preference key
     * @param defaultValue .
     * @param contexts preference contexts
     * @return the value
     */
    @CheckForNull
    public static String getValueFromPreferences(@Nonnull final Activator activator,
                                                 @Nonnull final LdapUpdaterPreferenceKey prefKey,
                                                 @Nullable final String defaultValue,
                                                 @Nullable final IScopeContext[] contexts) {
        final IPreferencesService prefs = Platform.getPreferencesService();
        return prefs.getString(activator.getPluginId(),
                               prefKey.getDescription(), defaultValue, contexts);
    }

}