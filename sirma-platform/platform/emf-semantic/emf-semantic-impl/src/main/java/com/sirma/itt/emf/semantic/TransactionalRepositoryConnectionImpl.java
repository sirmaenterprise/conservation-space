package com.sirma.itt.emf.semantic;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.openrdf.IsolationLevel;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.Update;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

import info.aduna.iteration.Iteration;

/**
 * Session bean that acts as a proxy to the actual {@link RepositoryConnection}. The connection is initialized on
 * transaction start and is destroyed on transaction end. The connection is stored in the thread local variable when
 * span between multiple services is required in a single transaction.
 * <p>
 * <b>NOTE: </b> The implementation supports read only connections but the {@link #close()} method should be called at
 * the end of the method that uses it. The implementation supports {@link AutoCloseable}.
 * <p>
 * Recommended use in no transactional context: <code><pre>
 * 	&#64;Inject
 * private Instance<TransactionalRepositoryConnection> repositoryConnection;
 * ....
 * &#64;TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
 * ....
 * try (TransactionalRepositoryConnection connection = repositoryConnection.get()) {
 * String query = ....;
 * Query prepareQuery = connection.prepareQuery(QueryLanguage.SPARQL, query);
 * Dataset dataset = prepareQuery.getDataset();
 * .....
 * } catch (RepositoryException | MalformedQueryException e) {
 * LOGGER.error("", e);
 * }
 * <p>
 * </pre></code>
 *
 * @author BBonev
 */
@Stateful
@StatefulTimeout(value = 10, unit = TimeUnit.MINUTES)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TransactionalRepositoryConnectionImpl implements TransactionalRepositoryConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalRepositoryConnectionImpl.class);

	/**
	 * The connection factory used for producing the connections.
	 */
	@Inject
	private ConnectionFactory connectionFactory;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private RepositoryConnectionMonitor monitor;

	/*
	 * store read only and read/write connections in separate fields the container creates new instances when calling
	 * non transactional and transactional methods and closes them at any time during the read/write transaction is
	 * active. If not implemented this way the read/write connection could be flushed in the middle of active
	 * transaction.
	 */
	/** A cache for the read/write repository connection for the current thread. */
	private static ThreadLocal<ConnectionHolder> readWriteConnection = new ThreadLocal<>();
	/** A cache for the read only repository connection for the current thread. */
	private static ThreadLocal<ConnectionHolder> readOnlyConnection = new ThreadLocal<>();
	/**
	 * Initially the object is created and is considered that it belongs to no transaction until the method
	 * {@link #afterBegin()} is called
	 */
	private boolean isInTransaction = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterBegin() throws RemoteException {
		// mark the instance as part of a transaction
		changeToTransactionMode();
		if (isConnectionPresent()) {
			// check if the connection has a transaction active
			RepositoryConnection repositoryConnection = getConnection();
			try {
				// if connection is not open we cannot start a transaction
				if (repositoryConnection.isOpen()) {
					beginTransaction(repositoryConnection);
					monitor.upgreadConnectionToWrite(System.identityHashCode(repositoryConnection));
				} else {
					// produce connection and start transaction
					getLocalConnectionHolder().set(
							new ConnectionHolder(connectionFactory::produceConnection, securityContext));
				}
			} catch (RepositoryException e) {
				throw new EJBException(e);
			}
		} else {
			// creates new connection and starts a transaction
			getLocalConnectionHolder().set(new ConnectionHolder(connectionFactory::produceConnection, securityContext));
		}
	}

	/**
	 * Begins transaction for the given repository connection if it's not started already
	 *
	 * @param repositoryConnection
	 * 		the connection to start
	 * @throws RepositoryException
	 */
	private static void beginTransaction(RepositoryConnection repositoryConnection) throws RepositoryException {
		// if there is no transaction active activate one
		if (!repositoryConnection.isActive()) {
			repositoryConnection.begin();
		}
	}

	/**
	 * Activate transaction if the current instance is in non transaction mode.
	 */
	private void changeToTransactionMode() {
		if (isConnectionPresent()) {
			ThreadLocal<ConnectionHolder> readOnly = getLocalConnectionHolder();
			ConnectionHolder repositoryConnection = readOnly.get();
			readOnly.set(null);
			// this is set to true here so that the method getLocalConnectionHolder could return the
			// correct instance
			isInTransaction = true;
			getLocalConnectionHolder().set(repositoryConnection);
		}
		isInTransaction = true;
	}

	/**
	 * Checks if is connection present.
	 *
	 * @return true, if is connection present
	 */
	private boolean isConnectionPresent() {
		return getLocalConnectionHolder().get() != null;
	}

	/**
	 * Gets the actual connection.
	 *
	 * @return the connection
	 */
	private RepositoryConnection getConnection() {
		return getConnection(true);
	}

	/**
	 * Gets the actual connection.
	 *
	 * @param createNewIfMissing
	 * 		if should create and return new connection if the current is missing
	 * @return the connection
	 */
	private RepositoryConnection getConnection(boolean createNewIfMissing) {

		ThreadLocal<ConnectionHolder> connectionHolder = getLocalConnectionHolder();
		ConnectionHolder repositoryConnection = connectionHolder.get();
		if (repositoryConnection == null) {
			if (!createNewIfMissing) {
				return null;
			}
			// if there is no connection - produce one and store it for later use
			repositoryConnection = new ConnectionHolder(connectionFactory::produceReadOnlyConnection, securityContext);
			connectionHolder.set(repositoryConnection);
		} else if (!repositoryConnection.isSameTenant(securityContext)) {
			// if not in the same tenant close the old connection and create new one
			disposeConnection(repositoryConnection.connection);
			// create new connection for the current tenant
			return getConnection();
		}
		return repositoryConnection.getConnectionForSecurityContext(securityContext);
	}

	/**
	 * @return thread local object that holds the current connection based on the transaction type.
	 */
	private ThreadLocal<ConnectionHolder> getLocalConnectionHolder() {
		return isInTransaction ? readWriteConnection : readOnlyConnection;
	}

	/**
	 * Close connection if present. If there is a transaction running then the method commit the transaction and dispose
	 * the connection
	 */
	private void closeConnection() {
		if (isConnectionPresent()) {
			RepositoryConnection local = getConnection();
			// flush the data to the semantic database if needed
			disposeConnection(local);
		}
	}

	private void disposeConnection(RepositoryConnection connection) {
		connectionFactory.disposeConnection(connection);
		getLocalConnectionHolder().set(null);
	}

	@Override
	public void beforeCompletion() throws RemoteException {
		closeConnection();
	}

	@Override
	public void afterCompletion(boolean committed) throws RemoteException {

		// false for rollback transaction status
		// the connection check is not to create new connection if already committed
		if (!committed) {
			if (isConnectionPresent()) {
				RepositoryConnection repositoryConnection = getConnection(false);
				try {
					if (repositoryConnection != null) {
						LOGGER.trace("Rolling back tranasction for connection " + System.identityHashCode(
								repositoryConnection));
						repositoryConnection.rollback();
					}
				} catch (RepositoryException e) {
					throw new EJBException(
							"Failed to rollback semantic transaction for connection " + System.identityHashCode(
									repositoryConnection), e);
				} finally {
					if (repositoryConnection != null) {
						disposeConnection(repositoryConnection);
					}
				}
			} else {
				// it will be very bad case when we see this message
				LOGGER.warn(
						"Detected transaction rollback but no repository connection was found. Probably already committed! Check you data for consistency.");
			/*
			 * this can happen in 2 cases: 1. the semantic transaction was committed successfully but something else
			 * caused the rollback - for example other session synchronization object - worst case 2. the semantic
			 * transaction had a problem committing - better case
			 */
			}
		}
	}

	@Override
	public void add(Statement arg0, Resource... arg1) throws RepositoryException {
		try {
			getConnection().add(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(Iterable<? extends Statement> arg0, Resource... arg1) throws RepositoryException {
		try {
			getConnection().add(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> arg0, Resource... arg1)
			throws RepositoryException, E {
		try {
			getConnection().add(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(InputStream arg0, String arg1, RDFFormat arg2, Resource... arg3)
			throws IOException, RDFParseException, RepositoryException {
		try {
			getConnection().add(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(Reader arg0, String arg1, RDFFormat arg2, Resource... arg3)
			throws IOException, RDFParseException, RepositoryException {
		try {
			getConnection().add(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(URL arg0, String arg1, RDFFormat arg2, Resource... arg3)
			throws IOException, RDFParseException, RepositoryException {
		try {
			getConnection().add(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(File arg0, String arg1, RDFFormat arg2, Resource... arg3)
			throws IOException, RDFParseException, RepositoryException {
		try {
			getConnection().add(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void add(Resource arg0, URI arg1, Value arg2, Resource... arg3) throws RepositoryException {
		try {
			getConnection().add(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void begin() throws RepositoryException {
		throw new SemanticPersistenceException("Cannot begin new transaction in the current JPA transaction!");
	}

	@Override
	public void clear(Resource... arg0) throws RepositoryException {
		try {
			getConnection().clear(arg0);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void clearNamespaces() throws RepositoryException {
		try {
			getConnection().clearNamespaces();
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void close() throws RepositoryException {
		try {
			closeConnection();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void commit() throws RepositoryException {
		throw new SemanticPersistenceException("Cannot commit the container managed JPA transaction!");
	}

	@Override
	public void export(RDFHandler arg0, Resource... arg1) throws RepositoryException, RDFHandlerException {
		try {
			getConnection().export(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public void exportStatements(Resource arg0, URI arg1, Value arg2, boolean arg3, RDFHandler arg4, Resource... arg5)
			throws RepositoryException, RDFHandlerException {
		try {
			getConnection().exportStatements(arg0, arg1, arg2, arg3, arg4, arg5);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
		try {
			return getConnection().getContextIDs();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getNamespace(String arg0) throws RepositoryException {
		try {
			return getConnection().getNamespace(arg0);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
		try {
			return getConnection().getNamespaces();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ParserConfig getParserConfig() {
		return getConnection().getParserConfig();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Repository getRepository() {
		return getConnection().getRepository();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Statement> getStatements(Resource arg0, URI arg1, Value arg2, boolean arg3,
			Resource... arg4) throws RepositoryException {
		try {
			return getConnection().getStatements(arg0, arg1, arg2, arg3, arg4);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ValueFactory getValueFactory() {
		return getConnection().getValueFactory();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasStatement(Statement arg0, boolean arg1, Resource... arg2) throws RepositoryException {
		try {
			return getConnection().hasStatement(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasStatement(Resource arg0, URI arg1, Value arg2, boolean arg3, Resource... arg4)
			throws RepositoryException {
		try {
			return getConnection().hasStatement(arg0, arg1, arg2, arg3, arg4);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public boolean isActive() throws RepositoryException {
		try {
			return getConnection().isActive();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see RepositoryConnection#isAutoCommit()
	 * @deprecated by unknown reasons
	 */
	@Override
	@Deprecated
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isAutoCommit() throws RepositoryException {
		return false;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isEmpty() throws RepositoryException {
		try {
			return getConnection().isEmpty();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isOpen() throws RepositoryException {
		try {
			return getConnection().isOpen();
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public BooleanQuery prepareBooleanQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareBooleanQuery(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public BooleanQuery prepareBooleanQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareBooleanQuery(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public GraphQuery prepareGraphQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareGraphQuery(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public GraphQuery prepareGraphQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareGraphQuery(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Query prepareQuery(QueryLanguage arg0, String arg1) throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareQuery(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Query prepareQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareQuery(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TupleQuery prepareTupleQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareTupleQuery(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TupleQuery prepareTupleQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareTupleQuery(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public Update prepareUpdate(QueryLanguage arg0, String arg1) throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareUpdate(arg0, arg1);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public Update prepareUpdate(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		try {
			return getConnection().prepareUpdate(arg0, arg1, arg2);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public void remove(Statement arg0, Resource... arg1) throws RepositoryException {
		try {
			getConnection().remove(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void remove(Iterable<? extends Statement> arg0, Resource... arg1) throws RepositoryException {
		try {
			getConnection().remove(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> arg0, Resource... arg1)
			throws RepositoryException, E {
		try {
			getConnection().remove(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void remove(Resource arg0, URI arg1, Value arg2, Resource... arg3) throws RepositoryException {
		try {
			getConnection().remove(arg0, arg1, arg2, arg3);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void removeNamespace(String arg0) throws RepositoryException {
		try {
			getConnection().removeNamespace(arg0);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void rollback() throws RepositoryException {
		try {
			// skip rollback if the transaction is not active because in this case an exception will be thrown
			if (isConnectionPresent() && isActive()) {
				getConnection().rollback();
			}
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see RepositoryConnection#isAutoCommit()
	 * @deprecated by unknown reasons
	 */
	@Override
	@Deprecated
	public void setAutoCommit(boolean arg0) throws RepositoryException {
		throw new SemanticPersistenceException("Cannot change the auto commit policy for JPA transactions!");
	}

	@Override
	public void setNamespace(String arg0, String arg1) throws RepositoryException {
		try {
			getConnection().setNamespace(arg0, arg1);
		} catch (SecurityException e) {
			rollback();
			throw new UnauthorizedException(e);
		} catch (RepositoryException e) {
			rollback();
			throw e;
		}
	}

	@Override
	public void setParserConfig(ParserConfig arg0) {
		getConnection().setParserConfig(arg0);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public long size(Resource... arg0) throws RepositoryException {
		try {
			return getConnection().size(arg0);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	/**
	 * Wrapper object that stores a {@link RepositoryConnection} instance and the tenant that belongs the connection to
	 *
	 * @author BBonev
	 */
	private static class ConnectionHolder {

		private final RepositoryConnection connection;
		private final String tenant;

		/**
		 * Instantiates a new connection holder.
		 *
		 * @param connection
		 * 		the connection supplier
		 * @param securityContext
		 * 		the current security context
		 */
		ConnectionHolder(Supplier<RepositoryConnection> connection, SecurityContext securityContext) {
			this.connection = connection.get();
			tenant = securityContext.getCurrentTenantId();
		}

		/**
		 * Of the tenant of the stored connection is the same as the given security context then the connection will be
		 * returned otherwise {@link SecurityException} will be thrown.
		 *
		 * @param securityContext
		 * 		the current security context to be used when resolving the connection
		 * @return a connection for the current tenant.
		 */
		RepositoryConnection getConnectionForSecurityContext(SecurityContext securityContext) {
			if (!nullSafeEquals(tenant, securityContext.getCurrentTenantId())) {
				throw new SecurityException("Tried to access connection from tenant " + tenant + " in tenant "
													+ securityContext.getCurrentTenantId());
			}
			return connection;
		}

		boolean isSameTenant(SecurityContext securityContext) {
			return nullSafeEquals(tenant, securityContext.getCurrentTenantId());
		}
	}

	@Override
	public void begin(IsolationLevel isolationLevel) throws RepositoryException {
		try {
			getConnection().begin(isolationLevel);
		} catch (SecurityException e) {
			throw new UnauthorizedException(e);
		}
	}

	@Override
	public IsolationLevel getIsolationLevel() {
		return getConnection().getIsolationLevel();
	}

	@Override
	public void setIsolationLevel(IsolationLevel isolationLevel) {
		getConnection().setIsolationLevel(isolationLevel);
	}
}