package com.sirma.itt.seip.shared.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Test the share code security authenticator.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ShareCodeSecurityAuthenticatorTest {

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private UserStore userStore;

	@InjectMocks
	private ShareCodeSecurityAuthenticator authenticator;

	@Test
	public void should_returnNull_onFailedValidation() {
		assertNull(authenticator.authenticate(AuthenticationContext.createEmpty()));
	}

	@Test
	public void should_returnUser() {
		User user = new EmfUser();
		when(userStore.loadByIdentityId(anyString())).thenReturn(user);

		Map<String, String> contextProperties = CollectionUtils.createHashMap(4);
		contextProperties.put(ShareCodeSecurityAuthenticator.SHARE_CODE,
				"8m10eZFL57C11002bmjbb532u75l6k802ub=W1l9OF0RXdm3W");
		contextProperties.put(ShareCodeSecurityAuthenticator.RESOURCE_ID, "ac9ab7d0-40d4-4de1-95cb-924a170e2e81");
		contextProperties.put(ShareCodeSecurityAuthenticator.SECRET_KEY, "key");

		assertEquals(user, authenticator.authenticate(AuthenticationContext.create(contextProperties)));
	}
}
