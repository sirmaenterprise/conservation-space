package com.sirma.itt.emf.semantic;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.ReadOnly;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Produces managed, read only and user managed connections to Ontology repository.<br>
 * Managed connections could be produced only in active transaction context and are committed and closed at the end
 * of the transaction.<br>
 * When data modifications will not be performed by a connection as performance improvement the read only connections
 * could be used as the transaction management overhead is omitted. These connections does not need to be closed and do
 * not participate in transaction. The produced read only connections are tenant aware and could be cached without issues
 *
 * @author kirq4e
 * @author bbonev
 */
@ApplicationScoped
public class ConnectionFactoryImpl implements ConnectionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Statistics statistics;
	private SemanticConfiguration semanticConfiguration;
	private SecurityContext securityContext;

	private TransactionCoordinator transactionCoordinator;

	private ConnectionFactoryImpl() {
		// to allow proxying
	}

	@Inject
	public ConnectionFactoryImpl(SemanticConfiguration semanticConfiguration, TransactionManager transactionManager,
			TransactionSynchronizationRegistry transactionSynchronizationRegistry,
			SecurityContext securityContext, Statistics statistics) {
		this.semanticConfiguration = semanticConfiguration;
		this.securityContext = securityContext;
		this.statistics = statistics;

		transactionCoordinator = new TransactionCoordinator(transactionManager, transactionSynchronizationRegistry, statistics, this::createUnmanagedConnection);
	}

	/**
	 * Retrieve a connection to the ontology repository and begin a transaction for it. A connection is available only
	 * for the current request.
	 *
	 * @return Active connection to the repository
	 */
	@Override
	public RepositoryConnection produceConnection() {
		if (transactionCoordinator.getTx() != null) {
			throw new IllegalStateException("There is active transaction. Cannot produce unmanaged repository connection");
		}
		return createUnmanagedConnection();
	}

	private RepositoryConnection createUnmanagedConnection() {
		try {
			RepositoryConnection connection = getRepository().getConnection();
			connection.begin(IsolationLevels.READ_COMMITTED);

			int connectionId = System.identityHashCode(connection);
			LOGGER.trace("Produced transacted connection: {}", connectionId);
			return connection;
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	@Override
	@Produces
	public RepositoryConnection produceManagedConnection() {
		statistics.updateMeter(getClass(), "semanticTxRate");
		return new ManagedRepositoryConnection(transactionCoordinator, produceReadOnlyConnection());
	}

	@Override
	@Produces
	@ReadOnly
	public RepositoryConnection produceReadOnlyConnection() {
		return new ReadOnlyRepositoryConnection(buildReadOnlyConnectionProvider());
	}

	private Supplier<RepositoryConnection> buildReadOnlyConnectionProvider() {
		// create an contextual instance that will keep different connections for different tenants
		// this reference is then stored in proxy handler in a single thread
		Contextual<RepositoryConnection> contextualConnectionStore = new ContextualReference<>(getTenantIdSupplier());
		contextualConnectionStore.initializeWith(this::createReadOnlyConnection);
		return contextualConnectionStore::getContextValue;
	}

	private Supplier<String> getTenantIdSupplier() {
		// remove the reference to the current class
		SecurityContext context = securityContext;
		return context::getCurrentTenantId;
	}

	private RepositoryConnection createReadOnlyConnection() {
		try {
			RepositoryConnection connection = getRepository().getConnection();
			// ensure isolation level if transaction is started after that otherwise rollback is not possible
			// probably not relevant for connections that are never going to be in transactions
			connection.setIsolationLevel(IsolationLevels.READ_COMMITTED);

			LOGGER.trace("Produced read only connection: {}", System.identityHashCode(connection));
			return connection;
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException(e);
		}
	}

	private Repository getRepository() {
		return semanticConfiguration.getRepository().get();
	}

	/**
	 * Called when a connection should be released. Releases the connection in the pool.
	 *
	 * @param connection connection that is released.
	 */
	@Override
	public void disposeConnection(RepositoryConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("Parameter 'connection' cannot be NULL!");
		}
		if (connection instanceof ManagedRepositoryConnection) {
			throw new IllegalArgumentException("Cannot dispose managed repository connection");
		}

		int connectionid = System.identityHashCode(connection);
		try {
			LOGGER.trace("Destroying semantic repository connection: {}", connectionid);
			commitTransaction(connection);

			connection.close();
			LOGGER.trace("Connection {} closed..", connectionid);
		} catch (RepositoryException e) {
			rollbackTransaction(connection);
			throw new SemanticPersistenceException(e);
		} finally {
			closeConnection(connection);
		}
	}

	private void rollbackTransaction(RepositoryConnection connection) {
		try {
			statistics.getCounter(getClass(), "semanticRollbackTxCount").increment();
			connection.rollback();
		} catch (RepositoryException e) {
			throw new SemanticPersistenceException("Failed to rollback connection", e);
		}
	}

	private void commitTransaction(RepositoryConnection connection) {
		if (connection.isActive()) {
			TimeTracker tracker = statistics.createTimeStatistics(getClass(), "semanticTransactionCommit").begin();
			connection.commit();
			LOGGER.trace("Commit active connection {} took {} ms", System.identityHashCode(connection), tracker.stop());
		} else {
			LOGGER.warn("Tried to commit not active transaction for connection {}", System.identityHashCode(connection));
		}
	}

	private void closeConnection(RepositoryConnection connection) {
		if (connection.isOpen()) {
			connection.close();
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
