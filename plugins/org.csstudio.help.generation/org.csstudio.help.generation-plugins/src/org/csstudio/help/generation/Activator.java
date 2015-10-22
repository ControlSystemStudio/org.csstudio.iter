/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.help.generation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.help.generation.preferences.Preferences;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin Activator
 * 
 * @author Lambert (sopra)
 */
public class Activator extends AbstractUIPlugin {

	private static Activator instance;
	private static Logger logger = Logger.getLogger("help.generation");

	/** Plug-in ID defined in MANIFEST.MF */
	final public static String ID = "org.csstudio.help.generation-plugins"; //$NON-NLS-1$

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

	
	public static void logInfo(String msg) {
		if (Preferences.isLogEnabled() && logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, msg);
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static Activator getInstance() {
		return instance;
	}
}
