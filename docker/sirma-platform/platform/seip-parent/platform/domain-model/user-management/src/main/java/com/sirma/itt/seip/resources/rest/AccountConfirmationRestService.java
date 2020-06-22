package com.sirma.itt.seip.resources.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.resources.security.AccountConfirmationRequest;
import com.sirma.itt.seip.resources.security.AccountConfirmationService;
import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Rest service for account confirmation process of newly created users. The service can be accessed by unauthenticated
 * users.
 *
 * @author smustafov
 */
@Path("/account")
@ApplicationScoped
public class AccountConfirmationRestService {

	@Inject
	private AccountConfirmationService accountConfirmationService;

	/**
	 * Returns link to captcha image associated for the given confirmation code.
	 *
	 * @param confirmationCode
	 *            for which a captcha image will be retrieved
	 * @return link to a captcha image
	 */
	@GET
	@Path("captcha")
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public String retrieveCaptcha(@QueryParam("code") String confirmationCode) {
		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add("captchaLink", accountConfirmationService.retrieveCaptchaLink(confirmationCode));
		return result.build().toString();
	}

	/**
	 * Confirms user account.
	 *
	 * @param accountConfirmationRequestBuilder
	 *            payload object containing all request information
	 */
	@POST
	@Path("/confirm")
	@PublicResource(tenantParameterName = RequestParams.KEY_TENANT)
	public void confirm(AccountConfirmationRequest.AccountConfirmationRequestBuilder accountConfirmationRequestBuilder) {
		AccountConfirmationRequest accountConfirmationRequest = accountConfirmationRequestBuilder.build()
				.orElseThrow(() -> new BadRequestException("Missing required parameter"));

		String username = accountConfirmationRequest.getUsername();
		String password = accountConfirmationRequest.getPassword();
		String confirmationCode = accountConfirmationRequest.getConfirmationCode();
		String captchaAnswer = accountConfirmationRequest.getCaptchaAnswer();
		String tenantId = accountConfirmationRequest.getTenantId();

		accountConfirmationService.confirmAccount(username, password, confirmationCode, captchaAnswer, tenantId);
	}

}
