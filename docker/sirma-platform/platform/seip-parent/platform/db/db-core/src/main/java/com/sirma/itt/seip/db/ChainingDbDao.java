package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;

/**
 * {@link DbDao} extension to provide means of chaining several DAOs one after another. The newly defined methods accept
 * the results from the previous dao instance for the next to filter and append if needed. Mainly used by
 * {@link com.sirma.itt.seip.db.DbDaoWrapper}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/10/2018
 */
public interface ChainingDbDao extends DbDao {
	/**
	 * Find an entity that matches the given type and identifier only if the given previous result is null.
	 *
	 * @param <E> the element type
	 * @param clazz the clazz
	 * @param id the id
	 * @param previous the previous entity to chain
	 * @return the e
	 */
	<E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id, Object previous);

	/**
	 * Fetch a data from the underlying database using named query and append the results to the one from the
	 * previous {@link DbDao} search.
	 *
	 * @param <R> the return type
	 * @param <E> the parameter type
	 * @param namedQuery the named query to execute
	 * @param params the parameters to pass to query
	 * @param previous the previous entities to chain
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, List<R> previous);

	/**
	 * Fetch a data from the underlying database using named query and append the results to the one from the
	 * previous {@link DbDao} search.
	 *
	 * @param <R> the return type
	 * @param <E> the parameter type
	 * @param namedQuery the named query to execute
	 * @param params the parameters to pass to query
	 * @param previous the previous entities to chain
	 * @param skip the results to skip
	 * @param limit the limit of results to return
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, List<R> previous,
			int skip, int limit);

	/**
	 * Fetch a data from the underlying database using dynamic query and append the results to the one from the
	 * previous {@link DbDao} search.
	 *
	 * @param <R> the return type
	 * @param <E> the parameter type
	 * @param query the query to execute
	 * @param params the parameters to pass to query
	 * @param previous the previous entities to chain
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, List<R> previous);

	/**
	 * Fetch a data from the underlying database using dynamic query and append the results to the one from the
	 * previous {@link DbDao} search.
	 *
	 * @param <R> the return type
	 * @param <E> the parameter type
	 * @param query the query to execute
	 * @param params the parameters to pass to query
	 * @param previous the previous entities to chain
	 * @param skip the results to skip
	 * @param limit the limit of results to return
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, List<R> previous, int skip,
			int limit);

	/**
	 * Fetch a data from the underlying database using native query and append the results to the one from the
	 * previous {@link DbDao} search.
	 * <p>
	 * IMPORTANT: This is not the recommended way for fetching results but some functionality may require running native
	 * queries for tables that cannot be mapped to {@link javax.persistence.Entity}.<br>
	 * Use {@link #fetch(String, List, List)} and {@link #fetchWithNamed(String, List, List)} along with their related
	 * methods for general use.
	 *
	 * @param <R> the return type
	 * @param <E> the parameter type
	 * @param query the query to execute
	 * @param params the parameters to pass to query
	 * @param previous the previous entities to chain
	 * @return the list of results
	 */
	<R, E extends Pair<String, Object>> List<R> fetchWithNative(String query, List<E> params, List<R> previous);
}
