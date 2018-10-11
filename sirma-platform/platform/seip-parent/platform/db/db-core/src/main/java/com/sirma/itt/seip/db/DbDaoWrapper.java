package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;

/**
 * Implementation of {@link DbDao} that wraps 2 other {@link DbDao} implementations
 *
 * @author BBonev
 */
public class DbDaoWrapper implements DbDao {

	private final DbDao primary;
	private final DbDao secondary;

	/**
	 * Instantiates a new db dao wrapper.
	 *
	 * @param primary
	 *            the primary db dao to chain. This dao will be called first for all operations
	 * @param secondary
	 *            the secondary db dao to chain. This dao will be called after the successful operation of the first
	 *            dao. If the first fails to execute then the second will not be called
	 */
	DbDaoWrapper(DbDao primary, DbDao secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		E result = primary.saveOrUpdate(entity);
		secondary.saveOrUpdate(entity);
		return result;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		E result = primary.saveOrUpdate(entity, oldEntity);
		secondary.saveOrUpdate(entity, oldEntity);
		return result;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		E found = primary.find(clazz, id);
		if (found == null) {
			found = secondary.find(clazz, id);
		}
		return found;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		E result = primary.refresh(entity);
		secondary.refresh(entity);
		return result;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdateInNewTx(E entity) {
		E result = primary.saveOrUpdateInNewTx(entity);
		secondary.saveOrUpdateInNewTx(entity);
		return result;
	}

	@Override
	public <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId) {
		int result = primary.delete(clazz, entityId);
		secondary.delete(clazz, entityId);
		return result;
	}

	@Override
	public <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId,
			boolean softDelete) {
		int result = primary.delete(clazz, entityId, softDelete);
		secondary.delete(clazz, entityId, softDelete);
		return result;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		List<R> result1 = primary.fetchWithNamed(namedQuery, params);
		List<R> result2 = secondary.fetchWithNamed(namedQuery, params);
		List<R> result = new ArrayList<>(result1.size() + result2.size());
		result.addAll(result1);
		result.addAll(result2);
		return result;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, int skip,
			int limit) {
		List<R> result1 = primary.fetchWithNamed(namedQuery, params, skip, limit);
		List<R> result2 = secondary.fetchWithNamed(namedQuery, params, skip, limit);
		List<R> result = new ArrayList<>(result1.size() + result2.size());
		result.addAll(result1);
		result.addAll(result2);
		return result;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		List<R> result1 = primary.fetch(query, params);
		List<R> result2 = secondary.fetch(query, params);
		List<R> result = new ArrayList<>(result1.size() + result2.size());
		result.addAll(result1);
		result.addAll(result2);
		return result;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit) {
		List<R> result1 = primary.fetch(query, params, skip, limit);
		List<R> result2 = secondary.fetch(query, params, skip, limit);
		List<R> result = new ArrayList<>(result1.size() + result2.size());
		result.addAll(result1);
		result.addAll(result2);
		return result;
	}

	@Override
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		int result = primary.executeUpdate(namedQuery, params);
		secondary.executeUpdate(namedQuery, params);
		return result;
	}

	@Override
	public <E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery, List<E> params) {
		int result = primary.executeUpdateInNewTx(namedQuery, params);
		secondary.executeUpdateInNewTx(namedQuery, params);
		return result;
	}

}
