package com.sirma.itt.seip.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.rest.secirity.ActiveUserSession;
import com.sirma.itt.seip.rest.secirity.ActiveUserSessionsDao;

/**
 * Tests for {@link ActiveUserSessionsDao}.
 *
 * @author smustafov
 */
public class ActiveUserSessionsDaoTest {

	private static final String SAML = "saml";
	private static final String JWT = "jwt";
	private static final String SESSION_INDEX = "sessionIndex";

	@InjectMocks
	private ActiveUserSessionsDao userSessionsDao;

	@Mock
	private DbDao dbDao;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_ReturnUserSession_When_FetchingBySamlAndThereIsAnEntryInDb() {
		ActiveUserSession activeUserSession = new ActiveUserSession();

		mockDbDao(ActiveUserSession.QUERY_GET_BY_SAML_KEY, SAML, SAML, Arrays.asList(activeUserSession));

		assertEquals(activeUserSession, userSessionsDao.getBySaml(SAML));
	}

	@Test
	public void should_ReturnNull_When_FetchingBySamlAndThereIsNoEntryInDb() {
		mockDbDao(ActiveUserSession.QUERY_GET_BY_SAML_KEY, SAML, SAML, Collections.emptyList());

		assertNull(userSessionsDao.getBySaml(SAML));
	}

	@Test
	public void should_ReturnUserSession_When_FetchingByJwt() {
		ActiveUserSession activeUserSession = new ActiveUserSession();

		mockDbDao(ActiveUserSession.QUERY_GET_BY_JWT_KEY, JWT, JWT, Arrays.asList(activeUserSession));

		assertEquals(activeUserSession, userSessionsDao.getByJwt(JWT));
	}

	@Test
	public void should_ReturnCollectionOfUserSessions_When_FetchingBySessionIndex() {
		ActiveUserSession activeUserSession = new ActiveUserSession();

		mockDbDao(ActiveUserSession.QUERY_GET_BY_SESSION_INDEX_KEY, SESSION_INDEX, SESSION_INDEX,
				Arrays.asList(activeUserSession));

		Collection<ActiveUserSession> sessions = userSessionsDao.getBySessionIndex(SESSION_INDEX);

		assertFalse(sessions.isEmpty());
		assertEquals(activeUserSession, sessions.iterator().next());
	}

	@Test
	public void should_DeleteUserSessionsBySessionIndex() {
		userSessionsDao.deleteBySessionIndex(SESSION_INDEX);

		verify(dbDao).executeUpdate(ActiveUserSession.QUERY_DELETE_BY_SESSION_INDEX_KEY,
				Collections.singletonList(new Pair<String, Object>(SESSION_INDEX, SESSION_INDEX)));
	}

	@Test
	public void should_PassCorrectParams_WhenFetchingAllUserSessions() {
		when(dbDao.fetchWithNamed(ActiveUserSession.QUERY_GET_ALL_KEY, Collections.emptyList()))
				.thenReturn(Arrays.asList(new ActiveUserSession(), new ActiveUserSession()));

		assertEquals(2, userSessionsDao.getAll().size());
	}

	@Test
	public void should_InvokeDbDao_WhenSavingUserSession() {
		ActiveUserSession activeUserSession = new ActiveUserSession();

		userSessionsDao.save(activeUserSession);

		verify(dbDao).saveOrUpdate(activeUserSession);
	}

	@Test
	public void should_InvokeDbDao_WhenDeletingUserSessionById() {
		userSessionsDao.deleteById(1L);
		verify(dbDao).delete(ActiveUserSession.class, 1L);
	}

	private void mockDbDao(String namedQuery, String key, String value, List<Object> sessions) {
		when(dbDao.fetchWithNamed(namedQuery, Collections.singletonList(new Pair<String, Object>(key, value))))
				.thenReturn(sessions);
	}

}