package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.Key;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.rest.secirity.JwtGenerator;
import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Tests for {@link SecurityTokensManager}.
 *
 * @author smustafov
 */
public class SecurityTokensManagerTest {

	private static final String JWT_TOKEN = "jwtToken";
	private static final String SAML_TOKEN = "samlToken";
	private static final String SESSION_INDEX = "sessionIndex";
	private static final String USER_ID = "john@tenant.com";

	@Mock
	private JwtGenerator jwtGenerator;

	@Mock
	private JwtConfiguration jwtConfig;

	@Mock
	private SecurityTokensHolder tokensHolder;

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private SecurityTokensManager tokensManager;

	private Random randomGenerator = new Random();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGenerate_Should_GenerateJwtToken() {
		User user = mockUser();

		when(jwtGenerator.generate(user)).thenReturn(JWT_TOKEN);

		String generated = tokensManager.generate(user);

		assertEquals(JWT_TOKEN, generated);
		verify(jwtGenerator).generate(user);
		verify(tokensHolder).addToken(JWT_TOKEN, SAML_TOKEN, SESSION_INDEX, USER_ID, Collections.emptyMap());
	}

	private static User mockUser() {
		User user = mock(User.class);
		when(user.getTicket()).thenReturn(SAML_TOKEN);
		when(user.getIdentityId()).thenReturn(USER_ID);
		Map<String, Serializable> props = new HashMap<>();
		props.put(JwtUtil.SESSION_INDEX, SESSION_INDEX);
		when(user.getProperties()).thenReturn(props);
		return user;
	}

	@Test
	public void testGetSamlToken_verifyRetrivedFromHolder() throws JoseException {
		JwtClaims claims = new JwtClaims();
		when(jwtConfig.getKey()).thenReturn(generateKey());
		when(tokensHolder.getSamlToken(anyString())).thenReturn(Optional.empty());

		tokensManager.getSamlToken(claims);

		verify(tokensHolder).getSamlToken(anyString());
	}

	@Test
	public void testCurrentJwtToken_Should_ReturnJwtTokenOfLoggedUser() {
		when(securityContext.getAuthenticated()).then(a -> mockUser());
		when(tokensHolder.getJwtToken(SAML_TOKEN)).thenReturn(Optional.of(JWT_TOKEN));
		Optional<String> jwtToken = tokensManager.getCurrentJwtToken();
		assertNotNull(jwtToken);
		assertTrue(jwtToken.isPresent());
		assertEquals(JWT_TOKEN, jwtToken.get());
	}

	@Test
	public void testCurrentJwtToken_Should_ReturnEmptyOptional_When_UserIsMissingTicket() {
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
		assertFalse(tokensManager.getCurrentJwtToken().isPresent());
	}

	@Test
	public void should_RemoveTokens_When_UserIsLoggedOut() {
		BeginLogoutEvent event = new BeginLogoutEvent(mockUser());
		tokensManager.onBeginLogout(event);
		verify(tokensHolder).removeBySamlToken(SAML_TOKEN);
	}

	private Key generateKey() {
		byte[] randomBytes = new byte[256];
		randomGenerator.nextBytes(randomBytes);
		return new SecretKeySpec(randomBytes, AlgorithmIdentifiers.HMAC_SHA256);
	}

}
