package com.sirma.itt.seip.rest.secirity;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * Holds cache with JWT tokens as keys and {@link TokenValue}s as values containing various properties.
 *
 * @author smustafov
 */
@ApplicationScoped
public class SecurityTokensHolder {

	// TODO: move to cache
	private Map<String, TokenValue> tokensMap = new ConcurrentHashMap<>(1024);

	/**
	 * Adds tokens to the cache.
	 *
	 * @param jwtToken
	 *            the jwt token as key
	 * @param samlToken
	 *            the saml token as value to the key
	 * @param sessionIndex
	 *            the session index
	 */
	public void addToken(String jwtToken, String samlToken, String sessionIndex) {
		tokensMap.put(jwtToken, new TokenValue(samlToken, sessionIndex));
	}

	/**
	 * Gets saml token by given jwt token from the cache. If not found returns null.
	 *
	 * @param jwtToken
	 *            the jwt token
	 * @return saml token by given jwt token
	 */
	public String getSamlToken(String jwtToken) {
		TokenValue value = tokensMap.get(jwtToken);
		if (value != null) {
			return value.getSamlToken();
		}
		return null;
	}

	/**
	 * Fetch JWT using the provided saml ticket. This is reverse lookup for the manager so the complexity for fetching a
	 * value is O(n).
	 *
	 * @param samlTicket
	 *            to fetch the corresponding JW token
	 * @return the found token if any
	 */
	public Optional<String> getJwtToken(String samlTicket) {
		return tokensMap
				.entrySet()
					.stream()
					.filter(entry -> nullSafeEquals(entry.getValue().getSamlToken(), samlTicket))
					.map(entry -> entry.getKey())
					.findFirst();
	}

	/**
	 * Removes token by given jwt token from the cache.
	 *
	 * @param jwtToken
	 *            the jwt token
	 */
	public void removeByJwtToken(String jwtToken) {
		tokensMap.remove(jwtToken);
	}

	/**
	 * Returns session index by jwt token.
	 *
	 * @param jwtToken
	 *            the jwt token
	 * @return the session index, otherwise null
	 */
	public String getSessionIndex(String jwtToken) {
		if (jwtToken == null) {
			return null;
		}
		TokenValue value = tokensMap.get(jwtToken);
		if (value != null) {
			return value.getSessionIndex();
		}
		return null;
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
		if (StringUtils.isNullOrEmpty(samlToken)) {
			return;
		}

		String jwt = tokensMap.entrySet().stream()
			.filter(entity -> entity.getValue().getSamlToken().equals(samlToken))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElse(null);

		String sessionIndex = getSessionIndex(jwt);
		removeBySessionIndex(sessionIndex);
	}

	/**
	 * Removes tokens by session index.
	 *
	 * @param sessionIndex
	 *            the session index
	 */
	public void removeBySessionIndex(String sessionIndex) {
		tokensMap.entrySet().removeIf(entry -> entry.getValue().getSessionIndex().equals(sessionIndex));
	}

	private static class TokenValue {

		private String samlToken;
		private String sessionIndex;

		/**
		 * Creates new token value.
		 *
		 * @param samlToken
		 *            the saml token
		 * @param sessionIndex
		 *            the session index
		 */
		public TokenValue(String samlToken, String sessionIndex) {
			this.samlToken = samlToken;
			this.sessionIndex = sessionIndex;
		}

		/**
		 * Getter method for samlToken.
		 *
		 * @return the samlToken
		 */
		public String getSamlToken() {
			return samlToken;
		}

		/**
		 * Getter method for sessionIndex.
		 *
		 * @return the sessionIndex
		 */
		public String getSessionIndex() {
			return sessionIndex;
		}

	}

}
