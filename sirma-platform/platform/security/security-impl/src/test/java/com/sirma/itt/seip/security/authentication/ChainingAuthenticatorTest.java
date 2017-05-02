package com.sirma.itt.seip.security.authentication;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.mocks.UserMock;

/**
 * @author BBonev
 */
@Test
public class ChainingAuthenticatorTest {

	@Mock
	Authenticator authenticator;

	@Spy
	private List<Authenticator> authenticators = new ArrayList<>();

	@InjectMocks
	ChainingAuthenticator chainingAuthenticator;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		authenticators.clear();
	}

	public void test_authenticate_context_noAuthenticator() {
		User user = chainingAuthenticator.authenticate(AuthenticationContext.create(new HashMap<>()));
		Assert.assertNull(user);
	}

	public void test_authenticate_user_noAuthenticator() {
		Object user = chainingAuthenticator.authenticate(new UserMock("", ""));
		Assert.assertNull(user);
	}

	public void test_authenticate_context_authenticator_notAuthenticated() {
		authenticators.add(authenticator);
		User user = chainingAuthenticator.authenticate(AuthenticationContext.create(new HashMap<>()));
		Assert.assertNull(user);
	}

	public void test_authenticate_user_authenticator_notAuthenticated() {
		authenticators.add(authenticator);
		Object user = chainingAuthenticator.authenticate(new UserMock("", ""));
		Assert.assertNull(user);
	}

	public void test_authenticate_context_authenticator() {
		authenticators.add(authenticator);
		AuthenticationContext context = AuthenticationContext.create(new HashMap<>());
		when(authenticator.authenticate(context)).thenReturn(new UserMock("", ""));
		assertNotNull(chainingAuthenticator.authenticate(context));
	}

	public void test_authenticate_user_authenticator() {
		authenticators.add(authenticator);
		UserMock authenticate = new UserMock("", "");
		when(authenticator.authenticate(authenticate)).thenReturn("test");
		assertEquals(chainingAuthenticator.authenticate(authenticate), "test");
	}
}
