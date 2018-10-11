package com.sirma.itt.seip.rest.secirity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * Persists {@link ActiveUserSession} by using {@link ActiveUserSessionsDao}.
 *
 * @author smustafov
 */
@ApplicationScoped
@Transactional
public class SecurityTokensHolder {

	@Inject
	private ActiveUserSessionsDao activeUserSessionsDao;

	/**
	 * Adds tokens to the db.
	 *
	 * @param jwtToken
	 *            the jwt token as key
	 * @param samlToken
	 *            the saml token as value to the key
	 * @param sessionIndex
	 *            the session index
	 * @param identityId
	 *            id of the authenticated identity
	 * @param identityProperties
	 *            properties of the authenticated identity
	 */
	public void addToken(String jwtToken, String samlToken, String sessionIndex, String identityId,
			Map<String, Serializable> identityProperties) {
		activeUserSessionsDao.save(createUserSession(jwtToken, samlToken, sessionIndex, identityId, identityProperties));
	}

	/**
	 * Gets saml token by given jwt token from the db. If not found returns null.
	 *
	 * @param jwtToken
	 *            the jwt token
	 * @return saml token by given jwt token
	 */
	public Optional<String> getSamlToken(String jwtToken) {
		ActiveUserSession session = activeUserSessionsDao.getByJwt(jwtToken);
		if (session != null) {
			return Optional.of(session.getSaml());
		}
		return Optional.empty();
	}

	/**
	 * Fetch JWT using the provided saml ticket.
	 *
	 * @param samlTicket
	 *            to fetch the corresponding JW token
	 * @return the found token if any
	 */
	public Optional<String> getJwtToken(String samlTicket) {
		ActiveUserSession securityTokens = activeUserSessionsDao.getBySaml(samlTicket);
		if (securityTokens != null) {
			return Optional.of(securityTokens.getJwt());
		}
		return Optional.empty();
	}

	/**
	 * Returns session index by jwt token.
	 *
	 * @param jwtToken
	 *            the jwt token
	 * @return the session index, otherwise null
	 */
	public Optional<String> getSessionIndex(String jwtToken) {
		if (StringUtils.isBlank(jwtToken)) {
			return Optional.empty();
		}

		ActiveUserSession securityTokens = activeUserSessionsDao.getByJwt(jwtToken);
		if (securityTokens != null) {
			return Optional.of(securityTokens.getSessionIndex());
		}
		return Optional.empty();
	}

	/**
	 * Finds tokens from jwt and removes them by session index.
	 *
	 * @param jwtToken
	 *            the jwt token
	 */
	public void removeByJwtToken(String jwtToken) {
		if (StringUtils.isBlank(jwtToken)) {
			return;
		}

		ActiveUserSession securityTokens = activeUserSessionsDao.getByJwt(jwtToken);
		if (securityTokens != null) {
			removeBySessionIndex(securityTokens.getSessionIndex());
		}
	}

	/**
	 * Removes tokens (both UI1 and UI2) by finding the session index associated with given saml token. Since the
	 * session index is same for both UIs its more appropriate to remove by it, so it doesn't matter from which ui is
	 * the logout.
	 *
	 * @param samlToken
	 *            the saml token
	 */
	public void removeBySamlToken(String samlToken) {
		if (StringUtils.isBlank(samlToken)) {
			return;
		}

		ActiveUserSession securityTokens = activeUserSessionsDao.getBySaml(samlToken);
		if (securityTokens != null) {
			removeBySessionIndex(securityTokens.getSessionIndex());
		}
	}

	/**
	 * Removes tokens by session index.
	 *
	 * @param sessionIndex
	 *            the session index
	 */
	public void removeBySessionIndex(String sessionIndex) {
		activeUserSessionsDao.deleteBySessionIndex(sessionIndex);
	}

	/**
	 * Returns all the tokens stored in the db.
	 *
	 * @return all the tokens stored in the db
	 */
	public Collection<ActiveUserSession> getAll() {
		return activeUserSessionsDao.getAll();
	}

	private static ActiveUserSession createUserSession(String jwtToken, String samlToken, String sessionIndex,
			String identityId, Map<String, Serializable> identityProperties) {
		ActiveUserSession userSession = new ActiveUserSession();
		userSession.setJwt(jwtToken);
		userSession.setSaml(samlToken);
		userSession.setSessionIndex(sessionIndex);
		userSession.setIdentityId(identityId);
		userSession.setIdentityProperties(convertPropertiesToJsonSring(identityProperties));
		userSession.setLoggedInDate(new Date());
		return userSession;
	}

	private static String convertPropertiesToJsonSring(Map<String, Serializable> identityProperties) {
		JSONObject json = new JSONObject();
		identityProperties.forEach((key, value) -> JsonUtil.addToJson(json, key, value));
		return json.toString();
	}

}
