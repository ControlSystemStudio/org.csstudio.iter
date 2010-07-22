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
package org.csstudio.config.ioconfig.model;

import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.DDB_PASSWORD;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.DDB_TIMEOUT;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.DDB_USER_NAME;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.DIALECT;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.HIBERNATE_CONNECTION_DRIVER_CLASS;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.HIBERNATE_CONNECTION_URL;
import static org.csstudio.config.ioconfig.model.preference.PreferenceConstants.SHOW_SQL;

import java.util.Date;

import org.csstudio.config.ioconfig.model.pbmodel.Channel;
import org.csstudio.config.ioconfig.model.pbmodel.ChannelStructure;
import org.csstudio.config.ioconfig.model.pbmodel.GSDFile;
import org.csstudio.config.ioconfig.model.pbmodel.GSDModule;
import org.csstudio.config.ioconfig.model.pbmodel.Master;
import org.csstudio.config.ioconfig.model.pbmodel.Module;
import org.csstudio.config.ioconfig.model.pbmodel.ModuleChannelPrototype;
import org.csstudio.config.ioconfig.model.pbmodel.ProfibusSubnet;
import org.csstudio.config.ioconfig.model.pbmodel.Slave;
import org.csstudio.config.ioconfig.model.preference.PreferenceConstants;
import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * 
 * @author hrickens
 * @author $Author$
 * @version $Revision$
 * @since 03.06.2009
 */
public final class HibernateManager {

	private static final class SessionWatchDog extends Job {
		private SessionFactory _sessionFactory;
		private int _sessionUseCounter;
		private long _timeToCloseSession = (3600000 * 5);

		private SessionWatchDog(String name) {
			super(name);
			setPriority(DECORATE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			boolean watch = true;
			Date date = new Date();
			while (watch) {
			    try {
			        this.getThread();
			        // Sleep 5 min.
			        Thread.sleep(30000);
			    } catch (InterruptedException e) {
			    }
				if (_sessionFactory == null || _sessionFactory.isClosed()) {
					break;
				}
				if (_sessionUseCounter == 0) {
					Date now = new Date();
					if (now.getTime() - date.getTime() > getTimeToCloseSession()) {
					    CentralLogger.getInstance().info(this, "DB Session closed by watchdog");
						_sessionFactory.close();
						_sessionFactory = null;
						break;
					}

				} else {
					date = new Date();
					_sessionUseCounter = 0;
				}
			}
			monitor.done();
			monitor = null;

			return Status.OK_STATUS;
		}

		public void setSessionFactory(SessionFactory sessionFactory) {
			_sessionFactory = sessionFactory;

		}

		public void useSession() {
			_sessionUseCounter++;
		}

		public long getTimeToCloseSession() {
			return _timeToCloseSession;
		}
	}

	private static SessionFactory _sessionFactoryDevDB;
	private static int openTransactions = 0;
	private static Configuration _cfg;

	/**
	 * The timeout in sec.
	 */
	private static int _timeout = 10;
	private static org.hibernate.classic.Session _sess;
	private static Transaction _trx;
	private static SessionWatchDog _sessionWatchDog;

	/**
	 * 
	 * @param timeout
	 *            set the DB Timeout.
	 */
	public static void setTimeout(int timeout) {
		_timeout = timeout;
	}

	private HibernateManager() {
	}

	static void setSessionFactory(SessionFactory sf) {
		synchronized (HibernateManager.class) {
			_sessionFactoryDevDB = sf;
		}
	}

	private static void initSessionFactoryDevDB() {
		if (_sessionFactoryDevDB != null && !_sessionFactoryDevDB.isClosed()) {
			return;
		}

//		if (_cfg == null) {
			buildConifg();
//		}
		HibernateManager.setSessionFactory(_cfg.buildSessionFactory());
	}

	private static void buildConifg() {
		new InstanceScope().getNode(Activator.getDefault().getPluginId())
				.addPreferenceChangeListener(new IPreferenceChangeListener() {

					@Override
					public void preferenceChange(PreferenceChangeEvent event) {
						setProperty(event.getKey(), event.getNewValue());
						HibernateManager.setSessionFactory(_cfg
								.buildSessionFactory());
					}
				});

		IPreferencesService prefs = Platform.getPreferencesService();
		String pluginId = Activator.getDefault().getPluginId();
		_cfg = new AnnotationConfiguration()
				.addAnnotatedClass(NodeImage.class)
				.addAnnotatedClass(Channel.class)
				.addAnnotatedClass(ChannelStructure.class)
				.addAnnotatedClass(Module.class)
				.addAnnotatedClass(Slave.class)
				.addAnnotatedClass(Master.class)
				.addAnnotatedClass(ProfibusSubnet.class)
				.addAnnotatedClass(GSDModule.class)
				.addAnnotatedClass(Ioc.class)
				.addAnnotatedClass(Facility.class)
				.addAnnotatedClass(FacilityLight.class)
				.addAnnotatedClass(Node.class)
				.addAnnotatedClass(GSDFile.class)
				.addAnnotatedClass(ModuleChannelPrototype.class)
				.addAnnotatedClass(Document.class)
				.addAnnotatedClass(SearchNode.class)
				.addAnnotatedClass(Sensors.class)
				.setProperty("org.hibernate.cfg.Environment.MAX_FETCH_DEPTH",
						"0")
				.setProperty(
						"hibernate.connection.driver_class",
						prefs.getString(pluginId,
								HIBERNATE_CONNECTION_DRIVER_CLASS, "", null))
				.setProperty("hibernate.dialect",
						prefs.getString(pluginId, DIALECT, "", null))
				.setProperty("hibernate.order_updates", "false")
				.setProperty(
						"hibernate.connection.url",
						prefs.getString(pluginId, HIBERNATE_CONNECTION_URL, "",
								null))
				.setProperty("hibernate.connection.username",
						prefs.getString(pluginId, DDB_USER_NAME, "", null))
				.setProperty("hibernate.connection.password",
						prefs.getString(pluginId, DDB_PASSWORD, "", null))
				.setProperty("transaction.factory_class",
						"org.hibernate.transaction.JDBCTransactionFactory")
				.setProperty("hibernate.cache.provider_class",
						"org.hibernate.cache.HashtableCacheProvider")
				.setProperty("hibernate.cache.use_minimal_puts", "true")
				.setProperty("hibernate.cache.use_query_cache", "true")
				// connection Pool
				.setProperty("c3p0.min_size", "1")
				.setProperty("c3p0.max_size", "3")
				.setProperty("c3p0.timeout", "1800")
				.setProperty("c3p0.acquire_increment", "1")
				.setProperty("c3p0.idel_test_period", "100") // sec
				.setProperty("c3p0.max_statements", "1")
//				.setProperty("hibernate.show_sql", "true")
//                .setProperty("hibernate.format_sql", "true")
//                .setProperty("hibernate.use_sql_comments", "true")
		.setProperty("hibernate.cache.use_second_level_cache", "true");
;
		// .setProperty("hibernate.hbm2ddl.auto", "update");
		setTimeout(prefs.getInt(pluginId, DDB_TIMEOUT, 90, null));
	}

	/**
	 * Set a Hibernate Property.
	 * 
	 * @param property
	 *            the Property to set a new Value.
	 * @param value
	 *            the value for the Property.
	 */
	protected static void setProperty(String property, Object value) {
		if (property.equals(PreferenceConstants.DDB_TIMEOUT)) {
			if (value instanceof Integer) {
				setTimeout((Integer) value);
			} else if (value instanceof String) {
				setTimeout(Integer.parseInt((String) value));
			}
		} else if (value instanceof String) {
			String stringValue = ((String) value).trim();

			if (property.equals(DDB_PASSWORD)) {
				_cfg.setProperty("hibernate.connection.password", stringValue);
			} else if (property.equals(DDB_USER_NAME)) {
				_cfg.setProperty("hibernate.connection.username", stringValue);
			} else if (property.equals(DIALECT)) {
				_cfg.setProperty("hibernate.dialect", stringValue);
			} else if (property.equals(HIBERNATE_CONNECTION_DRIVER_CLASS)) {
				_cfg.setProperty("hibernate.connection.driver_class",
						stringValue);
			} else if (property.equals(HIBERNATE_CONNECTION_URL)) {
				_cfg.setProperty("hibernate.connection.url", stringValue);
				
			} else if (property.equals(SHOW_SQL)) {
				_cfg.setProperty("hibernate.show_sql", stringValue);
				_cfg.setProperty("hibernate.format_sql", stringValue);
                _cfg.setProperty("hibernate.use_sql_comments", stringValue);

			}
		}
	}

	/**
	 * 
	 * @param <T>
	 *            The result Object type.
	 * @param callback
	 *            The Hibernate call back.
	 * @return the Session resulte.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T doInDevDBHibernateAlt(HibernateCallback callback) {
		initSessionFactoryDevDB();
		if (_sessionWatchDog == null) {
			_sessionWatchDog = new SessionWatchDog("Session Watch Dog");
			_sessionWatchDog.setSystem(true);
		}
		_sessionWatchDog.setSessionFactory(_sessionFactoryDevDB);
		_sessionWatchDog.schedule(30000);
		_sessionWatchDog.useSession();

		_trx = null;
		openTransactions++;
		try {
			_sess = _sessionFactoryDevDB.openSession();
			CentralLogger.getInstance().debug(
					HibernateManager.class.getSimpleName(),
					"Open a Session: " + openTransactions);
			CentralLogger.getInstance().debug(
					HibernateManager.class.getSimpleName(),
					"session is " + _sess);
			_trx = _sess.getTransaction();
			_trx.setTimeout(_timeout);
			_trx.begin();
			Object result = callback.execute(_sess);
			_trx.commit();
			return (T) result;
		} catch (HibernateException ex) {
			if (_trx != null) {
				try {
					_trx.rollback();
				} catch (HibernateException exRb) {
					CentralLogger.getInstance().error(
							HibernateManager.class.getSimpleName(), exRb);
					exRb.printStackTrace();
				}
			}
			CentralLogger.getInstance().error(
					HibernateManager.class.getSimpleName(), ex);
			ex.printStackTrace();
			throw ex;
		} finally {
			try {
				openTransactions--;
				if (_sess != null) {// && openTransactions<1) {
					_sess.close();
					_sess = null;
				}
			} catch (Exception exCl) {
				exCl.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param <T>
	 *            The result Object type.
	 * @param callback
	 *            The Hibernate call back.
	 * @return the Session resulte.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T doInDevDBHibernate(HibernateCallback callback) {
		
		if (_sess == null ||!_sess.isConnected() || !_sess.isOpen()) {
		    if (_sessionWatchDog == null) {
	            _sessionWatchDog = new SessionWatchDog("Session Watch Dog");
	            _sessionWatchDog.setSystem(true);
	        }
		    initSessionFactoryDevDB();
			_sess = _sessionFactoryDevDB.openSession();
		}
		_sessionWatchDog.setSessionFactory(_sessionFactoryDevDB);
        _sessionWatchDog.schedule(30000);
        _sessionWatchDog.useSession();
		_trx = null;
		try {
			CentralLogger.getInstance().debug(
					HibernateManager.class.getSimpleName(),
					"session is " + _sess);
			_trx = _sess.getTransaction();
			_trx.setTimeout(_timeout);
			_trx.begin();
			Object result = callback.execute(_sess);
			_trx.commit();
			return (T) result;
		} catch (HibernateException ex) {
			if (_trx != null) {
				try {
					_trx.rollback();
				} catch (HibernateException exRb) {
					CentralLogger.getInstance().error(
							HibernateManager.class.getSimpleName(), exRb);
				}
			}
			CentralLogger.getInstance().error(
					HibernateManager.class.getSimpleName(), ex);
			throw ex;
		}
	}
	
	public static void closeSession() {
	    if(_sess!=null&&_sess.isOpen()) {
	        _sess.close();
	        _sess=null;
	    }
	    if(_sessionFactoryDevDB!=null&&!_sessionFactoryDevDB.isClosed()) {
	        _sessionFactoryDevDB.close();
	        _sessionFactoryDevDB=null;
	    }
	    if(_sessionWatchDog!=null) {
	        _sessionWatchDog.cancel();
	        _sessionWatchDog=null;
	    }
	    CentralLogger.getInstance().info(HibernateManager.class, "DB Session closed");
        
    }
}