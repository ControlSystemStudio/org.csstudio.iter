/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.iter.widgets.archive.xygraph.databrowser2;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.archive.reader.UnknownChannelException;
import org.csstudio.archive.reader.rdb.ConnectionCache;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJobListener;
import org.csstudio.trends.databrowser2.model.ArchiveDataSource;
import org.csstudio.trends.databrowser2.model.PVItem;
import org.csstudio.trends.databrowser2.model.PVSamples;
import org.csstudio.trends.databrowser2.model.RequestType;

/**
 * archive job for RDB and ArchiveXYGraph
 *
 * @author lamberm (sopra)
 *
 */
public class XYArchiveFetchJob extends ArchiveFetchJob {

    final private static Logger LOGGER = Logger.getLogger(XYArchiveFetchJob.class.getName());

    /**
     * Main constructor
     *
     * @param pv name of the pv
     * @param archiveDataSource list of URL (RDB)
     * @param start beginning of the search
     * @param end end of the search
     * @param listener listener call when search completed
     * @throws Exception
     */
    public XYArchiveFetchJob(String pv, List<String> archiveDataSource, Instant start, Instant end,
        XYArchiveJobCompleteListener listener) throws Exception {
        super(getPVItem(pv, archiveDataSource), start, end, getArchiveFetchJobListener(pv, listener), true);
        ConnectionCache.clean();
    }

    /**
     * Create a {@link PVItem} with the name and url of the archive RDB
     *
     * @param pv name of the PV
     * @param archiveDataSource list of URL for the RDB
     * @return {@link PVItem}
     * @throws Exception
     */
    private static PVItem getPVItem(String pv, List<String> archiveDataSource) throws Exception {
        PVItem pvItem = new PVItem(pv, 0);
        pvItem.setRequestType(RequestType.RAW);

        // add datasource
        int j = 0;
        for (String urlTmp : archiveDataSource) {
            pvItem.addArchiveDataSource(new ArchiveDataSource(urlTmp, j, ""));
        }
        return pvItem;
    }

    /**
     * method provide an object for the listener {@link ArchiveFetchJobListener}
     *
     * @param pvName name of the pv
     * @param completeListener listener to call when finish
     * @return the listener
     */
    private static ArchiveFetchJobListener getArchiveFetchJobListener(String pvName,
        XYArchiveJobCompleteListener completeListener) {
        return new ArchiveFetchJobListener() {
            @Override
            public void archiveFetchFailed(ArchiveFetchJob job, ArchiveDataSource archive, Exception error) {
                if (!(error instanceof UnknownChannelException)) {
                    LOGGER.log(Level.WARNING,
                        "Archive fetch failed for pv '" + pvName + "' and url '" + archive.getUrl() + "'", error);
                }
            }

            @Override
            public void fetchCompleted(ArchiveFetchJob job) {
                PVSamples pvSamples = job.getPVItem().getSamples();
                LOGGER.log(Level.FINE, "Completed for " + job.getPVItem().getName() + " - size : " + pvSamples.size());
                if (pvSamples.size() <= 0 || completeListener == null) {
                    return;
                }
                completeListener.complete(pvSamples);
            }
        };
    }
}
