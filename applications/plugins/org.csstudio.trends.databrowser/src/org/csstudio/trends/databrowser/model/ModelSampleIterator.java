package org.csstudio.trends.databrowser.model;

import org.csstudio.archive.crawl.ValueIterator;
import org.csstudio.platform.data.IValue;

/** Facade that turns ModelSamples into a Sample Iterator.
 *  <p>
 *  Access to the ModelSamples must be synchronized.
 *  But we don't want to lock the samples for the whole export process.
 *  We assume that the samples will only be extended with new 'live' samples
 *  during export, so we access them with minimal synchronization,
 *  only locking within the hasNext() and next() calls, not for the whole
 *  iteration, hoping that the worst that can happen: You miss a few of the
 *  very latest samples.
 *  <p>
 *  This approach will fail if an iteration runs while new archived data is
 *  added!
 *  
 *  @author Kay Kasemir
 */
public class ModelSampleIterator implements ValueIterator
{
    final private IModelSamples samples;
    final private int end_index;
    private int i;
    
    /** Construct iterator over all the given samples.
     *  @param samples
     */
    public ModelSampleIterator(final IModelSamples samples)
    {
        this.samples = samples;
        i = 0;
        synchronized (samples)
        {
            this.end_index = samples.size();
        }
    }
    
    /** Construct iterator for subrange of samples.
     *  @param samples
     *  @param start_index
     *  @param end_index
     */
    public ModelSampleIterator(final IModelSamples samples,
                    final int start_index, final int end_index)
    {
        this.samples = samples;
        i = start_index;
        this.end_index = end_index;
    }

    /** {@inheritDoc} */
    public boolean hasNext()
    {
        return i < end_index;
    }

    /** @return Next sample.
     *  In the hopefully unlikely case that the ModelSample array
     *  got smaller, we will get <code>null</code>.
     */
    public IValue next()
    {
        IValue sample;
        synchronized (samples)
        {
            if (i < samples.size())
                sample = samples.get(i++).getSample();
            else
                sample = null;
        }
        return sample;
    }

    /** @throws UnsupportedOperationException */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}