/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.sscan.model;

import org.csstudio.trends.sscan.model.ModelItem;

/** Listener interface for the Model
 *  @author Kay Kasemir
 */
public interface ModelListener
{
    /** One of the colors (background, ...) changed */
    void changedColors();

    /** @param axis Axis that changed its color, range, ....
     *              If <code>null</code>, an axis was added or removed
     */
    void changedAxis(AxisConfig axis);
    
   // void changedAxes(AxesConfig axes);
    
    void changedItemData(final ModelItem item);

    /** @param item Item that was added to the model */
    void itemAdded(ModelItem item);

    /** @param item Item that was removed from the model */
    void itemRemoved(ModelItem item);

    /** @param item Item that turned visible/invisible */
    void changedItemVisibility(ModelItem item);
    
    /** @param item Item that changed its visible attributes:
     *              color, line width, display name, ...
     */
    void changedItemLook(ModelItem item);

    /** @param item Item that changed its data configuration:
     *              Archives, request method.
     */
    void changedItemDataConfig(ModelItem item);

	/** The annotation list changed*/
	void changedAnnotations();

	void changedXYGraphConfig();
}
