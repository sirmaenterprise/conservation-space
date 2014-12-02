package com.sirma.itt.emf.authentication.session;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.authentication.sso.saml.SSOConfiguration;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.security.model.User;

/**
 * Manager is responsible to synchronize and organize logout process during concurrent invocations.
 * 
 * @author bbanchev
 */
@ApplicationScoped
public class SessionManager {

	private static final Logger LOGGER = Logger.getLogger(SessionManager.class);

	/** PILCROW SIGN. */
	private static final String DELIMITER = "\u00B6";

	/** The use multi browser mode. */
	private final boolean useMultiBrowserMode = true;

	/** List of currently processed users. */
	private final Set<String> preprocessing = new HashSet<>();

	private final Map<String, HttpSession> sessions = new WeakHashMap<>();

	private static Map<HttpSession, StringPair> users = new LinkedHashMap<>();

	/** The session timeout period. */
	@Inject
	@Config(name = EmfConfigurationProperties.SESSION_TIMEOUT_PERIOD, defaultValue = "30")
	private Integer sessionTimeoutPeriod;

	private final Timer logoutTimer = new Timer();

	private final Timer infoTimer = new Timer();

	@Inject
	@Config(name = SSOConfiguration.INFORMATION_RATE_LOGGING, defaultValue = "15")
	private Integer rateLogging;

	/**
	 * Inits the logged in users info timer.
	 */
	@PostConstruct
	public void init() {
		long rate = rateLogging.longValue() * 60L * 1000L;
		infoTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				synchronized (users) {
					long timeout = sessionTimeoutPeriod * 60L * 1000L;
					Iterator<Entry<HttpSession, StringPair>> iterator = users.entrySet().iterator();
					long now = new Date().getTime();
					while (iterator.hasNext()) {
						Map.Entry<HttpSession, StringPair> entry = iterator.next();
						if ((now - entry.getKey().getLastAccessedTime()) >= timeout) {
							LOGGER.info("Automatically remove user '" + entry.getValue()
									+ "' due to inactivity");
							iterator.remove();
						}
					}
					LOGGER.info("List of currently logged users: " + users.values());
				}
			}
		}, rate, rate);
	}

	/**
	 * On shutdown terminates all timers.
	 */
	@PreDestroy
	public void onShutdown() {
		infoTimer.cancel();
		logoutTimer.cancel();
	}

	/**
	 * Thread to remove from cache.
	 * 
	 * @author bbanchev
	 */
	private class RemoveCachedProcess extends TimerTask {

		// local copy if id
		private final String userId;

		/**
		 * Construct the thread for that user.
		 * 
		 * @param userId
		 *            is the user id.
		 */
		public RemoveCachedProcess(String userId) {
			this.userId = userId;
		}

		@Override
		public void run() {
			synchronized (preprocessing) {
				preprocessing.remove(userId);
			}
		}
	}

	/**
	 * Invoke the user on logout started process. 5 sec after invoke user is removed automatically.
	 * 
	 * @param userId
	 *            is the user id.
	 */
	public void beginLogout(String userId) {
		if (userId != null) {
			synchronized (preprocessing) {
				boolean processing = isProcessing(userId);
				if (!processing) {
					preprocessing.add(userId);
					// start the process and remove the processing id, since it is expected all tabs
					// had finished the logout initiating process
					logoutTimer.schedule(new RemoveCachedProcess(userId), 5000L);
				}
			}
		}
	}

	/**
	 * Is the user currently processed.
	 * 
	 * @param userId
	 *            is the user id
	 * @return true if it is still in local cache, false if user is not in cache
	 */
	public boolean isProcessing(String userId) {
		if (userId != null) {
			synchronized (preprocessing) {
				if (preprocessing.contains(userId)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Explicitly remove user from cache.
	 * 
	 * @param userId
	 *            is the user id
	 */
	public void finishLogout(String userId) {
		if (userId != null) {
			synchronized (preprocessing) {
				logoutTimer.schedule(new RemoveCachedProcess(userId), 2000L);
			}
		}
	}

	/**
	 * Register a session to its idp index. If the same index exists it is replaced and the old
	 * value is returned
	 * 
	 * @param sessionIndex
	 *            the session index
	 * @param session
	 *            the session to link
	 * @return null if the value is not added or if the index has not been presented in the register
	 */
	public HttpSession registerSession(String sessionIndex, HttpSession session) {
		if ((sessionIndex != null) && (session != null)) {
			return sessions.put(sessionIndex, session);
		}
		return null;
	}

	/**
	 * Gets the session for the given index id.
	 * 
	 * @param sessionIndex
	 *            the session index
	 * @return the session for the key or null if not found or sessionIndex is null
	 */
	public HttpSession getSession(String sessionIndex) {
		return sessionIndex != null ? sessions.get(sessionIndex) : null;
	}

	/**
	 * Unregister session from the register.
	 * 
	 * @param sessionIndex
	 *            the session index
	 * @return the session that has been linked to the key or null if not found or sessionIndex is
	 *         null
	 */
	public HttpSession unregisterSession(String sessionIndex) {
		return sessionIndex != null ? sessions.remove(sessionIndex) : null;
	}

	/**
	 * Gets a unique identifier based on the request and current user.
	 * 
	 * @param currentUser
	 *            is the current user if null - null is returned
	 * @param request
	 *            is the request for servlet
	 * @return the id for the user or/and browser
	 */
	public String getClientId(User currentUser, HttpServletRequest request) {
		if (!useMultiBrowserMode) {
			return request.getSession().getId();
		}
		String header = request.getHeader("User-Agent");
		return header + DELIMITER + request.getSession().getId();
	}

	/**
	 * On login user add it to the tracking list of users. Store the session and user info. Removing
	 * information after timeout period.
	 * 
	 * @param session
	 *            the session to register
	 * @param userId
	 *            the user id
	 * @param clientId
	 *            the client id (browser info)
	 */
	public void trackUser(HttpSession session, String userId, String clientId) {
		if ((userId == null) || (clientId == null) || (session == null)) {
			LOGGER.warn("Missing required arguments: session (" + session + "), username ("
					+ userId + ") client identificator (" + clientId + ")");
		}
		synchronized (users) {
			StringPair loginInfo = new StringPair(userId, clientId);
			LOGGER.trace("Adding logged-in user: '" + loginInfo + "' and session id:"
					+ session.getId());
			users.put(session, loginInfo);
		}
	}

	/**
	 * On logout user method should be invoked on session invalidation or logging out user.
	 * 
	 * @param session
	 *            the session to remove
	 */
	public void untrackUser(HttpSession session) {
		if (session != null) {
			synchronized (users) {
				StringPair removed = users.remove(session);
				LOGGER.trace("Removing logged-out user: '" + (removed == null ? "-" : removed)
						+ "' and session id:" + session.getId());
			}
		}
	}
}
