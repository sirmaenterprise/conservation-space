package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.exceptions.DatabaseException;

/**
 * Implementation of the service {@link DbDao}. Providing base operations for working with relational database. If the
 * persistence context should be changed the class could be extended and the method {@link #getEntityManager()}
 * overridden to return the proper persistence context.
 *
 * @author BBonev
 */
public abstract class BaseRelationalDbDao extends AbstractDbDao {

	private static final long serialVersionUID = 1264574088515127933L;
	private static final String GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS = "Going to execute fetch with named query: {} with params: {}";
	private static final String GOING_TO_EXECUTE_QUERY_WITH_PARAMS = "Going to execute query: {} with params: {}";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	@Transactional(TxType.REQUIRED)
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		if (Options.DO_NOT_PERSIST_IN_RD.isEnabled()) {
			return entity;
		}
		return saveOrUpdateInternal(entity);
	}

	/**
	 * Save or update internal.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	protected <E extends Entity<? extends Serializable>> E saveOrUpdateInternal(E entity) {
		try {
			E merge = entity;
			if (idManager.isPersisted(entity)) {
				if (!getEntityManager().contains(entity)) {
					merge = getEntityManager().merge(entity);
				} else {
					LOGGER.trace("Ignoring EntityManager.merge() for {}={}", entity.getClass().getSimpleName(),
							entity.getId());
				}
			} else {
				getEntityManager().persist(entity);
				// this is not transaction
				idManager.persisted(entity);
			}
			LOGGER.trace("Saved entity {} with ID={}", entity.getClass(), merge.getId());
			return merge;
		} catch (Exception exc) {
			throw new DatabaseException("An error has occurd with the database: " + exc.getMessage(), exc);
		}
	}

	@Override
	@Transactional(TxType.REQUIRED)
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		if (Options.DO_NOT_PERSIST_IN_RD.isEnabled()) {
			return entity;
		}
		// the relational implementation does not benefit from the old entity. It's just ignored
		return saveOrUpdateInternal(entity);
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		LOGGER.trace("Finding entity with class: {} and id {}", clazz.getSimpleName(), id);
		try {
			return getEntityManager().find(clazz, id);
		} catch (Exception e) {
			throw new DatabaseException("An error has occurd with the database!", e);
		}
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		LOGGER.trace("Refreshing object of type {}", entity.getClass());

		// if the entity is not persisted according to the manager-a then we cannot expect the
		// method to return anything
		if (idManager.isPersisted(entity)) {
			getEntityManager().refresh(entity);
		}
		return entity;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		LOGGER.trace(GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS, namedQuery, params);

		Query query = getEntityManager().createNamedQuery(namedQuery);
		return fetchInternal(query, params, 0, -1);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, int skip,
			int limit) {
		LOGGER.trace(GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS, namedQuery, params);

		Query query = getEntityManager().createNamedQuery(namedQuery);
		return fetchInternal(query, params, skip, limit);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String queryHQL, List<E> params) {
		LOGGER.trace(GOING_TO_EXECUTE_QUERY_WITH_PARAMS, queryHQL, params);

		Query query = getEntityManager().createQuery(queryHQL);
		return fetchInternal(query, params, 0, -1);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String queryHQL, List<E> params, int skip, int limit) {
		LOGGER.trace(GOING_TO_EXECUTE_QUERY_WITH_PARAMS, queryHQL, params);

		Query query = getEntityManager().createQuery(queryHQL);
		return fetchInternal(query, params, skip, limit);
	}

	/**
	 * Executes the query to the relating db
	 *
	 * @param <R>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param query
	 *            the query to execute (named or standard)
	 * @param params
	 *            - query params to inject
	 * @param skip
	 *            how many entries to skip - if <= 0 it is ignored
	 * @param limit
	 *            how many entries to select - if <= 0 it is ignored
	 * @return the resultset after select
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "static-method" })
	protected <R, E extends Pair<String, Object>> List<R> fetchInternal(Query query, List<E> params, int skip,
			int limit) {
		if (limit > 0) {
			query.setMaxResults(limit);
		}
		if (skip > 0) {
			query.setFirstResult(skip);
		}
		for (E e : params) {
			if (e instanceof Triplet) {
				Triplet<String, Date, TemporalType> t = (Triplet) e;
				query.setParameter(t.getFirst(), t.getSecond(), t.getThird());
			} else {
				query.setParameter(e.getFirst(), e.getSecond());
			}
		}
		List<R> list = null;
		try {
			list = query.getResultList();
		} catch (Exception e) {
			LOGGER.error("Failed to query database", e);
			return Collections.emptyList();
		}
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Transactional(TxType.REQUIRED)
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		LOGGER.trace("Going to execute update with named query: {} with params: {}", namedQuery, params);
		Query query = getEntityManager().createNamedQuery(namedQuery);
		for (E e : params) {
			if (e instanceof Triplet) {
				Triplet<String, Date, TemporalType> t = (Triplet) e;
				query.setParameter(t.getFirst(), t.getSecond(), t.getThird());
			} else {
				query.setParameter(e.getFirst(), e.getSecond());
			}
		}
		return query.executeUpdate();
	}

	@Override
	protected <E extends Entity<? extends Serializable>> int deleteInternal(Class<E> clazz, Serializable entityId, boolean softDelete) {
		if (Options.DO_NOT_PERSIST_IN_RD.isEnabled()) {
			return 0;
		}
		LOGGER.trace("Deleting entity {} with id={}", clazz, entityId);

		E e = find(clazz, entityId);
		if (e != null) {
			getEntityManager().remove(e);
			return 1;
		}
		return 0;
	}

	/**
	 * Gets the entity manager to be used by the current implementation
	 *
	 * @return the entity manager
	 */
	protected abstract EntityManager getEntityManager();
}
