package com.sirma.itt.emf.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.exceptions.CmfDatabaseException;

/**
 * Implementation of the service {@link DbDao}. Providing base operations for working with
 * relational database. If the persistence context should be changed the class could be extended and
 * the method {@link #getEntityManager()} overridden to return the proper persistence context.
 * 
 * @author BBonev
 */
@Stateless
public class BaseDbDaoImpl extends AbstractDbDao {

	private static final String GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS = "Going to execute fetch with named query: {} with params: {}";

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseDbDaoImpl.class);

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3292198361766280754L;
	/** The entity manager. */
	@PersistenceContext(unitName = PERSISTENCE_UNIT_NAME, type = PersistenceContextType.TRANSACTION)
	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity) {
		try {
			E merge = entity;
			if (SequenceEntityGenerator.isPersisted(entity)) {
				if (!getEntityManager().contains(entity)) {
					merge = getEntityManager().merge(entity);
				} else {
					LOGGER.debug("Ignoring EntityManager.merge() for {}={}", entity.getClass()
							.getSimpleName(), entity.getId());
				}
			} else {
				getEntityManager().persist(entity);
				// this is not transaction
				SequenceEntityGenerator.persisted(entity);
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Saved entity {} with ID={}", entity.getClass(), merge.getId());
			}
			return merge;
		} catch (Exception exc) {
			throw new CmfDatabaseException("An error has occurd with the database: "
					+ exc.getMessage(), exc);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity, E oldEntity) {
		// the relational implementation does not benefit from the old entity. It's just ignored
		return saveOrUpdate(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, E extends Entity<S>> E find(Class<E> clazz, Object id) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("Finding entity with class: %s and id %s",
					clazz.getSimpleName(), id));
		}
		try {
			return getEntityManager().find(clazz, id);
		} catch (Exception e) {
			throw new CmfDatabaseException("An error has occurd with the database!", e);
		}
	}

	/**
	 * Gets the entity manager.
	 * 
	 * @return the entity manager
	 */
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable, E extends Entity<S>> E refresh(E entity) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Refreshing object of type " + entity.getClass());
		}
		// if the entity is not persisted according to the manager-a then we cannot expect the
		// method to return anything
		if (SequenceEntityGenerator.isPersisted(entity)) {
			getEntityManager().refresh(entity);
		}
		return entity;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery,
			List<E> params) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS, namedQuery, params);
		}
		Query query = getEntityManager().createNamedQuery(namedQuery);
		for (E e : params) {
			if (e instanceof Triplet) {
				Triplet<String, Date, TemporalType> t = (Triplet) e;
				query.setParameter(t.getFirst(), t.getSecond(), t.getThird());
			} else {
				query.setParameter(e.getFirst(), e.getSecond());
			}
		}
		List list = query.getResultList();
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <R, E extends Pair<String, Object>> List<R> fetch(String namedQuery, List<E> params) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(GOING_TO_EXECUTE_FETCH_WITH_NAMED_QUERY_WITH_PARAMS, namedQuery, params);
		}
		Query query = getEntityManager().createQuery(namedQuery);
		for (E e : params) {
			if (e instanceof Triplet) {
				Triplet<String, Date, TemporalType> t = (Triplet) e;
				query.setParameter(t.getFirst(), t.getSecond(), t.getThird());
			} else {
				query.setParameter(e.getFirst(), e.getSecond());
			}
		}
		List list = query.getResultList();
		if (list == null) {
			return Collections.emptyList();
		}
		return list;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Going to execute update with named query: " + namedQuery
					+ " with params: " + params);
		}
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <S extends Serializable, E extends Entity<S>> void delete(Class<E> clazz,
			Serializable entityId) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Deleting entity {} with id={}", clazz, entityId);
		}

		E e = find(clazz, entityId);
		if (e != null) {
			getEntityManager().remove(e);
		}
	}
}
