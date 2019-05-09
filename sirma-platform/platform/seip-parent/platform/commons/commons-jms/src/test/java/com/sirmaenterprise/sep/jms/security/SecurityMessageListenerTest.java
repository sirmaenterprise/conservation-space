package com.sirmaenterprise.sep.jms.security;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;

/**
 * Test for {@link SecurityMessageListener}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/06/2017
 */
public class SecurityMessageListenerTest {

	@InjectMocks
	private SecurityMessageListener listener;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private Message message;
	@Mock
	private UserStore userStore;
	@Mock
	private User user;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.getCurrentContext().getEffectiveAuthentication()).thenReturn(user);
		when(userStore.loadBySystemId(anyString())).thenReturn(user);
	}

	@Test
	public void beforeMessage_shouldSetSystemTenantIfNotSpecified() throws Exception {
		listener.beforeMessage(message);

		verify(securityContextManager).initializeTenantContext(SecurityContext.SYSTEM_TENANT, null);
	}

	@Test
	public void beforeMessage_shouldSetSystemTenantIfFailedToGetTenant() throws Exception {

		when(message.getStringProperty(CommunicationConstants.TENANT_ID_KEY)).thenThrow(JMSException.class);
		listener.beforeMessage(message);

		verify(securityContextManager).initializeTenantContext(SecurityContext.SYSTEM_TENANT, null);
	}

	@Test
	public void beforeMessage_shouldSetTenantIfSpecified() throws Exception {

		when(message.getStringProperty(CommunicationConstants.TENANT_ID_KEY)).thenReturn("testTenant.com");
		listener.beforeMessage(message);

		verify(securityContextManager).initializeTenantContext("testTenant.com", null);
	}

	@Test
	public void beforeMessage_shouldOverrideEffectiveAuthentication_IfDifferentUsers() throws Exception {
		mockMessage("emf:effectiveUser");

		listener.beforeMessage(message);

		verify(securityContextManager).initializeExecution(any(AuthenticationContext.class));
		verify(securityContextManager).beginContextExecution(any(User.class));
	}

	@Test
	public void beforeMessage_shouldNotOverrideEffectiveAuthentication_IfSame() throws Exception {
		mockMessage("emf:user-testTenant.com");

		listener.beforeMessage(message);

		verify(securityContextManager).initializeExecution(any(AuthenticationContext.class));
	}

	@Test
	public void beforeMessage_shouldOverrideRequestIdIfPresentWithTenantId() throws Exception {
		when(message.getStringProperty(CommunicationConstants.TENANT_ID_KEY)).thenReturn("testTenant.com");
		when(message.getStringProperty(CommunicationConstants.REQUEST_ID_KEY)).thenReturn("request-id");

		listener.beforeMessage(message);

		verify(securityContextManager).initializeTenantContext("testTenant.com", "request-id");
	}

	@Test
	public void onSuccess_shouldEndSecurityContext() {
		listener.beforeMessage(message);
		listener.onSuccess();

		verify(securityContextManager).initializeTenantContext(SecurityContext.SYSTEM_TENANT, null);
		verify(securityContextManager).endContextExecution();
	}

	@Test
	public void onSuccess_shouldEndSecurityContextTwice() throws JMSException {
		mockMessage("emf:effectiveUser");

		listener.beforeMessage(message);
		listener.onSuccess();

		verify(securityContextManager, times(2)).endContextExecution();
	}

	@Test
	public void onError_shouldEndSecurityContext() {
		listener.beforeMessage(message);
		listener.onError(new Exception());

		verify(securityContextManager).initializeTenantContext(SecurityContext.SYSTEM_TENANT, null);
		verify(securityContextManager).endContextExecution();
	}

	@Test
	public void onError_shouldEndSecurityContextTwice() throws JMSException {
		mockMessage("emf:effectiveUser");

		listener.beforeMessage(message);
		listener.onError(new Exception());

		verify(securityContextManager, times(2)).endContextExecution();
	}

	private void mockMessage(String effectiveUser) throws JMSException {
		when(user.getSystemId()).thenReturn("emf:user-testTenant.com");

		when(message.getStringProperty(CommunicationConstants.TENANT_ID_KEY)).thenReturn("testTenant.com");
		when(message.getStringProperty(CommunicationConstants.AUTHENTICATED_USER_KEY))
				.thenReturn("emf:user-testTenant" + ".com");
		when(message.getStringProperty(CommunicationConstants.EFFECTIVE_USER_KEY)).thenReturn(effectiveUser);
		when(message.getStringProperty(CommunicationConstants.REQUEST_ID_KEY)).thenReturn("request-id");
	}

}
