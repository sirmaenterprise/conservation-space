package com.sirmaenterprise.sep.help;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.objects.services.HelpService;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest services for the contextual help logic.
 *
 * @author nvelkov
 */
@ApplicationScoped
@Path(HelpRest.PATH)
@Produces(Versions.V2_JSON)
public class HelpRest {
	public static final String PATH = "/help";

	@Inject
	private HelpService helpService;

	/**
	 * Retrieve all help instances' id to target mapping.
	 *
	 * @return the help instances' id to target mapping
	 */
	@GET
	public Map<String, String> getHelpInstances() {
		return helpService.getHelpIdToTargetMapping();
	}

}
