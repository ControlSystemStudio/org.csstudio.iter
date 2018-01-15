/*******************************************************************************
 * Copyright (c) 2010-2016 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.css.product;
import org.csstudio.iter.css.product.util.WorkbenchUtil;
import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 *
 * <code>ITERWorkbenchWindowAdvisor</code> provides a bugfix to restore the window toolbar
 * when the last closed windows was an OPI in compact mode.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
@SuppressWarnings("restriction")
public class ITERWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    private boolean toolbarWasVisible;

    /**
     * Constructs a new advisor.
     *
     * @param configurer
     */
    public ITERWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        WorkbenchWindow window = (WorkbenchWindow)getWindowConfigurer().getWindow();
        toolbarWasVisible = window.isToolbarVisible();
        window.setCoolBarVisible(true);
        window.setPerspectiveBarVisible(true);
    }

    @Override
    public void postWindowOpen() {
        super.postWindowOpen();
        WorkbenchWindow window = (WorkbenchWindow)getWindowConfigurer().getWindow();
        MApplication application = (MApplication) window.getService(MApplication.class);
        WorkbenchUtil.removeUnwantedViews(application);

        if (!toolbarWasVisible) {
            //put a runnable on the display queue, so it gets updated the last, when the correct menu bar is set
            window.getShell().getDisplay().asyncExec(() ->
            {
                window.getShell().setMenuBar(null);
             /* This is commented out as it was causing to force compact mode in case there was a window opened in full screen mode and Cs-studio restarted.
              *
              *  CompactModeAction action = WorkbenchWindowService.getInstance().getCompactModeAction(window);
                if (action != null && !action.isInCompactMode())
                    action.run(); */
            });
        }
    }
}
