package com.sirma.sep.model.management;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.model.management.persistence.ModelChangeEntity;

/**
 * Fake instance for the {@link DbDao} that can store, retrieve and search for {@link ModelChangeEntity}s
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/08/2018
 */
public class DbDaoFake implements DbDao {

	private AtomicLong index = new AtomicLong();
	private Map<Long, ModelChangeEntity> entities = new LinkedHashMap<>();

	private Optional<ModelChangeEntity> find(Long id) {
		if (id == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(entities.get(id));
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		Long id = (Long) entity.getId();
		Optional<ModelChangeEntity> changeEntity = find(id);
		if (changeEntity.isPresent()) {
			entities.put(id, (ModelChangeEntity) entity);
		} else {
			store((ModelChangeEntity) entity);
		}
		return entity;
	}

	private void store(ModelChangeEntity entity) {
		long id = index.incrementAndGet();
		entity.setId(id);
		entities.put(id, entity);
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		return clazz.cast(entities.get(Long.valueOf(id.toString())));
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdateInNewTx(E entity) {
		return saveOrUpdate(entity);
	}

	@Override
	public <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId,
			boolean softDelete) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		Predicate<ModelChangeEntity> entityPredicate;
		Function<ModelChangeEntity, R> mapper = entity -> (R) entity;
		if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_CHANGES_SINCE_KEY)) {
			Long version = (Long) getParam(params, "version");
			entityPredicate = entity -> entity.getAppliedVersion() != null
					&& entity.getAppliedVersion() > version
					&& entity.getDeployedOn() == null;
		} else if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_CHANGES_BY_REQUEST_ID_KEY)) {
			Object requestId = getParam(params, "requestId");
			entityPredicate = entity -> requestId.equals(entity.getRequestId()) && entity.getAppliedOn() == null;
		} else if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE_KEY)) {
			String nodeAddress = getParam(params, "nodeAddress").toString();
			String cleanAddress = nodeAddress.substring(0, nodeAddress.length() - 2);
			Long version = (Long) getParam(params, "version");
			entityPredicate = entity -> entity.getPath().startsWith(cleanAddress) && entity.getDeployedOn() == null
					&& entity.getAppliedVersion() <= version;
		} else if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_LAST_KNOWN_MODEL_VERSION_KEY)) {
			if (entities.isEmpty()) {
				return Collections.emptyList();
			}
			return Collections.singletonList((R) (Long) entities.values()
					.stream()
					.filter(entity -> entity.getAppliedVersion() != null)
					.mapToLong(ModelChangeEntity::getAppliedVersion)
					.max().orElse(0L));
		} else if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION_KEY)) {
			Long version = (Long) getParam(params, "version");
			Collection statuses = (Collection) getParam(params, "status");
			entityPredicate = entity -> entity.getAppliedVersion() != null
					&& entity.getAppliedVersion() <= version
					&& entity.getDeployedOn() == null
					&& statuses.contains(entity.getStatus());
			mapper = entity -> (R) entity.getPath();
		} else if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_KEY)) {
			Long version = (Long) getParam(params, "version");
			Collection statuses = (Collection) getParam(params, "status");
			entityPredicate = entity -> entity.getAppliedVersion() != null
					&& entity.getAppliedVersion() <= version
					&& entity.getDeployedOn() == null
					&& statuses.contains(entity.getStatus());
		} else {
			throw new UnsupportedOperationException("Query " + namedQuery + " is not supported, yet.");
		}
		return getFiltered(entityPredicate, mapper);
	}

	private <R> List<R> getFiltered(Predicate<ModelChangeEntity> filter, Function<ModelChangeEntity, R> mapper) {
		return entities.values().stream().filter(Objects::nonNull).filter(filter).map(mapper).collect(Collectors.toList());
	}

	private <E extends Pair<String, Object>> Object getParam(List<E> params, String name) {
		return params.stream()
				.filter(p -> p.getFirst().equals(name))
				.findFirst()
				.map(Pair::getSecond)
				.orElseThrow(IllegalArgumentException::new);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, int skip,
			int limit) {
		return (List<R>) fetchWithNamed(namedQuery, params).subList(skip, skip + limit);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNative(String query, List<E> params) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		Consumer<ModelChangeEntity> updater;
		Predicate<ModelChangeEntity> entityPredicate;
		if (EqualsHelper.nullSafeEquals(namedQuery, ModelChangeEntity.UPDATE_AS_DEPLOYED_KEY)) {
			Date deployedOn = (Date) getParam(params, "deployedOn");
			String status = (String) getParam(params, "status");
			Collection<Long> ids = (Collection<Long>) getParam(params, "ids");

			entityPredicate = entity -> entity.getDeployedOn() == null && ids.contains(entity.getId());
			updater = entity -> {
				entity.setDeployedOn(deployedOn);
				entity.setStatus(status);
			};
		} else {
			throw new UnsupportedOperationException("Query " + namedQuery + " is not supported, yet.");
		}
		return (int) getFiltered(entityPredicate, Function.identity()).stream().peek(updater).count();
	}

	@Override
	public <E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery, List<E> params) {
		return executeUpdate(namedQuery, params);
	}
}
