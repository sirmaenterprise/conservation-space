package com.sirma.itt.seip.rest.security;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

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
public class JwtParameterAuthenticatorTest {

	private static final String ISSUER = "test issuer";

	@InjectMocks
	private JwtParameterAuthenticator authenticator = new JwtParameterAuthenticator();

	private Key secret;

	@Mock
	private JwtConfiguration jwtConfig;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Spy
	private SecurityTokensManager securityTokensManager;

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
		Mockito.when(jwtConfig.getKey()).thenReturn(secret);
		Mockito.when(jwtConfig.getIssuer()).thenReturn(ISSUER);
		Mockito.when(jwtConfig.getJwtParameterName()).thenReturn(JwtParameterAuthenticator.PARAMETER_NAME);
		Mockito.when(goodGuy.getIdentityId()).thenReturn("user@goodguy.org");
		Mockito.when(nonexistent.getIdentityId()).thenReturn("user@nonexistent.org");

		ReflectionUtils.setFieldValue(securityTokensManager, "jwtConfig", jwtConfig);

		when(jwtConfig.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>());
	}

	@Test
	public void testSuccessfulAuthentication() {
		when(userStore.loadByIdentityId(anyString(), anyString())).thenReturn(goodGuy);

		String jwt = generateJwt(ISSUER, goodGuy.getIdentityId(), true, true);
		User authenticate = authenticator
				.authenticate(createAuthContext(jwt));

		assertEquals(authenticate, goodGuy);
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
		assertNull(authenticator.authenticate(createAuthContext("Bearer am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1")));
	}

	@Test(expectedExceptions = AuthenticationException.class)
	public void should_FailWithEmptyUser() {
		authenticator.authenticate(createAuthContext(generateJwt(ISSUER, "", true, true)));
	}

	/**
	 * Test with various invalid jwt claims.
	 *
	 * @param header
	 *            Authorization header with invalid claims.
	 */
	@Test(dataProvider = "invalid-claims-data-provider")
	public void testInvalidClaims(String header) {
		assertNull(authenticator.authenticate(createAuthContext(header)));
	}

	@DataProvider(name = "invalid-claims-data-provider")
	private Object[][] provideHeader() {
		return new Object[][] { { generateJwt(null, goodGuy.getIdentityId(), true, true) },
				{ generateJwt("", goodGuy.getIdentityId(), true, true) },
				{ generateJwt("evil", goodGuy.getIdentityId(), true, true) }, { generateJwt(ISSUER, null, true, true) },
				{ generateJwt(ISSUER, goodGuy.getIdentityId(), false, true) },
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
