/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.opibuilder.scaler;

import java.awt.Dimension;

/**
 *
 * <code>To4kScaleAction</code> is an action that checks if the selected files are
 * in full HD format and scales them for a factor of 2.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class To4kScaleAction extends ToHDScaleAction {

    /*
     * (non-Javadoc)
     * @see org.csstudio.iter.opibuilder.scaler.ToHDScaleAction#isDimensionOK(java.awt.Dimension)
     */
    @Override
    protected boolean isDimensionOK(Dimension dim) {
        return dim.width <= 1920 && dim.height <= 1080;
    }

    /*
     * (non-Javadoc)
     * @see org.csstudio.iter.opibuilder.scaler.ScaleAction#getScale()
     */
    @Override
    protected double getScale() {
        return 2.0;
    }

    @Override
    protected String getScaleName() {
        return "4k scale";
    }
}
