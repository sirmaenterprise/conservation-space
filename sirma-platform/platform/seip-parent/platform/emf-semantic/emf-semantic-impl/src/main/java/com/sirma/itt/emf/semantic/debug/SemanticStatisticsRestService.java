package com.sirma.itt.emf.semantic.debug;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint to provide fast access to semantic statistics
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 06/03/2019
 */
@ApplicationScoped
@Path("/semantic")
@Produces(MediaType.APPLICATION_JSON)
public class SemanticStatisticsRestService {

	@Inject
	private ConnectionMonitor connectionMonitor;

	/**
	 * Gets the last known statistics  data
	 *
	 * @return the statistics data
	 */
	@GET
	@Path("/stats")
	public Collection<ConnectionStats> getStatistics() {
		return connectionMonitor.getSnapshot();
	}
}
