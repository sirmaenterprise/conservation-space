package com.sirma.itt.emf.web.notification;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.rest.EmfRestService;

/**
 * Service that handles operations to deal with notification messages like: clear, handle.
 *
 * @author svelikov
 */
@Path("/notification")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class NotificationRestService extends EmfRestService {

	@Inject
	private NotificationSupportImpl customNotificationSupport;

	@Inject
	private Event<MessageHandledEvent> messageHandledEvent;

	/**
	 * Notification popup is closed, clear notifications.
	 *
	 * @return the string
	 */
	@Path("/")
	@DELETE
	public Response clear() {
		if (debug) {
			LOG.debug("EMFWeb: NotificationController.clear");
		}
		customNotificationSupport.clearNotifications();
		return buildResponse(Status.OK, null);
	}

	/**
	 * Called when user clicks on notification popup action button. Rise an event to notify observers that the message
	 * was handled or to perform some other action.
	 *
	 * @param messageId
	 *            the message id
	 * @return the string
	 */
	@Path("handle")
	@GET
	public Response handle(@QueryParam("messageId") String messageId) {
		if (debug) {
			LOG.debug("EMFWeb: NotificationController.handle messageId [" + messageId + "]");
		}
		// rise event with the message id
		if (StringUtils.isNotNullOrEmpty(messageId)) {
			messageHandledEvent.fire(new MessageHandledEvent(messageId));
		}
		customNotificationSupport.clearNotifications();
		return buildResponse(Status.OK, null);
	}

}
