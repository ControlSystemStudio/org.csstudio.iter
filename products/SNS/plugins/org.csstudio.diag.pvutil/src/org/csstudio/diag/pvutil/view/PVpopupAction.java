package org.csstudio.diag.pvutil.view;

import org.csstudio.platform.model.IProcessVariable;
import org.csstudio.platform.ui.internal.dataexchange.ProcessVariablePopupAction;

/** Handle activation of Probe from the object contrib. context menu.
 *  @author Kay Kasemir
 *  @author Helge Rickens
 */
public class PVpopupAction extends ProcessVariablePopupAction
{
    /** @see org.csstudio.data.exchange.ProcessVariablePopupAction#handlePVs(]) */
    @Override
    public void handlePVs(IProcessVariable[] pv_names)
    {
        if (pv_names.length < 1)
            return;
        PVUtilView.activateWithPV(pv_names[0]);
    }
}