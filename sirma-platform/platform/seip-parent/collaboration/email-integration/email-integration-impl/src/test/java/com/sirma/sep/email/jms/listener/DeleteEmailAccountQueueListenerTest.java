package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Test for {@link DeleteEmailAccountQueueListener}.
 *
 * @author S.Djulgerova
 */
public class DeleteEmailAccountQueueListenerTest {

	@InjectMocks
	private DeleteEmailAccountQueueListener deleteEmailAccountQueueListener;

	@Mock
	private EmailAccountAdministrationService emailAccountAdministrationService;

	@Mock
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void setup() {
		deleteEmailAccountQueueListener = new DeleteEmailAccountQueueListener();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onMessageTest() throws JMSException, EmailIntegrationException {
		String accountEmail = "project-123-tenant.com@sirma.bg";
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn(accountEmail);

		EmailAccountInformation account = Mockito.mock(EmailAccountInformation.class);
		when(account.getAccountId()).thenReturn("123-456-789");
		when(emailAccountAdministrationService.getAccount(accountEmail)).thenReturn(account);

		deleteEmailAccountQueueListener.onDeleteAccount(message);
		verify(emailAccountAdministrationService, times(1)).deleteAccount("123-456-789");
		verify(shareFolderAdministrationService, times(1)).removeContactFromShareFolder(accountEmail);
	}

	@Test
	public void onMessageTest_noAccount() throws JMSException, EmailIntegrationException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn(null);

		deleteEmailAccountQueueListener.onDeleteAccount(message);
		verify(emailAccountAdministrationService, times(0)).deleteAccount("123-456-789");
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onMessageTest_withError() throws JMSException, EmailIntegrationException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EMAIL_ADDRESS)).thenReturn("project-123-tenant.com@sirma.bg");

		EmailAccountInformation account = Mockito.mock(EmailAccountInformation.class);
		when(account.getAccountId()).thenReturn("123-456-789");
		when(emailAccountAdministrationService.getAccount("project-123-tenant.com@sirma.bg"))
				.thenThrow(JMSException.class);

		deleteEmailAccountQueueListener.onDeleteAccount(message);
		verify(shareFolderAdministrationService, never()).removeContactFromShareFolder(anyString());
	}

}
