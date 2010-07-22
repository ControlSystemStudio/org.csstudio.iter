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
package org.csstudio.sds.internal.statistics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for class {@link StatisticUtil}.
 * 
 * @author Alexander Will
 * @version $Revision$
 * 
 */
public final class StatisticUtilTest {

	/**
	 * Set up the test case.
	 * 
	 * @throws java.lang.Exception
	 *             If an execption occurs during setup.
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Tear down the test case.
	 * 
	 * @throws java.lang.Exception
	 *             If an exception occurs during teardown.
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link org.csstudio.sds.internal.statistics.StatisticUtil#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		assertNotNull(StatisticUtil.getInstance());
	}

	/**
	 * Test method for
	 * {@link org.csstudio.sds.internal.statistics.StatisticUtil#recordWidgetRefresh(java.lang.Object)}.
	 */
	@Test
	public void testRecordWidgetRefresh() {
		StatisticUtil.getInstance().init();

		try {
			StatisticUtil.getInstance().trackExecution(MeasureCategoriesEnum.SYNC_EXEC_CATEGORY, 10);
			Thread.sleep(100);
			StatisticUtil.getInstance().trackExecution(MeasureCategoriesEnum.SYNC_EXEC_CATEGORY, 10);
			Thread.sleep(100);
			StatisticUtil.getInstance().trackExecution(MeasureCategoriesEnum.SYNC_EXEC_CATEGORY, 10);
			Thread.sleep(100);
			
			//TODO: Tests (swende)
		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
		
	}

	/**
	 * Test method for
	 * {@link org.csstudio.sds.internal.statistics.StatisticUtil#init()}.
	 */
	@Test
	public void testClearStatistics() {
		StatisticUtil.getInstance().init();
	}

	/**
	 * Test method for
	 * {@link org.csstudio.sds.internal.statistics.StatisticUtil#toString()}.
	 */
	@Test
	public void testToString() {
		StatisticUtil.getInstance().init();
	}

}