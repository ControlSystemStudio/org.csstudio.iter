package org.csstudio.iter.css.product;

import org.csstudio.iter.css.product.util.WorkbenchUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.csstudio.iter.css.product";
	
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				WorkbenchUtil.unbindDebugLast();
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
