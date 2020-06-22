package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirmaenterprise.sep.jms.api.MessageSender;

/**
 * Test for {@link EmailAccountDeleteHandler}.
 *
 * @author S.Djulgerova
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class EmailAccountDeleteHandlerTest {

	@InjectMocks
	private EmailAccountDeleteHandler emailAccountDeleteHandler;

	@Mock
	private MessageSender deleteEmailAccountQueue;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Before
	public void setup() {
		emailAccountDeleteHandler = new EmailAccountDeleteHandler();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onAfterInstanceDeleteEvent_email_address_present() {
		EmfInstance instance = Mockito.mock(EmfInstance.class);
		when(instance.getString("emailAddress")).thenReturn("project-123-tenant.com@sirma.bg");
		emailAccountDeleteHandler.onAfterInstanceDeleteEvent(new BeforeInstanceDeleteEvent(instance));
		Map<String, String> data = new HashMap<>();
		data.put(EMAIL_ADDRESS, "project-123-tenant.com@sirma.bg");
		data.put(ZIMBRA_ACCOUNT_STATUS, "closed");
		verify(deleteEmailAccountQueue, times(1)).send(data);
	}

	@Test
	public void onAfterInstanceDeleteEvent_email_address_missing() {
		EmfInstance instance = Mockito.mock(EmfInstance.class);
		emailAccountDeleteHandler.onAfterInstanceDeleteEvent(new BeforeInstanceDeleteEvent(instance));
		verify(deleteEmailAccountQueue, Mockito.never()).send(Mockito.anyMap());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onAfterInstanceDeleteEvent_error_during_sending_to_queue() {
		EmfInstance instance = Mockito.mock(EmfInstance.class);
		when(instance.getString("emailAddress")).thenReturn("project-123-tenant.com@sirma.bg");
		Mockito.doThrow(JMSException.class).when(deleteEmailAccountQueue).send(Mockito.anyMap());
		emailAccountDeleteHandler.onAfterInstanceDeleteEvent(new BeforeInstanceDeleteEvent(instance));
	}

}
