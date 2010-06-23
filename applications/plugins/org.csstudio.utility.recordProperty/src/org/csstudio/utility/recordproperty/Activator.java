package org.csstudio.utility.recordproperty;

import org.csstudio.platform.ui.AbstractCssUiPlugin;
import org.csstudio.utility.ldap.service.ILdapService;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractCssUiPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.csstudio.utility.recordProperty";

	// The shared instance
	private static Activator INSTANCE;

	private ILdapService _ldapService;

	/**
	 * The constructor
	 */
	public Activator() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Activator " + PLUGIN_ID + " does already exist.");
        }
        INSTANCE = this; // Antipattern is required by the framework!
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStart(final BundleContext context) throws Exception {
		_ldapService = getService(context, ILdapService.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStop(final BundleContext context) throws Exception {
		INSTANCE = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return INSTANCE;
	}

    /**
     * @return the LDAP service
     */
    public ILdapService getLdapService() {
        return _ldapService;
    }

}