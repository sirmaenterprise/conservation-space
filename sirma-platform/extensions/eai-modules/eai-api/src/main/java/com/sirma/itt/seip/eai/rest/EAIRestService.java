package com.sirma.itt.seip.eai.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.configuration.EAIConfigurationProvider;
import com.sirma.itt.seip.eai.service.EAIConfigurationService;
import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service used for integration operations import and update.
 *
 * @author bbanchev
 * @author siliev
 */
@Singleton
@Path("/integration")
@Produces(Versions.V2_JSON)
public class EAIRestService {
	@Inject
	private EAIConfigurationService integrationService;
	@Inject
	private IntegrateExternalObjectsService service;

	/**
	 * Method for importing external instances into the main system. The instances to import should contain the
	 * integration properties - integrated system name, external system id, and optionally other specific data
	 *
	 * @param instances
	 *            the list of instances to import
	 * @return List of imported and updated instances.
	 */
	@POST
	@Path("/import")
	@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
	public Collection<Instance> importObject(Collection<Instance> instances) {
		return service.importInstances(instances);
	}

	/**
	 * Gets the list of currently registered in the tenant external systems as a collection of ids.
	 * 
	 * @return a collection of external system ids
	 */
	@GET
	public Collection<String> getRegisteredSystems() {
		return new ArrayList<>(integrationService.getRegisteredSystems(EAIConfigurationProvider::isUserService));
	}

}
