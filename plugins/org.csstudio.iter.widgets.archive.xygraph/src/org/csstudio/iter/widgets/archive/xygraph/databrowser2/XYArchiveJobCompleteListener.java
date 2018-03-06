/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.widgets.archive.xygraph.databrowser2;

import org.csstudio.trends.databrowser2.model.PVSamples;

/**
 * interface of the listener for {@link XYArchiveJobCompleteListener}
 *
 * @author lamberm
 *
 */
public interface XYArchiveJobCompleteListener {
    /**
     * call when complete
     *
     * @param samples list of sample
     */
    void complete(PVSamples samples);
}
