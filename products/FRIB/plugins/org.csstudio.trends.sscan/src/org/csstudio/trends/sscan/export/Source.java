/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.sscan.export;

import org.csstudio.trends.sscan.Messages;

/** From where to export data
 *  @author Kay Kasemir
 */
public enum Source
{
    /** Use data from plot */
    PLOT(Messages.ExportSource_Plot),
    /** Fetch raw archive data */
    RAW_ARCHIVE(Messages.ExportSource_RawArchive),
    /** Get optimized (reduced) archive data */
    OPTIMIZED_ARCHIVE(Messages.ExportSource_OptimizedArchive);
    
    final private String name;
    
    private Source(final String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
