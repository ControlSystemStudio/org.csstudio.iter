/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.widgets.archive.xygraph.model;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.StringListProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;

/**
 * The model for XYGraph
 * @author Xihui Chen
 */
public class ArchiveXYGraphModel extends XYGraphModel {


    /** The ID of the number of time span. */
    public static final String PROP_TIME_SPAN = "time_span"; //$NON-NLS-1$
    
    /** The ID of the boolean value of plot data source. */
    public static final String PROP_PLOT_DATA_SOURCE = "plt_data_source"; //$NON-NLS-1$

    /** The ID of the items of archive data source. */
    public static final String PROP_ARCHIVE_DATA_SOURCE = "archive_data_source"; //$NON-NLS-1$

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.iter.widgets.archive.archiveXYGraph"; //$NON-NLS-1$

    /**default value of time span in second for the property*/
	private static final int DEFAULT_TIME_SPAN = 3600;

    public ArchiveXYGraphModel() {
        super();
    }

    @Override
    protected void configureProperties() {
    	super.configureProperties();
        addProperty(new IntegerProperty(PROP_TIME_SPAN, "Time span",
                WidgetPropertyCategory.Behavior, DEFAULT_TIME_SPAN, 0, Integer.MAX_VALUE));
        addProperty(new BooleanProperty(PROP_PLOT_DATA_SOURCE, "Plot Data Source",
                WidgetPropertyCategory.Behavior, true));
        addProperty(new StringListProperty(PROP_ARCHIVE_DATA_SOURCE, "Archive Data Source",
                WidgetPropertyCategory.Behavior, new ArrayList<>()));

        //on change for property plot_data_source, we change the visibility of archive_data_source
        getProperty(PROP_PLOT_DATA_SOURCE).addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setPropertyVisible(PROP_ARCHIVE_DATA_SOURCE, !(Boolean) evt.getNewValue());
			}
		});
        setPropertyVisible(PROP_ARCHIVE_DATA_SOURCE, false);
    }

    @Override
    public String getTypeID() {
        return ID;
    }
}