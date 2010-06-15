package org.csstudio.apputil.formula.node;

import org.csstudio.apputil.formula.Node;

/** One computational node.
 *  @author Kay Kasemir
 */
public class MinNode implements Node
{
    private final Node args[];
    
    public MinNode(final Node args[])
    {
        this.args = args;
    }
    
    public double eval()
    {
        double result = 0.0;
        for (int i = 0; i < args.length; i++)
        {
            final double v = args[i].eval();
            if (i==0  ||  v < result)
                result = v;
        }
        return result;
    }
    
    /** {@inheritDoc} */
    public boolean hasSubnode(final Node node)
    {
        for (Node arg : args)
            if (arg == node  ||  arg.hasSubnode(node))
                return true;
        return false;
    }

    /** {@inheritDoc} */
    public boolean hasSubnode(final String name)
    {
        for (Node arg : args)
            if (arg.hasSubnode(name))
                return true;
        return false;
    }
    
    @SuppressWarnings("nls")
    @Override
    public String toString()
    {
        final StringBuffer b = new StringBuffer("min(");
        for (int i = 0; i < args.length; i++)
        {
            if (i>0)
                b.append(", ");
            b.append(args[i].toString());
        }
        b.append(")");
        return b.toString();
    }
}