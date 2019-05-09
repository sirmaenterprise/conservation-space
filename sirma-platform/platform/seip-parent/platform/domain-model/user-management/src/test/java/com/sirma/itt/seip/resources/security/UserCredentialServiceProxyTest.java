package com.sirma.itt.seip.resources.security;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link UserCredentialServiceProxy}.
 *
 * @author smustafov
 */
public class UserCredentialServiceProxyTest {

	@InjectMocks
	private UserCredentialServiceProxy credentialService;

	@Mock
	private Contextual<UserCredentialService> delegate;

	@Mock
	private EventService eventService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_FireEvent_When_PasswordChanged() {
		UserCredentialService credentialServiceImpl = mock(UserCredentialService.class);
		when(credentialServiceImpl.changeUserPassword("regularuser@sep.test", "123456", "654321")).thenReturn(true);
		when(delegate.getContextValue()).thenReturn(credentialServiceImpl);

		credentialService.changeUserPassword("regularuser@sep.test", "123456", "654321");

		verify(eventService).fire(any(UserPasswordChangeEvent.class));
	}

	@Test
	public void should_NotFireEvent_When_PasswordChangeFails() {
		UserCredentialService credentialServiceImpl = mock(UserCredentialService.class);
		when(credentialServiceImpl.changeUserPassword(anyString(), anyString(), anyString())).thenThrow(
				new PasswordChangeFailException(PasswordChangeFailException.PasswordFailType.UNKNOWN_TYPE, ""));
		when(delegate.getContextValue()).thenReturn(credentialServiceImpl);

		try {
			credentialService.changeUserPassword("regularuser@sep.test", "123456", "654321");
		} catch (PasswordChangeFailException e) {
			verify(eventService, never()).fire(any(UserPasswordChangeEvent.class));
		}
	}

}
