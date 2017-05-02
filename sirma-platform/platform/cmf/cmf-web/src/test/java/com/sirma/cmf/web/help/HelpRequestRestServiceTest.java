package com.sirma.cmf.web.help;

import static org.testng.Assert.assertEquals;

import javax.enterprise.inject.Instance;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.notification.RequestHelpNotificationContext;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Tests for HelpRequestRestService.
 *
 * @author svelikov
 */
@Test
public class HelpRequestRestServiceTest extends CMFTest {

	@InjectMocks
	private HelpRequestRestService restService = new HelpRequestRestService() {
		@Override
		public Resource getCurrentUser() {
			EmfUser user = new EmfUser("admin");
			user.getProperties().put(ResourceProperties.FIRST_NAME, "Admin");
			user.getProperties().put(ResourceProperties.LAST_NAME, "Adminov");
			return user;
		}
	};

	@Mock
	private MailNotificationService mailNotificationService;
	@Mock
	private EventService eventService;

	@Spy
	private Instance<RequestHelpNotificationContext> requestContext = new InstanceProxyMock<>(
			new RequestHelpNotificationContext());

	@Override
	public void beforeMethod() {
		super.beforeMethod();
	}

	/**
	 * Send help request test.
	 */
	public void sendHelpRequestTest() {
		Response response = restService.sendHelpRequest(null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		//
		String requestString = "{\"subject\":\"some question\"}";
		response = restService.sendHelpRequest(requestString);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		//
		requestString = "{\"subject\":\"some question\",\"type\":\"Question\"}";
		response = restService.sendHelpRequest(requestString);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		//
		requestString = "{\"subject\":\"some question\",\"type\":\"Question\",\"description\":\"<p>???<br data-mce-bogus=\'1\'></p>\"}";
		response = restService.sendHelpRequest(requestString);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Mockito.verify(mailNotificationService, Mockito.atLeastOnce()).sendEmail(
				Matchers.any(MailNotificationContext.class), Matchers.anyString());
	}
}
