package com.sirma.itt.seip.eai.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchOrderCriterion;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.model.ModelService;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.model.SearchConfiguration;
import com.sirma.itt.seip.search.model.SearchOrder;
import com.sirma.itt.seip.search.rest.SearchRest;

/**
 * Search rest for external searches.<br>
 * 
 * @author bbanchev
 */
@Singleton
@Path(SearchRest.PATH)
public class EAISearchRestService extends SearchRest {
	@Inject
	private ModelService modelService;
	@Inject
	private EAIConfigurationService integrationService;

	@POST
	@Path("/external")
	@Consumes(Versions.V2_JSON)
	@Produces(Versions.V2_JSON)
	@Override
	public SearchArguments<Instance> search(SearchArguments<Instance> searchArgs) {
		return super.search(searchArgs);
	}

	/**
	 * Provides the search configuration for given context.
	 *
	 * @param requestedSystemId
	 *            the required context (subsystem)
	 * @return the search configuration or error (404) if not found
	 */
	@GET
	@Path("/configuration")
	@Produces(Versions.V2_JSON)
	public Response configuration(@QueryParam("context") String requestedSystemId) {
		if (!integrationService.getAllRegisteredSystems().contains(requestedSystemId)) {
			throw new RestServiceException("System with id " + requestedSystemId + " is not configured!",
					Status.NOT_FOUND);
		}
		SearchModelConfiguration searchConfiguration = modelService.getSearchConfiguration(requestedSystemId);
		List<EntitySearchOrderCriterion> orderData = searchConfiguration.getOrderData();
		SearchConfiguration config = new SearchConfiguration();
		SearchOrder order = new SearchOrder();
		if (!orderData.isEmpty()) {
			order.setDefaultOrder(orderData.get(0).getPropertyId());
		}
		for (EntitySearchCriterion criterion : orderData) {
			order.addSortingField(criterion.getPropertyId(),
					searchConfiguration.getPropertyByCriteration(criterion).getTitle());
		}
		config.setOrder(order);
		return Response.ok(config).build();

	}
}
