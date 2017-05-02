package com.sirma.itt.seip.rest.metrics;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.metrics.PingResponse.PingStatus;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Responsible for providing information about the system's health and
 * availability.
 *
 * @author yasko
 */
@Singleton
@Path(PingRestService.PATH)
@Produces(Versions.V2_JSON)
public class PingRestService {
	public static final String PATH = "/ping";

	/**
	 * Ping the system.
	 *
	 * @return {@link PingResponse} containing information about the system's state.
	 */
	@GET
	@PublicResource
	public PingResponse ping() {
		return PingResponse.fromStatus(PingStatus.OK);
	}
}
