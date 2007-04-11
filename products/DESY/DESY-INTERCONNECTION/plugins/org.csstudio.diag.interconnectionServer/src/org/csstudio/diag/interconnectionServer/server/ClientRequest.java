
/*
 *  ClientRequest.java, v1.0, 2005-12-22
 *
 *  Copyright (c) 2005 Markus M�ller
 *  Deutsches Elektronen-Synchrotron DESY, Hamburg 
 *  Notkestra�e 85, 22607 Hamburg, Germany
 * 
 *  All rights reserved.
 *
 */

package org.csstudio.diag.interconnectionServer.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.jms.MapMessage;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.Destination;
import javax.jms.JMSException;
import org.csstudio.utility.ldap.engine.*;

import org.csstudio.diag.interconnectionServer.server.InterconnectionServer.TagValuePairs;

//import de.desy.jms.server.InterconnectionServer.TagValuePairs;

public class ClientRequest extends Thread
{
    private DatagramSocket      socket          = null;
    private DatagramPacket      packet          = null;
    private Session             alarmSession, logSession, putLogSession         = null;
    private Destination         alarmDestination, logDestination, putLogDestination     = null;
    private MessageProducer		alarmSender, logSender, putLogSender	= null;
    //private MessageProducer     sender          = null;
    //private MapMessage          message         = null;
    private Statistic			statistic		= null;
    public Statistic.StatisticContent  statisticContent = null;
    public TagList 				tagList			= null;
    
	public ClientRequest(DatagramSocket d, DatagramPacket p, Session jmsAlarmSession, Destination jmsAlarmDestination, MessageProducer jmsAlarmSender, 
			Session jmsLogSession, Destination jmsLogDestination, MessageProducer jmsLogSender, 
			Session jmsPutLogSession, Destination jmsPutLogDestination, MessageProducer jmsPutLogSender)
	{
        this.socket       = d;
		this.packet       = p;
		this.alarmSession      = jmsAlarmSession;
		this.logSession      = jmsLogSession;
		this.putLogSession      = jmsPutLogSession;
        this.alarmDestination  = jmsAlarmDestination;
        this.logDestination  = jmsLogDestination;
        this.putLogDestination  = jmsPutLogDestination;
        this.alarmSender = jmsAlarmSender;
        this.logSender = jmsLogSender;
        this.putLogSender = jmsPutLogSender;
        this.statistic	  = Statistic.getInstance();
        this.tagList 	  = TagList.getInstance();
        
		this.start();
	}
	
	public void run()
	{
	    DatagramPacket 	newPacket       = null;
	    InetAddress    	address         = null;
	    String			hostName		= null;
	    String         	daten           = null;
	    String         	answerString    = null;
	    String[]       	attribute       = null;
	    int            	length          = 0;
	    int            	port            = 0;
	    String			statisticId		= null;
	    boolean 		received		= true;
	    MapMessage      message         = null;
	    MessageProducer sender          = null;
        
        address 	= packet.getAddress();
        hostName 	= address.getHostName();
        port 		= packet.getPort();
        length 		= packet.getLength();
        statisticId	= hostName + ":" + port;
        GregorianCalendar actualTime = new GregorianCalendar();
        
        System.out.println("Time: - start 		= " + dateToString(new GregorianCalendar()));
        //
        // write out some statistics
        //
        statisticContent = statistic.getContentObject( statisticId);
        statisticContent.setTime( received);
        statisticContent.setHost( hostName);
        statisticContent.setPort( port);
        statisticContent.setLastMessage( daten);
        statisticContent.setLastMessageSize( length); 
        
        Vector<TagValuePairs> tagValuePairs	= new Vector<TagValuePairs>();
        Hashtable tagValue = new Hashtable();	// could replace the Vector above
        TagValuePairs	id		= InterconnectionServer.getInstance().new TagValuePairs();
        TagValuePairs	type	= InterconnectionServer.getInstance().new TagValuePairs();
        
        //
        // you need debug information?
        //
        if (false) {
        	
        	daten = new String(packet.getData(), 0, length);
        	
            System.out.println("--------------------------------------------------------------------------------\n");
            System.out.println("Adresse:                  " + address.toString());
            System.out.println("Port:                     " + port);
            System.out.println("L�nge der Daten im Paket: " + length);
            System.out.println("L�nge des Datenstrings:   " + daten.length());
            System.out.println("Der String:\n" + daten + "\n");
        
            StringTokenizer tok = new StringTokenizer(daten, PreferenceProperties.DATA_TOKENIZER);
            
            System.out.println("Anzahl der Token: " + tok.countTokens() + "\n");
        }
        
        
        if ( parseMessage(  tagValue, tagValuePairs, id, type, statisticId)) {
        	
        	System.out.println("Time: - after parse 	= " + dateToString(new GregorianCalendar()));
        	boolean status	= true;
        	
        	//
        	// ok we successfully parsed whatever was sent
        	//
        	// now we'll send it to the jms server
        	//
        	
        	switch (TagList.getInstance().getMessageType( type.getValue())) {
        	case TagList.ALARM_MESSAGE:
        	case TagList.EVENT_MESSAGE:
        	case TagList.ALARM_STATUS_MESSAGE:
        		//
        		// ALARM jms server
        		//
        		try {
            		//sender = alarmSession.createProducer(alarmDestination);
            		System.out.println("Time-ALARM: - after sender= 	= " + dateToString(new GregorianCalendar()));
                    //message = alarmSession.createMapMessage();
            		message = InterconnectionServer.getInstance().prepareTypedJmsMessage( alarmSession.createMapMessage(), tagValuePairs, type);
            		System.out.println("Time-APARM: - after message= 	= " + dateToString(new GregorianCalendar()));
            		
            		alarmSender.setPriority( 9);
            		System.out.println("Time-ALARM: - before sender-send 	= " + dateToString(new GregorianCalendar()));
            		alarmSender.send(message);
            		message = null;
            		System.out.println("Time-ALARM: - after sender-send 	= " + dateToString(new GregorianCalendar()));
        		}
        		catch(JMSException jmse)
                {
        			status = false;
        			InterconnectionServer.getInstance().checkSendMessageErrorCount();
                    System.out.println("ClientRequest : send ALARM message : *** EXCEPTION *** : " + jmse.getMessage());
                }
        		ServerCommands.sendMesssage( ServerCommands.prepareMessage( id.getTag(), id.getValue(), status), socket, packet);
        		System.out.println("Time-ALARM: - after send UDP reply	= " + dateToString(new GregorianCalendar()));
        		//
        		// time to update the LDAP server entry
        		//
        		updateLdapEntry( tagValue);
        		
        		break;
        		
        	case TagList.STATUS_MESSAGE:
        	case TagList.SYSTEM_LOG_MESSAGE:
        	case TagList.APPLICATION_LOG_MESSAGE:
        		//
        		// LOG jms server
        		//
        		try{
        			// sender = logSession.createProducer(logDestination);
                    //message = logSession.createMapMessage();
                    message = InterconnectionServer.getInstance().prepareTypedJmsMessage( logSession.createMapMessage(), tagValuePairs, type);
            		logSender.send(message);
        		}
        		catch(JMSException jmse)
                {
        			status = false;
        			InterconnectionServer.getInstance().checkSendMessageErrorCount();
                    System.out.println("ClientRequest : send LOG message : *** EXCEPTION *** : " + jmse.getMessage());
                }
        		ServerCommands.sendMesssage( ServerCommands.prepareMessage( id.getTag(), id.getValue(), status), socket, packet);

        		break;
        		
        	case TagList.BEACON_MESSAGE:
        		//
        		// just send a reply
        		//
        		ServerCommands.sendMesssage( ServerCommands.prepareMessage( id.getTag(), id.getValue(), status), socket, packet);
        		System.out.println("Time-Beacon: - after send UDP reply	=  " + dateToString(new GregorianCalendar()));
        		//
        		// set beacon time locally
        		//
        		Statistic.getInstance().getContentObject(statisticId).setBeaconTime();
        		//
        		// generate system log message if connection state changed
        		//
        		if ( !Statistic.getInstance().getContentObject(statisticId).connectState) {
        			//
        			// connect state chaged!
        			//
        			Statistic.getInstance().getContentObject(statisticId).setConnectState (true);
        			try{
        			InterconnectionServer.getInstance().sendLogMessage( InterconnectionServer.getInstance().prepareJmsMessage ( logSession.createMapMessage(), InterconnectionServer.getInstance().jmsLogMessageNewClientConnected( statisticId)));
        			}
        			catch(JMSException jmse)
                    {
            			status = false;
                        System.out.println("ClientRequest : send NewClientConnected-LOG message : *** EXCEPTION *** : " + jmse.getMessage());
                    }
        			/*
        			Statistic.getInstance().getContentObject(statisticId).setConnectState (true);
        			try{
            			sender = logSession.createProducer(logDestination);
                        message = logSession.createMapMessage();
                        message = jmsLogMessageNewClientConnected( statisticId);
                		sender.send(message);
            		}
            		catch(JMSException jmse)
                    {
            			status = false;
                        System.out.println("ClientRequest : send NewClientConnected-LOG message : *** EXCEPTION *** : " + jmse.getMessage());
                    }
                    */
        		}
        		break;
        		
        	case TagList.PUT_LOG_MESSAGE:
        		//
        		// PUT-LOG jms server
        		//
        		try {
        			//sender = putLogSession.createProducer(putLogDestination);
                    //message = putLogSession.createMapMessage();
                    message = InterconnectionServer.getInstance().prepareTypedJmsMessage( putLogSession.createMapMessage(), tagValuePairs, type);
            		putLogSender.send(message);
        		}
        		catch(JMSException jmse)
                {
        			status = false;
        			InterconnectionServer.getInstance().checkSendMessageErrorCount();
                    System.out.println("ClientRequest : send ALARM message : *** EXCEPTION *** : " + jmse.getMessage());
                }
        		ServerCommands.sendMesssage( ServerCommands.prepareMessage( id.getTag(), id.getValue(), status), socket, packet);
        		
        		break;
        		
        	case 4711:
        		//
        		// in case we have to execule something asynchronously...
        		//
            	new ServerCommands (id.getTag(), id.getValue(), tagList.getTagProperties( attribute[0].toString()), socket, packet);
            	break;
        	case TagList.TEST_COMMAND:
        		//
        		// execute command asynchronously
        		//
            	new ServerCommands (id.getTag(), id.getValue(), tagList.getTagProperties( id.getTag()), socket, packet);
            	break;
            	
        		
        	case TagList.UNKNOWN_MESSAGE:
        		default:
        		status = false;
        		ServerCommands.sendMesssage( ServerCommands.prepareMessage( id.getTag(), id.getValue(), status), socket, packet);

        	}

        }

        // clean up
        try
        {
            if (sender != null) {
            	sender.close();
            	System.out.println("clean up");
            }
            message = null;
        }
        catch(JMSException jmse)
        {
            System.out.println("ClientRequest : clean up : *** EXCEPTION *** : " + jmse.getMessage());
        }         
	}
	
	private boolean parseMessage ( Hashtable<String,String> tagValue, Vector<TagValuePairs> tagValuePairs, TagValuePairs tag, TagValuePairs type, String statisticId) {
		boolean success = false;
		String[] attribute = null;
		boolean gotTag 		= false;
    	boolean gotId 		= false;
		
		String daten = new String(this.packet.getData(), 0, this.packet.getLength());
		
		//
		// just in case we should use another data format in the future
		// here's the place to implement anoher parser
		//

		StringTokenizer tok = new StringTokenizer(daten, PreferenceProperties.DATA_TOKENIZER);
        
		// TODO: make it a logMessage
        System.out.println("Anzahl der Token: " + tok.countTokens() + "\n");
        
        if(tok.countTokens() > 0)
        {
                while(tok.hasMoreTokens())
                {
                	String localTok = tok.nextToken();
                	//
                	// parsing Tag=value;Tag1=value1;
                	//
                	
                	//
                	// first make sure that it's a pair
                	// this requires a '=' and at least two more chares like a=b
                	//
                	
                	if ( (localTok !=null) && localTok.contains("=") && (localTok.length() > 2 )) {
                		
                		//
                		// ok seems to be ok to parse further
                		// now make sure that '=' is not the first and not the last char
                		// -> avoid ;=Value;Tag=; combinations
                		
                		if ( (!localTok.endsWith( "=")) && (!localTok.startsWith( "="))) {
	                		attribute = localTok.split("=");
	                		
	                		// TODO: make this a debug message
		                    System.out.println(statisticId + " : " + attribute[0] + " := "+ attribute[1]);
		                    //
		                    // fill Hash table in any case
		                    //
		                    tagValue.put(attribute[0].toString(), attribute[1].toString());
		                    
		                    if ( tagList.getTagType( attribute[0].toString()) == PreferenceProperties.TAG_TYPE_IS_ID) {
		                    	tag.setTag(attribute[0].toString());
		                    	tag.setValue(attribute[1].toString());
		                    	gotId = true;
		                    } else if ( tagList.getTagType( attribute[0].toString()) == PreferenceProperties.TAG_TYPE_IS_TYPE) {
		                    	type.setTag(attribute[0].toString());
		                    	type.setValue(attribute[1].toString());
		                    	gotTag = true;
		                    } else {
		                    	TagValuePairs newTagValuePair =  InterconnectionServer.getInstance().new TagValuePairs ( attribute[0].toString(), attribute[1].toString());
		                    	tagValuePairs.add(newTagValuePair);
		                    }
	                	} //if
	                } // if
                } //while
        } // if tok
        if ( gotId && gotTag){
        	success = true;
        	return success;
        }
		return success;
	}
	public String dateToString ( GregorianCalendar gregorsDate) {
		
		//
		// convert Gregorian date into string
		//
		//TODO: use other time format - actually : DD-MM-YYYY
		Date d = gregorsDate.getTime();
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
	    //DateFormat df = DateFormat.getDateInstance();
	    return df.format(d);
	}
	private void updateLdapEntry ( Hashtable<String,String> tagValue) {
		//
		// find necessary entries and activate ldapUpdateMethod
		//
		String channel,status,severity,timeStamp = null;
		if ( tagValue.containsKey("NAME") && tagValue.containsKey("STATUS") && tagValue.containsKey("SEVERITY") && tagValue.containsKey("TIME")) {
			channel = tagValue.get("NAME");
			status = tagValue.get("STATUS");
			severity = tagValue.get("SEVERITY");
			timeStamp = tagValue.get("TIME");
			//
			// send values to LDAP engine
			//
			Engine.getInstance().setSeverityStatusTimeStamp ( channel, status, severity, timeStamp);
		}
		
	}
	
}
