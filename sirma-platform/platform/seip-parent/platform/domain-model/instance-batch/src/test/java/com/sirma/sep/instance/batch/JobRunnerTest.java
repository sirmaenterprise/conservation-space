package com.sirma.sep.instance.batch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.batch.operations.JobOperator;
import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.runtime.JobExecution;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

/**
 * Tests for {@link JobRunner}.
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class JobRunnerTest {

    @Mock
    private JobOperator jobOperator;

    @Spy
    private JobRunner jobRunner;

    @Before
    public void setUp() {
        Mockito.when(jobRunner.getJobOperator()).thenReturn(jobOperator);
    }

    @Test
    public void should_StopAllExistingExecutions() {
        String jobName = "should_StopAllExistingExecutions_jobName";
        Long executionIdNotExist = 2L;
        Mockito.when(jobOperator.getRunningExecutions(jobName)).thenReturn(Arrays.asList(1L, executionIdNotExist, 3L));
        Mockito.doThrow(Mockito.mock(NoSuchJobExecutionException.class)).when(jobOperator).stop(executionIdNotExist);

        jobRunner.stopJobExecutions(jobName);

        Mockito.verify(jobOperator, Mockito.times(3)).stop(Matchers.any(Long.class));
    }

    @Test
    public void should_ReturnEmptyList_When_NoSuchJobExecutionExceptionIsThrown() {
        Long executionId = 2L;
        Mockito.when(jobOperator.getJobExecution(executionId)).thenThrow(Mockito.mock(NoSuchJobExecutionException.class));

        Assert.assertFalse(jobRunner.getJobExecution(executionId).isPresent());
    }

    @Test
    public void should_ReturnJobExecution() {
        Long executionId = 2L;
        JobExecution jobExecution = Mockito.mock(JobExecution.class);
        Mockito.when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);

        Optional<JobExecution> jobExecutions = jobRunner.getJobExecution(executionId);

        Assert.assertTrue(jobExecutions.isPresent());
        Assert.assertEquals(jobExecution, jobExecutions.get());
    }

    @Test
    public void should_ReturnEmptyList_When_NoSuchJobExceptionIsThrown() {
        String jobName = "should_ReturnEmptyList_When_NoSuchJobExceptionIsThrown_jobName";
        Mockito.when(jobOperator.getRunningExecutions(jobName)).thenThrow(Mockito.mock(NoSuchJobException.class));

        Assert.assertEquals(Collections.emptyList(), jobRunner.getRunningExecutions(jobName));
    }

    @Test
    public void should_ReturnRunningExecutions() {
        String jobName = "should_ReturnRunningExecutions_jobName";
        Mockito.when(jobOperator.getRunningExecutions(jobName)).thenReturn(Collections.singletonList(2L));

        Assert.assertEquals(1, jobRunner.getRunningExecutions(jobName).size());
    }

    @Test
    public void should_StartJob() {
        String jobName = "startJobName";
        Properties properties = new Properties();
        jobRunner.startJob(jobName, properties);
        Mockito.verify(jobOperator).start(jobName, properties);
    }
}