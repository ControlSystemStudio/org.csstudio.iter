
/*
 * Copyright (c) 2011 Stiftung Deutsches Elektronen-Synchrotron,
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
 *
 */

package org.csstudio.alarm.jms2ora.util;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;

import org.csstudio.alarm.jms2ora.IMessageConverter;
import org.csstudio.alarm.jms2ora.IMessageProcessor;
import org.csstudio.alarm.jms2ora.service.ArchiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mmoeller
 * @version 1.0
 * @since 29.08.2011
 */
public class MessageConverter extends Thread implements IMessageConverter {

    /** The class logger */
    private static final Logger LOG = LoggerFactory.getLogger(MessageConverter.class);

    /** Writes the ArchiveMessage content to the DB */
    private final IMessageProcessor messageProcessor;

    /** Object that gets all JMS messages */
    private final MessageAcceptor messageAcceptor;

    /** Queue for received messages */
    private final ConcurrentLinkedQueue<RawMessage> rawMessages;

    /** Object that creates the MessageContent objects */
    private final MessageContentCreator contentCreator;

    private final Object lock;

    private boolean working;

    public MessageConverter(@Nonnull final IMessageProcessor processor, @Nonnull final StatisticCollector c) {
        messageProcessor = processor;
        messageAcceptor = new MessageAcceptor(this, c);
        rawMessages = new ConcurrentLinkedQueue<RawMessage>();
        contentCreator = new MessageContentCreator(c);
        lock = new Object();
        working = true;
        this.start();
    }

    @Override
    public void run() {

        LOG.info("Running thread {}", MessageConverter.class.getSimpleName());

        while (working) {

            synchronized (lock) {
                try {
                    if (rawMessages.isEmpty()) {
                        lock.wait();
                    }
                } catch (final InterruptedException ie) {
                    LOG.warn("[*** InterruptedException ***]: {}", ie.getMessage());
                }
            }

            if (rawMessages.size() > 0) {
                final Vector<RawMessage> convertMe = new Vector<RawMessage>(rawMessages);
                rawMessages.removeAll(convertMe);
                final Vector<ArchiveMessage> am = contentCreator.convertRawMessages(convertMe);
                messageProcessor.putArchiveMessages(am);
                am.clear();
                convertMe.clear();
            }
        }

        messageAcceptor.closeAllReceivers();
    }

    public int getQueueSize() {
        return rawMessages.size();
    }

    @Override
    public final void putRawMessage(@Nonnull final RawMessage m) {
        rawMessages.add(m);
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Sets the working flag to false. The thread will leave then.
     */
    public void stopWorking() {
        working = false;
        synchronized (lock) {
            lock.notify();
        }
    }
}