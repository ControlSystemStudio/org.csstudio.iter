/*
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton,
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

package org.epics.css.dal;


/**
 * This interface is implemented by context that contains asynchronous methods.
 * An asynchronous method is a method that, when invoked, returns immediately,
 * but the processing still continues in the underlying data source layer.
 * When the processing finishes there, an asynchronous notification, called
 * a response, is sent back to the original caller. Because the dispatching of
 * responses follows the standard multicast JavaBeans event scheme, we must
 * identify which JavaBeans notification was generated by which asynchronous
 * invocation.
 * @author Igor Kriznar (igor.kriznarATcosylab.com)
 *
 */
public interface AsynchronousContext
{
	/**
	 * Adds a response listener that will receive the notifications
	 * about asynchronous completions for all asynchronous mode operations
	 * declared by the implementing instance.
	 *
	 * @param l listener object
	 */
	public void addResponseListener(ResponseListener<?> l);

	/**
	 * Removes a response listener.
	 *
	 * @param l listener object
	 *
	 * @see #addResponseListener
	 */
	public void removeResponseListener(ResponseListener<?> l);

	/**
	 * Returns a list of all response listeners.
	 *
	 * @return all response listeners of the object implementing this interface
	 */
	public ResponseListener<?>[] getResponseListeners();

	/**
	 * Returns the latest request invoked within the calling thread. The possible requests
	 * are set requests, requests for chatacteristics or setting triggers in
	 * the monitors issued by this <code>Updateable</code> and so on.
	 *
	 * @return Object the latest request
	 */
	public Request<?> getLatestRequest();

	/**
	 * Returns the latest response.
	 *
	 * @return Object the latest response
	 *
	 * @see #getLatestRequest
	 */
	public Response<?> getLatestResponse();

	/**
	 * Returns <code>true</code> if the latest response is error-free.
	 * The error-free condition is defined by the underlying implementation.
	 * If the condition is  error-free, there should be no need for the
	 * DAL users to query the latest response explicitly. Please note
	 * that the return value of this method applies to the latest response
	 * received (not the latest request completed). These two may differ
	 * because the request can generate multiple responses in general. This
	 * also  corresponds to natural interpretation of a request-response
	 * mechanism: what the user must check is the correctness of the
	 * responses, where the correctness of  requests is implied if any
	 * response can be produced in the first place.<p>Note: this method
	 * returns <code>true</code> if no response has arrived yet or if no
	 * request has been submitted.</p>
	 *
	 * @return boolean true iff the latest response is error free.
	 */
	public boolean getLatestSuccess();
} /* __oOo__ */


/* __oOo__ */