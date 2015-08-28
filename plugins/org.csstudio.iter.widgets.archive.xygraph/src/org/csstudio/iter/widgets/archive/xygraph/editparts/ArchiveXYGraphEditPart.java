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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.iter.widgets.archive.xygraph.Activator;
import org.csstudio.iter.widgets.archive.xygraph.databrowser2.XYArchiveFetchJob;
import org.csstudio.iter.widgets.archive.xygraph.model.ArchiveXYGraphModel;
import org.csstudio.iter.widgets.archive.xygraph.util.DataSourceUrl;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.editparts.XYGraphEditPart;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.csstudio.opibuilder.widgets.model.XYGraphModel.TraceProperty;
import org.csstudio.simplepv.VTypeHelper;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJobListener;
import org.csstudio.trends.databrowser2.model.ArchiveDataSource;
import org.csstudio.trends.databrowser2.model.PVItem;
import org.csstudio.trends.databrowser2.model.PVSamples;
import org.csstudio.trends.databrowser2.model.RequestType;
import org.eclipse.draw2d.IFigure;
import org.epics.util.time.Timestamp;
import org.epics.vtype.VType;

/**The XYGraph editpart
 * @author lamberm (Sopra)
 *
 */
public class ArchiveXYGraphEditPart extends XYGraphEditPart {
	
	private Map<Integer, List<VType>>cacheDuringLoad = new HashMap<>();

    @Override
    public ArchiveXYGraphModel getWidgetModel() {
        return (ArchiveXYGraphModel) getModel();
    }

    @Override
    protected IFigure doCreateFigure() {
    	IFigure xyGraphFigure = super.doCreateFigure();

        //add values from datasource if the execution is in run mode
    	if (getExecutionMode() == ExecutionMode.RUN_MODE) {
        	addValuesFromDatasource();
        }
        	
        return xyGraphFigure;
    }

    /**
     * Method will add to each trace param with plot datasource the plot point from db.
     */
    private void addValuesFromDatasource() {
    	for (int i = 0; i < getWidgetModel().getTracesAmount(); i++) {
    		String pv = "";
			try {
				Boolean pltDataSource = (Boolean) getWidgetModel().getProperty(ArchiveXYGraphModel.PROP_PLOT_DATA_SOURCE).getPropertyValue();

				List < String > archiveDataSource = null;
				if (pltDataSource) {
					archiveDataSource = DataSourceUrl.getURLs();
				} else {
	                archiveDataSource = (List<String>) getWidgetModel().getProperty(ArchiveXYGraphModel.PROP_ARCHIVE_DATA_SOURCE).getPropertyValue();
				}
                String propID = ArchiveXYGraphModel.makeTracePropID(TraceProperty.YPV.propIDPre, i);
                pv = (String) getWidgetModel().getProperty(propID).getPropertyValue();
                
                Integer timeSpan = (Integer) getWidgetModel().getProperty(ArchiveXYGraphModel.PROP_TIME_SPAN).getPropertyValue();

                //if one required data source is missing  
                if (archiveDataSource == null || archiveDataSource.size() <= 0
                		|| pv == null || pv.length() <= 0 || timeSpan <= 0) {
                	Activator.getLogger().log(Level.INFO, "data source is missing");
                	continue;
                }
                
                //get back the pv on x 
                boolean pvOnX = false;
                if (pv == null || pv.length() <= 0) {
                	propID = ArchiveXYGraphModel.makeTracePropID(TraceProperty.XPV.propIDPre, i);
                	pv = (String) getWidgetModel().getProperty(propID).getPropertyValue();
                	
                	pvOnX = (pv != null && pv.length() > 0);
                }

                //prepare the pv item for the job  
				Instant end = Instant.now();
				Instant start = end.minusSeconds(timeSpan);
				PVItem pvItem = new PVItem(pv, 0);
				pvItem.setRequestType(RequestType.RAW);

				//add datasource
				int j = 0;
				for (String urlTmp : archiveDataSource) {
					pvItem.addArchiveDataSource(new ArchiveDataSource(urlTmp, j, ""));
				}

				final Trace trace = traceList.get(i);
				final CircularBufferDataProvider dataProvider = (CircularBufferDataProvider) trace.getDataProvider();
				final Boolean pvOnXBln = pvOnX;
				final Integer traceIndex = i;
				
				//launch the job
				XYArchiveFetchJob job = new XYArchiveFetchJob(pvItem, start, end, new ArchiveFetchJobListener() {
					@Override
					public void archiveFetchFailed(ArchiveFetchJob job,
							ArchiveDataSource archive, Exception error) {
						Activator.getLogger().log(Level.WARNING, "Archive fetch failed for pv '" + pvItem.getName() + "' and url '" + archive.getUrl() + "'", error);
					}
					@Override
					public void fetchCompleted(ArchiveFetchJob job) {
						PVSamples pvSamples = job.getPVItem().getSamples();
						if (pvSamples.size() <= 0) {
							return;
						}
						//use UI thread to display (avoid to use a synchronize)
						UIBundlingThread.getInstance().addRunnable(
                                getViewer().getControl().getDisplay(), new Runnable() {
	                                public void run() {
	                                    if(isActive()) {
	                                    	//clear the data
	                                    	dataProvider.clearTrace();
	                                    	//get from cache data load withou db
	                                    	List <VType> listFinal = new ArrayList<VType>();

	                                    	//add data from db
	                                    	int sampleCount = pvSamples.size();
	                                    	for (int cpt = 0; cpt < sampleCount; cpt++) {
	                                    		listFinal.add(pvSamples.get(cpt).getVType());
	                                    	}
	                                    	
	                                    	//add data from cache
	                                    	List <VType> cacheTypeLoadTrace = cacheDuringLoad.get(traceIndex);
	                                    	if (cacheTypeLoadTrace != null) {
	                                    		listFinal.addAll(cacheTypeLoadTrace);
	                                    	}
	                                    	
	                                    	for (VType vtype : listFinal) {
	                                    		if (pvOnXBln) {
	                                    			setXValue(dataProvider, vtype);
	                                    		} else {
	                                    			setYValue(trace, dataProvider, vtype);
	                                    		}
											}
	                                    	
	                                    	cacheDuringLoad.clear();
	                                    }
	                                }
                                });
						Activator.getLogger().log(Level.INFO, "Completed for " + job.getPVItem().getName() + " - size : " + pvSamples.size());
					}
				});
				job.schedule();
			} catch (Exception e) {
				Activator.getLogger().log(Level.INFO, "Error while getting data from datasource for pv " + pv);
			}
		}
    }

    @Override
    protected void registerTracePropertyChangeHandlers() {
    	super.registerTracePropertyChangeHandlers();
        //set prop handlers and init all the potential axes
        for(int i=0; i<XYGraphModel.MAX_TRACES_AMOUNT; i++){
            boolean concatenate = (Boolean) getWidgetModel().getProperty(
                    XYGraphModel.makeTracePropID(TraceProperty.CONCATENATE_DATA.propIDPre, i)).getPropertyValue();
            String xPVPropID = XYGraphModel.makeTracePropID(
                    TraceProperty.XPV.propIDPre, i);
            String yPVPropID = XYGraphModel.makeTracePropID(
                    TraceProperty.YPV.propIDPre, i);
            for(TraceProperty traceProperty : TraceProperty.values()){
                final String propID = XYGraphModel.makeTracePropID(
                    traceProperty.propIDPre, i);
                final IWidgetPropertyChangeHandler handler = new TracePropertyChangeHandler(i, traceProperty, xPVPropID, yPVPropID);

                if(concatenate){
                    //cannot use setPropertyChangeHandler because the PV value has to be buffered
                    //which means that it cannot be ignored.
                    getWidgetModel().getProperty(propID).addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(final PropertyChangeEvent evt) {
                            UIBundlingThread.getInstance().addRunnable(
                                    getViewer().getControl().getDisplay(), new Runnable() {
                                public void run() {
                                    if(isActive())
                                        handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
                                    }
                                });
                        }
                    });
                }else
                    setPropertyChangeHandler(propID, handler);
            }
        }
    }

    @Override
    protected void setYValue(Trace trace,
            CircularBufferDataProvider dataProvider, VType y_value) {
        if(VTypeHelper.getSize(y_value) == 1 && trace.getXAxis().isDateEnabled() && dataProvider.isChronological()) {
        	long time = yValueTimeStampToLong(y_value);
        	//verification that the last add is before the next that will be added
        	int size = dataProvider.getSize() - 1;
        	if (size > 0) {
	        	ISample sample = dataProvider.getSample(size);
	            if (sample.getXValue() > time) {
	            	return;
	            }
            }
            dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value), time);
        }else{
            if(VTypeHelper.getSize(y_value) > 1){
                dataProvider.setCurrentYDataArray(VTypeHelper.getDoubleArray(y_value));
            }else {
                dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value));
            }
        }
    }
    
    
    private long yValueTimeStampToLong(VType y_value) {
    	if (y_value == null) {
    		return Long.MAX_VALUE;
    	}
    	Timestamp timestamp = VTypeHelper.getTimestamp(y_value);
        return timestamp.getSec() * 1000 + timestamp.getNanoSec()/1000000;
    }

    class TracePropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private int traceIndex;
        private TraceProperty traceProperty;
        private String xPVPropID;
        private String yPVPropID;
        public TracePropertyChangeHandler(int traceIndex, TraceProperty traceProperty, String xPVPropID, String yPVPropID) {
            this.traceIndex = traceIndex;
            this.traceProperty = traceProperty;
            this.xPVPropID = xPVPropID;
            this.yPVPropID = yPVPropID;
        }
        public boolean handleChange(Object oldValue, Object newValue,
                IFigure refreshableFigure) {
            Trace trace = traceList.get(traceIndex);

            List <VType> samples = cacheDuringLoad.get(new Integer(traceIndex));
            if (samples != null) {
            	samples.add((VType) newValue);
            }

            setTraceProperty(trace, traceProperty, newValue, xPVPropID, yPVPropID);
            return false;
        }
    }
}
