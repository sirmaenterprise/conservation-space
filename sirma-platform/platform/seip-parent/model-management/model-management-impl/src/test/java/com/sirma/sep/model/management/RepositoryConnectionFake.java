package com.sirma.sep.model.management;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;

/**
 * Fake implementation of {@link RepositoryConnection} that is able to store statements via
 * <ul>
 * <li>{@link #add(Statement, Resource...)}
 * <li>{@link #add(Iterable, Resource...)}
 * <li>{@link #add(Resource, IRI, Value, Resource...)}
 * </ul>
 * and retrieve statements via the
 * <ul>
 * <li>{@link #getStatements(Resource, IRI, Value, boolean, Resource...)}
 * </ul>
 * and check for presence via
 * <ul>
 * <li>{@link #hasStatement(Statement, boolean, Resource...)}
 * <li>{@link #hasStatement(Resource, IRI, Value, boolean, Resource...)}
 * </ul>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/08/2018
 */
public class RepositoryConnectionFake implements RepositoryConnection {

	private Map<Resource, List<Statement>> storage = new LinkedHashMap<>();
	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	@Override
	public Repository getRepository() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setParserConfig(ParserConfig config) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ParserConfig getParserConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	@Override
	public boolean isOpen() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query) throws RepositoryException, MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query prepareQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TupleQuery prepareTupleQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public GraphQuery prepareGraphQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query, String baseURI) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update) throws RepositoryException, MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Update prepareUpdate(QueryLanguage ql, String update, String baseURI) throws RepositoryException,
			MalformedQueryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RepositoryResult<Resource> getContextIDs() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RepositoryResult<Statement> getStatements(Resource subj, IRI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws RepositoryException {
		Predicate<Statement> predicateFilter = statement -> true;
		Predicate<Statement> objectFilter = statement -> true;
		if (pred != null) {
			predicateFilter = statement -> statement.getPredicate().equals(pred);
		}
		if (obj != null) {
			objectFilter = statement -> statement.getObject().equals(obj);
		}

		List<Statement> foundStatements = selectBucket(subj).stream()
				.filter(predicateFilter.and(objectFilter))
				.collect(Collectors.toList());
		return new RepositoryResult<>(new StatementsIterator(foundStatements));
	}

	@Override
	public boolean hasStatement(Resource subj, IRI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws RepositoryException {
		return getStatements(subj, pred, obj).hasNext();
	}

	@Override
	public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts) throws
			RepositoryException {
		return selectBucket(st.getSubject()).contains(st);
	}

	@Override
	public void exportStatements(Resource subj, IRI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts) throws RepositoryException, RDFHandlerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void export(RDFHandler handler, Resource... contexts) throws RepositoryException, RDFHandlerException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long size(Resource... contexts) throws RepositoryException {
		return storage.values().stream().mapToLong(List::size).sum();
	}

	@Override
	public boolean isEmpty() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAutoCommit() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIsolationLevel(IsolationLevel level) throws IllegalStateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IsolationLevel getIsolationLevel() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void begin() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void begin(IsolationLevel level) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void commit() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void rollback() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(InputStream in, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException,
			RDFParseException, RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Reader reader, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException,
			RDFParseException, RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(URL url, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException,
			RDFParseException, RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(File file, String baseURI, RDFFormat dataFormat, Resource... contexts) throws IOException,
			RDFParseException, RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
		selectBucket(subject).add(valueFactory.createStatement(subject, predicate, object));
	}

	private List<Statement> selectBucket(Resource subject) {
		return storage.computeIfAbsent(subject, s -> new LinkedList<>());
	}

	@Override
	public void add(Statement st, Resource... contexts) throws RepositoryException {
		selectBucket(st.getSubject()).add(st);
	}

	@Override
	public void add(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
		statements.forEach(this::add);
	}

	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> statements, Resource... contexts) throws
			RepositoryException, E {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Resource subject, IRI predicate, Value object, Resource... contexts) throws RepositoryException {
		selectBucket(subject).remove(valueFactory.createStatement(subject, predicate, object));
	}

	@Override
	public void remove(Statement st, Resource... contexts) throws RepositoryException {
		selectBucket(st.getSubject()).remove(st);
	}

	@Override
	public void remove(Iterable<? extends Statement> statements, Resource... contexts) throws RepositoryException {
		statements.forEach(st -> selectBucket(st.getSubject()).remove(st));
	}

	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> statements, Resource... contexts) throws
			RepositoryException, E {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear(Resource... contexts) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public RepositoryResult<Namespace> getNamespaces() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNamespace(String prefix) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNamespace(String prefix, String name) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeNamespace(String prefix) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearNamespaces() throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	private static class StatementsIterator implements CloseableIteration<Statement, RepositoryException> {

		private final Iterator<Statement> statements;

		private StatementsIterator(Iterable<Statement> statements) {this.statements = statements.iterator();}

		@Override
		public void close() throws RepositoryException {

		}

		@Override
		public boolean hasNext() throws RepositoryException {
			return statements.hasNext();
		}

		@Override
		public Statement next() throws RepositoryException {
			return statements.next();
		}

		@Override
		public void remove() throws RepositoryException {
			throw new UnsupportedOperationException();
		}
	}
}
