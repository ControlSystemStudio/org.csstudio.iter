package org.csstudio.iter.widgets.archive.xygraph.databrowser2;

import java.time.Instant;

import org.csstudio.trends.databrowser2.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJobListener;
import org.csstudio.trends.databrowser2.model.PVItem;

public class XYArchiveFetchJob extends ArchiveFetchJob {

	public XYArchiveFetchJob(PVItem item, Instant start, Instant end,
			ArchiveFetchJobListener listener) {
		super(item, start, end, listener);

		this.failedThrowExceptionGetData = false;
		this.displayUnknowChannelException = false;

//		ConnectionCache.clean();
	}
}
