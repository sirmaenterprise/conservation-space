package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.spec.SecretKeySpec;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.NumericDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link SecurityTokensManager}.
 *
 * @author smustafov
 */
public class SecurityTokensManagerTest {

	private static final String SESSION_INDEX = "sessionIndex";
	private static final String USER_ID = "john@tenant.com";

	@Mock
	private JwtConfiguration jwtConfig;

	@InjectMocks
	private SecurityTokensManager tokensManager;

	private Random randomGenerator = new Random();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(jwtConfig.getIssuer()).thenReturn("testIssuer");
		when(jwtConfig.getKey()).thenReturn(generateKey());
	}

	@Test
	public void testGenerate_Should_GenerateJwtToken() {
		User user = mockUser();

		String generated = tokensManager.generate(user);

		assertNotNull(generated);
	}

	@Test
	public void readUserName_Should_ProperlyExtractUserName() {
		User user = mockUser();

		String generated = tokensManager.generate(user);
		Pair<String, NumericDate> userName = tokensManager.readUserNameAndDate(generated);

		assertEquals(USER_ID, userName.getFirst());
	}

	@Test
	public void readUserName_Should_ReturnNullWhenFails() {
		Pair<String, NumericDate> userName = tokensManager.readUserNameAndDate("invalidToken");
		assertNull(userName);
	}

	@Test
	public void isRevoked_ShouldReturnFalse_When_NoRevokedDateSet() {
		mockRevokeConfig(null);
		assertFalse(tokensManager.isRevoked(NumericDate.now()));
	}

	@Test
	public void isRevoked_ShouldReturnFalse_When_CurrentTimeAfterRevoked() {
		mockRevokeConfig(new Date(System.currentTimeMillis() - 1000));
		assertFalse(tokensManager.isRevoked(NumericDate.now()));
	}

	@Test
	public void isRevoked_ShouldReturnTrue_When_CurrentTimeBeforeRevoked() {
		mockRevokeConfig(new Date());
		assertTrue(tokensManager.isRevoked(NumericDate.fromMilliseconds(System.currentTimeMillis() - 1000)));
	}

	private void mockRevokeConfig(Date date) {
		when(jwtConfig.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>(date));
	}

	private static User mockUser() {
		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn(USER_ID);
		Map<String, Serializable> props = new HashMap<>();
		props.put(JwtUtil.SESSION_INDEX, SESSION_INDEX);
		when(user.getProperties()).thenReturn(props);
		return user;
	}

	private Key generateKey() {
		byte[] randomBytes = new byte[256];
		randomGenerator.nextBytes(randomBytes);
		return new SecretKeySpec(randomBytes, AlgorithmIdentifiers.HMAC_SHA256);
	}

}
