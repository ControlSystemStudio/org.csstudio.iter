/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron, Member of the Helmholtz
 * Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. WITHOUT WARRANTY OF ANY
 * KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE IN ANY RESPECT, THE USER ASSUMES
 * THE COST OF ANY NECESSARY SERVICING, REPAIR OR CORRECTION. THIS DISCLAIMER OF WARRANTY
 * CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER
 * EXCEPT UNDER THIS DISCLAIMER. DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS. THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION,
 * MODIFICATION, USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY AT
 * HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.sds.behavior.desy;




import org.csstudio.sds.components.model.KnobModel;
import org.epics.css.dal.context.ConnectionState;
import org.epics.css.dal.simple.AnyDataChannel;

/**
 *
 * Default DESY-Behavior for the {@link KnobModel} widget with Connection state and Alarms.
 *
 * @author hrickens
 * @author $Author: hrickens $
 * @version $Revision: 1.4 $
 * @since 20.04.2010
 */
public class KnobAlarmBehavior extends MarkedWidgetDesyAlarmBehavior<KnobModel> {

    private String _defFillColor;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize( final KnobModel widget) {
        super.doInitialize(widget);
        _defFillColor = widget.getColor(KnobModel.PROP_KNOB_COLOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doProcessConnectionStateChange( final KnobModel widget, final AnyDataChannel anyDataChannel) {
        super.doProcessConnectionStateChange(widget, anyDataChannel);
        ConnectionState connectionState = anyDataChannel.getProperty().getConnectionState();
        String fillColor = isConnected(anyDataChannel)?_defFillColor  : determineBackgroundColor(connectionState);
        widget.setPropertyValue(KnobModel.PROP_KNOB_COLOR, fillColor);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] doGetSettablePropertyIds() {
        return new String[] { KnobModel.PROP_VALUE };
    }
}
