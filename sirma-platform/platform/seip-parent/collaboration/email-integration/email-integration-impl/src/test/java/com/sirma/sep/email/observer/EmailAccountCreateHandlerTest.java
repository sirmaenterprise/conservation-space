package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.save.event.BeforeInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.event.ResourceAddedEvent;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.exception.EmailAccountCreationException;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test for {@link EmailAccountCreateHandler}.
 *
 * @author S.Djulgerova
 */
public class EmailAccountCreateHandlerTest {

	@InjectMocks
	private EmailAccountCreateHandler emailAccountCreateHandler;

	@Mock
	private DbDao dbDao;

	@Mock
	private SenderService senderService;

	@Mock
	private EventService eventService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private javax.enterprise.inject.Instance<EmailIntegrationConfiguration> emailIntegrationConfiguration;

	private static final String USER = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User";

	@Before
	public void setup() {
		emailAccountCreateHandler = new EmailAccountCreateHandler();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onAfterInstancePersistedSuccessfully_noMailboxSupportable() throws EmailAccountCreationException {
		Map<String, Serializable> properties = new HashMap<>();
		emailAccountCreateHandler
				.onAfterInstancePersistedSuccessfully(new AfterInstancePersistEvent(mockInstance(properties, false)));
		verify(eventService, times(0))
				.fire(new CreateEmailAccountEvent("instance-id", "project-123-tenant.com@sirma.bg", new HashMap<>()));
	}

	@Test(expected = EmailAccountCreationException.class)
	public void onAfterInstancePersistedSuccessfully_noEmail() throws EmailAccountCreationException {
		Map<String, Serializable> properties = new HashMap<>();
		emailAccountCreateHandler
				.onAfterInstancePersistedSuccessfully(new AfterInstancePersistEvent(mockInstance(properties, true)));
	}

	@Test(expected = EmailAccountCreationException.class)
	public void onAfterInstancePersistedSuccessfully_noId() throws EmailAccountCreationException {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(EMAIL_ADDRESS, "project-123-tenant.com@sirma.bg");
		Instance instance = mockInstance(properties, true);
		instance.setId(null);
		emailAccountCreateHandler.onAfterInstancePersistedSuccessfully(new AfterInstancePersistEvent(instance));
	}

	@Test
	public void onAfterInstancePersistedSuccessfully_correctData() throws JMSException, EmailAccountCreationException {
		HashMap<String, Serializable> properties = new HashMap<>();
		properties.put(EMAIL_ADDRESS, "project-123-tenant.com@sirma.bg");
		emailAccountCreateHandler
				.onAfterInstancePersistedSuccessfully(new AfterInstancePersistEvent(mockInstance(properties, true)));
		verify(senderService, times(1)).send(anyString(), anyMap(), any(SendOptions.class));
	}

	@Test
	public void onBeforeInstanceSaveEvent() {
		HashMap<String, Serializable> properties = new HashMap<>();
		properties.put(ResourceProperties.USER_ID, "user-tenant.com");
		Instance instance = mockInstance(properties, true);
		InstanceType instanceType = mock(InstanceType.class);
		instance.setType(instanceType);
		when(instanceType.isMailboxSupportable()).thenReturn(true);
		when(instanceType.is(anyString())).thenReturn(true);

		EmailIntegrationConfiguration integrationConfiguration = Mockito.mock(EmailIntegrationConfiguration.class);
		when(emailIntegrationConfiguration.get()).thenReturn(integrationConfiguration);
		when(integrationConfiguration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock("sirma.com"));
		when(integrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock("test"));
		emailAccountCreateHandler.onBeforeInstanceSaveEvent(
				new BeforeInstanceSaveEvent(instance, instance, Mockito.mock(Operation.class)));
		Assert.assertEquals(instance.getProperties().get(EMAIL_ADDRESS), "user-tenant.com-test@sirma.com");
	}

	@Test
	public void onCreateEmailAccountEvent() throws JMSException {
		emailAccountCreateHandler.onCreateEmailAccountEvent(
				new CreateEmailAccountEvent("instance-id", "project-123-tenant.com@sirma.bg", new HashMap<>()));
		Map<String, String> data = new HashMap<>();
		data.put(EMAIL_ADDRESS, "project-123-tenant.com@sirma.bg");
		data.put("instanceId", "instance-id");
		verify(senderService, times(1)).send(anyString(), anyMap(), any(SendOptions.class));
	}

	@Test
	public void createUserEmailAccount_correctData() throws JMSException, EmailAccountCreationException {
		mockMailboxSupportable(USER);
		EmailIntegrationConfiguration integrationConfiguration = Mockito.mock(EmailIntegrationConfiguration.class);
		when(emailIntegrationConfiguration.get()).thenReturn(integrationConfiguration);
		when(integrationConfiguration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock("sirma.com"));
		when(integrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock("test"));

		emailAccountCreateHandler.createUserEmailAccount(new ResourceAddedEvent(mockUser(true)));
		verify(senderService, times(1)).send(anyString(), anyMap(), any(SendOptions.class));
	}

	@Test(expected = EmailAccountCreationException.class)
	public void createUserEmailAccount_noId() throws EmailAccountCreationException {
		mockMailboxSupportable(USER);
		Resource user = mockUser(true);
		user.setId(null);
		EmailIntegrationConfiguration integrationConfiguration = Mockito.mock(EmailIntegrationConfiguration.class);
		when(emailIntegrationConfiguration.get()).thenReturn(integrationConfiguration);
		when(integrationConfiguration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock("sirma.com"));
		when(integrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock("test"));

		emailAccountCreateHandler.createUserEmailAccount(new ResourceAddedEvent(user));
	}

	@Test
	public void createUserEmailAccount_noMailboxSupportable() throws EmailAccountCreationException {
		emailAccountCreateHandler.createUserEmailAccount(new ResourceAddedEvent(mockUser(false)));
		verify(eventService, times(0)).fire(any(CreateEmailAccountEvent.class));
	}

	@Test
	public void createUserEmailAccount_noMailboxSupportableUser() throws EmailAccountCreationException {
		mockMailboxSupportable("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		emailAccountCreateHandler.createUserEmailAccount(new ResourceAddedEvent(mockUser(false)));
		verify(eventService, times(0)).fire(any(CreateEmailAccountEvent.class));
	}

	@Test
	public void createUserEmailAccount_noUserInstance() throws EmailAccountCreationException {
		mockMailboxSupportable(USER);
		EmailIntegrationConfiguration integrationConfiguration = Mockito.mock(EmailIntegrationConfiguration.class);
		when(emailIntegrationConfiguration.get()).thenReturn(integrationConfiguration);
		when(integrationConfiguration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock("sirma.com"));
		Resource resource = new EmfResource();
		resource.setType(mockClassInstance(true));

		emailAccountCreateHandler.createUserEmailAccount(new ResourceAddedEvent(resource));
		verify(eventService, times(0)).fire(any(CreateEmailAccountEvent.class));
	}

	private static Instance mockInstance(Map<String, Serializable> properties, boolean isMailboxSupportable) {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		instance.setIdentifier("instance-id");
		instance.setProperties(properties);

		ClassInstance classInstance = mockClassInstance(isMailboxSupportable);
		instance.setType(classInstance.type());
		return instance;
	}

	private static Resource mockUser(boolean isMailboxSupportable) {
		Resource user = new EmfUser();
		user.setId("user-id");
		user.setIdentifier("user-id");
		user.setName("stella@stella.com");

		ClassInstance classInstance = mockClassInstance(isMailboxSupportable);
		user.setType(classInstance.type());
		return user;
	}

	private static ClassInstance mockClassInstance(boolean isMailboxSupportable) {
		ClassInstance classInstance = new ClassInstance();
		Map<String, Serializable> classProperties = new HashMap<>();
		classProperties.put("mailboxSupportable", isMailboxSupportable);
		classInstance.setProperties(classProperties);
		classInstance.setCategory("test");
		return classInstance;
	}

	private void mockMailboxSupportable(String className) {
		Collection<String> existingClasses = new ArrayList<>();
		existingClasses.add(className);
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(existingClasses);
	}
}