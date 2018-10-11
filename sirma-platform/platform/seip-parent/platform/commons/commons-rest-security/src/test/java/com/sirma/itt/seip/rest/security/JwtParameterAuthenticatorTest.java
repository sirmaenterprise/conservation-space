package com.sirma.itt.seip.rest.security;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.mockito.InjectMocks;
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
import com.sirma.itt.seip.rest.secirity.JwtParameterAuthenticator;
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

/**
 * Test for {@link JwtAuthenticator}.
 *
 * @author yasko
 */
@Test
public class JwtParameterAuthenticatorTest {
	@InjectMocks
	private JwtParameterAuthenticator authenticator = new JwtParameterAuthenticator();
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
		Mockito.when(jwtConfig.getJwtParameterName()).thenReturn(JwtParameterAuthenticator.PARAMETER_NAME);
		Mockito.when(goodGuy.getIdentityId()).thenReturn("user@goodguy.org");
		Mockito.when(nonexistent.getIdentityId()).thenReturn("user@nonexistent.org");
	}

	/**
	 * Test successful authentication.
	 *
	 * @throws JoseException
	 *             thrown on failed signing JWT
	 */
	public void testSuccessfulAuthentication() throws JoseException {
		when(userStore.loadByIdentityId(anyString(), anyString())).thenReturn(goodGuy);
		when(securityTokensManager.getSamlToken(Matchers.any(JwtClaims.class))).thenReturn("samlToken");

		String jwt = generateJwt(ISSUER, goodGuy.getIdentityId(), true, true);
		User authenticate = authenticator
				.authenticate(createAuthContext(jwt));

		assertEquals(authenticate, goodGuy);
		verify(sessionManager).updateLoggedUser(jwt, goodGuy.getIdentityId());
	}

	/**
	 * Test authentication with a non-existent user.
	 */
	@Test(expectedExceptions = AuthenticationException.class)
	public void testUserNotFound() {

		authenticator.authenticate(createAuthContext(generateJwt(ISSUER, nonexistent.getIdentityId(), true, true)));
	}

	/**
	 * Tests authentication with user that has no SAML token.
	 *
	 * @throws JoseException
	 *             thrown on failed signing JWT
	 */
	@Test(expectedExceptions = SecurityException.class)
	public void testUserNoSamlToken() throws JoseException {
		when(userStore.loadByIdentityId(anyString(), anyString())).thenReturn(goodGuy);
		Mockito.when(securityTokensManager.getSamlToken(Matchers.any(JwtClaims.class))).thenReturn(null);
		authenticator.authenticate(createAuthContext(generateJwt(ISSUER, goodGuy.getIdentityId(), true, true)));
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
	public void testInvalidClaims(String header) {
		authenticator.authenticate(createAuthContext(header));
	}

	@DataProvider(name = "invalid-claims-data-provider")
	private Object[][] provideHeader() {
		return new Object[][] { { generateJwt(null, goodGuy.getIdentityId(), true, true) },
				{ generateJwt("", goodGuy.getIdentityId(), true, true) },
				{ generateJwt("evil", goodGuy.getIdentityId(), true, true) }, { generateJwt(ISSUER, null, true, true) },
				{ generateJwt(ISSUER, "", true, true) }, { generateJwt(ISSUER, goodGuy.getIdentityId(), false, true) },
				{ generateJwt(ISSUER, goodGuy.getIdentityId(), true, false) } };
	}

	@DataProvider(name = "invalid-header-data-provider")
	private Object[][] provideInvalidClaimsHeader() {
		return new Object[][] { { "\t" }, { " " } };
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

	private static AuthenticationContext createAuthContext(String headerValue) {
		Map<String, String> map = new HashMap<>();
		map.put(JwtParameterAuthenticator.PARAMETER_NAME, headerValue);
		return AuthenticationContext.create(map);
	}
}
