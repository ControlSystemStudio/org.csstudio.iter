/*******************************************************************************
* Copyright (c) 2010-2012 ITER Organization.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/
package org.csstudio.iter.utility.jmssendcmd;

import java.util.logging.Logger;

public class Activator {

	/** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.iter.utility.jmssendcmd";

    final private static Logger logger = Logger.getLogger(ID);

	/** @return Logger for plugin ID */
	public static Logger getLogger() {
		return logger;
	}

}
