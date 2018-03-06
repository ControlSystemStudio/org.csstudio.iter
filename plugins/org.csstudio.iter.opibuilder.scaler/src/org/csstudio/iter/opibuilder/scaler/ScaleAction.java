/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.scaler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 *
 * <code>ScaleAction</code> is a Navigator action that scales the selecte4d files for a factor that user enters into
 * a message dialog.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class ScaleAction implements IObjectActionDelegate {

    private List<IFile> selectedFiles;
    protected Shell parent;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        if (selectedFiles.isEmpty()) {
            MessageDialog.openWarning(parent, "No File Selected",
                    "You need to select at least one OPI file to transform");
        } else {
            double scale = getScale();
            if (!Double.isNaN(scale)) {
                if (!areFilesAndScaleOK(scale, selectedFiles)) {
                    return;
                }
                final List<String> errors = new ArrayList<String>();

                new Job("Opi scaling") {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        for (IFile f : selectedFiles) {
                            if (monitor.isCanceled()) {
                                return Status.CANCEL_STATUS;
                            }
                            try {
                                //create a backup file
                                String bck = f.getLocation().toFile().getAbsolutePath();
                                bck = bck.substring(0, bck.length() - 4) + "_bak.opi";
                                File backup = new File(bck);
                                Files.copy(f.getLocation().toFile().toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                //scale opi
                                OPIScaler.scale(f.getLocation().toFile(), f.getLocation().toFile(), scale);
                                //refresh the project to see the backup file as well
                                f.getProject().refreshLocal(IResource.DEPTH_ONE, monitor);
                            } catch (Exception e) {
                                errors.add(e.getMessage());
                            }
                        }
                        if (monitor.isCanceled()) {
                            return Status.CANCEL_STATUS;
                        }

                        Display.getDefault().asyncExec(new Runnable(){
                            public void run() {
                                if (errors.isEmpty()) {
                                    MessageDialog.openInformation(parent, "Scaling Completed",
                                            (selectedFiles.size() == 1 ? "File " + selectedFiles.get(0).getName() :
                                            (selectedFiles.size() + " files")) + " successfully scaled by a factor of "
                                                    + scale + ".");
                                } else {
                                    StringBuilder sb = new StringBuilder((errors.size() + 2)* 150);
                                    sb.append("The following errors ocurred during OPI scaling:\n");
                                    for (String s : errors) {
                                        sb.append(s).append('\n');
                                    }
                                    int num = selectedFiles.size() - errors.size();
                                    sb.append('\n').append(num).append(num == 1 ? " file" : " files").append(" scaled successfully.");
                                    MessageDialog.openError(parent, "Scaling Error", sb.toString());
                                }
                            };
                        });

                        return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
                    }
                }.schedule();
            }
        }
    }

    /**
     * Check if the files are OK to be scaled with the given scale. If yes, it returns true,
     * if no it returns false. A message can be displayed within this method if needed.
     *
     * @param scale the scale used for transformation
     * @param files the files which will be scaled
     * @return true if the files can be scaled or false otherwise
     */
    protected boolean areFilesAndScaleOK(double scale, List<IFile> files) {
        return true;
    }

    /**
     * Returns the scale which will be used for OPI transformation.
     * This method displays a dialog that allows user to enter the scaling factor.
     *
     * @return the scale factor
     */
    protected double getScale() {
        InputDialog dialog = new InputDialog(parent,"Enter Scaling Factor",
                "Please enter scaling factor to be used in conversion: ",
                "2.0",new IInputValidator() {
                    @Override
                    public String isValid(String newText) {
                        try {
                            double d = Double.parseDouble(newText);
                            if (d <= 0) {
                                return "Enter a positive numerical value";
                            }
                        } catch (NumberFormatException e) {
                            return "Enter a positive numerical value";
                        }
                        return null;
                    }
                });
        if (dialog.open() == Window.OK) {
            return Double.parseDouble(dialog.getValue());
        } else {
            return Double.NaN;
        }

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        selectedFiles = new ArrayList<>(0);
        if (selection instanceof IStructuredSelection) {
            selectedFiles = ((List<IFile>) ((IStructuredSelection) selection)
                    .toList());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        parent = targetPart.getSite().getShell();
    }

}
