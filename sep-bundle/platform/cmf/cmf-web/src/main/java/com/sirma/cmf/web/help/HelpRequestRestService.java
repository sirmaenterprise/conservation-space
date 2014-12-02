package com.sirma.cmf.web.help;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import com.sirma.itt.cmf.services.impl.MailNotificationHelperService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.mail.notification.MailNotificationService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;

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

	@Inject
	private MailNotificationService mailNotificationService;

	@Inject
	private MailNotificationHelperService mailHelper;

	@Inject
	private EventService eventService;

	/**
	 * This method will manage data for request help from. Will generate notification mail and send
	 * to current administrator.
	 * 
	 * @param data
	 *            needed mail data for generating the letter.
	 * @return response status
	 */
	@Path("/")
	@POST
	public Response sendHelpRequest(String data) {
		if (StringUtils.isNullOrEmpty(data)) {
			log.warn("Missing required arguments for request help to be sent.");
			return buildResponse(Status.BAD_REQUEST,
					"Missing required arguments for request help to be sent.");
		}
		try {
			JSONObject jsonData = new JSONObject(data);
			String subject = JsonUtil.getStringValue(jsonData, "subject");
			String type = JsonUtil.getStringValue(jsonData, "type");
			String description = JsonUtil.getStringValue(jsonData, "description");

			if (StringUtils.isNotNullOrEmpty(subject) && StringUtils.isNotNullOrEmpty(type)
					&& StringUtils.isNotNull(description)) {

				User user = (User) getCurrentUser();
				subject = generateFullSubject(type, subject);
				description = new String(Base64.encodeBase64(description.getBytes()));

				RequestHelpNotificationContext mail = new RequestHelpNotificationContext(
						mailHelper, subject, description, user);

				// REVIEW: If MailNotificationServiceImpl is not configured, it won't send any mail
				// but still the response is OK.
				mailNotificationService.sendEmail(mail);
				log.info("Sent mail with data [{}]", jsonData.toString());
				eventService.fire(new HelpRequestEvent());
			} else {
				log.warn("Missing required arguments for request help to be sent.");
				return buildResponse(Response.Status.BAD_REQUEST,
						"Missing required arguments for request help to be sent.");
			}
		} catch (JSONException e) {
			log.warn("Error in sending request help!", e);
			return buildResponse(Response.Status.INTERNAL_SERVER_ERROR,
					"Error in sending request help!");
		}

		return buildResponse(Response.Status.OK, null);
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
	private String generateFullSubject(String type, String subject) {
		char leftBracket = '(';
		char rightBracket = ')';
		char emptySpace = ' ';

		StringBuilder fullSubject = new StringBuilder();

		fullSubject.append(leftBracket).append(type).append(rightBracket);
		fullSubject.append(emptySpace).append(subject);

		return fullSubject.toString();
	}

}
