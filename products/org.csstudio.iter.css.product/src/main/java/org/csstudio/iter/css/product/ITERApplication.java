/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.css.product;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.csstudio.startup.application.Application;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;

/**
 *
 * <code>ITERApplication</code> is an extension of the default CSS application that suppresses a specific exception printout made
 * by third party plugins
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class ITERApplication extends Application {

    @Override
    protected Object promptForWorkspace(Display display, IApplicationContext context) throws Exception {
        Object obj = super.promptForWorkspace(display, context);
        if (obj == IApplication.EXIT_OK) {
            return obj;
        }
        final Location loc = Platform.getInstanceLocation();
        final String args[] = (String[]) context.getArguments().get("application.args");
        for (String arg : args) {
            if (arg.equals("-cleanWorkbench")) {
                clearWorkbenchData(loc);
            }
            if (arg.equals("-cleanDiirtSettings")) {
                clearDiirtSettings(loc);
            }

        }
        cleanAlarmTable(loc);
        return obj;
    }

    private void cleanAlarmTable(Location loc) throws IOException {
        if (loc.isSet()) {
            URL url = loc.getURL();
            File file = new File(url.getFile());
            file = new File(file, ".metadata");
            file = new File(file, ".plugins");
            file = new File(file, "org.eclipse.e4.workbench");
            file = new File(file, "workbench.xmi");
            if (file.exists()) {
                boolean delete = false;
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    while (br.ready()) {
                        String line = br.readLine();
                        if (line != null && line.contains("org.csstudio.iter.alarm.beast.ui.alarmtable")) {
                            delete = true;
                            break;
                        }
                    }
                }
                if (delete) {
                    file.delete();
                }
            }
        }
    }

    private void clearDiirtSettings(Location loc) {
        final URL url = loc.getURL();
        File file = new File(url.getFile() + "/.metadata/.plugins/org.eclipse.core.runtime/.settings");
        file = new File(file, "org.csstudio.diirt.util.core.preferences.prefs");
        file.delete();
    }

    private void clearWorkbenchData(Location loc) {
        final URL url = loc.getURL();
        File file = new File(url.getFile());
        file = new File(file, ".metadata");
        file = new File(file, ".plugins");
        file = new File(file, "org.eclipse.e4.workbench");
        file = new File(file, "workbench.xmi");
        file.delete();
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        // org.csstudio.alarm.beast.ui.alarmtable
        Object o = super.start(context);
        // Bugfix/workaround for org.apache.felix.gogo.shell.Activator,
        // which prints InterruptedException if stopped before it was even started.
        // It is using a hardcoded 100 ms sleep, so we should be safe if we wait 150 ms for
        // that plugin to finish its magic.
        Thread.sleep(150);
        return o;
    }
}
