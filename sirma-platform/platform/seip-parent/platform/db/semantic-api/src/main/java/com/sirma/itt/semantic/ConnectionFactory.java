package com.sirma.itt.semantic;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Produces managed, read only and user managed connections to Ontology repository.<br>
 * Managed connections could be produced only in active transaction context and are committed and closed at the end
 * of the transaction.<br>
 * When data modifications will not be performed by a connection as performance improvement the read only connections
 * could be used as the transaction management overhead is omitted. These connections does not need to be closed and do
 * not participate in transaction. The produced read only connections are tenant aware and could be cached without issues
 *
 * @author Valeri Tishev
 * @author BBonev
 */
public interface ConnectionFactory {

	/**
	 * Retrieve a connection to the ontology repository for the current tenant. The connection is not automatically
	 * managed and should be disposed by calling the method {@link #disposeConnection(RepositoryConnection)}. This
	 * method cannot be called during active transaction. The returned connection is with active transaction
	 *
	 * @return Active connection to the repository
	 * @throws IllegalStateException if there is active transaction
	 */
	RepositoryConnection produceConnection();

	/**
	 * Retrieve application managed connection to the ontology repository. The connection is bound to the
	 * current transaction and will be automatically committed or rollbacked at the end of the JTA transaction.
	 * <p>
	 * If there is not active transaction when this connection is used that behave as connection produced by
	 * {@link #produceReadOnlyConnection()}<br>
	 * This is the default connection type that will be used when requested by CDI injection.
	 * <br>It should be noted that if this connection is kept too long open it may cause the repository to timeout it's
	 * transaction and this will cause the local transaction to fail on commit.
	 * </p>
	 *
	 * @return Active connection to the repository
	 */
	RepositoryConnection produceManagedConnection();

	/**
	 * Retrieve a read only connection to the ontology repository.
	 * <p>There is no need to close these connections as that do not have constantly open connection to the repository but
	 * rather open one/use from a pool when needed. This does not mean that the result iterators returned from the query
	 * methods should not be closed as they keep the connection open.</p>
	 *
	 * @return a read only repository connection
	 */
	RepositoryConnection produceReadOnlyConnection();

	/**
	 * Called when a connection should be released. Releases a connection produced by the method {@link #produceConnection()}
	 *
	 * @param connection connection that is released.
	 */
	void disposeConnection(RepositoryConnection connection);

	/**
	 * Returns an instance of ValueFactory for creating Literals and URIs
	 *
	 * @return ValueFactory instance
	 */
	ValueFactory produceValueFactory();

	/**
	 * Tear down method for closing connections to the repository <br/>
	 */
	void tearDown();
}
