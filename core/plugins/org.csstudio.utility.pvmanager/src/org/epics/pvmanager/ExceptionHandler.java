/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package org.epics.pvmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class receives all the exceptions generated by a PV.
 * {@link #handleException(java.lang.Exception) } is called on the thread that
 * generated the exception. It's up to the handler to handle thread safety
 * and notification.
 *
 * @author carcassi
 */
public class ExceptionHandler {

    private static final Logger log = Logger.getLogger(ExceptionHandler.class.getName());

    /**
     * Notifies of an exception being thrown.
     * 
     * @param ex the exception
     */
    public void handleException(Exception ex) {
        log.log(Level.INFO, "Exception for PV", ex);
    }
    
    public static ExceptionHandler createDefaultExceptionHandler(final PVWriter<?> pvWriter, final ThreadSwitch threadSwitch) {
        final PVWriterImpl<?> pvWriterImpl = (PVWriterImpl<?>) pvWriter;
        return new ExceptionHandler() {
            @Override
            public void handleException(final Exception ex) {
                threadSwitch.post(new Runnable() {

                    @Override
                    public void run() {
                        pvWriterImpl.setLastWriteException(ex);
                        pvWriterImpl.firePvValueWritten();
                    }
                });
            }
            
        };
    }
}
