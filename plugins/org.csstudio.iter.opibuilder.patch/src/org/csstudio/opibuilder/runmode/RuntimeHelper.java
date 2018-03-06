/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.MacrosInput;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 *
 * <code>RuntimeHelper</code> provides convenience methods to display the opi. This methods replace community methods
 * from the SingleSourceHelper.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public final class RuntimeHelper {

    private static IWorkbenchWindow runWindow;

    /**
     * Opens the OPI in the main opi running window. The method does the same as was used before
     * community replace the opi editor implementation with the opi view.
     *
     * @param path the path to the file to open
     * @param input macros to use
     */
    static void openOPIShell(IPath path, MacrosInput input) {
        try {
            if (runWindow == null) {
                runWindow = PlatformUI.getWorkbench().openWorkbenchWindow(OPIRunnerPerspective.ID, null);
                runWindow.addPageListener(new IPageListener() {
                    public void pageClosed(IWorkbenchPage page) {
                        runWindow = null;
                    }

                    public void pageActivated(IWorkbenchPage page) {
                        // NOP
                    }

                    public void pageOpened(IWorkbenchPage page) {
                        // NOP
                    }
                });
            }
            IWorkbenchPage page = runWindow.getActivePage();
            Shell shell = runWindow.getShell();
            if (shell.getMinimized())
                shell.setMinimized(false);
            shell.forceActive();
            shell.forceFocus();
            openDisplay(page, new RunnerInput(path, null, input));
            shell.moveAbove(null);
        } catch (WorkbenchException e) {
            ErrorHandlerUtil.handleError(NLS.bind("Failed to open {0}", path), e);
        }
    }

    /**
     * Opens an opi file in a new editor.
     *
     * @param page the page in which to open the editor
     * @param input the file to open
     */
    static void openDisplay(IWorkbenchPage page, RunnerInput input) {
        try {
            page.openEditor(input, OPIRunner.ID);
        } catch (PartInitException e) {
            ErrorHandlerUtil.handleError(NLS.bind("Failed to open {0}", input.getPath()), e);
        }

    }
}
