/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
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
package org.csstudio.archive.common.service.mysqlimpl.dao;

import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.DATABASE_NAME;
import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.FAILOVER_HOST;
import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.HOST;
import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.PASSWORD;
import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.PORT;
import static org.csstudio.archive.common.service.mysqlimpl.MySQLArchiveServicePreference.USER;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.csstudio.archive.common.service.ArchiveConnectionException;
import org.csstudio.archive.common.service.mysqlimpl.archivermgmt.ArchiverMgmtDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.archivermgmt.IArchiverMgmtDao;
import org.csstudio.archive.common.service.mysqlimpl.channel.ArchiveChannelDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.channel.IArchiveChannelDao;
import org.csstudio.archive.common.service.mysqlimpl.channelgroup.ArchiveChannelGroupDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.channelgroup.IArchiveChannelGroupDao;
import org.csstudio.archive.common.service.mysqlimpl.channelstatus.ArchiveChannelStatusDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.channelstatus.IArchiveChannelStatusDao;
import org.csstudio.archive.common.service.mysqlimpl.controlsystem.ArchiveControlSystemDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.controlsystem.IArchiveControlSystemDao;
import org.csstudio.archive.common.service.mysqlimpl.engine.ArchiveEngineDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.engine.IArchiveEngineDao;
import org.csstudio.archive.common.service.mysqlimpl.sample.ArchiveSampleDaoImpl;
import org.csstudio.archive.common.service.mysqlimpl.sample.IArchiveSampleDao;
import org.csstudio.platform.logging.CentralLogger;
import org.csstudio.platform.util.StringUtil;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * The archive dao manager.
 *
 * Envisioned to handle connection pools and transactions with CRUD command abstraction.
 *
 * @author bknerr
 * @since 11.11.2010
 */
public enum ArchiveDaoManager {

    INSTANCE;

    private static final String ARCHIVE_CONNECTION_EXCEPTION_MSG = "Archive connection could not be established";

    static final Logger LOG = CentralLogger.getInstance().getLogger(ArchiveDaoManager.class);
    static final Logger WORKER_LOG = CentralLogger.getInstance().getLogger(PersistDataWorker.class);

    /**
     * DAOs.
     */
    private IArchiveChannelDao _archiveChannelDao;
    private IArchiveChannelGroupDao _archiveChannelGroupDao;
    private IArchiveEngineDao _archiveEngineDao;
    private IArchiverMgmtDao _archiverMgmtDao;
    private IArchiveSampleDao _archiveSampleDao;
    private IArchiveControlSystemDao _archiveControlSystemDao;
    private IArchiveChannelStatusDao _archiveChannelStatusDao;

    /**
     * The datasource that specifies the connections.
     */
    private MysqlDataSource _dataSource;

    /**
     * Any thread owns a connection.
     */
    private final ThreadLocal<Connection> _archiveConnection =
        new ThreadLocal<Connection>();

    /**
     * Prefs from plugin_customization.
     */
    private String _prefHost;
    private String _prefFailoverHost;
    private String _prefUser;
    private String _prefPassword;
    private Integer _prefPort;
    private String _prefDatabaseName;


    /**
     * Constructor.
     */
    private ArchiveDaoManager() {

        loadAndCheckPreferences();
        _dataSource = createDataSource();
    }


    private void loadAndCheckPreferences() {
        _prefHost = HOST.getValue();
        _prefFailoverHost = FAILOVER_HOST.getValue();
        _prefPort = PORT.getValue();
        _prefDatabaseName = DATABASE_NAME.getValue();
        _prefUser = USER.getValue();
        _prefPassword = PASSWORD.getValue();

    }

    @Nonnull
    private MysqlDataSource createDataSource() {

        final MysqlDataSource ds = new MysqlDataSource();
        String hosts = _prefHost;
        if (!StringUtil.isBlank(_prefFailoverHost)) {
            hosts += "," + _prefFailoverHost;
        }
        ds.setServerName(hosts);
        ds.setPort(_prefPort);
        ds.setDatabaseName(_prefDatabaseName);
        ds.setUser(_prefUser);
        ds.setPassword(_prefPassword);
        ds.setFailOverReadOnly(false);
        ds.setMaxAllowedPacket(64*1024); // up tp 64MB TODO (bknerr): same pref as in the engine mgr

        return ds;
    }


    /**
     * Connects with the RDB instance for the given datasource.
     *
     * An existing connection is closed and an new connection is established.
     *
     * @param ds the mysql data source
     * @return connection the newly established connection
     * @throws ArchiveConnectionException
     */
    @Nonnull
    public Connection connect(@Nonnull final MysqlDataSource ds) throws ArchiveConnectionException {

        Connection connection = _archiveConnection.get();
        try {
            if (connection != null) { // close existing connection
                _archiveConnection.set(null);
                connection.close();
            }
            // Get class loader to find the driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            connection = ds.getConnection();

            if (connection != null) {
                final DatabaseMetaData meta = connection.getMetaData();
                if (meta != null) {
                    // Constructor call -> LOG.debug not possible, not yet initialised
                    CentralLogger.getInstance().getLogger(ArchiveDaoManager.class).debug("MySQL connection:\n" +
                              meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                } else {
                    // Constructor call -> LOG.debug not possible, not yet initialised
                    CentralLogger.getInstance().getLogger(ArchiveDaoManager.class).debug("No meta data for MySQL connection");
                }
                // set to true to enable failover to other host
                connection.setAutoCommit(true);
                _archiveConnection.set(connection);
            }
        } catch (final Exception e) {
            handleExceptions(e);
        }
        if (connection == null || StringUtil.isBlank(_prefDatabaseName)) {
            throw new ArchiveConnectionException("Connection could not be established or database name is not set.", null);
        }
        return connection;
    }

    /**
     * To reduce the readability of the invoking method. Catches checked exceptions, wraps them in
     * dedicated abstraction level exception. Rethrows any other exception as new RuntimeException.
     *
     * @param e Exception to handle
     * @throws RuntimeException wrapper for unhandled exception
     */
    private void handleExceptions(@Nonnull final Exception e) throws ArchiveConnectionException {
        try {
            throw e;
        } catch (final InstantiationException ie) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, ie);
        } catch (final IllegalAccessException iae) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, iae);
        } catch (final ClassNotFoundException cfe) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, cfe);
        } catch (final SQLException se) {
            throw new ArchiveConnectionException(ARCHIVE_CONNECTION_EXCEPTION_MSG, se);
        } catch (final Exception re) {
            throw new RuntimeException(re);
        }
    }

    /**
     * Disconnects the connection for the owning thread.
     * @throws ArchiveConnectionException
     */
    public void disconnect() throws ArchiveConnectionException {
        final Connection connection = _archiveConnection.get();
        if (connection != null) {
            try {
                connection.close();
                _archiveConnection.set(null);
            } catch (final SQLException e) {
                throw new ArchiveConnectionException("Archive disconnection failed!", e);
            }
        }
    }


    /**
     * Returns the current connection for the owning thread.
     * This method is invoked by the dedicated daos to retrieve their connection.
     * A connection's datasource is configured via the plugin_customization.ini
     *
     * @return the connection
     * @throws ArchiveConnectionException
     */
    @Nonnull
    public Connection getConnection() throws ArchiveConnectionException {
        final Connection connection = _archiveConnection.get();
        if (connection == null) {
            // the calling thread has not yet a connection registered.
            return connect(_dataSource);
        }
        return connection;
    }

    @CheckForNull
    public String getDatabaseName() {
        return _prefDatabaseName;
    }

    @Nonnull
    public IArchiveChannelDao getChannelDao() {
        if (_archiveChannelDao == null) {
            _archiveChannelDao = new ArchiveChannelDaoImpl();
        }
        return _archiveChannelDao;
    }

    @Nonnull
    public IArchiverMgmtDao getArchiverMgmtDao() {
        if (_archiverMgmtDao == null) {
            _archiverMgmtDao  = new ArchiverMgmtDaoImpl();
        }
        return _archiverMgmtDao;
    }

    @Nonnull
    public IArchiveChannelGroupDao getChannelGroupDao() {
        if (_archiveChannelGroupDao == null) {
            _archiveChannelGroupDao = new ArchiveChannelGroupDaoImpl();
        }
        return _archiveChannelGroupDao;
    }


    public IArchiveSampleDao getSampleDao() {
        if (_archiveSampleDao == null) {
            _archiveSampleDao = new ArchiveSampleDaoImpl();
        }
        return _archiveSampleDao;
    }

    public IArchiveEngineDao getEngineDao() {
        if (_archiveEngineDao == null) {
            _archiveEngineDao = new ArchiveEngineDaoImpl();
        }
        return _archiveEngineDao;
    }


    @Nonnull
    public IArchiveControlSystemDao getControlSystemDao() {
        if (_archiveControlSystemDao == null) {
            _archiveControlSystemDao = new ArchiveControlSystemDaoImpl();
        }
        return _archiveControlSystemDao;
    }


    @Nonnull
    public IArchiveChannelStatusDao getChannelStatusDao() {
        if (_archiveChannelStatusDao == null) {
            _archiveChannelStatusDao = new ArchiveChannelStatusDaoImpl();
        }
        return _archiveChannelStatusDao;
    }
}

