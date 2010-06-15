package org.csstudio.trends.databrowser.model;

import org.csstudio.platform.data.IMinMaxDoubleValue;
import org.csstudio.platform.data.INumericMetaData;
import org.csstudio.platform.data.ISeverity;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;
import org.csstudio.platform.data.TimestampFactory;
import org.csstudio.platform.data.ValueFactory;
import org.csstudio.platform.data.ValueUtil;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.trends.databrowser.Messages;
import org.eclipse.osgi.util.NLS;

/** Data Sample from control system (IValue)
 *  with interface for XYGraph (ISample)
 *  @author Kay Kasemir
 */
public class PlotSample implements ISample
{
    final public static INumericMetaData dummy_meta = ValueFactory.createNumericMetaData(0, 0, 0, 0, 0, 0, 1, "a.u."); //$NON-NLS-1$
    final public static ISeverity ok_severity = ValueFactory.createOKSeverity();
    
    /** Value contained in this sample */
    final private IValue value;

    /** Source of the data */
    final private String source;
    
    /** Info string.
     *  @see #getInfo()
     */
    private String info;
    

    /** Initialize with valid control system value
     *  @param source Info about the source of this sample
     *  @param value
     */
    public PlotSample(final String source, final IValue value)
    {
        this.value = value;
        this.source = source;
        info = null;
    }

    /** Initialize with (error) info, creating a non-plottable sample 'now'
     *  @param info Text used for info as well as error message
     */
    public PlotSample(final String source, final String info)
    {
        this(source, ValueFactory.createDoubleValue(TimestampFactory.now(),
                ValueFactory.createInvalidSeverity(), info, dummy_meta,
                IValue.Quality.Original, new double[] { Double.NaN }));
        this.info = info;
    }

    /** Package-level constructor, only used in unit tests */
    @SuppressWarnings("nls")
    PlotSample(final double x, final double y)
    {
        this("Test",
             ValueFactory.createDoubleValue(TimestampFactory.fromDouble(x),
               ok_severity, ok_severity.toString(), dummy_meta,
               IValue.Quality.Original, new double[] { y }));
    }
    
    /** @return Source of the data */
    public String getSource()
    {
        return source;
    }

    /** @return Control system value */
    public IValue getValue()
    {
        return value;
    }

    /** @return Control system time stamp */
    public ITimestamp getTime()
    {
        return value.getTime();
    }
    
    /** Since the 'X' axis is used as a 'Time' axis, this
     *  returns the time stamp of the control system sample.
     *  The XYGraph expects it to be milliseconds(!) since 1970.
     *  @return Time as milliseconds since 1970
     */
    public double getXValue()
    {
        return value.getTime().toDouble()*1000.0;
    }

    /** {@inheritDoc} */
    public double getYValue()
    {
        if (value.getSeverity().hasValue())
            return ValueUtil.getDouble(value);
        // No numeric value. Plot shows NaN as marker.
        return Double.NaN;
    }

    /** Get sample's info text.
     *  If not set on construction, the value's text is used.
     *  @return Sample's info text. */
    public String getInfo()
    {
        if (info == null)
            return toString();
        return info;
    }

    /** {@inheritDoc} */
    public double getXMinusError()
    {
        return 0;
    }

    /** {@inheritDoc} */
    public double getXPlusError()
    {
        return 0;
    }

    /** {@inheritDoc} */
    public double getYMinusError()
    {
        if (!(value instanceof IMinMaxDoubleValue))
            return 0;
        final IMinMaxDoubleValue minmax = (IMinMaxDoubleValue)value;
        return minmax.getValue() - minmax.getMinimum();
    }

    /** {@inheritDoc} */
    public double getYPlusError()
    {
        if (!(value instanceof IMinMaxDoubleValue))
            return 0;
        final IMinMaxDoubleValue minmax = (IMinMaxDoubleValue)value;
        return minmax.getMaximum() - minmax.getValue();
    }

    @Override
    public String toString()
    {
        return NLS.bind(Messages.PlotSampleFmt, new Object[] { value, source, value.getQuality().toString() });
    }
}