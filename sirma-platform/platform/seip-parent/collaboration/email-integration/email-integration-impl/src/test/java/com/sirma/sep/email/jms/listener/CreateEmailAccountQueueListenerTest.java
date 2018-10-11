package com.sirma.sep.email.jms.listener;

import static com.sirma.email.ZimbraEmailIntegrationConstants.OPTIONS_ENABLED;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.email.EmailSenderService;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Test for {@link CreateEmailAccountQueueListener}.
 *
 * @author S.Djulgerova
 */
public class CreateEmailAccountQueueListenerTest {

	@InjectMocks
	private CreateEmailAccountQueueListener createEmailAccountQueueListener;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Mock
	private EmailAccountAdministrationService emailAccountAdministrationService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private EmailSenderService messageSender;

	@Mock
	private InstanceService instanceService;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private SecurityContextManager contextManager;

	@Mock
	private ApplicationConfigurationProvider applicationConfigurationProvider;

	@Mock
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@Before
	public void setup() {
		createEmailAccountQueueListener = new CreateEmailAccountQueueListener();
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
	}

	@Test
	public void onCreateEmailAccountTest() throws JMSException, EmailIntegrationException {
		String createdMail = "project-123-tenant.com@sirma.bg";
		initOnCreateEmailAccountTest();
		ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);

		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn(createdMail);
		when(message.getString(INSTANCE_ID)).thenReturn("");

		Resource resource = Mockito.mock(Resource.class);
		when(resourceService.findResource(message.getString(INSTANCE_ID))).thenReturn(resource);
		when(resource.getName()).thenReturn("emf:123456");

		when(emailIntegrationConfiguration.getWebmailProtocol()).thenReturn(new ConfigurationPropertyMock("https"));
		when(contextManager.getAdminUser()).thenReturn(Mockito.mock(EmfUser.class));

		createEmailAccountQueueListener.onCreateEmailAccount(message);

		verify(emailAccountAdministrationService, times(1)).createAccount(eq(createdMail), anyString(), anyMap());
		verify(labelProvider, times(1)).getValue(eq("emailintegration.welcome.mail.subject"));
		verify(messageSender, times(1)).sendMessage(anyString(), anyString(), anyString(), anyString(), anyString());
		verify(emailAddressResolver, times(1)).insertEmailAddress(anyString(), anyString(), anyString(), anyString());
		verify(emailAccountAdministrationService).createAccount(anyString(), anyString(), captor.capture());
		verify(shareFolderAdministrationService).shareFolderWithUser(createdMail);
		Map<String, String> capturedAttributes = captor.getValue();
		assertFalse(capturedAttributes.containsKey(OPTIONS_ENABLED));
	}

	@Test
	public void onMessageForObjectTest() throws JMSException, EmailIntegrationException {
		String createdMail = "user-123-tenant.com@sirma.bg";
		MapMessage message = Mockito.mock(MapMessage.class);
		ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn(createdMail);
		when(message.getString(INSTANCE_ID)).thenReturn("emf:123456");
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock<String>("test-domain.com"));
		when(resourceService.findResource(message.getString(INSTANCE_ID))).thenReturn(null);
		when(instanceService.loadByDbId(anyString())).thenReturn(mockInstance());
		when(contextManager.getAdminUser()).thenReturn(Mockito.mock(EmfUser.class));
		createEmailAccountQueueListener.onCreateEmailAccount(message);
		verify(emailAccountAdministrationService, times(1)).createAccount(eq(createdMail), anyString(), anyMap());
		verify(emailAddressResolver, times(1)).insertEmailAddress(anyString(), anyString(), anyString(), anyString());
		verify(emailAccountAdministrationService).createAccount(anyString(), anyString(), captor.capture());
		verify(shareFolderAdministrationService).shareFolderWithUser(createdMail);
		Map<String, String> capturedAttributes = captor.getValue();
		assertTrue(capturedAttributes.containsKey(OPTIONS_ENABLED));
		assertEquals(capturedAttributes.get(OPTIONS_ENABLED), "FALSE");
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onMessageTest_withError() throws JMSException, EmailIntegrationException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("project-123-tenant.com@sirma.bg");
		when(message.getString(INSTANCE_ID)).thenReturn("emf:123456");
		when(emailAccountAdministrationService.createAccount(eq("project-123-tenant.com@sirma.bg"), anyString(),
				anyMap())).thenThrow(JMSException.class);
		when(instanceService.loadByDbId(anyString())).thenReturn(mockInstance());
		when(contextManager.getAdminUser()).thenReturn(Mockito.mock(EmfUser.class));

		createEmailAccountQueueListener.onCreateEmailAccount(message);
	}

	@Test
	public void onMessageIfEmailExist() throws JMSException, EmailIntegrationException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("user-123-tenant.com@sirma.bg");
		when(message.getString(INSTANCE_ID)).thenReturn("emf:123456");

		when(emailAddressResolver.getEmailAddress("user-123-tenant.com@sirma.bg")).thenReturn(new EmailAddress());

		createEmailAccountQueueListener.onCreateEmailAccount(message);
		verify(emailAccountAdministrationService, times(0)).createAccount(anyString(), anyString(), anyMap());
		verify(shareFolderAdministrationService, times(0)).shareFolderWithUser(anyString());
	}

	@Test
	public void onMessageForAdminUserTest() throws JMSException, EmailIntegrationException {
		initOnCreateEmailAccountTest();
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("admin-tenant.com@sirma.bg");
		when(message.getString(INSTANCE_ID)).thenReturn("emf:admin-tenant.com");
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock<String>("test-domain.com"));

		Resource resource = Mockito.mock(Resource.class);

		when(resourceService.findResource(message.getString(INSTANCE_ID))).thenReturn(resource);
		when(resource.getName()).thenReturn("emf:admin-tenant.com");
		when(instanceService.loadByDbId(anyString())).thenReturn(mockInstance());

		EmfUser adminUser = Mockito.mock(EmfUser.class);
		when(contextManager.getAdminUser()).thenReturn(adminUser);
		when(adminUser.getIdentityId()).thenReturn("emf:admin-tenant.com");

		Map<String, String> expectedAttributes = new HashMap<>();
		expectedAttributes.put(GIVEN_NAME, Objects.toString(message.getString(GIVEN_NAME), ""));
		expectedAttributes.put(SN, Objects.toString(message.getString(SN), ""));
		expectedAttributes.put(DISPLAY_NAME, Objects.toString(message.getString(DISPLAY_NAME)));

		createEmailAccountQueueListener.onCreateEmailAccount(message);
		verify(emailAccountAdministrationService, times(1)).createAccount(eq("admin-tenant.com@sirma.bg"), anyString(),
				eq(expectedAttributes));
		verify(emailAddressResolver, times(1)).insertEmailAddress(anyString(), anyString(), anyString(), anyString());
	}

	private void initOnCreateEmailAccountTest() {
		EmfUser tenantAdmin = Mockito.mock(EmfUser.class);
		when(tenantAdmin.getCredentials()).thenReturn("test-password");
		when(tenantAdmin.getName()).thenReturn("test-name");

		when(emailIntegrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock("test"));
		when(emailIntegrationConfiguration.getTenantAdminAccount())
				.thenReturn(new ConfigurationPropertyMock(tenantAdmin));
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock<String>("test-domain.com"));
		when(emailIntegrationConfiguration.getWebmailUrl())
				.thenReturn(new ConfigurationPropertyMock<String>("test-webmail.com"));
		when(emailIntegrationConfiguration.getWebmailPort()).thenReturn(new ConfigurationPropertyMock<String>("25"));
		when(emailIntegrationConfiguration.getWebmailProtocol())
				.thenReturn(new ConfigurationPropertyMock<String>("https"));
		when(labelProvider.getValue("emailintegration.welcome.mail.subject")).thenReturn("Test subject label");
	}

	private Instance mockInstance() {
		Instance instance = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "Instance Title");
		instance.setProperties(properties);
		return instance;
	}
}
