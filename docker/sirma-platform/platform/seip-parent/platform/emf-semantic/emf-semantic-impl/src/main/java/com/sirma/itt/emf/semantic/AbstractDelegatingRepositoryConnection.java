package com.sirma.itt.emf.semantic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.Iteration;
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
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;

/**
 * Abstract delegating repository connection. The concrete implementation should provide the actual delegate by
 * implementing the method {@link #getDelegate()}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/03/2018
 */
public abstract class AbstractDelegatingRepositoryConnection implements RepositoryConnection {

	@Override
	public Repository getRepository() {
		return getDelegate().getRepository();
	}

	@Override
	public void setParserConfig(ParserConfig parserConfig) {
		getDelegate().setParserConfig(parserConfig);
	}

	@Override
	public ParserConfig getParserConfig() {
		return getDelegate().getParserConfig();
	}

	@Override
	public ValueFactory getValueFactory() {
		return getDelegate().getValueFactory();
	}

	@Override
	public boolean isOpen() {
		return getDelegate().isOpen();
	}

	@Override
	public Query prepareQuery(String query) {
		return getDelegate().prepareQuery(query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query) {
		return getDelegate().prepareQuery(queryLanguage, query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return getDelegate().prepareQuery(queryLanguage, query, baseURI);
	}

	@Override
	public TupleQuery prepareTupleQuery(String query) {
		return getDelegate().prepareTupleQuery(query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query) {
		return getDelegate().prepareTupleQuery(queryLanguage, query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return getDelegate().prepareTupleQuery(queryLanguage, query, baseURI);
	}

	@Override
	public GraphQuery prepareGraphQuery(String query) {
		return getDelegate().prepareGraphQuery(query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query) {
		return getDelegate().prepareGraphQuery(queryLanguage, query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return getDelegate().prepareGraphQuery(queryLanguage, query, baseURI);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(String query) {
		return getDelegate().prepareBooleanQuery(query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query) {
		return getDelegate().prepareBooleanQuery(queryLanguage, query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		return getDelegate().prepareBooleanQuery(queryLanguage, query, baseURI);
	}

	@Override
	public Update prepareUpdate(String query) {
		return getDelegate().prepareUpdate(query);
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String query) {
		return getDelegate().prepareUpdate(queryLanguage, query);
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String query, String baseURI) {
		return getDelegate().prepareUpdate(queryLanguage, query, baseURI);
	}

	@Override
	public RepositoryResult<Resource> getContextIDs() {
		return getDelegate().getContextIDs();
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource resource, IRI iri, Value value, boolean includeInferred,
			Resource... contexts) {
		return getDelegate().getStatements(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Resource resource, IRI iri, Value value, boolean includeInferred, Resource... contexts) {
		return getDelegate().hasStatement(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Statement statement, boolean includeInferred, Resource... contexts) {
		return getDelegate().hasStatement(statement, includeInferred, contexts);
	}

	@Override
	public void exportStatements(Resource resource, IRI iri, Value value, boolean includeInferred, RDFHandler rdfHandler,
			Resource... contexts) {
		getDelegate().exportStatements(resource, iri, value, includeInferred, rdfHandler, contexts);
	}

	@Override
	public void export(RDFHandler rdfHandler, Resource... contexts) {
		getDelegate().export(rdfHandler, contexts);
	}

	@Override
	public long size(Resource... contexts) {
		return getDelegate().size(contexts);
	}

	@Override
	public boolean isEmpty() {
		return getDelegate().isEmpty();
	}

	@Override
	public void setAutoCommit(boolean b) {
		getDelegate().setAutoCommit(b); // NOSONAR
	}

	@Override
	public boolean isAutoCommit() {
		return getDelegate().isAutoCommit(); // NOSONAR
	}

	@Override
	public boolean isActive() {
		return getDelegate().isActive();
	}

	@Override
	public void setIsolationLevel(IsolationLevel isolationLevel) {
		getDelegate().setIsolationLevel(isolationLevel);
	}

	@Override
	public IsolationLevel getIsolationLevel() {
		return getDelegate().getIsolationLevel();
	}

	@Override
	public void begin() {
		getDelegate().begin();
	}

	@Override
	public void begin(IsolationLevel isolationLevel) {
		getDelegate().begin(isolationLevel);
	}

	@Override
	public void commit() {
		getDelegate().commit();
	}

	@Override
	public void rollback() {
		getDelegate().rollback();
	}

	@Override
	public void add(InputStream inputStream, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		getDelegate().add(inputStream, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(Reader reader, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		getDelegate().add(reader, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(URL url, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		getDelegate().add(url, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(File file, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		getDelegate().add(file, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(Resource resource, IRI iri, Value value, Resource... contexts) {
		getDelegate().add(resource, iri, value, contexts);
	}

	@Override
	public void add(Statement statement, Resource... contexts) {
		getDelegate().add(statement, contexts);
	}

	@Override
	public void add(Iterable<? extends Statement> iterable, Resource... contexts) {
		getDelegate().add(iterable, contexts);
	}

	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> iteration, Resource... contexts) throws E {
		getDelegate().add(iteration, contexts);
	}

	@Override
	public void remove(Resource resource, IRI iri, Value value, Resource... contexts) {
		getDelegate().remove(resource, iri, value, contexts);
	}

	@Override
	public void remove(Statement statement, Resource... contexts) {
		getDelegate().remove(statement, contexts);
	}

	@Override
	public void remove(Iterable<? extends Statement> iterable, Resource... contexts) {
		getDelegate().remove(iterable, contexts);
	}

	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> iteration, Resource... contexts) throws
			E {
		getDelegate().remove(iteration, contexts);
	}

	@Override
	public void clear(Resource... contexts) {
		getDelegate().clear(contexts);
	}

	@Override
	public RepositoryResult<Namespace> getNamespaces() {
		return getDelegate().getNamespaces();
	}

	@Override
	public String getNamespace(String prefix) {
		return getDelegate().getNamespace(prefix);
	}

	@Override
	public void setNamespace(String prefix, String name) {
		getDelegate().setNamespace(prefix, name);
	}

	@Override
	public void removeNamespace(String prefix) {
		getDelegate().removeNamespace(prefix);
	}

	@Override
	public void clearNamespaces() {
		getDelegate().clearNamespaces();
	}

	protected abstract RepositoryConnection getDelegate();
}
