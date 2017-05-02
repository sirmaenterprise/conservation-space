package com.sirma.itt.emf.semantic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;

/**
 * Rest service to provide access to the repository connection monitoring data
 *
 * @author BBonev
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/admin/connections")
@ApplicationScoped
public class RepositoryConnectionMonitorRest {

	@Inject
	private RepositoryConnectionMonitor monitor;

	/**
	 * Read the connections status
	 *
	 * @return status as JSON
	 */
	@GET
	@PublicResource
	public String getConnectionInfo() {
		return monitor.getInfo().toString();
	}

}
