package com.sirma.itt.emf.db;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;

/**
 * Base layer for accessing the underlying database.
 * 
 * @author BBonev
 */
public interface DbDao {

	/** Constant for the default persistence unit name of CMF. */
	String PERSISTENCE_UNIT_NAME = "EMF-Primary";

	/** The Constant DATASOURCE_NAME. */
	String DATASOURCE_NAME = "java:jboss/datasources/cmfDS";

	/**
	 * Save or update.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	<S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity);

	/**
	 * Save or update the given entity. If the application provides an previous version of the
	 * entity it will be used to create diff on save. Useful for semantic implementation.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @param oldEntity
	 *            the old entity instance. If <code>null</code> the method will be the same as
	 *            calling the {@link #saveOrUpdate(Entity)} method.
	 * @return the saved instance.
	 */
	<S extends Serializable, E extends Entity<S>> E saveOrUpdate(E entity, E oldEntity);

	/**
	 * Find.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the clazz
	 * @param id
	 *            the id
	 * @return the e
	 */
	<S extends Serializable, E extends Entity<S>> E find(Class<E> clazz, Object id);

	/**
	 * Refreshes the given entity from the database.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the refreshed entity
	 */
	<S extends Serializable, E extends Entity<S>> E refresh(E entity);

	/**
	 * Save or update in new tx.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the e
	 */
	<S extends Serializable, E extends Entity<S>> E saveOrUpdateInNewTx(E entity);

	/**
	 * Save or update the given entity in new transaction. If the application provides an previous
	 * version of the entity it will be used to create diff on save. Useful for semantic
	 * implementation.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @param oldEntity
	 *            the old entity instance. If <code>null</code> the method will be the same as
	 *            calling the {@link #saveOrUpdate(Entity)} method.
	 * @return the saved instance.
	 */
	<S extends Serializable, E extends Entity<S>> E saveOrUpdateInNewTx(E entity, E oldEntity);

	/**
	 * Deletes entity instance from the DB.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the class of the entity to delete
	 * @param entityId
	 *            the entity id
	 */
	<S extends Serializable, E extends Entity<S>> void delete(Class<E> clazz, Serializable entityId);

	/**
	 * Deletes entity instance from the DB in new transaction.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the class of the entity to delete
	 * @param entityId
	 *            the entity id
	 */
	<S extends Serializable, E extends Entity<S>> void deleteInNewTx(Class<E> clazz,
			Serializable entityId);

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

	/**
	 * Invoke the given {@link Callable} in transaction.
	 * 
	 * @param <E>
	 *            the element type
	 * @param callable
	 *            the callable
	 * @return the e
	 */
	<E> E invokeInTx(Callable<E> callable);

	/**
	 * Invoke the given {@link Callable} in new transaction.
	 * 
	 * @param <E>
	 *            the element type
	 * @param callable
	 *            the callable
	 * @return the e
	 */
	<E> E invokeInNewTx(Callable<E> callable);

}
