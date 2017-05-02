package com.sirmaenterprise.sep.helprequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest services of help request functionality. 
 * Send mail to help desk, provided by the Servicing Organization.
 * 
 * This rest will send message to help desk which email is configured by configuration "help.support.email".
 * Json of rest have to be: 
 * <pre>
 * {
 *  params: {
 * 	subject: "subject of mail",
 * 	type: "code of code list. For example: CL600",
 * 	description: "content of mail" 
 * 	} 
 * }
 * </pre>
 * 
 * Json object will be transform to {@link HelpRequestMessage} via {@link HelpRequestMessageBodyReader}.
 * Converted object will be used to send message via {@link HelpRequestService}.
 * 
 * 
 * 
 * 
 * @author Boyan Tonchev
 */
@Path("/user/help")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class HelpRequestRestService {

	@Inject
	private HelpRequestService helpRequestService;

	/**
	 * Send help request mail to desk, provided by the Servicing Organization.
	 * 
	 * @param message - hold data of mail entered by user.
	 * @return response with status 200 if success.
	 */
	@POST
	@Path("/request")
	public Response sendHelpRequest(HelpRequestMessage message) {
		helpRequestService.sendHelpRequest(message);
		return Response.ok().build();
	}

}
