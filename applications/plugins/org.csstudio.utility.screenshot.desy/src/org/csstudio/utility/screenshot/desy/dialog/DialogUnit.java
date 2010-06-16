
/* 
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.csstudio.utility.screenshot.desy.dialog;

import org.eclipse.swt.graphics.Rectangle;

public final class DialogUnit
{
    private final static int baseUnitX  = 6;
    private final static int baseUnitY  = 13;
    private final static int CORRECTION = 18;

    private DialogUnit() { }
    
    public final static int mapUnitX(int unitX)
    {
        return (unitX * baseUnitX) / 4;
    }

    public final static int mapUnitY(int unitY)
    {
        return (unitY * baseUnitY) / 8;
    }
    
    public final static int mapUnitYWithCorrection(int unitY)
    {
        return ((unitY * baseUnitY) / 8) - CORRECTION;
    }
    
    public final static Rectangle mapUnits(Rectangle r)
    {
        Rectangle rect = new Rectangle(DialogUnit.mapUnitX(r.x), DialogUnit.mapUnitYWithCorrection(r.y), DialogUnit.mapUnitX(r.width), DialogUnit.mapUnitY(r.height));
        
        return rect;
    }
    
    public final static Rectangle mapUnitsWithoutCorrection(Rectangle r)
    {
        Rectangle rect = new Rectangle(DialogUnit.mapUnitX(r.x), DialogUnit.mapUnitY(r.y), DialogUnit.mapUnitX(r.width), DialogUnit.mapUnitY(r.height));
        
        return rect;
    }
}