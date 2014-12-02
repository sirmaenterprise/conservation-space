package com.sirma.itt.emf.semantic;

import info.aduna.iteration.Iteration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
import org.openrdf.repository.UnknownTransactionStateException;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * Session bean that acts as a proxy to the actual {@link RepositoryConnection}. The connection is
 * initialized on transaction start and is destroyed on transaction end. The connection is stored in
 * the thread local variable when span between multiple services is required in a single
 * transaction.
 * <p>
 * <b>NOTE: </b> The implementation supports read only connections but the {@link #close()} method
 * should be called at the end of the method that uses it
 * 
 * @author BBonev
 */
@Stateful
@StatefulTimeout(-1)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TransactionalRepositoryConnectionImpl implements TransactionalRepositoryConnection {

	/**
	 * The connection factory used for producing the connections. This is injected as instance for
	 * optimization. When new instances are created for multiple services in the same transaction no
	 * need to reference new factory bean instance when is not needed.
	 */
	@Inject
	private Instance<ConnectionFactory> connectionFactory;

	/** A cache for the actual repository connection for the current thread. */
	private static ThreadLocal<RepositoryConnection> connection = new ThreadLocal<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterBegin() throws EJBException, RemoteException {
		if (isConnectionPresent()) {
			// check if the connection has a transaction active
			RepositoryConnection repositoryConnection = getConnection();
			try {
				// if connection is not open we cannot start a transaction
				if (repositoryConnection.isOpen()) {
					// if there is no transaction active activate one
					if (!repositoryConnection.isActive()) {
						repositoryConnection.begin();
					}
				} else {
					// produce connection and start transaction
					connection.set(connectionFactory.get().produceConnection());
				}
			} catch (UnknownTransactionStateException e) {
				throw new EJBException(e);
			} catch (RepositoryException e) {
				throw new EJBException(e);
			}
		} else {
			// creates new connection and starts a transaction
			connection.set(connectionFactory.get().produceConnection());
		}
	}

	/**
	 * Checks if is connection present.
	 * 
	 * @return true, if is connection present
	 */
	private boolean isConnectionPresent() {
		return connection.get() != null;
	}

	/**
	 * Gets the actual connection.
	 *
	 * @return the connection
	 */
	private RepositoryConnection getConnection() {
		RepositoryConnection repositoryConnection = connection.get();
		if (repositoryConnection == null) {
			// if there is no connection - produce one and store it for later use
			repositoryConnection = connectionFactory.get().produceReadOnlyConnection();
			connection.set(repositoryConnection);
		}
		return repositoryConnection;
	}

	/**
	 * Close connection if present. If there is a transaction running then the method commit the
	 * transaction and dispose the connection
	 */
	private void closeConnection() {
		if (isConnectionPresent()) {
			RepositoryConnection local = getConnection();
			// flush the data to the semantic database if needed
			connectionFactory.get().disposeConnection(local);
			connection.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterCompletion(boolean arg0) throws EJBException, RemoteException {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeCompletion() throws EJBException, RemoteException {
		closeConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Statement arg0, Resource... arg1) throws RepositoryException {
		getConnection().add(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Iterable<? extends Statement> arg0, Resource... arg1)
			throws RepositoryException {
		getConnection().add(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> arg0, Resource... arg1)
			throws RepositoryException, E {
		getConnection().add(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(InputStream arg0, String arg1, RDFFormat arg2, Resource... arg3)
			throws IOException, RDFParseException, RepositoryException {
		getConnection().add(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Reader arg0, String arg1, RDFFormat arg2, Resource... arg3) throws IOException,
			RDFParseException, RepositoryException {
		getConnection().add(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(URL arg0, String arg1, RDFFormat arg2, Resource... arg3) throws IOException,
			RDFParseException, RepositoryException {
		getConnection().add(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(File arg0, String arg1, RDFFormat arg2, Resource... arg3) throws IOException,
			RDFParseException, RepositoryException {
		getConnection().add(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Resource arg0, URI arg1, Value arg2, Resource... arg3)
			throws RepositoryException {
		getConnection().add(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void begin() throws RepositoryException {
		throw new SemanticPersistenceException(
				"Cannot begin new transaction in the current JPA transaction!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(Resource... arg0) throws RepositoryException {
		getConnection().clear(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearNamespaces() throws RepositoryException {
		getConnection().clearNamespaces();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void close() throws RepositoryException {
		closeConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void commit() throws RepositoryException {
		throw new SemanticPersistenceException(
				"Cannot commit the container managed JPA transaction!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void export(RDFHandler arg0, Resource... arg1) throws RepositoryException,
			RDFHandlerException {
		getConnection().export(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportStatements(Resource arg0, URI arg1, Value arg2, boolean arg3,
			RDFHandler arg4, Resource... arg5) throws RepositoryException, RDFHandlerException {
		getConnection().exportStatements(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
		return getConnection().getContextIDs();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String getNamespace(String arg0) throws RepositoryException {
		return getConnection().getNamespace(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
		return getConnection().getNamespaces();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ParserConfig getParserConfig() {
		return getConnection().getParserConfig();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Repository getRepository() {
		return getConnection().getRepository();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public RepositoryResult<Statement> getStatements(Resource arg0, URI arg1, Value arg2,
			boolean arg3, Resource... arg4) throws RepositoryException {
		return getConnection().getStatements(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public ValueFactory getValueFactory() {
		return getConnection().getValueFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasStatement(Statement arg0, boolean arg1, Resource... arg2)
			throws RepositoryException {
		return getConnection().hasStatement(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasStatement(Resource arg0, URI arg1, Value arg2, boolean arg3, Resource... arg4)
			throws RepositoryException {
		return getConnection().hasStatement(arg0, arg1, arg2, arg3, arg4);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() throws UnknownTransactionStateException, RepositoryException {
		return getConnection().isActive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isAutoCommit() throws RepositoryException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isEmpty() throws RepositoryException {
		return getConnection().isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isOpen() throws RepositoryException {
		return getConnection().isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public BooleanQuery prepareBooleanQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareBooleanQuery(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public BooleanQuery prepareBooleanQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareBooleanQuery(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public GraphQuery prepareGraphQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareGraphQuery(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public GraphQuery prepareGraphQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareGraphQuery(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Query prepareQuery(QueryLanguage arg0, String arg1) throws RepositoryException,
			MalformedQueryException {
		return getConnection().prepareQuery(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Query prepareQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareQuery(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TupleQuery prepareTupleQuery(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareTupleQuery(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public TupleQuery prepareTupleQuery(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareTupleQuery(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Update prepareUpdate(QueryLanguage arg0, String arg1) throws RepositoryException,
			MalformedQueryException {
		return getConnection().prepareUpdate(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Update prepareUpdate(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		return getConnection().prepareUpdate(arg0, arg1, arg2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Statement arg0, Resource... arg1) throws RepositoryException {
		getConnection().remove(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Iterable<? extends Statement> arg0, Resource... arg1)
			throws RepositoryException {
		getConnection().remove(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> arg0,
			Resource... arg1) throws RepositoryException, E {
		getConnection().remove(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Resource arg0, URI arg1, Value arg2, Resource... arg3)
			throws RepositoryException {
		getConnection().remove(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeNamespace(String arg0) throws RepositoryException {
		getConnection().removeNamespace(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rollback() throws RepositoryException {
		getConnection().rollback();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public void setAutoCommit(boolean arg0) throws RepositoryException {
		throw new SemanticPersistenceException(
				"Cannot change the auto commit policy for JPA transactions!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNamespace(String arg0, String arg1) throws RepositoryException {
		getConnection().setNamespace(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParserConfig(ParserConfig arg0) {
		getConnection().setParserConfig(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public long size(Resource... arg0) throws RepositoryException {
		return getConnection().size(arg0);
	}

}