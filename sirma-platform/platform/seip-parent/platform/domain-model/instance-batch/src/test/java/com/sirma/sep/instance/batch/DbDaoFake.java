package com.sirma.sep.instance.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Fake database dao used in batch tests
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/01/2019
 */
@Singleton
public class DbDaoFake implements DbDao {

	private Map<String, BatchJobEntity> jobs = new LinkedHashMap<>();
	private Map<String, List<BatchEntity>> jobData = new LinkedHashMap<>();

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		if (entity instanceof BatchJobEntity) {
			jobs.put(((BatchJobEntity) entity).getJobInstanceId(), (BatchJobEntity) entity);
		} else if (entity instanceof BatchEntity) {
			jobData.computeIfAbsent(((BatchEntity) entity).getJobInstanceId(), id -> new LinkedList<>())
					.add((BatchEntity) entity);
		} else {
			throw new IllegalArgumentException("Entity type " + entity + " not supported");
		}
		return entity;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		return null;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		return null;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		return null;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdateInNewTx(E entity) {
		return null;
	}

	@Override
	public <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId,
			boolean softDelete) {
		return 0;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		switch (namedQuery) {
			case BatchEntity.QUERY_JOBS_INFO_KEY:
			case BatchEntity.QUERY_JOB_DATA_KEY:
				return queryJobData(namedQuery, params);
			case BatchJobEntity.QUERY_ALL_JOBS_KEY:
			case BatchJobEntity.QUERY_JOB_BY_INSTANCE_ID_KEY:
				return queryJobs(namedQuery, params);
			default:
				throw new IllegalArgumentException("Unsupported query " + namedQuery);
		}
	}

	@SuppressWarnings("unchecked")
	private <R, E extends Pair<String, Object>> List<R> queryJobs(String namedQuery, List<E> params) {
		if (namedQuery.equals(BatchJobEntity.QUERY_ALL_JOBS_KEY)) {
			return (List<R>) new ArrayList<>(jobs.values());
		} else if (namedQuery.equals(BatchJobEntity.QUERY_JOB_BY_INSTANCE_ID_KEY)) {
			String jobInstanceId = getParam(params, "jobInstanceId").toString();
			BatchJobEntity entity = jobs.get(jobInstanceId);
			if (entity == null) {
				return Collections.emptyList();
			}
			return Collections.singletonList((R) entity);
		}
		throw new IllegalArgumentException("Named query not supported " + namedQuery);
	}

	@SuppressWarnings("unchecked")
	private <R, E extends Pair<String, Object>> List<R> queryJobData(String namedQuery, List<E> params) {
		if (namedQuery.equals(BatchEntity.QUERY_JOBS_INFO_KEY)) {
			Boolean processed = (Boolean) getParam(params, "processed");
			return (List<R>) jobData.values()
					.stream()
					.flatMap(Collection::stream)
					.filter(entity -> entity.isProcessed() == processed)
					.collect(Collectors.groupingBy(BatchEntity::getJobInstanceId, Collectors.counting()))
					.entrySet()
					.stream()
					.map(e -> new Object[] { e.getKey(), e.getValue() })
					.collect(Collectors.toList());
		} else if (namedQuery.equals(BatchEntity.QUERY_JOB_DATA_KEY)) {
			String jobId = getParam(params, "jobId").toString();
			return (List<R>) jobData.getOrDefault(jobId, Collections.emptyList())
					.stream()
					.map(BatchEntity::getInstanceId)
					.collect(Collectors.toList());
		}
		throw new IllegalArgumentException("Named query not supported " + namedQuery);
	}

	private <E extends Pair<String, Object>> Object getParam(List<E> params, String name) {
		return params.stream()
				.filter(p -> p.getFirst().equals(name))
				.findFirst()
				.map(Pair::getSecond)
				.orElseThrow(IllegalArgumentException::new);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params, int skip,
			int limit) {
		return (List<R>) fetchWithNamed(namedQuery, params).subList(skip, skip + limit);
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		return null;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit) {
		return null;
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNative(String query, List<E> params) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		if (namedQuery.equals(BatchEntity.UPDATE_AS_PROCESSED_KEY)) {
			String jobId = getParam(params, "jobId").toString();
			Set<String> instanceIds = new HashSet<>((Collection<String>) getParam(params, "instanceIds"));
			return (int) jobData.getOrDefault(jobId, Collections.emptyList())
					.stream()
					.filter(entity -> instanceIds.contains(entity.getInstanceId()))
					.peek(entity -> entity.setProcessed(true))
					.count();
		} else if (namedQuery.equals(BatchEntity.DELETE_DATA_BY_JOB_ID_KEY)) {
			String jobId = getParam(params, "jobId").toString();
			List<BatchEntity> entities = jobData.remove(jobId);
			if (entities == null) {
				return 0;
			}
			return entities.size();
		}
		return 0;
	}

	@Override
	public <E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery, List<E> params) {
		return 0;
	}

	List<BatchEntity> getJobData(String jobId) {
		return jobData.getOrDefault(jobId, Collections.emptyList());
	}

	Optional<BatchJobEntity> getJob(String jobId) {
		return Optional.ofNullable(jobs.get(jobId));
	}
}
