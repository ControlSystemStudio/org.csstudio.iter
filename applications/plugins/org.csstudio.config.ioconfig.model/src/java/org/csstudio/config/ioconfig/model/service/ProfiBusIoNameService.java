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
/*
 * $Id: ProfiBusIoNameService.java,v 1.3 2009/12/08 07:36:02 hrickens Exp $
 */
package org.csstudio.config.ioconfig.model.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.csstudio.config.ioconfig.model.PersistenceException;
import org.csstudio.config.ioconfig.model.Repository;
import org.csstudio.dct.IoNameService;
import org.csstudio.platform.logging.CentralLogger;

/**
 * @author hrickens
 * @author $Author: hrickens $
 * @version $Revision: 1.3 $
 * @since 12.02.2009
 */
public class ProfiBusIoNameService implements IoNameService {

    /**
     * Get the Epics Address string to an IO Name. It the name not found return the string '$$$
     * IO-Name NOT found! $$$'.
     * 
     * @param ioName the IO-Name.
     * @return Field and the Epics Address String for the given IO-Name separated by ':'.
     * @throws PersistenceException 
     */
    @Override
    @Nonnull
    public final String getEpicsAddress(@Nonnull String ioName, @Nonnull String field) {
        // return the Bus-type
        if(field.equals("DTYP")) {
            // at the moment only Profibus DP
            return "PBDP";
        } else if(field.equals("DESC")) {
            try {
                return Repository.getShortChannelDesc(ioName);
            } catch (PersistenceException e) {
                CentralLogger.getInstance().error(this, e);
                return "%%% Database not available %%%";
            }
        }
        try {
            return Repository.getEpicsAddressString(ioName);
        } catch (PersistenceException e) {
            CentralLogger.getInstance().error(this, e);
            return "%%% Database not available %%%";
        }
    }
    
    @Override
    @CheckForNull
    public List<String> getAllIoNames(){
        try {
            return Repository.getIoNames();
        } catch (PersistenceException e) {
            CentralLogger.getInstance().error(this, e);
            return null;
        }
    }
    
    /**
     * Get the Epics Address string to an IO Name. It the name not found return the string '$$$
     * IO-Name NOT found! $$$'.
     * 
     * @param ioName the IO-Name.
     * @return the Epics Adress for the given IO-Name.
     */
    @Nonnull 
    public List<String> getIoNamesFromIoc(@Nonnull String iocName){
        try {
            return Repository.getIoNames(iocName);
        } catch (PersistenceException e) {
            CentralLogger.getInstance().error(this, e);
            ArrayList<String> al = new ArrayList<String>();
            al.add("%%% Database not available %%%");
            return al;
        }
    }

}