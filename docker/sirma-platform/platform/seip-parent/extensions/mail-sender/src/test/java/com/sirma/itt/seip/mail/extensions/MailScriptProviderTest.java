package com.sirma.itt.seip.mail.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.mail.MailNotificationContext;
import com.sirma.itt.seip.mail.MailNotificationService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.mails.UsersMailExtractor;
import com.sirma.itt.seip.script.ScriptInstance;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for {@link MailScriptProvider}.
 *
 * @author A. Kunchev
 */
public class MailScriptProviderTest {

	@InjectMocks
	private MailScriptProvider provider;

	@Mock
	private MailNotificationService mailNotificationService;

	@Spy
	private InstanceProxyMock<MailNotificationService> instnaceOfMailNotificationService = new InstanceProxyMock<>();

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private UsersMailExtractor usersMailExtractor;

	@Mock
	private ResourceService resourceService;

	@Before
	public void setup() {
		provider = new MailScriptProvider();
		MockitoAnnotations.initMocks(this);
		instnaceOfMailNotificationService.set(mailNotificationService);

		when(usersMailExtractor.extractMails(anyCollectionOf(String.class), any(Instance.class)))
				.thenReturn(Arrays.asList("user@sirma.bg"));
		when(securityContextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(true);
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser("user"));
		EmfUser user = new EmfUser();
		user.setEmail("someEmail@test.com");
		when(resourceService.findResource(any())).thenReturn(user);
	}

	@Test
	public void initialize() {
		provider.initialize();
		verify(instnaceOfMailNotificationService).isUnsatisfied();
	}

	@Test
	public void getBindings_mailServiceDisabled_emptyMap() {
		when(instnaceOfMailNotificationService.isUnsatisfied()).thenReturn(true);
		provider.initialize();
		assertEquals(Collections.emptyMap(), provider.getBindings());
	}

	@Test
	public void getBindings_mailServiceEnabled_notEmptyMap() {
		when(instnaceOfMailNotificationService.isUnsatisfied()).thenReturn(false);
		provider.initialize();
		assertEquals(1, provider.getBindings().size());
	}

	@Test
	public void getScripts_mailServiceDisabled_emptyCollection() {
		when(instnaceOfMailNotificationService.isUnsatisfied()).thenReturn(true);
		provider.initialize();
		assertEquals(0, provider.getScripts().size());
	}

	@Test
	public void getScripts_mailServiceDisabled_notEmptyCollection() {
		when(instnaceOfMailNotificationService.isUnsatisfied()).thenReturn(false);
		provider.initialize();
		assertEquals(1, provider.getScripts().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendNotificationsScriptNode_emptyRecipientsNullUser_securityServiceNotCalled() {
		provider.sendNotifications(mock(ScriptInstance.class), "", "", new String[0], null);
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(securityContextManager, never()).getCurrentContext();
	}

	@Test
	public void sendNotificationsScriptNodeOverloaded() {
		ScriptInstance node = mockScriptInstance();

		when(usersMailExtractor.extractMails(anyCollectionOf(String.class), any(Instance.class)))
				.thenReturn(Arrays.asList("user@sirma.bg"));
		provider.sendNotifications(node, new EmfUser(), "subject", "template", new String[] { "user@sirma.bg" },
				new ScriptInstance[] { mock(ScriptInstance.class) });
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(mailNotificationService).sendEmail(any(MailNotificationContext.class), anyString());
	}

	private ScriptInstance mockScriptInstance() {
		Instance target = new EmfInstance();
		target.setId("someId");
		target.add(DefaultProperties.TITLE, "instanceTitle");
		target.add(DefaultProperties.MIMETYPE, "application/pdf");
		ScriptInstance node = mock(ScriptInstance.class);
		when(node.getTarget()).thenReturn(target);
		return node;
	}

	@Test
	public void sendNotificationsScriptNodeOverloaded_nullUser_loggedUser() {
		ScriptInstance node = mockScriptInstance();

		provider.sendNotifications(node, null, "subject", "template", new String[] { "user@sirma.bg" },
				new ScriptInstance[] { mock(ScriptInstance.class) });
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(mailNotificationService).sendEmail(any(MailNotificationContext.class), anyString());
	}

	@Test
	public void sendFromCurrentUser_shouldSendFromSystemIfAdminUser() {
		when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(Boolean.TRUE);
		ScriptInstance node = mockScriptInstance();

		provider.createNew()
				.instance(node)
				.fromCurrentUser()
				.subject("subject")
				.template("template")
				.sendTo("user@sirma.bg");

		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		ArgumentCaptor<MailNotificationContext> captor = ArgumentCaptor.forClass(MailNotificationContext.class);

		verify(mailNotificationService).sendEmail(captor.capture(), anyString());
		MailNotificationContext context = captor.getValue();
		assertNull("The FROM field should be empty in order to send from system", context.getSendFrom());
	}

	@Test
	public void sendFromCurrentUser_shouldSendFromCurrentUser() {
		ScriptInstance node = mockScriptInstance();

		provider.createNew()
				.instance(node)
				.fromCurrentUser()
				.subject("subject")
				.template("template")
				.sendTo("user@sirma.bg");

		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		ArgumentCaptor<MailNotificationContext> captor = ArgumentCaptor.forClass(MailNotificationContext.class);

		verify(mailNotificationService).sendEmail(captor.capture(), anyString());
		MailNotificationContext context = captor.getValue();
		assertNotNull("The FROM field should have user to get the email from", context.getSendFrom());
	}

	@Test
	public void sendFromCurrentUser_shouldSendFromCustomEmail() {
		ScriptInstance node = mockScriptInstance();

		provider.createNew()
				.instance(node)
				.fromEmail("custom@email.com")
				.subject("subject")
				.template("template")
				.sendTo("user@sirma.bg");

		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		ArgumentCaptor<MailNotificationContext> captor = ArgumentCaptor.forClass(MailNotificationContext.class);

		verify(mailNotificationService).sendEmail(captor.capture(), anyString());
		MailNotificationContext context = captor.getValue();
		assertNotNull("The FROM field should have user to get the email from", context.getSendFrom());
		assertEquals("custom@email.com", context.getSendFrom().getEmail());
	}
}
