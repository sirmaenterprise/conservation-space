package com.sirma.sep.email.observer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.IdentifierGeneratedEvent;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAddressGeneratorService;

/**
 * Test for IdentifierGeneratedObserver.
 * 
 * @author svelikov
 */
public class IdentifierGeneratedObserverTest {

	@InjectMocks
	private IdentifierGeneratedObserver identifierGeneratedObserver;

	@Mock
	private EmailAddressGeneratorService emailAddressGeneratorService;

	@Before
	public void setup() {
		identifierGeneratedObserver = new IdentifierGeneratedObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = EmailIntegrationException.class)
	public void onIdentifierGenerated_throws_exception() throws EmailIntegrationException {
		Instance mockInstance = mockInstance(true);
		Mockito.when(emailAddressGeneratorService.generateEmailAddress(mockInstance))
				.thenThrow(EmailIntegrationException.class);
		IdentifierGeneratedEvent event = new IdentifierGeneratedEvent(mockInstance);

		identifierGeneratedObserver.onIdentifierGenerated(event);
	}

	@Test
	public void onIdentifierGenerated() throws EmailIntegrationException {
		Instance mockInstance = mockInstance(true);
		Mockito.when(emailAddressGeneratorService.generateEmailAddress(mockInstance))
				.thenReturn("project-1@domain.bg");
		IdentifierGeneratedEvent event = new IdentifierGeneratedEvent(mockInstance);

		identifierGeneratedObserver.onIdentifierGenerated(event);

		Assert.assertEquals("project-1@domain.bg", mockInstance.getString("emailAddress"));
	}

	@Test
	public void onIdentifierGenerated_mailbox_not_supported() throws EmailIntegrationException {
		Instance mockInstance = mockInstance(false);
		IdentifierGeneratedEvent event = new IdentifierGeneratedEvent(mockInstance);

		identifierGeneratedObserver.onIdentifierGenerated(event);

		Assert.assertNull(mockInstance.getString("emailAddress"));
	}

	private Instance mockInstance(boolean isMailboxSupportable) {
		Instance instance = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "Instance Title");
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
