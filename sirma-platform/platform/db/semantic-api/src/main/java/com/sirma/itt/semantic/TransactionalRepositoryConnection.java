package com.sirma.itt.semantic;

import javax.ejb.SessionSynchronization;

import org.openrdf.repository.RepositoryConnection;

/**
 * Extension for the semantic {@link RepositoryConnection} to add a transaction support. Also adds a auto close option.
 *
 * @author BBonev
 */
public interface TransactionalRepositoryConnection extends SessionSynchronization, RepositoryConnection, AutoCloseable {

}
