package com.sirma.itt.cmf.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.mail.MailResourceProvider;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceType;

/**
 * Test for {@link RequestHelpNotificationContext}.
 *
 * @author A. Kunchev
 */
public class RequestHelpNotificationContextTest {

	@InjectMocks
	private RequestHelpNotificationContext context = new RequestHelpNotificationContext();

	@Mock
	private MailResourceProvider mailResourceProvider;

	@Mock
	private ConfigurationProperty<String> sendtoUser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mailResourceProvider.getLabel(anyString())).thenReturn("Request Help: {0}");
		when(sendtoUser.get()).thenReturn("user");

		EmfUser user = new EmfUser();
		user.add(ResourceProperties.EMAIL, "user@sirma.bg");
		when(mailResourceProvider.getResource(any(), any(ResourceType.class))).thenReturn(user);
	}


	@Test
	public void getTemplateId_requestHelpTemplate() {
		String templateId = context.getTemplateId();
		assertEquals("email_help_request", templateId);
	}


	@Test
	public void getSubject_nullSubject_unformattedMsg() {
		String subject = context.getSubject();
		assertEquals("Request Help: ", subject);
	}

	@Test
	public void getSubject_notNullSubject_msgWithType() {
		context.setSubject("subject");
		String subject = context.getSubject();
		assertEquals("Request Help: subject", subject);
	}


	@Test
	public void getModel_withBody() {
		context.setBody("body");
		Map<? extends String, ? extends Object> model = context.getModel();
		assertEquals(new String(Base64.decodeBase64("body"), StandardCharsets.UTF_8), model.get("content"));
	}


	@Test
	public void getSendTo() {
		Collection<String> sendTo = context.getSendTo();
		assertEquals(Arrays.asList("user@sirma.bg"), sendTo);
	}

}
