/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.scan.server;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    /** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.iter.scan.server";

    final private static Logger logger = Logger.getLogger(ID);

    /** @return Logger for plugin ID */
    public static Logger getLogger() {
        return logger;
    }

    @Override
    public void start(BundleContext context) throws Exception {
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
