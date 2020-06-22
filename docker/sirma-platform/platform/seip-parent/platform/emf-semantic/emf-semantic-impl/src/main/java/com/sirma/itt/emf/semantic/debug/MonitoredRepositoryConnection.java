package com.sirma.itt.emf.semantic.debug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;

import com.sirma.itt.emf.semantic.AbstractDelegatingRepositoryConnection;

/**
 * Repository connection that collects statistical information about how a connection is used.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 06/03/2019
 */
public class MonitoredRepositoryConnection extends AbstractDelegatingRepositoryConnection {

	private final RepositoryConnection delegate;
	private final ConnectionMonitor connectionMonitor;

	public MonitoredRepositoryConnection(RepositoryConnection delegate, ConnectionMonitor connectionMonitor) {
		this.delegate = delegate;
		this.connectionMonitor = connectionMonitor;
		connectionMonitor.onCreate();
	}

	@Override
	protected RepositoryConnection getDelegate() {
		return delegate;
	}

	@Override
	public Query prepareQuery(String query) {
		connectionMonitor.onRead();
		return super.prepareQuery(query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query) {
		connectionMonitor.onRead();
		return super.prepareQuery(queryLanguage, query);
	}

	@Override
	public Query prepareQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		connectionMonitor.onRead();
		return super.prepareQuery(queryLanguage, query, baseURI);
	}

	@Override
	public TupleQuery prepareTupleQuery(String query) {
		connectionMonitor.onRead();
		return super.prepareTupleQuery(query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query) {
		connectionMonitor.onRead();
		return super.prepareTupleQuery(queryLanguage, query);
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		connectionMonitor.onRead();
		return super.prepareTupleQuery(queryLanguage, query, baseURI);
	}

	@Override
	public GraphQuery prepareGraphQuery(String query) {
		connectionMonitor.onRead();
		return super.prepareGraphQuery(query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query) {
		connectionMonitor.onRead();
		return super.prepareGraphQuery(queryLanguage, query);
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		connectionMonitor.onRead();
		return super.prepareGraphQuery(queryLanguage, query, baseURI);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(String query) {
		connectionMonitor.onRead();
		return super.prepareBooleanQuery(query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query) {
		connectionMonitor.onRead();
		return super.prepareBooleanQuery(queryLanguage, query);
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage queryLanguage, String query, String baseURI) {
		connectionMonitor.onRead();
		return super.prepareBooleanQuery(queryLanguage, query, baseURI);
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource resource, IRI iri, Value value, boolean includeInferred,
			Resource... contexts) {
		connectionMonitor.onRead();
		return super.getStatements(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Resource resource, IRI iri, Value value, boolean includeInferred,
			Resource... contexts) {
		connectionMonitor.onRead();
		return super.hasStatement(resource, iri, value, includeInferred, contexts);
	}

	@Override
	public boolean hasStatement(Statement statement, boolean includeInferred, Resource... contexts) {
		connectionMonitor.onRead();
		return super.hasStatement(statement, includeInferred, contexts);
	}

	@Override
	public Update prepareUpdate(String query) {
		connectionMonitor.onUpdate();
		return super.prepareUpdate(query);
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String query) {
		connectionMonitor.onUpdate();
		return super.prepareUpdate(queryLanguage, query);
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String query, String baseURI) {
		connectionMonitor.onUpdate();
		return super.prepareUpdate(queryLanguage, query, baseURI);
	}

	@Override
	public void add(InputStream inputStream, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws
			IOException {
		connectionMonitor.onUpdate();
		super.add(inputStream, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(Reader reader, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		connectionMonitor.onUpdate();
		super.add(reader, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(URL url, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		connectionMonitor.onUpdate();
		super.add(url, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(File file, String baseURI, RDFFormat rdfFormat, Resource... contexts) throws IOException {
		connectionMonitor.onUpdate();
		super.add(file, baseURI, rdfFormat, contexts);
	}

	@Override
	public void add(Resource resource, IRI iri, Value value, Resource... contexts) {
		connectionMonitor.onUpdate();
		super.add(resource, iri, value, contexts);
	}

	@Override
	public void add(Statement statement, Resource... contexts) {
		connectionMonitor.onUpdate();
		super.add(statement, contexts);
	}

	@Override
	public void add(Iterable<? extends Statement> iterable, Resource... contexts) {
		connectionMonitor.onUpdate();
		super.add(iterable, contexts);
	}

	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> iteration, Resource... contexts) throws E {
		connectionMonitor.onUpdate();
		super.add(iteration, contexts);
	}

	@Override
	public void remove(Resource resource, IRI iri, Value value, Resource... contexts) {
		connectionMonitor.onRemove();
		super.remove(resource, iri, value, contexts);
	}

	@Override
	public void remove(Statement statement, Resource... contexts) {
		connectionMonitor.onRemove();
		super.remove(statement, contexts);
	}

	@Override
	public void remove(Iterable<? extends Statement> iterable, Resource... contexts) {
		connectionMonitor.onRemove();
		super.remove(iterable, contexts);
	}

	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> iteration, Resource... contexts) throws
			E {
		connectionMonitor.onRemove();
		super.remove(iteration, contexts);
	}

	@Override
	public void clear(Resource... contexts) {
		connectionMonitor.onRemove();
		super.clear(contexts);
	}
}
