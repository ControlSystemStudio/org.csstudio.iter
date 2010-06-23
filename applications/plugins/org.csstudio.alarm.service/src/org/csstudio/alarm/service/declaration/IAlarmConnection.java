/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron, Member of the Helmholtz
 * Association, (DESY), HAMBURG, GERMANY. THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN
 * "../AS IS" BASIS. WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE IN
 * ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR CORRECTION. THIS
 * DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. NO USE OF ANY SOFTWARE IS
 * AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER. DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE,
 * SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS. THE FULL LICENSE SPECIFYING FOR THE SOFTWARE
 * THE REDISTRIBUTION, MODIFICATION, USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE
 * DISTRIBUTION OF THIS PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY
 * FIND A COPY AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM $Id: IAlarmConnection.java,v 1.2 2010/04/26
 * 09:35:21 jpenning Exp $
 */
package org.csstudio.alarm.service.declaration;

import javax.annotation.Nonnull;

/**
 * Is used by the AlarmService to represent a connection into JMS or DAL resp.. It abstracts the
 * process of connecting and disconnecting.
 *
 * @author jpenning
 * @author $Author$
 * @version $Revision$
 * @since 21.04.2010
 */
public interface IAlarmConnection {

    /**
     * @return true, if the underlying implementation can handle topics (currently JMS only)
     */
    boolean canHandleTopics();

    /**
     * Connects to the underlying system.
     * You have to provide a connectionMonitor to track connection state.
     * You have to provide a listener to receive messages.
     * You have to provide the alarm resource (create it via alarm service) which supplies the parameters.
     *
     * @param connectionMonitor
     * @param listener
     * @param resource
     * @throws AlarmConnectionException
     */
    void connectWithListenerForResource(@Nonnull final IAlarmConnectionMonitor connectionMonitor,
                                        @Nonnull final IAlarmListener listener,
                                        @Nonnull final IAlarmResource resource) throws AlarmConnectionException;

    /**
     * Disconnect from the underlying system, freeing resources. The connection monitor will be
     * removed before disconnection, so disconnect DOES NOT result in a call to the connection
     * monitor.
     */
    void disconnect();

}