package com.sirma.sep.instance.batch.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.JobInfo;

/**
 * Test for {@link JobExecutionResource}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/02/2019
 */
public class JobExecutionResourceTest {

	@InjectMocks
	private JobExecutionResource executionResource;

	@Mock
	private BatchService batchService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(batchService.getJobs()).thenReturn(
				Arrays.asList(createJob("id-1", 0, 0), createJob("id-2", 1, 2),
						createJob("id-3", 0, 5), createJob("id-4", 0, 0),
						createJob("id-5", 6, 0), createJob("id-44", 0, 0)));
	}

	private static JobInfo createJob(String id, int processed, int remaining) {
		JobInfo info = new JobInfo(id);
		info.setRemaining(remaining);
		info.setProcessed(processed);
		info.setAlias(id);
		info.setName("someJob");
		return info;
	}

	@Test
	public void getAllJobs_shouldReturnAllJobsIfNothingIsSpecified() throws Exception {
		Collection<JobInfo> allJobs = executionResource.getAllJobs(null, false);
		assertEquals(6, allJobs.size());
	}

	@Test
	public void getAllJobs_shouldReturnOnlyActiveJobs() throws Exception {
		Collection<JobInfo> allJobs = executionResource.getAllJobs(null, true);
		assertEquals(2, allJobs.size());
	}

	@Test
	public void getAllJobs_shouldReturnFilteredJobs() throws Exception {
		Collection<JobInfo> allJobs = executionResource.getAllJobs("*4", false);
		assertEquals(2, allJobs.size());
	}

	@Test
	public void getAllJobs_shouldReturnFilteredActiveJobs() throws Exception {
		Collection<JobInfo> allJobs = executionResource.getAllJobs("*1", true);
		assertEquals(0, allJobs.size());
		allJobs = executionResource.getAllJobs("*3", true);
		assertEquals(1, allJobs.size());
	}
}