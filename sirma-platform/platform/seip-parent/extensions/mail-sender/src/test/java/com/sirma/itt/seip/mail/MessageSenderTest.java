package com.sirma.itt.seip.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.mail.attachments.MailAttachmentService;

/**
 * Tests for {@link MessageSender}.
 */
public class MessageSenderTest {

	@InjectMocks
	private MessageSender messageSender;

	@Mock
	private ConfigurationProperty<MailSender> mailSender;

	@Mock
	private GroupConverterContext context;

	@Mock
	private MailAttachmentService attachmentService;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void buildSender_ShouldNotSetAuth_When_NoUsernameSet() {
		MailSender mailSender = MessageSender.buildSender(context, attachmentService);
		assertNull(mailSender.getAuthenticator());

		MailConfiguration mailConfiguration = mailSender.getConfiguration();
		assertNull(mailConfiguration.getProperty(MailConfiguration.USERNAME));
		assertNull(mailConfiguration.getProperty(MailConfiguration.PASSWORD));
	}

	@Test
	public void buildSender_ShouldSetAuth_When_UsernameIsSet() {
		when(context.get("mail.username")).thenReturn("testUser");
		when(context.get("mail.password")).thenReturn("test123");

		MailSender mailSender = MessageSender.buildSender(context, attachmentService);
		assertNotNull(mailSender.getAuthenticator());

		MailConfiguration mailConfiguration = mailSender.getConfiguration();
		assertEquals("testUser", mailConfiguration.getProperty(MailConfiguration.USERNAME));
		assertEquals("test123", mailConfiguration.getProperty(MailConfiguration.PASSWORD));
	}

	@Test
	public void verify_ConfigChangeListenerInvoked() {
		messageSender.addMailConfigurationChangeListener(config -> {
		});
		verify(mailSender).addConfigurationChangeListener(any());
	}

}
