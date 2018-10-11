package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryWrapper;

/**
 * Repository wrapper class creating connections with logic for debug logging operations
 * 
 * @author kirq4e
 */
public class DebugRepository extends RepositoryWrapper {

	/**
	 * Initializes the instance and delegate
	 * 
	 * @param repository
	 *            repository intance
	 */
	public DebugRepository(Repository repository) {
		super(repository);
	}

	@Override
	public RepositoryConnection getConnection() throws RepositoryException {
		RepositoryConnection connection = super.getConnection();
		return new DebugRepositoryConnection(getDelegate(), connection);
	}
}
