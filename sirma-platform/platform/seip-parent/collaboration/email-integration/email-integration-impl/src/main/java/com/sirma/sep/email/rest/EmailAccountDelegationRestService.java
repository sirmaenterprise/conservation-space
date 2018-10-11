package com.sirma.sep.email.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.rest.annotations.http.method.PATCH;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAccountDelegationService;

/**
 * Rest service for delegating rights to email accounts.
 * 
 * @author g.tsankov
 */
@Transactional
@Path("/instances")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class EmailAccountDelegationRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailAccountDelegationRestService.class);

	@Inject
	private EmailAccountDelegationService emailAccountDelegationService;

	/**
	 * Grants delegation rights to target email account.
	 * 
	 * @param target
	 *            user email account.
	 * @param instanceId
	 *            id of object instance which the user will have delegated rights.
	 * @return empty response if successful or an error otherwise.
	 */
	@PATCH
	@Path("{id}/email-account-delegate-permission")
	public Response emailAccountDelegatePermission(@QueryParam("target") String target,
			@PathParam(KEY_ID) String instanceId) {
		return modifyAccountDelegation(target, instanceId, true);
	}

	/**
	 * Removes delegation rights to target email account.
	 * 
	 * @param target
	 *            user email account.
	 * @param instanceId
	 *            id of object instance which the user will have removed rights.
	 * @return empty response if successful or an error otherwise.
	 */
	@PATCH
	@Path("{id}/email-account-remove-permission")
	public Response emailAccountRemovePermission(@QueryParam("target") String target,
			@PathParam(KEY_ID) String instanceId) {
		return modifyAccountDelegation(target, instanceId, false);
	}

	/**
	 * Get email address and display name for external account.
	 * 
	 * @param emailAccount
	 *            email account
	 * @return external account email address and display name if extraction is successful or error if fail
	 * @throws EmailIntegrationException
	 *             thrown if account attributes can not be extracted
	 */
	@GET
	@Path("{emailAccount}/email-account-attributes")
	public Response getEmailAccountAttributes(@PathParam("emailAccount") String emailAccount)
			throws EmailIntegrationException {
		return RestUtil.buildDataResponse(emailAccountDelegationService.getEmailAccountAttributes(emailAccount));
	}

	private Response modifyAccountDelegation(String target, String instanceId, boolean shouldDelegate) {
		try {
			emailAccountDelegationService.modifyAccountDelegationPermission(target, instanceId, shouldDelegate);
			return RestUtil.buildResponse(Status.OK, null);
		} catch (EmailIntegrationException e) {
			String message = "Modification of delegation rights failed for user:" + target + " and instance with mail:"
					+ instanceId;
			LOGGER.error(message, e);
			return RestUtil.buildErrorResponse(Status.INTERNAL_SERVER_ERROR, message);
		}
	}
}
