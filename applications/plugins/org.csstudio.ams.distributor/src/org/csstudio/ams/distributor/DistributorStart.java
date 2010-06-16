
/* 
 * Copyright (c) 2008 Stiftung Deutsches Elektronen-Synchrotron, 
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
 
package org.csstudio.ams.distributor;

import java.net.InetAddress;
import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.csstudio.ams.AmsActivator;
import org.csstudio.ams.AmsConstants;
import org.csstudio.ams.Log;
import org.csstudio.ams.SynchObject;
import org.csstudio.ams.Utils;
import org.csstudio.ams.distributor.preferences.DistributorPreferenceKey;
import org.csstudio.ams.internal.AmsPreferenceKey;
import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.remotercp.common.servicelauncher.ServiceLauncher;
import org.remotercp.ecf.ECFConstants;
import org.remotercp.login.connection.HeadlessConnection;

public class DistributorStart implements IApplication
{
    public final static int STAT_INIT = 0;
    public final static int STAT_OK = 1;
    public final static int STAT_GROUP_BLOCKED = 1;
    public final static int STAT_ERR_APPLICATION_DB = 2;
    public final static int STAT_ERR_APPLICATION_DB_SEND = 3;
    public final static int STAT_ERR_CONFIG_DB = 4;
    public final static int STAT_ERR_JMSCON_INT = 5;                            // jms communication to ams internal jms partners
    public final static int STAT_ERR_JMSCON_EXT = 6;                            // jms communication to external jms (AlarmTopic)
    public final static int STAT_ERR_JMSCON_FREE_SEND = 7;                      // jms communication to free topics
    public final static int STAT_ERR_FLG_RPL = 8;                               // could not update (application-db) db flag (ReplicationState)
    public final static int STAT_ERR_FLG_BUP = 9;                               // could not update (config-db) db flag (BupState)
    public final static int STAT_ERR_UNKNOWN = 10;

    // only internal
    public final static int STAT_FALSE = 999;                                   // replaces boolean false in methods

    public final static long WAITFORTHREAD = 10000;
    
    public final static boolean CREATE_DURABLE = true;
    
    private static DistributorStart _instance = null;

    private Context extContext = null;
    private ConnectionFactory extFactory = null;
    private Connection extConnection = null;
    private Session extSession = null;
    
    private MessageProducer extPublisherStatusChange = null;
    
    private SynchObject sObj = null;
    private String managementPassword;
    private int lastStatus = 0;
    private boolean bStop;
    private boolean restart;
    
    public DistributorStart()
    {
        _instance = this;
        sObj = new SynchObject(STAT_INIT, System.currentTimeMillis());
        
        IPreferencesService pref = Platform.getPreferencesService();
        managementPassword = pref.getString(AmsActivator.PLUGIN_ID, AmsPreferenceKey.P_AMS_MANAGEMENT_PASSWORD, "", null);
        if(managementPassword == null) {
            managementPassword = "";
        }
    }
    
    public void stop()
    {
        return;
    }

    public static DistributorStart getInstance()
    {
        return _instance;
    }

    public synchronized void setRestart()
    {
        restart = true;
        bStop = true;
    }

    public synchronized void setShutdown()
    {
        restart = false;
        bStop = true;
    }

    /**
     * 
     * @return
     */
    public synchronized String getPassword()
    {
        return managementPassword;
    }
    
    /**
     * 
     */
    public Object start(IApplicationContext context) throws Exception
    {
        DistributorWork dw = null;
        boolean bInitedJms = false;
        lastStatus = getStatus();                                               // use synchronized method

        Log.log(this, Log.INFO, "Starting");
        DistributorPreferenceKey.showPreferences();
        
        bStop = false;
        restart = false;
        
        connectToXMPPServer();
        
        while(bStop == false)
        {
            try
            {
                if (dw == null)
                {
                    dw = new DistributorWork(this);
                    dw.start();
                }
                
                if (!bInitedJms)
                {
                    bInitedJms = initJms();
                }
        
                Log.log(this, Log.DEBUG, "run");
                Thread.sleep(1000);
                
                SynchObject actSynch = new SynchObject(0, 0);
                if (!sObj.hasStatusSet(actSynch, 300, STAT_ERR_UNKNOWN))        // if status has not changed in the last 5 minutes
                {                                                               // every 5 minutes if blocked
                    Log.log(this, Log.FATAL, "TIMEOUT: status has not changed the last 5 minutes.");
                }

                String statustext = "unknown";
                if (actSynch.getStatus() != lastStatus)                         // if status value changed
                {
                    switch (actSynch.getStatus())
                    {
                        case STAT_INIT:
                            statustext = "init";
                            break;
                        case STAT_OK:
                            statustext = "ok";
                            break;
                        case STAT_ERR_APPLICATION_DB:
                        case STAT_ERR_APPLICATION_DB_SEND:
                            statustext = "err_application_db";
                            break;
                        case STAT_ERR_CONFIG_DB:
                            statustext = "err_configuration_db";
                            break;
                        case STAT_ERR_JMSCON_INT:
                            statustext = "err_jms_internal";
                            break;
                        case STAT_ERR_JMSCON_EXT:
                            statustext = "err_jms_external";
                            break;
                        case STAT_ERR_JMSCON_FREE_SEND:
                            statustext = "err_jms_free_topics";
                            break;
                        case STAT_ERR_FLG_RPL:
                            statustext = "err_flag_rpl_state";
                            break;
                        case STAT_ERR_FLG_BUP:
                            statustext = "err_flag_bup_state";
                            break;
                    }
                    Log.log(this, Log.INFO, "set status to " + statustext + "(" + actSynch.getStatus() + ")");
                    lastStatus = actSynch.getStatus();
                    if (bInitedJms)
                    {
                        if (!sendStatusChange(actSynch.getStatus(), statustext, actSynch.getTime()))
                        {
                            closeJms();
                            bInitedJms = false;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Log.log(this, Log.FATAL, e);
                
                closeJms();
                bInitedJms = false;
            }
        }

        Log.log(this, Log.INFO, "FilterManagerStart is exiting now");
        
        if(dw != null)
        {
            // Clean stop of the working thread
            dw.stopWorking();
            
            try
            {
                dw.join(WAITFORTHREAD);
            }
            catch(InterruptedException ie) { }
    
            if(dw.stoppedClean())
            {
                Log.log(this, Log.FATAL, "Restart/Exit: Thread stopped clean.");
                
                dw = null;
            }
            else
            {
                Log.log(this, Log.FATAL, "Restart/Exit: Thread did NOT stop clean.");
                dw.closeJmsExternal();
                dw.closeJmsInternal();
                dw.closeApplicationDb();
                dw = null;
            }
        }

        if(restart)
            return EXIT_RESTART;
        else
            return EXIT_OK;
    }

    public void connectToXMPPServer()
    {
    	IPreferencesService pref = Platform.getPreferencesService();
    	String xmppServer = pref.getString(DistributorPlugin.PLUGIN_ID, DistributorPreferenceKey.P_XMPP_SERVER, "krynfs.desy.de", null);
        String xmppUser = pref.getString(DistributorPlugin.PLUGIN_ID, DistributorPreferenceKey.P_XMPP_USER, "anonymous", null);
        String xmppPassword = pref.getString(DistributorPlugin.PLUGIN_ID, DistributorPreferenceKey.P_XMPP_PASSWORD, "anonymous", null);

        try
        {
            HeadlessConnection.connect(xmppUser, xmppPassword, xmppServer, ECFConstants.XMPP);
            ServiceLauncher.startRemoteServices();     
        }
        catch(Exception e)
        {
            CentralLogger.getInstance().warn(this, "Could not connect to XMPP server: " + e.getMessage());
        }
    }

    public int getStatus()
    {
        return sObj.getSynchStatus();
    }
    public void setStatus(int status)
    {
        sObj.setSynchStatus(status);                                            // set always, to update time
    }
    
    private boolean initJms()
    {
        try
        {
            IPreferenceStore storeAct = AmsActivator.getDefault().getPreferenceStore();
            Hashtable<String, String> properties = new Hashtable<String, String>();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, 
                    storeAct.getString(org.csstudio.ams.internal.AmsPreferenceKey.P_JMS_EXTERN_CONNECTION_FACTORY_CLASS));
            properties.put(Context.PROVIDER_URL, 
                    storeAct.getString(org.csstudio.ams.internal.AmsPreferenceKey.P_JMS_EXTERN_SENDER_PROVIDER_URL));
            extContext = new InitialContext(properties);
            
            extFactory = (ConnectionFactory) extContext.lookup(
                    storeAct.getString(org.csstudio.ams.internal.AmsPreferenceKey.P_JMS_EXTERN_CONNECTION_FACTORY));
            extConnection = extFactory.createConnection();
            
            // ADDED BY: Markus Möller, 25.05.2007
            extConnection.setClientID("DistributorStartSenderExternal");
            
            extSession = extConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            
            // CHANGED BY: Markus Möller, 25.05.2007
            /*extPublisherStatusChange = extSession.createProducer((Topic)extContext.lookup(
                    storeAct.getString(org.csstudio.ams.internal.SampleService.P_JMS_EXT_TOPIC_STATUSCHANGE)));
            */
            
            extPublisherStatusChange = extSession.createProducer(extSession.createTopic(
                    storeAct.getString(org.csstudio.ams.internal.AmsPreferenceKey.P_JMS_EXT_TOPIC_STATUSCHANGE)));
            if (extPublisherStatusChange == null)
            {
                Log.log(this, Log.FATAL, "could not create extPublisherStatusChange");
                return false;
            }

            extConnection.start();

            return true;
        }
        catch(Exception e)
        {
            Log.log(this, Log.FATAL, "could not init external Jms", e);
        }
        return false;
    }

    private void closeJms()
    {
        Log.log(this, Log.INFO, "exiting external jms communication");
        
        if (extPublisherStatusChange != null){try{extPublisherStatusChange.close();}
        catch (JMSException e){Log.log(this, Log.WARN, e);}finally{extPublisherStatusChange=null;}}    
        if (extSession != null){try{extSession.close();}
        catch (JMSException e){Log.log(this, Log.WARN, e);}finally{extSession=null;}}
        if (extConnection != null){try{extConnection.stop();}
        catch (JMSException e){Log.log(this, Log.WARN, e);}}
        if (extConnection != null){try{extConnection.close();}
        catch (JMSException e){Log.log(this, Log.WARN, e);}finally{extConnection=null;}}
        if (extContext != null){try{extContext.close();}
        catch (NamingException e){Log.log(this, Log.WARN, e);}finally{extContext=null;}}

        Log.log(this, Log.INFO, "jms external communication closed");
    }
    
    private boolean sendStatusChange(int status, String strStat, long lSetTime) throws Exception
    {
        MapMessage mapMsg = null;
        try
        {
            mapMsg = extSession.createMapMessage();
        }
        catch(Exception e)
        {
            Log.log(this, Log.FATAL, "could not createMapMessage", e);
        }
        if (mapMsg == null)
            return false;

        mapMsg.setString(AmsConstants.MSGPROP_CHECK_TYPE, "PStatus");
        mapMsg.setString(AmsConstants.MSGPROP_CHECK_PURL, InetAddress.getLocalHost().getHostAddress());
        mapMsg.setString(AmsConstants.MSGPROP_CHECK_PLUGINID, DistributorPlugin.PLUGIN_ID);
        mapMsg.setString(AmsConstants.MSGPROP_CHECK_STATUSTIME, Utils.longTimeToUTCString(lSetTime));
        mapMsg.setString(AmsConstants.MSGPROP_CHECK_STATUS, String.valueOf(status));
        mapMsg.setString(AmsConstants.MSGPROP_CHECK_TEXT, strStat);

        Log.log(this, Log.INFO, "StatusChange - start external jms send. MessageProperties= " + Utils.getMessageString(mapMsg));

        try
        {
            extPublisherStatusChange.send(mapMsg);
        }
        catch(Exception e)
        {
            Log.log(this, Log.FATAL, "could not send to external jms", e);
            return false;
        }

        Log.log(this, Log.INFO, "send external jms message done");

        return true;
    }
}