package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;

/**
 * Test for {@link DatabaseBatchDataService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/07/2017
 */
public class DatabaseBatchDataServiceTest {

	@InjectMocks
	private DatabaseBatchDataService batchRuntimeService;
	@Mock
	private DbDao dbDao;
	@Mock
	private BatchProperties batchProperties;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(batchProperties.getJobId(anyLong())).thenReturn("jobId");
	}

	@Test
	public void addData() throws Exception {
		batchRuntimeService.addData("jobName", "jobId", "instanceId");

		verify(dbDao).saveOrUpdate(new BatchEntity("jobName", "jobId", "instanceId"));
	}

	@Test
	public void getBatchData() throws Exception {

		when(dbDao.fetchWithNamed(BatchEntity.QUERY_JOB_DATA_KEY,
				Collections.singletonList(new Pair<>(BatchProperties.JOB_ID, "jobId")), 0, 10)).thenReturn(
				Arrays.asList("item1", "item2"));

		List<String> batchData = batchRuntimeService.getBatchData(1L, 0, 10);
		assertEquals(Arrays.asList("item1", "item2"), batchData);
	}

	@Test
	public void markJobDataAsProcessed_shouldDoNothingOnEmptyData() throws Exception {
		batchRuntimeService.markJobDataAsProcessed(1L, Collections.emptyList());

		verify(dbDao, never()).executeUpdate(anyString(), anyList());
	}

	@Test
	public void markJobDataAsProcessed_shouldUpdatedGivenData() throws Exception {
		batchRuntimeService.markJobDataAsProcessed(1L, Arrays.asList("item1", "item2"));

		verify(dbDao).executeUpdate(BatchEntity.UPDATE_AS_PROCESSED_KEY, Arrays.asList(new Pair<>(BatchProperties
				.JOB_ID, "jobId"), new Pair<>("instanceIds", Arrays.asList("item1", "item2"))));
	}

	@Test
	public void clearJobData_shouldClearByJobExecutionId() throws Exception {
		batchRuntimeService.clearJobData("jobExecutionId");

		verify(dbDao).executeUpdate(BatchEntity.DELETE_DATA_BY_JOB_ID_KEY,
				Collections.singletonList(new Pair<>(BatchProperties.JOB_ID, "jobExecutionId")));
	}

	@Test
	public void clearJobData_shouldClearDataByJobId() throws Exception {
		batchRuntimeService.clearJobData(1L);
		verify(dbDao).executeUpdate(BatchEntity.DELETE_DATA_BY_JOB_ID_KEY,
				Collections.singletonList(new Pair<>(BatchProperties.JOB_ID, "jobId")));
	}

}
