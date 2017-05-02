package com.sirma.itt.emf.semantic;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.openrdf.IsolationLevels;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Contains a pool with connections to the Ontology repository and maintains the connections open.
 *
 * @author kirq4e
 */
@ApplicationScoped
public class ConnectionFactoryImpl implements ConnectionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Statistics statistics;

	@Inject
	private SemanticConfiguration semanticConfiguration;

	@Inject
	private RepositoryConnectionMonitor connectionMonitor;

	/**
	 * Retrieve a connection to the ontology repository and begin a transaction for it. A connection is available only
	 * for the current request.
	 *
	 * @return Active connection to the repository
	 */
	@Override
	public RepositoryConnection produceConnection() {
		try {
			LOGGER.trace("Producing new semantic repository connection..");
			RepositoryConnection connection = getRepository().getConnection();
			connection.begin(IsolationLevels.READ_COMMITTED);

			int connectionId = System.identityHashCode(connection);
			connectionMonitor.onNewConnection(connectionId, true);
			LOGGER.trace("Produced connection: {}", connectionId);
			return connection;
		} catch (RepositoryException e) {
			throw new EmfRuntimeException(e);
		}
	}

	private Repository getRepository() {
		return semanticConfiguration.getRepository().get();
	}

	@Override
	public RepositoryConnection produceReadOnlyConnection() {
		try {
			LOGGER.trace("Producing new read only semantic repository connection..");
			RepositoryConnection connection = getRepository().getConnection();
			// ensure isolation level if transaction is started after that otherwise rollback is not possible
			connection.setIsolationLevel(IsolationLevels.READ_COMMITTED);

			int connectionId = System.identityHashCode(connection);
			connectionMonitor.onNewConnection(connectionId, false);
			LOGGER.trace("Produced read only connection: {}", connectionId);
			return connection;
		} catch (RepositoryException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Called when a connection should be released. Releases the connection in the pool.
	 *
	 * @param connection
	 *            connection that is released.
	 */
	@Override
	public void disposeConnection(RepositoryConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("Parameter 'connection' cannot be NULL!");
		}

		int connectionid = System.identityHashCode(connection);
		try {
			LOGGER.trace("Destroying semantic repository connection: {}", connectionid);
			commitTransaction(connection);

			connection.close();
			LOGGER.trace("Connection {} closed..", connectionid);
		} catch (RepositoryException e) {
			rollbackTransaction(connection);
			throw new EmfRuntimeException(e);
		} finally {
			connectionMonitor.onConnectionClose(connectionid);
			closeConnection(connection);
		}
	}

	/**
	 * Rollback transaction.
	 *
	 * @param connection
	 *            the connection
	 */
	private static void rollbackTransaction(RepositoryConnection connection) {
		try {
			connection.rollback();
		} catch (RepositoryException e1) {
			LOGGER.error("Failed to rollback connection " + System.identityHashCode(connection), e1);
		}
	}

	/**
	 * Commit transaction.
	 *
	 * @param connection
	 *            the connection
	 * @throws RepositoryException
	 *             the repository exception
	 */
	private void commitTransaction(RepositoryConnection connection) throws RepositoryException {
		if (connection.isActive()) {
			TimeTracker tracker = statistics.createTimeStatistics(getClass(), "semanticTransactionCommit").begin();
			connection.commit();
			LOGGER.trace("Commit active connection {} took {} ms", System.identityHashCode(connection), tracker.stop());
		}
	}

	/**
	 * Close connection.
	 *
	 * @param connection
	 *            the connection
	 */
	private static void closeConnection(RepositoryConnection connection) {
		try {
			if (connection.isOpen()) {
				connection.close();
			}
		} catch (RepositoryException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Returns an instance of ValueFactory for creating Literals and URIs
	 *
	 * @return ValueFactory instance
	 */
	@Override
	@Produces
	@ApplicationScoped
	public ValueFactory produceValueFactory() {
		return getRepository().getValueFactory();
	}

	@Override
	public void tearDown() {
		// if the connection factory is requested to shutdown then we just redirect the call to configurations where the
		// actual repositories and connections are stored.
		Destroyable.destroy(semanticConfiguration);
	}
}
