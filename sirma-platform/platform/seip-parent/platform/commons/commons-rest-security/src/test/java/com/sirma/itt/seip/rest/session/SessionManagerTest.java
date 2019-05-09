package com.sirma.itt.seip.rest.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Tests for {@link SessionManager}.
 *
 * @author smustafov
 */
public class SessionManagerTest {

	private static final String USER_ID = "user@tenant.com";
	private static final String JWT_TOKEN = "token";
	private static final String SESSION_INDEX = "index";
	private static final Date LOGGED_IN_DATE = new Date();
	private static final Integer SESSION_TIMEOUT = Integer.valueOf(10);

	@InjectMocks
	private SessionManager sessionManager;

	@Mock
	private ContextualMap<String, HttpSession> sessions;

	@Mock
	private Map<HttpSession, StringPair> users;

	@Mock
	private Map<String, Pair<String, Date>> loggedUsers;

	@Mock
	private UserPreferences userPreferences;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		when(userPreferences.getSessionTimeout()).thenReturn(SESSION_TIMEOUT);
	}

	@Test
	public void should_NotAddEntry_When_JwtTokenIsEmpty() {
		sessionManager.init();
		sessionManager.addLoggedUser(null, USER_ID);

		assertTrue(sessionManager.getLoggedUsers().isEmpty());
	}

	@Test
	public void should_AddNewEntry_When_AllParamsArePresent() {
		sessionManager.init();
		sessionManager.addLoggedUser(JWT_TOKEN, USER_ID);

		Map<String, Pair<String, Date>> loggedUsers = sessionManager.getLoggedUsers();
		assertTrue(loggedUsers.size() == 1);
		assertEquals(USER_ID, loggedUsers.get(JWT_TOKEN).getFirst());
		assertNotNull(loggedUsers.get(JWT_TOKEN).getSecond());
	}

	@Test
	public void should_NotModifyLoggedUsers_When_TokenIsEmpty() {
		sessionManager.init();
		sessionManager.addLoggedUser(JWT_TOKEN, USER_ID);

		sessionManager.removeLoggedUser(null);

		assertTrue(sessionManager.getLoggedUsers().size() == 1);
	}

	@Test
	public void should_NotModifyLoggedUsers_When_ThereIsNoUserWithGivenToken() {
		sessionManager.init();
		sessionManager.addLoggedUser(JWT_TOKEN, USER_ID);

		sessionManager.removeLoggedUser("notExistingToken");

		assertTrue(sessionManager.getLoggedUsers().size() == 1);
	}

	@Test
	public void should_RemoveLoggedUser_When_TokenIsPresent() {
		sessionManager.init();
		sessionManager.addLoggedUser(JWT_TOKEN, USER_ID);

		sessionManager.removeLoggedUser(JWT_TOKEN);

		assertTrue(sessionManager.getLoggedUsers().isEmpty());
	}

	@Test
	public void should_AddNewLoggedUser_When_TryingToUpdateSessionAndItsMissing() {
		sessionManager.init();
		assertTrue(sessionManager.getLoggedUsers().isEmpty());
		sessionManager.updateLoggedUser(JWT_TOKEN, USER_ID);

		Map<String, Pair<String, Date>> loggedUsers = sessionManager.getLoggedUsers();
		assertTrue(loggedUsers.size() == 1);
		assertEquals(USER_ID, loggedUsers.get(JWT_TOKEN).getFirst());
		assertNotNull(loggedUsers.get(JWT_TOKEN).getSecond());
	}

	@Test
	public void should_UpdateLoggedUser_When_HeHasRegisteredSession() {
		sessionManager.init();

		sessionManager.updateLoggedUser(JWT_TOKEN, USER_ID);

		Map<String, Pair<String, Date>> loggedUsers = sessionManager.getLoggedUsers();
		assertTrue(loggedUsers.size() == 1);
		assertEquals(USER_ID, loggedUsers.get(JWT_TOKEN).getFirst());
		assertNotEquals(LOGGED_IN_DATE, loggedUsers.get(JWT_TOKEN).getSecond());
	}

	@Test
	public void should_DoNothing_When_TryingToUpdateSessionWithEmptyParams() {
		when(loggedUsers.size()).thenReturn(1);

		sessionManager.updateLoggedUser(null, null);
		verifyLoggedUsers(sessionManager.getLoggedUsers());

		sessionManager.updateLoggedUser(null, "");
		verifyLoggedUsers(sessionManager.getLoggedUsers());

		sessionManager.updateLoggedUser("", null);
		verifyLoggedUsers(sessionManager.getLoggedUsers());

		sessionManager.updateLoggedUser("", "");
		verifyLoggedUsers(sessionManager.getLoggedUsers());

		sessionManager.updateLoggedUser(JWT_TOKEN, null);
		verifyLoggedUsers(sessionManager.getLoggedUsers());

		sessionManager.updateLoggedUser(null, USER_ID);
		verifyLoggedUsers(sessionManager.getLoggedUsers());
	}

	@Test
	public void should_CleanUpJwtSessionForUser_When_HeWasInactiveMoreThanAllowed() {
		sessionManager.init();

		sessionManager.logTimeout();

		assertTrue(sessionManager.getLoggedUsers().isEmpty());
	}

	@Test
	public void should_RegisterHttpSession_When_ParamsAreNotEmpty() {
		HttpSession httpSession = mock(HttpSession.class);

		sessionManager.registerSession(SESSION_INDEX, httpSession);

		verify(sessions).put(SESSION_INDEX, httpSession);
	}

	@Test
	public void should_NotRegisterHttpSession_When_ParamsAreEmpty() {
		HttpSession httpSession = mock(HttpSession.class);

		sessionManager.registerSession(null, httpSession);
		verify(sessions, never()).put(anyString(), any(HttpSession.class));

		sessionManager.registerSession(SESSION_INDEX, null);
		verify(sessions, never()).put(anyString(), any(HttpSession.class));

		sessionManager.registerSession(null, null);
		verify(sessions, never()).put(anyString(), any(HttpSession.class));
	}

	@Test
	public void should_ReturnHttpSession_When_ItWasRegistered() {
		when(sessions.get(SESSION_INDEX)).thenReturn(mock(HttpSession.class));

		assertNotNull(sessionManager.getSession(SESSION_INDEX));
	}

	@Test
	public void should_ReturnNull_When_GettingHttpSessionAndItsNotRegistered() {
		assertNull(sessionManager.getSession(SESSION_INDEX));
	}

	@Test
	public void should_ReturnNull_When_SessionIndexParamIsNull() {
		assertNull(sessionManager.getSession(null));
	}

	@Test
	public void should_RemoveHttpSessionFromMap_When_UnregisteringGivenSession() {
		sessionManager.unregisterSession(SESSION_INDEX);

		verify(sessions).remove(SESSION_INDEX);
	}

	@Test
	public void should_DoNothing_When_UnregisteringNullSession() {
		sessionManager.unregisterSession(null);

		verify(sessions, never()).remove(anyString());
	}

	@Test
	public void should_CorrectlyBuildClientId() {
		String sessionId = "jsessionid";
		String userAgent = "agent";
		HttpSession httpSession = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);

		when(request.getHeader("User-Agent")).thenReturn(userAgent);
		when(request.getSession()).thenReturn(httpSession);
		when(httpSession.getId()).thenReturn(sessionId);

		assertEquals(userAgent + "\u00B6" + sessionId, sessionManager.getClientId(request));
	}

	@Test
	public void should_RemoveHttpSession_When_TimeoutIsReached() {
		Set<Entry<HttpSession, StringPair>> httpSessionsSet = new HashSet<>();
		httpSessionsSet
				.add(new EntryMock(mockHttpSession(SESSION_TIMEOUT * -1, false), new StringPair("user", "data")));

		when(users.entrySet()).thenReturn(httpSessionsSet);

		sessionManager.logTimeout();

		assertTrue(httpSessionsSet.isEmpty());
	}

	@Test
	public void should_RemoveHttpSession_When_IllegalStateExceptionIsThrown() {
		Set<Entry<HttpSession, StringPair>> httpSessionsSet = new HashSet<>();
		httpSessionsSet.add(new EntryMock(mockHttpSession(SESSION_TIMEOUT, true), new StringPair("user", "data")));

		when(users.entrySet()).thenReturn(httpSessionsSet);

		sessionManager.logTimeout();

		assertTrue(httpSessionsSet.isEmpty());
	}

	@Test
	public void should_TrackHttpSessionForUser() {
		sessionManager.trackUser(mockHttpSession(0, false), USER_ID, "clientid");

		verify(users).put(any(HttpSession.class), any(StringPair.class));
	}

	@Test
	public void should_NotTrackHttpSessionForUser_When_ThereHttpSessionParamIsNull() {
		sessionManager.trackUser(null, USER_ID, "clientid");

		verify(users, never()).put(any(HttpSession.class), any(StringPair.class));
	}

	@Test
	public void should_UntrackUser_When_HttpSessionParamIsNotNull() {
		sessionManager.untrackUser(mockHttpSession(0, false));

		verify(users).remove(any(HttpSession.class));
	}

	@Test
	public void should_DoNothing_When_HttpSessionParamIsNull() {
		sessionManager.untrackUser(null);

		verify(users, never()).remove(any(HttpSession.class));
	}

	private static void verifyLoggedUsers(Map<String, Pair<String, Date>> loggedUsers) {
		assertTrue(loggedUsers.size() == 1);
	}

	private static HttpSession mockHttpSession(int minutes, boolean throwException) {
		HttpSession session = mock(HttpSession.class);

		if (throwException) {
			when(session.getLastAccessedTime()).thenThrow(new IllegalStateException());
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, minutes);

			when(session.getLastAccessedTime()).thenReturn(calendar.getTimeInMillis());
		}

		return session;
	}

	private class EntryMock implements Entry<HttpSession, StringPair> {

		private HttpSession key;
		private StringPair value;

		public EntryMock(HttpSession key, StringPair value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public HttpSession getKey() {
			return key;
		}

		@Override
		public StringPair getValue() {
			return value;
		}

		@Override
		public StringPair setValue(StringPair value) {
			return null;
		}

	}

}
