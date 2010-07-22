package org.csstudio.trends.databrowser.model;

import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.TimestampFactory;
import org.csstudio.swt.chart.TraceType;
import org.junit.Test;

/** (Headless) JUnit Plug-in Test for ModelItem
 *  <p>
 *  Requires test database or 'excas' to run.
 *
 *  @author Kay Kasemir
 *
 *  FIXME (kasemir) : commented sysos (showstopper for org.csstudio.testsuite) - use assertions anyway
 */
@SuppressWarnings("nls")
public class FormulaModelItemTest
{
    @Test
    public void testModelItemScan() throws Exception
    {
        final Model model = new Model();
        final PVModelItem fred = new PVModelItem(model, "fred", null,
                        1024, 0, 0, 0, true, true, false, 0, 0, 0, 0,
                        TraceType.Lines, false,
                        IPVModelItem.RequestType.OPTIMIZED);
        final PVModelItem janet = new PVModelItem(model, "janet", null,
                        1024, 0, 0, 0, true, true, false, 0, 0, 0, 0,
                        TraceType.Lines, false,
                        IPVModelItem.RequestType.OPTIMIZED);
        fred.start();
        janet.start();
        final int num = 20;
        // 'Scan' the item once per second
        for (int i = 0; i < num; ++i)
        {
            Thread.sleep(1000);
            //System.out.format("scan %3d / %s\n", i+1, num);
            final ITimestamp now = TimestampFactory.now();
            fred.addCurrentValueToSamples(now);
            janet.addCurrentValueToSamples(now);
            if (fred.getSamples().size() >= 5) {
                break;
            }
        }
        janet.stop();
        fred.stop();

        IModelSamples samples = fred.getSamples();
        final int N = samples.size();
        if (N < 5) {
            throw new Exception("Only " + N + " values?");
        }

        //System.out.println("Original Samples for fred:");
        dumpSamples(samples);
        //System.out.println("Original Samples for janet:");
        dumpSamples(janet.getSamples());

        //System.out.println("Formula:");
        final FormulaModelItem formula = new FormulaModelItem(model, "calc", null,
                        0, 0, 0, true, true, false, 0, 0, 0, 0,
                        TraceType.Lines, false);
        final FormulaInput inputs[] = new FormulaInput[]
        {
            new FormulaInput(fred),
            new FormulaInput(janet),
        };
        formula.setFormula("1000*fred + janet", inputs);
        formula.compute();
        samples = formula.getSamples();
        dumpSamples(samples);
    }

    private void dumpSamples(final IModelSamples samples)
    {
        for (int i=0; i<samples.size(); ++i)
        {
            final ModelSample sample = samples.get(i);
            //System.out.println(sample.toString());
        }
    }
}