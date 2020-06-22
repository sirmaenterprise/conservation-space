package com.sirma.itt.seip.rest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Key;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Tests for {@link JwtUtil}.
 *
 * @author smustafov
 */
public class JwtUtilTest {

	private static final String ISSUER = "issuer";

	@Mock
	private JwtConfiguration jwtConfig;

	@InjectMocks
	private JwtUtil jwtUtil = new JwtUtil();

	private static Key key;

	@BeforeClass
	public static void beforeClass() {
		Random randomGenerator = new Random();
		byte[] randomBytes = new byte[256];
		randomGenerator.nextBytes(randomBytes);
		key = new SecretKeySpec(randomBytes, AlgorithmIdentifiers.HMAC_SHA256);
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(jwtConfig.getIssuer()).thenReturn(ISSUER);
		when(jwtConfig.getKey()).thenReturn(key);
	}

	@Test
	public void testReadUser_withInvalidToken() {
		String jwtToken = "invalidToken";
		SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();
		UserStore userStore = mock(UserStore.class);

		User user = jwtUtil.readUser(securityContextManager, userStore, jwtToken);

		assertNull(user);
	}

	@Test
	public void testReadUser_withValidToken() throws JoseException {
		String userIdentity = "user@tenant.com";
		String userId = "emf:user";
		String userTenant = userIdentity.substring(userIdentity.indexOf('@') + 1);

		EmfUser resultUser = new EmfUser();
		resultUser.setId(userId);
		User userToGenerateJwtFor = mock(User.class);
		UserStore userStore = mock(UserStore.class);
		SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();

		when(userToGenerateJwtFor.getIdentityId()).thenReturn(userIdentity);
		String jwtToken = generateJwtToken(userToGenerateJwtFor, null);
		when(userStore.loadByIdentityId(userIdentity, userTenant)).thenReturn(resultUser);

		User user = jwtUtil.readUser(securityContextManager, userStore, jwtToken);

		assertEquals(userId, user.getSystemId());
	}

	@Test(expected = SecurityException.class)
	public void testExtractSessionIndex_withMissingIndexInJwtToken() throws JoseException {
		String userIdentity = "user@tenant.com";
		User userToGenerateJwtFor = mock(User.class);

		when(userToGenerateJwtFor.getIdentityId()).thenReturn(userIdentity);
		String jwtToken = generateJwtToken(userToGenerateJwtFor, null);

		jwtUtil.extractSessionIndex(jwtToken);
	}

	@Test
	public void testExtractSessionIndex_withAvailableIndexInJwtToken() throws JoseException {
		String sessionIndex = "sessionIndex";
		String userIdentity = "user@tenant.com";
		User userToGenerateJwtFor = mock(User.class);

		when(userToGenerateJwtFor.getIdentityId()).thenReturn(userIdentity);
		String jwtToken = generateJwtToken(userToGenerateJwtFor, sessionIndex);

		assertEquals(sessionIndex, jwtUtil.extractSessionIndex(jwtToken));
	}

	@Test
	public void testExtractUserId_Should_CorrectlyExtractUserId_WhenJwtTokenIsValid() throws JoseException {
		String sessionIndex = "sessionIndex";
		String userIdentity = "user@tenant.com";
		User userToGenerateJwtFor = mock(User.class);

		when(userToGenerateJwtFor.getIdentityId()).thenReturn(userIdentity);
		String jwtToken = generateJwtToken(userToGenerateJwtFor, sessionIndex);

		assertEquals(userIdentity, jwtUtil.extractUserId(jwtToken));
	}

	private static String generateJwtToken(User user, String sessionIndex) throws JoseException {
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(ISSUER);
		claims.setSubject(user.getIdentityId());
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();
		if (StringUtils.isNotBlank(sessionIndex)) {
			claims.setStringClaim(JwtUtil.SESSION_INDEX, sessionIndex);
		}

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(key);
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
		return jws.getCompactSerialization();
	}

}
