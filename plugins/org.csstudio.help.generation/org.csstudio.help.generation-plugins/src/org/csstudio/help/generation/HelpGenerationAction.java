/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.help.generation;

import java.util.logging.Level;

import org.csstudio.help.generation.html.GenerateHTML;
import org.csstudio.help.generation.preferences.Preferences;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class HelpGenerationAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;
    @Override
    public void run(IAction arg0) {
        Job job = new Job("Help generation") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                GenerateHTML generateHTML = new GenerateHTML();
                try {
                    generateHTML.create();
                } catch (Exception e) {
                    Activator.getLogger().log(Level.WARNING, "generation pdf error", e);
                    final Exception e1 = e;
                    Display.getDefault().asyncExec(new Runnable() {
                          public void run() {
                              MessageDialog.openError(window.getShell(), "Generation PDF Error",
                                e1.getMessage());
                          }
                    });
                    return Status.CANCEL_STATUS;
                }
                Display.getDefault().asyncExec(new Runnable() {
                      public void run() {
                          MessageDialog.openInformation(
                            window.getShell(), "Info",
                            "Help has been successfully generated at '" + Preferences.getGeneratedDocumentationPath() + "'");
                      }
                });
                return Status.OK_STATUS;
            }
        };

        // Start the Job
        job.schedule();
    }

    @Override
    public void selectionChanged(IAction arg0, ISelection arg1) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

}
