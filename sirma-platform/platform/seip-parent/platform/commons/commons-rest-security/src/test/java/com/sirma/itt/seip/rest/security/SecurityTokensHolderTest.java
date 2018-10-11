package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.secirity.ActiveUserSession;
import com.sirma.itt.seip.rest.secirity.ActiveUserSessionsDao;
import com.sirma.itt.seip.rest.secirity.UserSessionProperties;
import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;

/**
 * Tests for {@link SecurityTokensHolder}.
 *
 * @author smustafov
 */
public class SecurityTokensHolderTest {

	private static final String JWT_TOKEN = "jwtToken";
	private static final String SAML_TOKEN = "samlToken";
	private static final String SESSION_INDEX = "sessionIndex";
	private static final String USER_ID = "regularuser@tenant.com";
	private static final String USER_AGENT = "OS/Browser";

	@InjectMocks
	private SecurityTokensHolder tokensHolder;

	@Mock
	private ActiveUserSessionsDao securityTokensDao;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAddToken_Should_SaveTokenToDb() {
		ArgumentCaptor<ActiveUserSession> argCaptor = ArgumentCaptor.forClass(ActiveUserSession.class);

		tokensHolder.addToken(JWT_TOKEN, SAML_TOKEN, SESSION_INDEX, USER_ID, Collections.emptyMap());
		verify(securityTokensDao).save(argCaptor.capture());

		ActiveUserSession securityTokens = argCaptor.getValue();
		assertSecurityTokens(securityTokens);
		assertEquals("{}", securityTokens.getIdentityProperties());
	}

	@Test
	public void testAddToken_Should_ConvertIdentityPropertiesToJsonString() {
		ArgumentCaptor<ActiveUserSession> argCaptor = ArgumentCaptor.forClass(ActiveUserSession.class);
		HashMap<String, Serializable> identityProperties = new HashMap<>();
		identityProperties.put(UserSessionProperties.USER_AGENT, USER_AGENT);

		tokensHolder.addToken(JWT_TOKEN, SAML_TOKEN, SESSION_INDEX, USER_ID, identityProperties);
		verify(securityTokensDao).save(argCaptor.capture());

		ActiveUserSession securityTokens = argCaptor.getValue();
		assertSecurityTokens(securityTokens);
		assertEquals("{\"User-Agent\":\"OS/Browser\"}", securityTokens.getIdentityProperties());
	}

	private static void assertSecurityTokens(ActiveUserSession securityTokens) {
		assertEquals(JWT_TOKEN, securityTokens.getJwt());
		assertEquals(SAML_TOKEN, securityTokens.getSaml());
		assertEquals(SESSION_INDEX, securityTokens.getSessionIndex());
		assertEquals(USER_ID, securityTokens.getIdentityId());
	}

	@Test
	public void testGetSamlToken_Should_ReturnNullOrToken() {
		ActiveUserSession securityTokens = createSecurityTokens(1L);

		assertEquals(Optional.empty(), tokensHolder.getSamlToken(JWT_TOKEN));

		when(securityTokensDao.getByJwt(JWT_TOKEN)).thenReturn(securityTokens);
		assertEquals(Optional.of(SAML_TOKEN), tokensHolder.getSamlToken(JWT_TOKEN));
	}

	@Test
	public void testJwtToken_Should_ReturnNullOrToken() {
		ActiveUserSession securityTokens = createSecurityTokens(1L);

		assertFalse(tokensHolder.getJwtToken(SAML_TOKEN).isPresent());

		when(securityTokensDao.getBySaml(SAML_TOKEN)).thenReturn(securityTokens);
		assertEquals(JWT_TOKEN, tokensHolder.getJwtToken(SAML_TOKEN).get());
	}

	@Test
	public void testGetSessionIndex_Should_ReturnNullOrIndex() {
		ActiveUserSession securityTokens = createSecurityTokens(1L);

		assertEquals(Optional.empty(), tokensHolder.getSessionIndex(null));
		assertEquals(Optional.empty(), tokensHolder.getSessionIndex(""));

		when(securityTokensDao.getByJwt(JWT_TOKEN)).thenReturn(securityTokens);
		assertEquals(Optional.of(SESSION_INDEX), tokensHolder.getSessionIndex(JWT_TOKEN));
	}

	@Test
	public void testRemoveByJwtToken_Should_DoNothing_WhenNoTokenFound() {
		tokensHolder.removeByJwtToken(null);
		tokensHolder.removeByJwtToken("");
		tokensHolder.removeByJwtToken(JWT_TOKEN);
		verify(securityTokensDao, times(0)).deleteBySessionIndex(anyString());
	}

	@Test
	public void testRemoveByJwtToken_Should_RemoveAllTokensBySessionIndex() {
		ActiveUserSession securityTokens = createSecurityTokens(1L);

		when(securityTokensDao.getByJwt(JWT_TOKEN)).thenReturn(securityTokens);
		when(securityTokensDao.getBySessionIndex(SESSION_INDEX)).thenReturn(Arrays.asList(securityTokens));
		tokensHolder.removeByJwtToken(JWT_TOKEN);

		verify(securityTokensDao).deleteBySessionIndex(securityTokens.getSessionIndex());
	}

	@Test
	public void testRemoveBySamlToken_Should_DoNothing_WhenNoTokenFound() {
		tokensHolder.removeBySamlToken(null);
		tokensHolder.removeBySamlToken("");
		tokensHolder.removeBySamlToken(SAML_TOKEN);
		verify(securityTokensDao, times(0)).deleteBySessionIndex(anyString());
	}

	@Test
	public void testRemoveBySamlToken_Should_RemoveAllTokensBySessionIndex() {
		ActiveUserSession securityTokens = createSecurityTokens(1L);

		when(securityTokensDao.getBySaml(SAML_TOKEN)).thenReturn(securityTokens);
		when(securityTokensDao.getBySessionIndex(SESSION_INDEX)).thenReturn(Arrays.asList(securityTokens));
		tokensHolder.removeBySamlToken(SAML_TOKEN);

		verify(securityTokensDao).deleteBySessionIndex(securityTokens.getSessionIndex());
	}

	@Test
	public void testRemoveBySessionIndex_Should_RemoveTokensFromDb() {
		when(securityTokensDao.getBySessionIndex(SESSION_INDEX))
				.thenReturn(Arrays.asList(createSecurityTokens(1L), createSecurityTokens(2L)));
		tokensHolder.removeBySessionIndex(SESSION_INDEX);
		verify(securityTokensDao).deleteBySessionIndex(SESSION_INDEX);
	}

	@Test
	public void testGetAll_ShouldFetchFromDb() {
		tokensHolder.getAll();
		verify(securityTokensDao).getAll();
	}

	private static ActiveUserSession createSecurityTokens(Long id) {
		ActiveUserSession securityTokens = new ActiveUserSession();
		securityTokens.setId(id);
		securityTokens.setJwt(JWT_TOKEN);
		securityTokens.setSaml(SAML_TOKEN);
		securityTokens.setSessionIndex(SESSION_INDEX);
		return securityTokens;
	}

}
