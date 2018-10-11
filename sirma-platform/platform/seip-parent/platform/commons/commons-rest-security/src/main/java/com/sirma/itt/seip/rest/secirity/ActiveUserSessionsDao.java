package com.sirma.itt.seip.rest.secirity;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;

/**
 * Dao for {@link ActiveUserSession}.
 *
 * @author smustafov
 */
@Singleton
public class ActiveUserSessionsDao {

	@Inject
	@CoreDb
	private DbDao dbDao;

	/**
	 * Gets session info by saml token.
	 *
	 * @param saml
	 *            token with which to find all the tokens related to that one
	 * @return found tokens related to the given saml token
	 */
	public ActiveUserSession getBySaml(String saml) {
		return getOne(dbDao.fetchWithNamed(ActiveUserSession.QUERY_GET_BY_SAML_KEY,
				Collections.singletonList(new Pair<String, Object>("saml", saml))));
	}

	/**
	 * Gets session info by jwt token.
	 *
	 * @param jwt
	 *            token with which to find all the tokens related to that one
	 * @return found tokens related to the given jwt token
	 */
	public ActiveUserSession getByJwt(String jwt) {
		return getOne(dbDao.fetchWithNamed(ActiveUserSession.QUERY_GET_BY_JWT_KEY,
				Collections.singletonList(new Pair<String, Object>("jwt", jwt))));
	}

	private static ActiveUserSession getOne(Collection<ActiveUserSession> tokens) {
		if (!tokens.isEmpty()) {
			return tokens.iterator().next();
		}
		return null;
	}

	/**
	 * Finds tokens by session index. Note that there can be two {@link ActiveUserSession} persisted entries with the same
	 * session index, because of the old and new web. Both have the same session index but different jwt and saml
	 * tokens.
	 *
	 * @param sessionIndex
	 *            with which to find all the tokens associated with
	 * @return collection of found tokens associated with the given session index
	 */
	public Collection<ActiveUserSession> getBySessionIndex(String sessionIndex) {
		return dbDao.fetchWithNamed(ActiveUserSession.QUERY_GET_BY_SESSION_INDEX_KEY,
				Collections.singletonList(new Pair<String, Object>("sessionIndex", sessionIndex)));
	}

	/**
	 * Deletes all the security tokens by session index.
	 *
	 * @param sessionIndex
	 *            of authenticated identity
	 */
	public void deleteBySessionIndex(String sessionIndex) {
		dbDao.executeUpdate(ActiveUserSession.QUERY_DELETE_BY_SESSION_INDEX_KEY,
				Collections.singletonList(new Pair<String, Object>("sessionIndex", sessionIndex)));
	}

	/**
	 * Returns all the tokens stored in the db.
	 *
	 * @return all the tokens stored in the db
	 */
	public Collection<ActiveUserSession> getAll() {
		return dbDao.fetchWithNamed(ActiveUserSession.QUERY_GET_ALL_KEY, Collections.emptyList());
	}

	/**
	 * Saves given security tokens.
	 *
	 * @param tokens
	 *            to save
	 * @return the saved security tokens
	 */
	public ActiveUserSession save(ActiveUserSession tokens) {
		return dbDao.saveOrUpdate(tokens);
	}

	/**
	 * Deletes security tokens by id.
	 *
	 * @param id
	 *            of the security tokens
	 */
	public void deleteById(Long id) {
		dbDao.delete(ActiveUserSession.class, id);
	}

}
