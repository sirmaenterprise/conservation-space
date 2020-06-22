package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Test for {@link EmailAccountUpdateHandler}.
 *
 * @author S.Djulgerova
 */
public class EmailAccountUpdateHandlerTest {

	@InjectMocks
	private EmailAccountUpdateHandler emailAccountUpdateHandler;

	@Mock
	private MessageSender updateEmailAccountQueueListener;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Before
	public void setup() {
		emailAccountUpdateHandler = new EmailAccountUpdateHandler();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onUpdateEmailAccount_changed_persisted_instance() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(EMAIL_ADDRESS, "stella@sirma.bg");
		Instance instance = mockInstance(properties, true);
		EmailAddress emailAddress = new EmailAddress();
		Mockito.when(emailAddressResolver.getEmailAddress("stella@sirma.bg")).thenReturn(emailAddress);

		emailAccountUpdateHandler.onInstanceChanged(new InstanceChangeEvent(instance));

		verify(updateEmailAccountQueueListener, times(1)).send(anyMap());
	}

	@Test
	public void onUpdateEmailAccount_changed_not_persisted_instance() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.UNIQUE_IDENTIFIER, "NO_ID");
		Instance instance = mockInstance(properties, true);
		EmailAddress emailAddress = new EmailAddress();
		Mockito.when(emailAddressResolver.getEmailAddress("stella@sirma.bg")).thenReturn(emailAddress);

		emailAccountUpdateHandler.onInstanceChanged(new InstanceChangeEvent(instance));

		verify(updateEmailAccountQueueListener, times(0)).send(anyMap());
	}

	@Test
	public void onUpdateEmailAccount_isnatance_dont_support_mailbox() {
		EmailAddress emailAddress = new EmailAddress();
		Mockito.when(emailAddressResolver.getEmailAddress("stella@sirma.bg")).thenReturn(emailAddress);

		emailAccountUpdateHandler.onInstanceChanged(new InstanceChangeEvent(mockInstance(new HashMap<>(), false)));

		verify(updateEmailAccountQueueListener, times(0)).send(anyMap());
	}

	@Test
	public void onUpdateEmailAccount_isnatance_supports_mailbox_but_no_account_exists_yet() {
		Instance instance = mockInstance(new HashMap<>(), true);
		EmailAddress emailAddress = new EmailAddress();
		Mockito.when(emailAddressResolver.getEmailAddress("stella@sirma.bg")).thenReturn(null);

		emailAccountUpdateHandler.onInstanceChanged(new InstanceChangeEvent(instance));

		verify(updateEmailAccountQueueListener, times(0)).send(anyMap());
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

	private static ClassInstance mockClassInstance(boolean isMailboxSupportable) {
		ClassInstance classInstance = new ClassInstance();
		Map<String, Serializable> classProperties = new HashMap<>();
		classProperties.put("mailboxSupportable", isMailboxSupportable);
		classInstance.setProperties(classProperties);
		classInstance.setCategory("test");
		return classInstance;
	}

}
