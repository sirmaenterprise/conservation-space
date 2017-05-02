package com.sirma.cmf.web.help;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.help.HelpRequestEvent;
import com.sirma.itt.cmf.notification.RequestHelpNotificationContext;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.Resource;

/**
 * Rest service for help request.
 *
 * @author cdimitrov
 */
@Path("/helpRequest")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class HelpRequestRestService extends EmfRestService {

	private static final String ERROR_IN_SENDING_REQUEST_HELP = "Error in sending request help!";
	private static final String MISSING_REQUIRED_ARGUMENTS_FOR_REQUEST_HELP_TO_BE_SENT = "Missing required arguments for request help to be sent.";

	@Inject
	private MailNotificationService mailNotificationService;

	@Inject
	private EventService eventService;

	@Inject
	private Instance<RequestHelpNotificationContext> requestContext;

	/**
	 * This method will manage data for request help from. Will generate notification mail and send to current
	 * administrator.
	 *
	 * @param data
	 *            needed mail data for generating the letter.
	 * @return response status
	 */
	@Path("/")
	@POST
	@Transactional
	public Response sendHelpRequest(String data) {
		if (StringUtils.isNullOrEmpty(data)) {
			LOG.warn(MISSING_REQUIRED_ARGUMENTS_FOR_REQUEST_HELP_TO_BE_SENT);
			return buildResponse(Status.BAD_REQUEST, MISSING_REQUIRED_ARGUMENTS_FOR_REQUEST_HELP_TO_BE_SENT);
		}
		try {
			JSONObject jsonData = new JSONObject(data);
			String subject = JsonUtil.getStringValue(jsonData, "subject");
			String type = JsonUtil.getStringValue(jsonData, "type");
			String description = JsonUtil.getStringValue(jsonData, "description");

			if (StringUtils.isNotNullOrEmpty(subject) && StringUtils.isNotNullOrEmpty(type)
					&& StringUtils.isNotNull(description)) {

				Resource user = getCurrentUser();
				subject = generateFullSubject(type, subject);

				RequestHelpNotificationContext mail = requestContext.get();
				mail.setSubject(subject);
				mail.setBody(encodeAsBase64(description));
				mail.setUser(user);

				// REVIEW: If MailNotificationServiceImpl is not configured, it won't send any mail
				// but still the response is OK.
				mailNotificationService.sendEmail(mail, UUID.randomUUID().toString());
				LOG.info("Sent mail with data [{}]", jsonData.toString());
				eventService.fire(new HelpRequestEvent());
			} else {
				LOG.warn(MISSING_REQUIRED_ARGUMENTS_FOR_REQUEST_HELP_TO_BE_SENT);
				return buildResponse(Response.Status.BAD_REQUEST,
						MISSING_REQUIRED_ARGUMENTS_FOR_REQUEST_HELP_TO_BE_SENT);
			}
		} catch (JSONException e) {
			LOG.warn(ERROR_IN_SENDING_REQUEST_HELP, e);
			return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, ERROR_IN_SENDING_REQUEST_HELP);
		}

		return buildResponse(Response.Status.OK, null);
	}

	private static String encodeAsBase64(String description) {
		return new String(Base64.encodeBase64(description.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}

	/**
	 * Combining subject and type for mail.
	 *
	 * @param type
	 *            mail purpose
	 * @param subject
	 *            mail subject
	 * @return combined subject
	 */
	private static String generateFullSubject(String type, String subject) {
		char leftBracket = '(';
		char rightBracket = ')';
		char emptySpace = ' ';

		StringBuilder fullSubject = new StringBuilder();

		fullSubject.append(leftBracket).append(type).append(rightBracket);
		fullSubject.append(emptySpace).append(subject);

		return fullSubject.toString();
	}

}
