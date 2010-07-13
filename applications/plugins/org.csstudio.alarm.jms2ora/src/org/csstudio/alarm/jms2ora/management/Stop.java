
/* 
 * Copyright (c) 2009 Stiftung Deutsches Elektronen-Synchrotron, 
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

package org.csstudio.alarm.jms2ora.management;

import org.csstudio.alarm.jms2ora.Jms2OraPlugin;
import org.csstudio.alarm.jms2ora.Stoppable;
import org.csstudio.alarm.jms2ora.preferences.PreferenceConstants;
import org.csstudio.platform.management.CommandParameters;
import org.csstudio.platform.management.CommandResult;
import org.csstudio.platform.management.IManagementCommand;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/**
 * @author Markus Moeller
 *
 */
public class Stop implements IManagementCommand
{
    private static Stoppable objectToBeStopped = null;

    /* (non-Javadoc)
     * @see org.csstudio.platform.management.IManagementCommand#execute(org.csstudio.platform.management.CommandParameters)
     */
    public CommandResult execute(CommandParameters parameters)
    {
        IPreferencesService prefs = Platform.getPreferencesService();
        String xmppShutdownPassword = prefs.getString(Jms2OraPlugin.PLUGIN_ID, PreferenceConstants.XMPP_SHUTDOWN_PASSWORD, "", null);

        String password = (String)parameters.get("Password");
        if(password == null)
        {
            return CommandResult.createFailureResult("\nParameter not available.");
        }
        
        try
        {
            if(password.compareTo(xmppShutdownPassword) != 0)
            {
                return CommandResult.createFailureResult("\nInvalid password");
            }
        }
        catch(Exception e)
        {
            return CommandResult.createFailureResult(e.getMessage());
        }

        objectToBeStopped.stopWorking();
        
        return CommandResult.createMessageResult("\nStopping Jms2Ora...");
    }

    public static void staticInject(Stoppable obj)
    {
        objectToBeStopped = obj;
    }
}