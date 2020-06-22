package com.sirma.itt.emf.semantic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * Repository connection that allows only read operations to the underlying Ontology repository. Any use of any data
 * modifying method will result in {@link IllegalStateException}.<br>
 * If the application needs such connection it could be requested via injection point and with additional
 * qualifier {@link com.sirma.itt.semantic.ReadOnly}.
 *
 * <pre>
 *     <code>
 *  &#64;Inject
 *         &#64;ReadOnly
 *         private RepositoryConnection readOnlyConnection;
 *     </code>
 * </pre>
 * There is no need to close these connections as that do not have constantly open connection to the repository but
 * rather open one/use from a pool when needed. This does not mean that the result iterators returned from the query
 * methods should not be closed as they keep the connection open.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/03/2018
 */
class ReadOnlyRepositoryConnection extends AbstractDelegatingRepositoryConnection {

	private static final String CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION = "Cannot remove data via read only connection";
	private static final String CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION = "Cannot add data via read only connection";
	private static final String CANNOT_CHANGE_NAMESPACES_VIA_READ_ONLY_CONNECTION = "Cannot change namespaces via read only connection";

	private final Supplier<RepositoryConnection> connectionSupplier;

	ReadOnlyRepositoryConnection(Supplier<RepositoryConnection> connectionSupplier) {
		this.connectionSupplier = Objects.requireNonNull(connectionSupplier);
	}

	@Override
	protected RepositoryConnection getDelegate() {
		return connectionSupplier.get();
	}

	@Override
	public void close() {
		// do nothing on close as these connections should not be closed
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String s) {
		throw new IllegalStateException("Cannot modify data via read only connection");
	}

	@Override
	public Update prepareUpdate(QueryLanguage queryLanguage, String s, String s1) {
		throw new IllegalStateException("Cannot modify data via read only connection");
	}

	@Override
	public void setAutoCommit(boolean b) {
		throw new IllegalStateException("Cannot change auto commit mode for read only connection");
	}

	@Override
	public void setIsolationLevel(IsolationLevel isolationLevel) {
		throw new IllegalStateException("Cannot change the isolation level for read only connection");
	}

	@Override
	public void begin() {
		throw new IllegalStateException("Cannot begin transaction on read only connection");
	}

	@Override
	public void begin(IsolationLevel isolationLevel) {
		throw new IllegalStateException("Cannot begin transaction on read only connection");
	}

	@Override
	public void commit() {
		throw new IllegalStateException("Cannot commit read only connection");
	}

	@Override
	public void rollback() {
		throw new IllegalStateException("Cannot rollback read only connection");
	}

	@Override
	public void add(InputStream inputStream, String s, RDFFormat rdfFormat, Resource... resources) throws IOException {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(Reader reader, String s, RDFFormat rdfFormat, Resource... resources) throws IOException {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(URL url, String s, RDFFormat rdfFormat, Resource... resources) throws IOException {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(File file, String s, RDFFormat rdfFormat, Resource... resources) throws IOException {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(Resource resource, IRI iri, Value value, Resource... resources) {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(Statement statement, Resource... resources) {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void add(Iterable<? extends Statement> iterable, Resource... resources) {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public <E extends Exception> void add(Iteration<? extends Statement, E> iteration, Resource... resources) throws E {
		throw new IllegalStateException(CANNOT_ADD_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void remove(Resource resource, IRI iri, Value value, Resource... resources) {
		throw new IllegalStateException(CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void remove(Statement statement, Resource... resources) {
		throw new IllegalStateException(CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void remove(Iterable<? extends Statement> iterable, Resource... resources) {
		throw new IllegalStateException(CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public <E extends Exception> void remove(Iteration<? extends Statement, E> iteration, Resource... resources) throws
			E {
		throw new IllegalStateException(CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void clear(Resource... resources) {
		throw new IllegalStateException(CANNOT_REMOVE_DATA_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void setNamespace(String s, String s1) {
		throw new IllegalStateException(CANNOT_CHANGE_NAMESPACES_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void removeNamespace(String s) {
		throw new IllegalStateException(CANNOT_CHANGE_NAMESPACES_VIA_READ_ONLY_CONNECTION);
	}

	@Override
	public void clearNamespaces() {
		throw new IllegalStateException(CANNOT_CHANGE_NAMESPACES_VIA_READ_ONLY_CONNECTION);
	}
}
