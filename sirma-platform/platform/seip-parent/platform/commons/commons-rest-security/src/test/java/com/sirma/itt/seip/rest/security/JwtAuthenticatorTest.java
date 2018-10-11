package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.ws.rs.core.HttpHeaders;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.mockito.Matchers;
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
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link JwtAuthenticator}.
 *
 * @author yasko
 */
@Test
public class JwtAuthenticatorTest {
	private JwtAuthenticator authenticator = new JwtAuthenticator();
	private static final String ISSUER = "test issuer";

	private Key secret;

	@Mock
	private JwtConfiguration jwtConfig;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private SecurityTokensManager securityTokensManager;

	@Mock
	private UserStore userStore;

	@Mock
	private User goodGuy;

	@Mock
	private User nonexistent;

	@Mock
	private SessionManager sessionManager;

	/**
	 * Init
	 *
	 * @throws NoSuchAlgorithmException
	 *             if signing alg is nowhere to be found.
	 */
	@BeforeTest
	protected void init() throws NoSuchAlgorithmException {
		secret = KeyGenerator.getInstance("HmacSHA256").generateKey();

		MockitoAnnotations.initMocks(this);
		Mockito.when(jwtConfig.getKey()).thenReturn(secret);
		Mockito.when(jwtConfig.getIssuer()).thenReturn(ISSUER);
		Mockito.when(goodGuy.getIdentityId()).thenReturn("user@goodguy.org");
		Mockito.when(nonexistent.getIdentityId()).thenReturn("user@nonexistent.org");
		when(userStore.loadByIdentityId("user@goodguy.org", "goodguy.org")).thenReturn(goodGuy);

		ReflectionUtils.setFieldValue(authenticator, "jwtConfig", jwtConfig);
		ReflectionUtils.setFieldValue(authenticator, "securityContextManager", securityContextManager);
		ReflectionUtils.setFieldValue(authenticator, "userStore", userStore);
		ReflectionUtils.setFieldValue(authenticator, "securityTokensManager", securityTokensManager);
		ReflectionUtils.setFieldValue(authenticator, "sessionManager", sessionManager);
	}

	/**
	 * Test successful authentication.
	 *
	 * @throws JoseException
	 *             thrown on failed signing JWT
	 */
	public void testSuccessfulAuthentication() throws JoseException {
		when(securityTokensManager.getSamlToken(Matchers.any(JwtClaims.class))).thenReturn("samlToken");

		String jwt = generateJwt(ISSUER, goodGuy.getIdentityId(), true, true);
		User authenticate = authenticator
				.authenticate(createAuthContext("Bearer " + jwt));

		assertEquals(authenticate, goodGuy);
		verify(sessionManager).updateLoggedUser(jwt, goodGuy.getIdentityId());
	}

	/**
	 * Test authentication with a non-existent user.
	 */
	@Test(expectedExceptions = AuthenticationException.class)
	public void testUserNotFound() {

		authenticator.authenticate(
				createAuthContext("Bearer " + generateJwt(ISSUER, nonexistent.getIdentityId(), true, true)));
	}

	/**
	 * Tests authentication with user that has no SAML token.
	 *
	 * @throws JoseException
	 *             thrown on failed signing JWT
	 */
	@Test(expectedExceptions = SecurityException.class)
	public void testUserNoSamlToken() throws JoseException {
		Mockito.when(securityTokensManager.getSamlToken(Matchers.any(JwtClaims.class))).thenReturn(null);
		authenticator
				.authenticate(createAuthContext("Bearer " + generateJwt(ISSUER, goodGuy.getIdentityId(), true, true)));
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
	 *
	 * @param header
	 *            Invalid authorization header.
	 */
	@Test(expectedExceptions = AuthenticationException.class)
	public void testInvalidJwtInHeader() {
		authenticator.authenticate(createAuthContext("Bearer am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1"));
	}

	/**
	 * Test with various invalid jwt claims.
	 *
	 * @param header
	 *            Authorization header with invalid claims.
	 */
	@Test(expectedExceptions = AuthenticationException.class, dataProvider = "invalid-claims-data-provider")
	public void testInvalidClaims(String header, String failMessage) {
		authenticator.authenticate(createAuthContext(header));
		fail(failMessage);
	}

	@DataProvider(name = "invalid-claims-data-provider")
	private Object[][] provideInvalidClaimsHeader() {
		return new Object[][] {
				{ "Bearer " + generateJwt(null, goodGuy.getIdentityId(), true, true),
						"Should have failed for missing issuer" },
				{ "Bearer " + generateJwt("", goodGuy.getIdentityId(), true, true),
						"Should have failed for empty issuer" },
				{ "Bearer " + generateJwt("evil", goodGuy.getIdentityId(), true, true),
						"Should have failed for wrong issuer" },
				{ "Bearer " + generateJwt(ISSUER, null, true, true), "Should have failed for missing subject" },
				{ "Bearer " + generateJwt(ISSUER, "", true, true), "Should have failed for empty subject" },
				{ "Bearer " + generateJwt(ISSUER, goodGuy.getIdentityId(), false, true),
						"Should have failed for missing issued date" },
				{ "Bearer " + generateJwt(ISSUER, goodGuy.getIdentityId(), true, false),
						"Should have failed for missing id" } };
	}

	@DataProvider(name = "invalid-header-data-provider")
	private Object[][] provideHeader() {
		return new Object[][] {
			{ "Basic am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" },
			{ "Bearer \t" },
			{ "Bearer " },
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
