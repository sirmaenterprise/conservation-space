package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;

/**
 * Base layer for accessing the underlying database.
 *
 * @author BBonev
 */
public interface DbDao {

	/**
	 * Save or update.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	<E extends Entity<? extends Serializable>> E saveOrUpdate(E entity);

	/**
	 * Save or update the given entity. If the application provides an previous version of the entity it will be used to
	 * create diff on save. Useful for semantic implementation.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @param oldEntity
	 *            the old entity instance. If <code>null</code> the method will be the same as calling the
	 *            {@link #saveOrUpdate(Entity)} method.
	 * @return the saved instance.
	 */
	<E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity);

	/**
	 * Find.
	 *
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the clazz
	 * @param id
	 *            the id
	 * @return the e
	 */
	<E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id);

	/**
	 * Refreshes the given entity from the database.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the refreshed entity
	 */
	<E extends Entity<? extends Serializable>> E refresh(E entity);

	/**
	 * Save or update in new tx.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	<E extends Entity<? extends Serializable>> E saveOrUpdateInNewTx(E entity);

	/**
	 * Deletes entity instance from the DB.
	 *
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the class of the entity to delete
	 * @param entityId
	 *            the entity id
	 * @return the number of deleted entries
	 */
	default <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId) {
		return delete(clazz, entityId, true);
	}

	/**
	 * Deletes entity instance from the DB. If the entity and or model supports soft delete it should consider the value
	 * of the parameter {@code softDelete}.
	 *
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the class of the entity to delete
	 * @param entityId
	 *            the entity id
	 * @param softDelete
	 *            if the delete operation should be soft delete or hard
	 * @return the number of deleted entries
	 */
	<E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId, boolean softDelete);

	/**
	 * Fetch a data from the DB by executing the given named query.
	 *
	 * @param <R>
	 *            the return type
	 * @param <E>
	 *            the parameter type
	 * @param namedQuery
	 *            the named query to execute
	 * @param params
	 *            the parameters to pass to query
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params);

	/**
	 * Fetch a data from the DB by executing the given named query. Could set skip and limit params
	 *
	 * @param <R>
	 *            the return type
	 * @param <E>
	 *            the parameter type
	 * @param namedQuery
	 *            the named query to execute
	 * @param params
	 *            the parameters to pass to query
	 * @param skip
	 *            the results to skip
	 * @param limit
	 *            the limit of results to return
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, int skip, int limit);

	/**
	 * Fetch a data from the DB by executing the given dynamic query.
	 *
	 * @param <R>
	 *            the return type
	 * @param <E>
	 *            the parameter type
	 * @param query
	 *            the query to execute
	 * @param params
	 *            the parameters to pass to query
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params);

	/**
	 * Fetch a data from the DB by executing the given dynamic query.
	 *
	 * @param <R>
	 *            the return type
	 * @param <E>
	 *            the parameter type
	 * @param query
	 *            the query to execute
	 * @param params
	 *            the parameters to pass to query
	 * @param skip
	 *            the results to skip
	 * @param limit
	 *            the limit of results to return
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit);

	/**
	 * Fetch entries from the DB by executing the given native query. This will return all entries matching the query.
	 *
	 * IMPORTANT: This is not the recommended way for fetching results but some functionalities may require running native queries for
	 * tables that cannot be mapped to {@link javax.persistence.Entity}.
	 * Use {@link #fetch(String, List)} and {@link #fetchWithNamed(String, List)} along with their related methods for general use.
	 *
	 * @param <R>
	 *            the return type
	 * @param <E>
	 *            the parameter type
	 * @param query
	 *            the query to execute
	 * @param params
	 *            the parameters to pass to query
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNative(String query, List<E> params);

	/**
	 * Execute query update in a transaction.
	 *
	 * @param <E>
	 *            the element type
	 * @param namedQuery
	 *            the named query
	 * @param params
	 *            the parameters for the query
	 * @return the updated DB rows
	 */
	<E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params);

	/**
	 * Execute query update in new transaction.
	 *
	 * @param <E>
	 *            the element type
	 * @param namedQuery
	 *            the named query
	 * @param params
	 *            the parameters for the query
	 * @return the updated DB rows
	 */
	<E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery, List<E> params);

}
