
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

package org.csstudio.ams.systemmonitor.check;

import java.util.Hashtable;
import javax.jms.MapMessage;
import javax.jms.Message;
import org.csstudio.ams.systemmonitor.AmsSystemMonitorActivator;
import org.csstudio.ams.systemmonitor.AmsSystemMonitorException;
import org.csstudio.ams.systemmonitor.internal.PreferenceKeys;
import org.csstudio.ams.systemmonitor.jms.MessageHelper;
import org.csstudio.ams.systemmonitor.status.MonitorStatusEntry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

public class AmsSystemCheck extends ACheckProcessor
{
    public AmsSystemCheck(String senderClientId, String receiverClientId, String subscriberName) throws AmsSystemMonitorException
    {
        super(senderClientId, receiverClientId, subscriberName);
    }

    @Override
    public void doCheck(MonitorStatusEntry currentStatusEntry) throws AmsSystemMonitorException
    {
        Hashtable<String, String> messageContent = null;
        Message message = null;
        MapMessage mapMessage = null;
        Object waitObject = null;
        long endTime = 0;
        long currentTime = 0;
        boolean success = false;

        logger.info("Starting check of AMS System.");
        
        IPreferencesService pref = Platform.getPreferencesService();
        long amsWaitTime = pref.getLong(AmsSystemMonitorActivator.PLUGIN_ID, PreferenceKeys.P_AMS_WAIT_TIME, -1, null);
        if(amsWaitTime == -1)
        {
            logger.warn("The waiting time for the AMS system check is not valid. Using default: 30 sec.");
            amsWaitTime = 30000;
        }
        
        logger.info("Waiting time for the AMS system check: " + amsWaitTime + " ms");
        
        messageContent = messageHelper.getNewCheckMessage(MessageHelper.MessageType.SYSTEM, currentStatusEntry);
        // checkTimeStamp = convertDateStringToLong(messageContent.get("EVENTTIME"));
            
        // Check old messages if the last check was not answered, otherwise delete all old messages
        if(currentStatusEntry.getCheckStatus() != CheckResult.TIMEOUT)
        {
            success = amsPublisher.sendMessage(messageContent);
            if(success)
            {
                logger.info("Message sent.");
            }
            else
            {
                logger.error("Message could NOT be sent.");
                
                closeJms();
                throw new AmsSystemMonitorException("Message could NOT be sent.", AmsSystemMonitorException.ERROR_CODE_SYSTEM_MONITOR);
            }
        }
        else
        {
            logger.info("A new message has NOT been sent. Looking for an old check message.");
        }
        
        waitObject = new Object();
        success = false;
        endTime = System.currentTimeMillis() + amsWaitTime;
        
        // Get all messages
        do
        {
            message = amsReceiver.receive("amsSystemMonitor");
            if(message != null)
            {
                // Compare the incoming message with the sent message
                logger.info("Message received.");
                
                if(message instanceof MapMessage)
                {
                    mapMessage = (MapMessage)message;
                    
                    if(messageHelper.isAmsAnswer(messageContent, mapMessage))
                    {
                        success = true;
                    }
                    else
                    {
                        logger.warn("Received message is NOT equal.");
                    }
                }
                else
                {
                    logger.warn("Message is not a MapMessage object: " + message.getClass().getName());
                }
                
                acknowledge(message);
            }
            
            synchronized(waitObject)
            {
                try
                {
                    waitObject.wait(1000);
                }
                catch(InterruptedException ie) {}
            }
            
            currentTime = System.currentTimeMillis();
        
        }while((success == false) && (currentTime <= endTime));
    
        closeJms();
        
        if(!success)
        {
            // Timeout?
            if(currentTime > endTime)
            {
                throw new AmsSystemMonitorException("Timeout! Sent message could not be received.", AmsSystemMonitorException.ERROR_CODE_TIMEOUT);
            }
        }
    }
}