package com.sirma.itt.semantic;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

/**
 * Interface of the connection pool to the Ontology repository
 *
 * @author Valeri Tishev
 */
public interface ConnectionFactory {

	/**
	 * Retrieve a connection to the ontology repository and begin a transaction for it. A connection is available only
	 * for the current request.
	 *
	 * @return Active connection to the repository
	 */
	RepositoryConnection produceConnection();

	/**
	 * Retrieve a read only connection to the ontology repository. A connection is available only for the current
	 * request.
	 *
	 * @return the repository connection
	 */
	RepositoryConnection produceReadOnlyConnection();

	/**
	 * Called when a connection should be released. Releases the connection in the pool.
	 *
	 * @param connection
	 *            connection that is released.
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
