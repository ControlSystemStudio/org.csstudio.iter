/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.scaler;

import java.awt.Dimension;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 *
 * <code>ToHDScaleAction</code> is an action that checks if the selected files are
 * in 4K format and scales them with a factor of 0.5.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class ToHDScaleAction extends ScaleAction {

    /**
     * Checks if the dimension is acceptable for transformation and returns true if it is or false otherwise.
     *
     * @param dim the dimension to check
     * @return true if dimensio is accepted or false if denied
     */
    protected boolean isDimensionOK(Dimension dim) {
        return dim.width > 1920 || dim.height > 1080;
    }
    /*
     * (non-Javadoc)
     * @see org.csstudio.iter.opibuilder.scaler.ScaleAction#areFilesAndScaleOK(double, java.util.List)
     */
    @Override
    protected boolean areFilesAndScaleOK(double scale, List<IFile> files) {
        boolean error = false;
        for (IFile file : files) {
            error = false;
            try {
                Dimension dim = OPIScaler.getDisplayDimension(file.getLocation().toFile());
                if (dim == null) {
                    error = true;
                } else if (!isDimensionOK(dim)) {
                    MessageDialog.openError(parent, "Invalid OPI Scale", "OPI " + file.getName() + " appears to "
                            + "be in " + getScaleName() + " already. Please select a different file and try again "
                            + "or choose a custom scale.");
                    return false;
                }
            } catch (Exception e) {
                error = true;
            }

            if (error) {
                MessageDialog.openError(parent, "Invalid OPI File", "File " + file.getName() + " is not a "
                        + "valid OPI file. The display dimension could not be verified. "
                        + "Please select a different file and try again or choose a custom scale.");
                return false;
            }
        }
        return true;
    }

    protected String getScaleName() {
        return "Full HD scale";
    }

    /*
     * (non-Javadoc)
     * @see org.csstudio.iter.opibuilder.scaler.ScaleAction#getScale()
     */
    @Override
    protected double getScale() {
        return 0.5;
    }

}
