package com.sirma.itt.seip.mail.extensions;

import static org.junit.Assert.assertEquals;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
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

	@Mock
	private javax.enterprise.inject.Instance<MailNotificationService> instnaceOfMailNotificationService;

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private UsersMailExtractor usersMailExtractor;

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private ContentService contentService;

	@Mock
	private ResourceService resourceService;

	@Before
	public void setup() {
		provider = new MailScriptProvider();
		MockitoAnnotations.initMocks(this);
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

	@Test
	public void sendNotificationsScriptNode_emptyRecipientsNullUser_securityServiceNotCalled() {
		provider.sendNotifications(mock(ScriptInstance.class), "", "", new String[0], null);
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(securityContextManager, never()).getCurrentContext();
	}

	@Test
	public void sendNotificationsScriptNodeOverloaded() {
		Instance target = new EmfInstance();
		target.setId("someId");
		target.add(DefaultProperties.TITLE, "instanceTitle");
		target.add(DefaultProperties.MIMETYPE, "application/pdf");
		ScriptInstance node = mock(ScriptInstance.class);
		when(node.getTarget()).thenReturn(target);

		when(databaseIdManager.getValidId(any())).thenReturn("someId");
		InstanceReference reference = mock(InstanceReference.class);
		when(typeConverter.convert(any(), anyString())).thenReturn(reference);
		when(reference.toInstance()).thenReturn(target);
		when(contentService.loadTextContent(any(Instance.class))).thenReturn("content");
		when(usersMailExtractor.extractMails(anyCollectionOf(String.class), any(Instance.class)))
				.thenReturn(Arrays.asList("user@sirma.bg"));
		ReflectionUtils.setField(provider, "mailNotificationService", new InstanceProxyMock<>(mailNotificationService));
		provider.sendNotifications(node, new EmfUser(), "subject", "template", new String[] { "user@sirma.bg" },
				new ScriptInstance[] { mock(ScriptInstance.class) });
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(mailNotificationService).sendEmail(any(MailNotificationContext.class), anyString());
	}

	@Test
	public void sendNotificationsScriptNodeOverloaded_nullUser_loggedUser() {
		Instance target = new EmfInstance();
		target.setId("someId");
		target.add(DefaultProperties.TITLE, "instanceTitle");
		target.add(DefaultProperties.MIMETYPE, "application/pdf");
		ScriptInstance node = mock(ScriptInstance.class);
		when(node.getTarget()).thenReturn(target);

		when(databaseIdManager.getValidId(any())).thenReturn("someId");
		InstanceReference reference = mock(InstanceReference.class);
		when(typeConverter.convert(any(), anyString())).thenReturn(reference);
		when(reference.toInstance()).thenReturn(target);
		when(contentService.loadTextContent(any(Instance.class))).thenReturn("content");
		when(usersMailExtractor.extractMails(anyCollectionOf(String.class), any(Instance.class)))
				.thenReturn(Arrays.asList("user@sirma.bg"));
		when(securityContextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(true);
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser("user"));
		when(resourceService.getResource(any())).thenReturn(new EmfUser());

		ReflectionUtils.setField(provider, "mailNotificationService", new InstanceProxyMock<>(mailNotificationService));
		provider.sendNotifications(node, null, "subject", "template", new String[] { "user@sirma.bg" },
				new ScriptInstance[] { mock(ScriptInstance.class) });
		verify(usersMailExtractor).extractMails(anyCollectionOf(String.class), any(Instance.class));
		verify(mailNotificationService).sendEmail(any(MailNotificationContext.class), anyString());
	}

}
