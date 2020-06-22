package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.service.EmailAccountAdministrationService;

/**
 * Test for {@link UpdateEmailAccountQueueListener}.
 *
 * @author S.Djulgerova
 */
public class UpdateEmailAccountQueueListenerTest {

	@InjectMocks
	private UpdateEmailAccountQueueListener updateEmailAccountQueueListener;

	@Mock
	private EmailAccountAdministrationService accountAdministrationService;

	@Before
	public void setup() {
		updateEmailAccountQueueListener = new UpdateEmailAccountQueueListener();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_modifyAccount_called_successfuly() throws EmailIntegrationException, JMSException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("project-123-tenant.com@sirma.bg");
		when(message.getString(DISPLAY_NAME)).thenReturn("Test Project");
		when(message.getMapNames()).thenReturn(Collections.enumeration(Arrays.asList(EMAIL_ADDRESS, DISPLAY_NAME)));

		EmailAccountInformation info = new EmailAccountInformation();
		info.setAccountId("accountId");
		when(accountAdministrationService.getAccount("project-123-tenant.com@sirma.bg")).thenReturn(info);

		updateEmailAccountQueueListener.onUpdateEmailAccount(message);

		List<GenericAttribute> accountAttributes = new LinkedList<>();
		accountAttributes.add(new GenericAttribute(DISPLAY_NAME, "Test Project"));
		verify(accountAdministrationService, times(1)).modifyAccount("accountId",
				accountAttributes);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void testOnMessageWithErors() throws EmailIntegrationException, JMSException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("project-123-tenant.com@sirma.bg");
		when(message.getString(DISPLAY_NAME)).thenReturn("Test Project");
		when(accountAdministrationService.getAccount("project-123-tenant.com@sirma.bg"))
				.thenThrow(new EmailIntegrationException());

		updateEmailAccountQueueListener.onUpdateEmailAccount(message);
	}

}
