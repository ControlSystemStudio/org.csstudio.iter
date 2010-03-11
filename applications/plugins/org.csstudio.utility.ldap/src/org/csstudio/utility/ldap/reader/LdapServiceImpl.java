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
package org.csstudio.utility.ldap.reader;

import java.util.Observer;

import org.apache.log4j.Logger;
import org.csstudio.platform.logging.CentralLogger;


public class LdapServiceImpl implements LdapService {

	private final Logger LOGGER = CentralLogger.getInstance().getLogger(this);


	/**
	 * Constructor.
	 */
	public LdapServiceImpl() {
		// Empty
	}



	private LdapResultList createLdapReader(final String readerName, final String filter, final Observer observer) {
		final LdapResultList list = new LdapResultList(observer);

		final LDAPReader ldapr = new LDAPReader(readerName, filter, list);
		// For some reason the LdapResultList doesn't notify its observers
		// when the result is written to it. The job change listener works
		// around this problem by sending the notification when the LDAP
		// reader job is done.
		//		ldapr.addJobChangeListener(new JobChangeAdapter() {
		//			@Override
		//			public void done(final IJobChangeEvent event) {
		//				if (event.getResult().isOK()) {
		//					list.notifyView();
		//				}
		//			}
		//		});
		ldapr.schedule();

		return list;
	}

	@Override
	public LdapResultList readLdapEntries(final String readerName, final String filter, final Observer observer) {
		final LdapResultList result = createLdapReader(readerName, filter, observer);
		return result;
	}
}
