package org.csstudio.iter.widgets.archive.databrowser2;

import org.csstudio.trends.databrowser2.model.PVSamples;

/**
 * interface of the listener for {@link XYArchiveJobCompleteListener}
 * @author lamberm
 *
 */
public interface XYArchiveJobCompleteListener {
	/**
	 * call when complete
	 * @param samples list of sample
	 */
	void complete(PVSamples samples);
}
