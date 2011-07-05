/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.product;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** Default perspective for CSS
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class CSS_Perspective implements IPerspectiveFactory
{
    /** Perspective ID registered in plugin.xml */
    final public static String ID = "org.csstudio.utility.product.CSS_Perspective";

    // Other view IDs
    // Copied them here instead of using their ...View.ID member so that
    // this plugin doesn't depend on other app plugins.
    final private static String ID_PROBE = "org.csstudio.diag.probe.Probe";
    final private static String ID_CLOCK = "org.csstudio.utility.clock.ClockView";
    final private static String ID_DATABROWSER_PERSP = "org.csstudio.trends.databrowser.Perspective";
    final private static String ID_DATABROWSER_CONFIG = "org.csstudio.trends.databrowser.configview.ConfigView";
    final private static String ID_SNS_PV_UTIL = "org.csstudio.diag.pvutil.view.PVUtilView";
    final private static String ID_ALARM_TREE = "org.csstudio.alarm.ui.alarmtree.View";
    final private static String ID_ALARM_TABLE= "org.csstudio.alarm.ui.alarmtable.view";

    @Override
    public void createInitialLayout(IPageLayout layout)
    {
        // left | editor
        //      |
        //      |
        //      +-------------
        //      | bottom
        String editor = layout.getEditorArea();
        IFolderLayout left = layout.createFolder("left",
                        IPageLayout.LEFT, 0.25f, editor);
        IFolderLayout bottom = layout.createFolder("bottom",
                        IPageLayout.BOTTOM, 0.66f, editor);

        // Stuff for 'left'
        left.addView("org.eclipse.ui.views.ResourceNavigator");
        left.addPlaceholder(ID_SNS_PV_UTIL);
        left.addPlaceholder(ID_ALARM_TREE);

        // Stuff for 'bottom'
        bottom.addPlaceholder(ID_PROBE);
        bottom.addPlaceholder(ID_PROBE + ":*");
        bottom.addPlaceholder(ID_DATABROWSER_CONFIG);
        bottom.addPlaceholder(ID_ALARM_TABLE);
        bottom.addPlaceholder(IPageLayout.ID_PROGRESS_VIEW);

        // Populate the "Window/Perspectives..." menu with suggested persp.
        layout.addPerspectiveShortcut(ID);
        layout.addPerspectiveShortcut(ID_DATABROWSER_PERSP);

        // Populate the "Window/Views..." menu with suggested views
        layout.addShowViewShortcut(ID_CLOCK);
        layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
	}
}
