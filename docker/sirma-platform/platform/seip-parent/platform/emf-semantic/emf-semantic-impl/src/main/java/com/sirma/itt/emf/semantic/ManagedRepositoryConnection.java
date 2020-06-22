package com.sirma.itt.emf.semantic;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFHandler;

/**
 * Represents a proxy for managed repository connections. These connections participate in two phase commit and are
 * managed by the current active transaction and are committed or rollbacked when the transaction is committed or
 * rollbacked. <br>
 * If there is not active transaction when this connection is used that behave as {@link ReadOnlyRepositoryConnection}s
 * <br>This is the default connection type that will be used when requested by CDI injection.
 * <br>It should be noted that if this connection is kept too long open it may cause the repository to timeout it's
 * transaction and this will cause the local transaction to fail on commit.
 *
 * @see ReadOnlyRepositoryConnection
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/03/2018
 */
class ManagedRepositoryConnection extends AbstractDelegatingRepositoryConnection {

	// keep a single readonly connection per injection point. It's used when there is no active transaction
	// this depends on the fact that the produced connection is tenant aware
	private final RepositoryConnection readOnlyConnection;
	private final TransactionCoordinator transactionCoordinator;

	ManagedRepositoryConnection(TransactionCoordinator transactionCoordinator, RepositoryConnection readOnlyConnection) {
		this.readOnlyConnection = readOnlyConnection;
		this.transactionCoordinator = transactionCoordinator;
	}

	@Override
	protected RepositoryConnection getDelegate() {
		return transactionCoordinator.getConnection();
	}

	@Override
	public void commit() {
		throw new IllegalStateException("Cannot manually commit managed connection");
	}

	@Override
	public void begin() {
		throw new IllegalStateException("Cannot manually begin transaction for managed connection");
	}

	@Override
	public void begin(IsolationLevel isolationLevel) {
		throw new IllegalStateException("Cannot manually begin transaction for managed connection");
	}

	@Override
	public void close() {
		throw new IllegalStateException("Cannot manually close managed connection");
	}

	@Override
	public boolean isAutoCommit() {
		return false;
	}

	@Override
	public void setAutoCommit(boolean b) {
		throw new IllegalStateException("Cannot change the auto commit mode for managed connection");
	}

	@Override
	public void rollback() {
		throw new IllegalStateException("Cannot manually rollback managed connection");
	}

	@Override
	public void setIsolationLevel(IsolationLevel isolationLevel) {
		throw new IllegalStateException("Cannot change the isolation level for managed connection");
	}

	// read only methods will use only the read only connection as optimization
	// when no write connection is needed then none will be created

	@Override
	public Repository getRepository() {
		return readOnlyConnection.getRepository();
	}

	@Override
	public void setParserConfig(ParserConfig parserConfig) {
		readOnlyConnection.setParserConfig(parserConfig);
	}

	@Override
	public ParserConfig getParserConfig() {
		return readOnlyConnection.getParserConfig();
	}

	@Override
	public ValueFactory getValueFactory() {
		return readOnlyConnection.getValueFactory();
	}

	@Override
	public Query prepareQuery(String query) {
		return readOnlyConnection.prepareQuery(query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query) {
		return readOnlyConnection.prepareQuery(queryLanguage, query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return readOnlyConnection.prepareQuery(queryLanguage, query, baseURI);
	}

	@Override
	public TupleQuery prepareTupleQuery(String query) {
		return readOnlyConnection.prepareTupleQuery(query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query) {
		return readOnlyConnection.prepareTupleQuery(queryLanguage, query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return readOnlyConnection.prepareTupleQuery(queryLanguage, query, baseURI);
	}

	@Override
	public GraphQuery prepareGraphQuery(String query) {
		return readOnlyConnection.prepareGraphQuery(query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query) {
		return readOnlyConnection.prepareGraphQuery(queryLanguage, query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return readOnlyConnection.prepareGraphQuery(queryLanguage, query, baseURI);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(String query) {
		return readOnlyConnection.prepareBooleanQuery(query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query) {
		return readOnlyConnection.prepareBooleanQuery(queryLanguage, query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return readOnlyConnection.prepareBooleanQuery(queryLanguage, query, baseURI);
	}

	@Override
	public RepositoryResult<Resource> getContextIDs() {
		return readOnlyConnection.getContextIDs();
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource resource, IRI iri, Value value, boolean includeInferred,
			Resource... contexts) {
		return readOnlyConnection.getStatements(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Resource resource, IRI iri, Value value, boolean includeInferred, Resource... contexts) {
		return readOnlyConnection.hasStatement(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Statement statement, boolean includeInferred, Resource... contexts) {
		return readOnlyConnection.hasStatement(statement, includeInferred, contexts);
	}

	@Override
	public void exportStatements(Resource resource, IRI iri, Value value, boolean includeInferred, RDFHandler rdfHandler,
			Resource... contexts) {
		readOnlyConnection.exportStatements(resource, iri, value, includeInferred, rdfHandler, contexts);
	}

	@Override
	public void export(RDFHandler rdfHandler, Resource... contexts) {
		readOnlyConnection.export(rdfHandler, contexts);
	}

	@Override
	public long size(Resource... contexts) {
		return readOnlyConnection.size(contexts);
	}

	@Override
	public RepositoryResult<Namespace> getNamespaces() {
		return readOnlyConnection.getNamespaces();
	}

	@Override
	public String getNamespace(String prefix) {
		return readOnlyConnection.getNamespace(prefix);
	}
}
