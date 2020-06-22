package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAddressGeneratorService;

/**
 * Test for GenerateEmailAddressListener.
 * 
 * @author svelikov
 */
public class GenerateEmailAddressListenerTest {

	@InjectMocks
	private GenerateEmailAddressListener generateEmailAddressListener;

	@Mock
	private EventService eventService;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private EmailAddressGeneratorService emailAddressGeneratorService;

	@Before
	public void setup() {
		generateEmailAddressListener = new GenerateEmailAddressListener();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onGenerateEmailAddress_throws_error() throws EmailIntegrationException, JMSException {
		Instance mockInstance = mockInstance();
		when(domainInstanceService.loadInstance("emf:123456")).thenReturn(mockInstance);
		when(emailAddressGeneratorService.generateEmailAddress(mockInstance))
				.thenThrow(EmailIntegrationException.class);
		MapMessage message = mock(MapMessage.class);
		when(message.getString("instanceId")).thenReturn("emf:123456");

		generateEmailAddressListener.onGenerateEmailAddress(message);
	}

	@Test
	public void onGenerateEmailAddress_save_and_fire_event() throws EmailIntegrationException, JMSException {
		Instance mockInstance = mockInstance();
		when(domainInstanceService.loadInstance("emf:123456")).thenReturn(mockInstance);
		when(emailAddressGeneratorService.generateEmailAddress(mockInstance)).thenReturn("project-1@domain.bg");
		MapMessage message = mock(MapMessage.class);
		when(message.getString("instanceId")).thenReturn("emf:123456");

		generateEmailAddressListener.onGenerateEmailAddress(message);

		verify(domainInstanceService, times(1)).save(any(InstanceSaveContext.class));
		Map<String, String> attributes = new HashMap<>();
		attributes.put(DISPLAY_NAME, "Instance Title");
		verify(eventService, times(1)).fire(any(CreateEmailAccountEvent.class));
	}

	private Instance mockInstance() {
		Instance instance = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("title", "Instance Title");
		instance.setProperties(properties);
		return instance;
	}

}
