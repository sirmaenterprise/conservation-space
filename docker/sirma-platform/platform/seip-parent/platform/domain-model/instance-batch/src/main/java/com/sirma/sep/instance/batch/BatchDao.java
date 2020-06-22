package com.sirma.sep.instance.batch;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Dao for managing batch job entities
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/01/2019
 */
class BatchDao {

	@Inject
	private DbDao dbDao;

	/**
	 * Retrieves information about all job
	 *
	 * @return job meta data
	 */
	Collection<JobInfo> getJobs() {
		List<BatchJobEntity> allJobsData = dbDao.fetchWithNamed(BatchJobEntity.QUERY_ALL_JOBS_KEY, Collections.emptyList());
		Map<String, JobInfo> jobs = allJobsData.stream()
				.map(toJobInfo())
				.collect(CollectionUtils.toIdentityMap(JobInfo::getId));

		List<Object[]> processed = dbDao.fetchWithNamed(BatchEntity.QUERY_JOBS_INFO_KEY,
				Collections.singletonList(new Pair<>("processed", Boolean.TRUE)));
		List<Object[]> notProcessed = dbDao.fetchWithNamed(BatchEntity.QUERY_JOBS_INFO_KEY,
				Collections.singletonList(new Pair<>("processed", Boolean.FALSE)));

		for (Object[] row : processed) {
			JobInfo jobInfo = jobs.computeIfAbsent(row[0].toString(), JobInfo::new);
			jobInfo.setProcessed(Integer.valueOf(row[1].toString()));
		}
		for (Object[] row : notProcessed) {
			JobInfo jobInfo = jobs.computeIfAbsent(row[0].toString(), JobInfo::new);
			jobInfo.setRemaining(Integer.valueOf(row[1].toString()));
		}
		return jobs.values();
	}

	/**
	 * Finds a job for a given job id
	 *
	 * @param jobId the job id to look for
	 * @return the found job or empty optional
	 */
	Optional<JobInfo> findJobData(String jobId) {
		return findJobDataInternal(jobId).map(toJobInfo());
	}

	private Function<BatchJobEntity, JobInfo> toJobInfo() {
		return entity -> {
			JobInfo jobInfo = new JobInfo(entity.getJobInstanceId());
			jobInfo.setName(entity.getJobName());
			jobInfo.setAlias(entity.getAlias());
			jobInfo.setExecutionId(entity.getExecutionId());
			jobInfo.setProperties(readProperties(entity.getProperties()));
			if (entity.getCreatedOn() != null) {
				jobInfo.setCreatedOn(ISO8601DateFormat.format(entity.getCreatedOn()));
			}
			if (entity.getUpdatedOn() != null) {
				jobInfo.setModifiedOn(ISO8601DateFormat.format(entity.getUpdatedOn()));
			}
			return jobInfo;
		};
	}

	private Optional<BatchJobEntity> findJobDataInternal(String jobId) {
		List<BatchJobEntity> jobData = dbDao.fetchWithNamed(BatchJobEntity.QUERY_JOB_BY_INSTANCE_ID_KEY,
				Collections.singletonList(new Pair<>("jobInstanceId", jobId)));
		if (jobData.isEmpty()) {
			return Optional.empty();
		}
		BatchJobEntity jobEntity = jobData.get(0);
		return Optional.of(jobEntity);
	}

	private Map<String, String> readProperties(String properties) {
		Map<String, Serializable> map = JSON.readObject(properties, JSON::jsonToMap);
		Map<String, String> result = CollectionUtils.createHashMap(map.size());
		map.forEach((k, v) -> result.put(k, v.toString()));
		return result;
	}

	/**
	 * Save or update information about batch job
	 * @param executionId the job execution id
	 * @param jobName the job name
	 * @param alias the job alias if known
	 * @param properties the job properties
	 */
	void persistJobData(long executionId, String jobName, String alias, Properties properties) {
		String jobId = properties.getProperty(BatchProperties.JOB_ID);
		BatchJobEntity entity = findJobDataInternal(jobId).orElseGet(BatchJobEntity::new);
		entity.setExecutionId(executionId);
		entity.setJobName(jobName);
		entity.setAlias(EqualsHelper.getOrDefault(StringUtils.trimToNull(alias), jobName));
		entity.setJobInstanceId(jobId);
		entity.setProperties(convertProperties(properties));
		if (entity.getId() == null) {
			entity.setCreatedOn(new Date());
		} else {
			entity.setUpdatedOn(new Date());
		}
		dbDao.saveOrUpdate(entity);
	}

	private String convertProperties(Properties properties) {
		try (StringWriter writer = new StringWriter();
				JsonGenerator generator = Json.createGenerator(writer)) {
			generator.writeStartObject();
			for (Object key : properties.keySet()) {
				String stringKey = key.toString();
				generator.write(stringKey, properties.getProperty(stringKey));
			}
			generator.writeEnd();
			generator.flush();
			return writer.toString();
		} catch (IOException e) {
			// should not happen as it's in memory and does not throw such expcetions
			throw new IllegalStateException(e);
		}
	}
}
