/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser.model;

import org.csstudio.platform.data.IValue;

/** One input to the formula: Model item that provides data, Variable name
 *  for use in the formula
 *  @author Kay Kasemir
 */
public class FormulaInput
{
    /** The model item used as input. */
    final private ModelItem item;
    
    /** The variable name for this input. */
    final private String variable_name;

    /** Index of the sample that next() will return or -1 when 'done' */
    private int index = -1;

    /** Constructor
     *  @param item ModelItem that provides the input data
     *  @param variable_name Name used in formula for this input
     */
    public FormulaInput(final ModelItem item, final String variable_name)
    {
        this.item = item;
        this.variable_name = variable_name;
    }
    
    /** @return ModelItem that this input reads */
    public ModelItem getItem()
    {
        return item;
    }

    /** @return Name of the variable for this input */
    public String getVariableName()
    {
        return variable_name;
    }

    /** Rest the sample iterator to the first sample
     *  @see #next()
     *  @return First sample or <code>null</code>
     */
    public IValue first()
    {
        if (item.getSamples().getSize() > 0)
            index = 0;
        else
            index = -1;
        return next();
    }

    /** Iterate over the samples of the input's ModelItem
     *  @return Next value or <code>null</code>
     */
    public IValue next()
    {
        if (index < 0)
            return null;
        final IValue result;
        final PlotSamples samples = item.getSamples();
        synchronized (samples)
        {
            if (index < samples.getSize())
                result = samples.getSample(index++).getValue();
            else
            {
                result = null;
                index = -1;
            }
        }
        return result;
    }

    /** Check for new samples.
     *  Note that this will not reset the new-sample flag,
     *  so its use has to be coordinated with the overall mechanism
     *  that checks for the need to redraw.
     *  @return <code>true</code> if ModelItem has new samples
     */
    public boolean hasNewSamples()
    {
        return item.getSamples().hasNewSamples();
    }
    
    /** @return Debug text */
    @SuppressWarnings("nls")
    @Override
    public String toString()
    {
        return "FormulaInput '" + variable_name + "': " + item.getName();
    }
}