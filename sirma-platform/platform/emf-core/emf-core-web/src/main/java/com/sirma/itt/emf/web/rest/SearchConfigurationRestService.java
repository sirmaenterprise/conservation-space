package com.sirma.itt.emf.web.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.search.AdvancedSearchConfiguration;
import com.sirma.itt.seip.search.AdvancedSearchConfigurationProvider;

/**
 * Service providing REST end-points for obtaining configurations objects related to the searches.
 *
 * @author Mihail Radkov
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class SearchConfigurationRestService extends EmfRestService {

	@Inject
	private AdvancedSearchConfigurationProvider advancedSearchConfigurationProvider;

	/**
	 * End-point for fetching the advanced search configuration.
	 *
	 * @return the configuration as JSON response
	 */
	@GET
	@Path("/search/configuration/advanced")
	public Response getAdvancedSearchConfiguration() {
		AdvancedSearchConfiguration configuration = advancedSearchConfigurationProvider.getConfiguration();
		return buildOkResponse(configuration);
	}
}
