package com.sirma.itt.seip.rest.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.ws.rs.core.HttpHeaders;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.rest.secirity.JwtAuthenticator;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link JwtAuthenticator}.
 *
 * @author yasko
 */
@Test
public class JwtAuthenticatorTest {

	private static final String ISSUER = "test issuer";

	private JwtAuthenticator authenticator = new JwtAuthenticator();

	private Key secret;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Spy
	private SecurityTokensManager securityTokensManager;

	@Mock
	private JwtConfiguration jwtConfiguration;

	@Mock
	private UserStore userStore;

	@Mock
	private User goodGuy;

	@Mock
	private User nonexistent;

	@BeforeTest
	protected void init() throws NoSuchAlgorithmException {
		secret = KeyGenerator.getInstance("HmacSHA256").generateKey();

		MockitoAnnotations.initMocks(this);
		Mockito.when(goodGuy.getIdentityId()).thenReturn("user@goodguy.org");
		Mockito.when(nonexistent.getIdentityId()).thenReturn("user@nonexistent.org");
		when(userStore.loadByIdentityId("user@goodguy.org", "goodguy.org")).thenReturn(goodGuy);

		ReflectionUtils.setFieldValue(authenticator, "securityContextManager", securityContextManager);
		ReflectionUtils.setFieldValue(authenticator, "userStore", userStore);
		ReflectionUtils.setFieldValue(authenticator, "securityTokensManager", securityTokensManager);

		ReflectionUtils.setFieldValue(securityTokensManager, "jwtConfig", jwtConfiguration);
		when(jwtConfiguration.getIssuer()).thenReturn(ISSUER);
		when(jwtConfiguration.getKey()).thenReturn(secret);
	}

	@Test
	public void testSuccessfulAuthentication() {
		when(jwtConfiguration.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>());
		String jwt = generateJwt(ISSUER, goodGuy.getIdentityId(), true, true);
		User authenticate = authenticator
				.authenticate(createAuthContext(JwtAuthenticator.AUTHORIZATION_METHOD + " " + jwt));

		assertEquals(authenticate, goodGuy);
		verify(userStore).setUserTicket(goodGuy, jwt);
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void testUserNotFound() {
		when(jwtConfiguration.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>());
		authenticator.authenticate(createAuthContext(
				JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(ISSUER, nonexistent.getIdentityId(), true,
						true)));
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void should_ThrowException_When_TokenIsRevoked() {
		when(jwtConfiguration.getRevocationTimeConfig())
				.thenReturn(new ConfigurationPropertyMock<>(new Date(System.currentTimeMillis() + 1000)));
		String jwt = generateJwt(ISSUER, goodGuy.getIdentityId(), true, true);
		authenticator.authenticate(createAuthContext(JwtAuthenticator.AUTHORIZATION_METHOD + " " + jwt));
	}

	/**
	 * Test with various invalid authorization headers.
	 *
	 * @param header
	 *            Invalid authorization header.
	 */
	@Test(dataProvider = "invalid-header-data-provider")
	public void testInvalidHeader(String header) {
		Assert.assertNull(authenticator.authenticate(createAuthContext(header)));
	}

	/**
	 * Test invalid JWT in auth header.
	 */
	@Test
	public void testInvalidJwtInHeader() {
		assertNull(authenticator.authenticate(createAuthContext(JwtAuthenticator.AUTHORIZATION_METHOD + " am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1")));
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void should_FailForEmptySubject() {
		when(jwtConfiguration.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>());
		authenticator.authenticate(createAuthContext(JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(ISSUER, "", true, true)));
	}

	/**
	 * Test with various invalid jwt claims.
	 *
	 * @param header
	 *            Authorization header with invalid claims.
	 */
	@Test(dataProvider = "invalid-claims-data-provider")
	public void testInvalidClaims(String header, String failMessage) {
		assertNull(authenticator.authenticate(createAuthContext(header)), failMessage);
	}

	@DataProvider(name = "invalid-claims-data-provider")
	private Object[][] provideInvalidClaimsHeader() {
		return new Object[][] {
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(null, goodGuy.getIdentityId(), true, true),
						"Should have failed for missing issuer" },
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt("", goodGuy.getIdentityId(), true, true),
						"Should have failed for empty issuer" },
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt("evil", goodGuy.getIdentityId(), true, true),
						"Should have failed for wrong issuer" },
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(ISSUER, null, true, true), "Should have failed for missing subject" },
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(ISSUER, goodGuy.getIdentityId(), false, true),
						"Should have failed for missing issued date" },
				{ JwtAuthenticator.AUTHORIZATION_METHOD + " " + generateJwt(ISSUER, goodGuy.getIdentityId(), true, false),
						"Should have failed for missing id" } };
	}

	@DataProvider(name = "invalid-header-data-provider")
	private Object[][] provideHeader() {
		return new Object[][] {
			{ "Basic am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" },
			{ JwtAuthenticator.AUTHORIZATION_METHOD + " \t" },
			{ JwtAuthenticator.AUTHORIZATION_METHOD + " " },
			{ " am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" }
		};
	}

	private String generateJwt(String issuer, String user, boolean iat, boolean id) {
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(issuer);
		if (id) {
			claims.setGeneratedJwtId();
		}
		if (iat) {
			claims.setIssuedAtToNow();
		}
		claims.setSubject(user);

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(secret);

		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);

		try {
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			Assert.fail("Error while generating JWT", e);
		}
		return null;
	}

	private AuthenticationContext createAuthContext(String headerValue) {
		Map<String, String> map = new HashMap<>();
		map.put(HttpHeaders.AUTHORIZATION, headerValue);
		return AuthenticationContext.create(map);
	}
}
