/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.widgets.archive.xygraph.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.archive.vtype.ArchiveVType;
import org.csstudio.archive.vtype.TimestampHelper;
import org.csstudio.iter.widgets.archive.xygraph.Activator;
import org.csstudio.iter.widgets.archive.xygraph.databrowser2.XYArchiveFetchJob;
import org.csstudio.iter.widgets.archive.xygraph.databrowser2.XYArchiveJobCompleteListener;
import org.csstudio.iter.widgets.archive.xygraph.model.ArchiveXYGraphModel;
import org.csstudio.iter.widgets.archive.xygraph.util.DataSourceUrl;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.editparts.XYGraphEditPart;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.csstudio.opibuilder.widgets.model.XYGraphModel.TraceProperty;
import org.csstudio.simplepv.VTypeHelper;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider.UpdateMode;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.ToolbarArmedXYGraph;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.undo.ZoomType;
import org.csstudio.trends.databrowser2.model.PVSamples;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.diirt.vtype.VType;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;

/**
 * The Archive XYGraph editpart
 *
 * @author lamberm (Sopra)
 * @author borut.terpinc@cosylab.com
 *
 */
public class ArchiveXYGraphEditPart extends XYGraphEditPart {

    private Map<Integer, List<VType>> cacheDuringLoad = new HashMap<>();

    private boolean isTriggerMode = false;
    private XYGraph xyGraph;

    private static final Long DEFAULT_MAX;
    static {
        long v = 100;
        try {
            Field f = XYGraphModel.class.getDeclaredField("DEFAULT_MAX");
            f.setAccessible(true);
            Object obj = f.get(null);
            if (obj instanceof Number) {
                v = ((Number) obj).longValue();
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Activator.getLogger().log(Level.WARNING, "Error while setting default value");
            Activator.getLogger().log(Level.WARNING, e.getMessage());
        }
        DEFAULT_MAX = v;
    }

    @Override
    public ArchiveXYGraphModel getWidgetModel() {
        return (ArchiveXYGraphModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
        final IFigure xyGraphFigure = super.doCreateFigure();

        // add values from datasource if the execution is in run mode
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            cacheDuringLoad = new HashMap<>();
            addValuesFromDatasource();

            // add listeners to the graph with Toolbar.
            if (xyGraphFigure instanceof ToolbarArmedXYGraph) {
                final ToolbarArmedXYGraph armedXYGraph = (ToolbarArmedXYGraph) xyGraphFigure;
                xyGraph = armedXYGraph.getXYGraph();

                // if the scrolling is changed
                xyGraph.addPropertyChangeListener(XYGraph.SCROLLING_PROPERTY, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(XYGraph.SCROLLING_PROPERTY) && evt.getNewValue().equals(false))
                            handleScrollingEnabled();
                    }
                });

                // add mouse listener to catch the events on zoom and panning
                final ArchiveGraphMouseListener mouseListener = new ArchiveGraphMouseListener();
                xyGraph.getPlotArea().addMouseListener(mouseListener);
                xyGraph.getPlotArea().addMouseMotionListener(mouseListener);

            }
        }
        return xyGraphFigure;
    }

    private void handleScrollingEnabled() {
        xyGraph.setZoomType(ZoomType.NONE);
        addValuesFromDatasource();
    }

    /**
     * Listener to mouse events, performs data update on zoom and panning events.
     */
    class ArchiveGraphMouseListener extends MouseMotionListener.Stub implements MouseListener {

        @Override
        public void mouseDragged(final MouseEvent me) {
            final ZoomType zoomType = xyGraph.getZoomType();
            if (zoomType == ZoomType.PANNING || zoomType == ZoomType.ZOOM_IN || zoomType == ZoomType.ZOOM_OUT
                    || zoomType == ZoomType.ZOOM_IN_HORIZONTALLY || zoomType == ZoomType.RUBBERBAND_ZOOM) {
                addValuesFromDatasource(xyGraph.primaryXAxis.getRange().getLower(),
                        xyGraph.primaryXAxis.getRange().getUpper());
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            final ZoomType zoomType = xyGraph.getZoomType();
            if (zoomType == ZoomType.PANNING || zoomType == ZoomType.ZOOM_IN || zoomType == ZoomType.ZOOM_OUT
                    || zoomType == ZoomType.ZOOM_IN_HORIZONTALLY || zoomType == ZoomType.RUBBERBAND_ZOOM) {
                addValuesFromDatasource(xyGraph.primaryXAxis.getRange().getLower(),
                        xyGraph.primaryXAxis.getRange().getUpper());
            }
        }

        @Override
        public void mousePressed(final MouseEvent me) {
        } // don't do anything on mousePressed event

        @Override
        public void mouseDoubleClicked(final MouseEvent me) {
        } // don't do anything on doubleclick event

        @Override
        public void mouseExited(final MouseEvent me) {
        } // don't do anything on doubleclick event

    }

    /**
     * Method will add to each trace param with plot datasource the plot point from db.
     */
    private void addValuesFromDatasource() {
        addValuesFromDatasource(0, 0);
    }

    @SuppressWarnings("unchecked")
    private void addValuesFromDatasource(double t1, double t2) {
        List<Trace> traceList = getTraceList();
        for (int i = 0; i < getWidgetModel().getTracesAmount(); i++) {
            String pv = "";
            try {
                cacheDuringLoad.put(Integer.valueOf(i), new ArrayList<VType>());
                Boolean pltDataSource = (Boolean) getWidgetModel()
                        .getProperty(ArchiveXYGraphModel.PROP_PLOT_DATA_SOURCE).getPropertyValue();

                List<String> archiveDataSource = null;
                if (pltDataSource) {
                    archiveDataSource = DataSourceUrl.getURLs();
                } else {
                    archiveDataSource = (List<String>) getWidgetModel()
                            .getProperty(ArchiveXYGraphModel.PROP_ARCHIVE_DATA_SOURCE).getPropertyValue();
                }
                String propID = ArchiveXYGraphModel.makeTracePropID(TraceProperty.YPV.propIDPre, i);
                pv = (String) getWidgetModel().getProperty(propID).getPropertyValue();

                Instant te = Instant.EPOCH;
                Instant ts = Instant.EPOCH;
                Integer tSp = 0;
                if (t1 == 0 && t2 == 0) {
                    tSp = (Integer) getWidgetModel().getProperty(ArchiveXYGraphModel.PROP_TIME_SPAN).getPropertyValue();
                    te = Instant.now();
                    ts = te.minusSeconds(tSp);
                } else {
                    tSp = (int) (t2 - t1);
                    te = Instant.ofEpochMilli((long) t2);
                    ts = Instant.ofEpochMilli((long) t1);
                }
                final Integer timeSpan = tSp;

                // if one required data source is missing we continue the loop
                if (archiveDataSource == null || archiveDataSource.size() <= 0 || pv == null || pv.length() <= 0
                        || timeSpan <= 0) {
                    Activator.getLogger().log(Level.INFO, "data source is missing");
                    continue;
                }

                // get back the pv on x
                boolean pvOnX = false;
                if (pv.length() <= 0) {
                    propID = ArchiveXYGraphModel.makeTracePropID(TraceProperty.XPV.propIDPre, i);
                    pv = (String) getWidgetModel().getProperty(propID).getPropertyValue();

                    pvOnX = (pv != null && pv.length() > 0);
                }

                // prepare the pv item for the job
                final Instant end = te;
                final Instant start = ts;

                // set timespan property
                final Trace trace = traceList.get(i);

                final CircularBufferDataProvider dataProvider = (CircularBufferDataProvider) trace.getDataProvider();
                final Boolean pvOnXBln = pvOnX;
                final Integer traceIndex = i;

                // launch the job
                XYArchiveFetchJob job = new XYArchiveFetchJob(pv, archiveDataSource, start, end,
                        new XYArchiveJobCompleteListener() {
                            @Override
                            public void complete(PVSamples samples) {
                                // call ui thread for the graph updating
                                UIBundlingThread.getInstance().addRunnable(getViewer().getControl().getDisplay(),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                updateGraph(samples, dataProvider, traceIndex, pvOnXBln, trace, start,
                                                        end, timeSpan);
                                            }
                                        });
                            }
                        });
                job.schedule();
            } catch (Exception e) {
                Activator.getLogger().log(Level.INFO, "Error while getting data from datasource for pv " + pv);
            }
        }
    }

    /**
     * update the graph with data from DB and loading cache
     *
     * @param pvSamples
     *            data from db to plot
     * @param dataProvider
     *            to set data in datapprovider
     * @param traceIndex
     *            index of the trace
     * @param pvOnXBln
     *            boolean to indicate if the value is on X or Y
     * @param trace
     *            the trace
     */
    private void updateGraph(PVSamples pvSamples, CircularBufferDataProvider dataProvider, Integer traceIndex,
            Boolean pvOnXBln, Trace trace, Instant start, Instant end, int timeSpan) {
        if (isActive()) {
            // clear the data
            dataProvider.clearTrace();
            // get from cache data load withou db
            List<VType> listFinal = new ArrayList<VType>();

            // add data from db
            int sampleCount = pvSamples.size();
            for (int cpt = 0; cpt < sampleCount; cpt++) {
                VType vtype = pvSamples.get(cpt).getVType();
                listFinal.add(vtype);
            }
            // add data from cache
            List<VType> cacheTypeLoadTrace = cacheDuringLoad.get(traceIndex);
            if (cacheTypeLoadTrace != null && cacheTypeLoadTrace.size() > 0) {
                sampleCount = cacheTypeLoadTrace.size();
                for (int cpt = 0; cpt < sampleCount; cpt++) {
                    VType vtype = cacheTypeLoadTrace.get(cpt);
                    listFinal.add(vtype);
                }
            }

            if (dataProvider.getUpdateMode() == UpdateMode.TRIGGER)
                isTriggerMode = true;
            // set the data on the graph
            for (VType vtype : listFinal) {
                if (pvOnXBln) {
                    setXValue(dataProvider, vtype);
                } else {
                    // hack to plot a data not update before timespan (value has not change since the timespan)
                    // it avoid to have a long axis x
                    long time = yValueTimeStampToLong(vtype);
                    long diffFromNow = (end.toEpochMilli() - time) / 1000;
                    if (diffFromNow > timeSpan) {
                        if (vtype instanceof ArchiveVType) {
                            try {
                                Instant startTS = TimestampHelper.fromMillisecs(start.toEpochMilli());
                                Field field = ArchiveVType.class.getDeclaredField("timestamp");
                                field.setAccessible(true);
                                field.set(vtype, startTS);
                            } catch (Exception e) {
                                Activator.getLogger().log(Level.WARNING, "archivevtype set timestamp error", e);
                            }
                        }
                    }
                    setYValue(trace, dataProvider, vtype);
                }
            }
            Activator.getLogger().log(Level.INFO,
                    "Trace " + traceIndex + " - " + listFinal.size() + " historical data retrieved");

            // clear the cache to not used it anymore
            cacheDuringLoad.replace(traceIndex, null);
        }
    }

    @Override
    protected void registerTracePropertyChangeHandlers() {
        super.registerTracePropertyChangeHandlers();
        // set prop handlers and init all the potential axes
        for (int i = 0; i < XYGraphModel.MAX_TRACES_AMOUNT; i++) {
            boolean concatenate = (Boolean) getWidgetModel()
                    .getProperty(XYGraphModel.makeTracePropID(TraceProperty.CONCATENATE_DATA.propIDPre, i))
                    .getPropertyValue();
            String xPVPropID = XYGraphModel.makeTracePropID(TraceProperty.XPV.propIDPre, i);
            String yPVPropID = XYGraphModel.makeTracePropID(TraceProperty.YPV.propIDPre, i);
            for (TraceProperty traceProperty : TraceProperty.values()) {
                final String propID = XYGraphModel.makeTracePropID(traceProperty.propIDPre, i);
                final IWidgetPropertyChangeHandler handler = new TracePropertyChangeHandler(i, traceProperty, xPVPropID,
                        yPVPropID);

                if (concatenate) {
                    // cannot use setPropertyChangeHandler because the PV value has to be buffered
                    // which means that it cannot be ignored.
                    getWidgetModel().getProperty(propID).addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(final PropertyChangeEvent evt) {
                            UIBundlingThread.getInstance().addRunnable(getViewer().getControl().getDisplay(),
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isActive())
                                                handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
                                        }
                                    });
                        }
                    });
                } else
                    setPropertyChangeHandler(propID, handler);
            }
        }
    }

    private void setXValue(CircularBufferDataProvider dataProvider, VType value) {
        if (VTypeHelper.getSize(value) > 1) {
            dataProvider.setCurrentXDataArray(VTypeHelper.getDoubleArray(value));
            if (isTriggerMode) {
                dataProvider.addDataArray();
            }
        } else {
            dataProvider.setCurrentXData(VTypeHelper.getDouble(value));
            if (isTriggerMode) {
                long time = yValueTimeStampToLong(value);
                dataProvider.addDataPoint(time);
            }
        }
    }

    private void setYValue(Trace trace, CircularBufferDataProvider dataProvider, VType y_value) {
        long time = yValueTimeStampToLong(y_value);
        if (VTypeHelper.getSize(y_value) == 1 && trace.getXAxis().isDateEnabled() && dataProvider.isChronological()) {
            // verification that the last add is before the next that will be added
            int size = dataProvider.getSize() - 1;
            if (size > 0) {
                ISample sample = dataProvider.getSample(size);
                if (sample.getXValue() > time) {
                    return;
                }
            }
            dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value), time);
            if (isTriggerMode) {
                dataProvider.addDataPoint(time);
            }
        } else {
            if (VTypeHelper.getSize(y_value) > 1) {
                dataProvider.setCurrentYDataArray(VTypeHelper.getDoubleArray(y_value));
                if (isTriggerMode) {
                    dataProvider.addDataArray();
                }
            } else {
                dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value));
                if (isTriggerMode) {
                    dataProvider.addDataPoint(time);
                }
            }
        }
    }

    private long yValueTimeStampToLong(VType y_value) {
        if (y_value == null) {
            return Long.MAX_VALUE;
        }
        Instant timestamp = VTypeHelper.getTimestamp(y_value);
        return timestamp.getEpochSecond() * 1000 + timestamp.getNano() / 1000000;
    }

    class TracePropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private int traceIndex;
        private TraceProperty traceProperty;
        private String xPVPropID;
        private String yPVPropID;

        public TracePropertyChangeHandler(int traceIndex, TraceProperty traceProperty, String xPVPropID,
                String yPVPropID) {
            this.traceIndex = traceIndex;
            this.traceProperty = traceProperty;
            this.xPVPropID = xPVPropID;
            this.yPVPropID = yPVPropID;
        }

        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
            List<Trace> traceList = getTraceList();
            Trace trace = traceList.get(traceIndex);

            List<VType> samples = cacheDuringLoad.get(Integer.valueOf(traceIndex));
            if (samples != null && newValue instanceof VType) {
                samples.add((VType) newValue);
            }

            // loop on trace to draw curve with 2 points with the begin point and end point
            ToolbarArmedXYGraph figure = ((ToolbarArmedXYGraph) ArchiveXYGraphEditPart.this.getFigure());
            long timeRangeUpper = (long) figure.getXYGraph().getXAxisList().get(0).getRange().getUpper();
            // FIXME Is this correct? The if clause seems a bit odd...
            if (DEFAULT_MAX.compareTo(timeRangeUpper) != 0) {
                for (Trace traceTmp : traceList) {
                    int countPoint = traceTmp.getDataProvider().getSize();
                    if (countPoint == 2) {
                        if (figure.getXYGraph().getXAxisList().size() > 0) {
                            try {
                                Field field = Sample.class.getDeclaredField("xValue");
                                field.setAccessible(true);
                                field.set(traceTmp.getDataProvider().getSample(1), timeRangeUpper);
                            } catch (Exception e) {
                                Activator.getLogger().log(Level.WARNING, "archivevtype set timestamp error", e);
                            }
                        }
                    }
                }
            }

            setTraceProperty(trace, traceProperty, newValue, xPVPropID, yPVPropID);
            return false;
        }
    }
}
