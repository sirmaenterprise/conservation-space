package com.sirma.itt.emf.semantic.debug;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.base.RepositoryWrapper;

/**
 * Repository wrapper that produce monitored connections
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 06/03/2019
 */
public class MonitoredRepository extends RepositoryWrapper {

	private final ConnectionMonitor connectionMonitor;

	/**
	 * Initializes the instance and delegate
	 *
	 * @param repository repository instance
	 * @param connectionMonitor the collector of the monitored connections
	 */
	public MonitoredRepository(Repository repository, ConnectionMonitor connectionMonitor) {
		super(repository);
		this.connectionMonitor = connectionMonitor;
	}

	@Override
	public RepositoryConnection getConnection() {
		RepositoryConnection connection = super.getConnection();
		return connectionMonitor.monitor(connection);
	}
}
