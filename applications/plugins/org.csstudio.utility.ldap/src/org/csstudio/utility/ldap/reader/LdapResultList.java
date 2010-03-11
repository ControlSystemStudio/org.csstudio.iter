/*
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchrotron,
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

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import org.csstudio.utility.ldap.Activator;
import org.csstudio.utility.ldap.Messages;
import org.csstudio.utility.namespace.utility.ControlSystemItem;
import org.csstudio.utility.namespace.utility.NameSpaceResultList;
import org.csstudio.utility.namespace.utility.ProcessVariable;


public class LdapResultList extends NameSpaceResultList {

	private List<String> _result = new ArrayList<String>();
	private String _eventTime;
	private String _parentName;
	private String _severity;
	private String _status;
	private final Observer _observer;

	public LdapResultList() {
		_observer = null;
	}

	public LdapResultList(final Observer observer) {
		_observer = observer;
		addObserver(observer);
	}

	/* (non-Javadoc)
	 * @see org.csstudio.utility.nameSpaceBrowser.utility.NameSpaceResultList#copy()
	 */
	@Override
	public NameSpaceResultList copy() {
		final LdapResultList e = new LdapResultList(_observer);
		e.setResultList(_result);
		return e;
	}

	public List<String> getAnswer() {
		final List<String> tmp = new ArrayList<String>(_result);
		_result.clear();
		return tmp;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.utility.nameSpaceBrowser.utility.NameSpaceResultList#getResultList()
	 */
	@Override
	public List<ControlSystemItem> getCSIResultList() {
		final List<ControlSystemItem> tmpList = new ArrayList<ControlSystemItem>();
		if(_result == null)
			return null;
		for (final String row : _result) {
			String cleanList = row;
			// Delete "-Chars that add from LDAP-Reader when the result contains special character
			if(cleanList.startsWith("\"")){ //$NON-NLS-1$
				if(cleanList.endsWith("\"")) {
					cleanList = cleanList.substring(1,cleanList.length()-1);
				} else {
					cleanList = cleanList.substring(1);
				}
			}
			final String[] token = cleanList.split("[,=]"); //$NON-NLS-1$
			if(token.length<2) {
				if(!token[0].equals("no entry found")){
					Activator.logError(Messages.getString("CSSView.Error1")+row+"'");//$NON-NLS-1$ //$NON-NLS-2$
				}
				break;

			}

			if(token[0].compareTo("eren")==0){ //$NON-NLS-1$
				tmpList.add(new ProcessVariable(token[1], cleanList));
			}
			else{
				tmpList.add(new ControlSystemItem(token[1], cleanList));
			}
		}
		_result = new ArrayList<String>();
		return tmpList;
	}

	public String getEventTime() {
		return _eventTime;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.utility.nameSpaceBrowser.utility.NameSpaceResultList#getNew()
	 */
	@Override
	public NameSpaceResultList getNew() {
		return new LdapResultList(_observer);
	}

	public Observer getObserver() {
		return _observer;
	}

	public String getParentName() {
		return _parentName;
	}

	public String getSeverity() {
		return _severity;
	}

	public String getStatus() {
		return _status;
	}

	@Override
	public void notifyView() {
		setChanged();
		notifyObservers();
	}

	public void setEventTime(final String eventTime) {
		_eventTime = eventTime;
	}

	public void setParentName(final String parentName) {
		_parentName = parentName;
	}

	/* (non-Javadoc)
	 * @see org.csstudio.utility.nameSpaceBrowser.utility.NameSpaceResultList#setResultList(java.util.ArrayList)
	 */
	@Override
	public void setResultList(final List<String> resultList) {
		_result.clear();
		_result.addAll(resultList);
		notifyView();

	}

	public void setSeverity(final String severity) {
		_severity = severity;
	}

	public void setStatus(final String status) {
		_status = status;
	}

}
