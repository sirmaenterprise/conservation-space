package com.sirma.sep.instance.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Default implementation of {@link BatchDataService} that works with a database persistence
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
@Singleton
class DatabaseBatchDataService implements BatchDataService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DbDao dbDao;

	@Inject
	private BatchProperties batchProperties;

	@Override
	public int getJobProgress(String jobId) {
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>("processed", Boolean.TRUE));
		args.add(new Pair<>(BatchProperties.JOB_ID, jobId));
		List<Number> count = dbDao.fetchWithNamed(BatchEntity.QUERY_JOB_INFO_KEY, args);
		if (count.isEmpty()) {
			return 0;
		}
		return count.get(0).intValue();
	}

	@Override
	public void addData(String jobName, String jobId, String data) {
		dbDao.saveOrUpdate(new BatchEntity(jobName, jobId, data));
	}

	@Override
	public List<String> getBatchData(long jobExecutionId, int offset, int itemsToLoad) {
		String jobId = batchProperties.getJobId(jobExecutionId);
		return dbDao.fetchWithNamed(BatchEntity.QUERY_JOB_DATA_KEY,
				Collections.singletonList(new Pair<>(BatchProperties.JOB_ID, jobId)), offset, itemsToLoad);
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void markJobDataAsProcessed(long jobExecutionId, List<String> processedIds) {
		if (isEmpty(processedIds)) {
			return;
		}
		String jobId = batchProperties.getJobId(jobExecutionId);
		int updated = dbDao.executeUpdate(BatchEntity.UPDATE_AS_PROCESSED_KEY,
				Arrays.asList(new Pair<>(BatchProperties.JOB_ID, jobId), new Pair<>("instanceIds", processedIds)));
		LOGGER.debug("Processed {} items by job {}", updated, jobId);
	}

	@Override
	@Transactional
	public void clearJobData(String jobExecutionId) {
		dbDao.executeUpdate(BatchEntity.DELETE_DATA_BY_JOB_ID_KEY,
				Collections.singletonList(new Pair<>(BatchProperties.JOB_ID, jobExecutionId)));
	}

	@Override
	@Transactional
	public void clearJobData(long jobExecutionId) {
		clearJobData(batchProperties.getJobId(jobExecutionId));
	}
}
