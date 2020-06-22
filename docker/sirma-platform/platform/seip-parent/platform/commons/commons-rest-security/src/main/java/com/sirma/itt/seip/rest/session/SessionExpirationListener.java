package com.sirma.itt.seip.rest.session;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.sirma.itt.seip.rest.session.SessionManager;

/**
 * The listener interface for receiving session related events. On session destruction loggin information related to the
 * user is
 *
 * @see SessionExpirationEvent
 */
@WebListener
public class SessionExpirationListener implements HttpSessionListener {

	/** The session manager. */
	@Inject
	private SessionManager sessionManager;

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		// we dont care for session created (on login custom session info is tracked)
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		sessionManager.untrackUser(arg0.getSession());
	}
}