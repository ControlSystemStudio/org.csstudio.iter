/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser.ui;

import org.csstudio.platform.model.IArchiveDataSource;

/** Interface used by Plot to send events in response to user input:
 *  Zoom changed, scrolling turned on/off
 *  @author Kay Kasemir
 */
public interface PlotListener
{
    /** Called when the user enables/disables scrolling
     *  @param enable_scrolling true when user requested scrolling via GUI
     */
    public void scrollRequested(boolean enable_scrolling);

    /** Called when the user requests time config dialog. */
    public void timeConfigRequested();
    
    /** Called when the user enables/disables scrolling
     *  @param start_ms New time axis start time in ms since 1970
     *  @param end_ms ... end time ...
     */
    public void timeAxisChanged(long start_ms, long end_ms);

    /** Called when the user changed a value (Y) axis
     *  @param index Value axis index 0, 1, ...
     *  @param lower Lower range limit
     *  @param upper Upper range limit
     */
    public void valueAxisChanged(int index, double lower, double upper);
    
    /** Received a name, presumably a PV name via drag & drop
     *  @param name PV(?) name
     */
    public void droppedName(String name);

    /** Received a PV name and/or archive data source via drag & drop
     *  @param name PV name or <code>null</code>
     *  @param archive Archive data source or <code>null</code>
     */
    public void droppedPVName(String name, IArchiveDataSource archive);

}