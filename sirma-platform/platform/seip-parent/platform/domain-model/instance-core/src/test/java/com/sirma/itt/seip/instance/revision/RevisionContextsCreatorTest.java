package com.sirma.itt.seip.instance.revision;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.sep.model.ModelImportCompleted;

/**
 * Tests for {@link RevisionContextsCreator}.
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class RevisionContextsCreatorTest {

	@Mock
	private RevisionService revisionService;

	@Mock
	private SchedulerService schedulerService;

	@InjectMocks
	private RevisionContextsCreator revisionContextsCreatorOnDefinitionsChangedEvent;

	@Test
	public void should_StartSchedulerTaskToUpdateRevisionsContexts() {
		revisionContextsCreatorOnDefinitionsChangedEvent.onModelsImportComplete(Mockito.mock(ModelImportCompleted.class));

		Mockito.verify(schedulerService).schedule(Matchers.eq(RevisionContextsCreator.NAME), Matchers.argThat(matchConfiguration()));
	}

	@Test
	public void should_StartUpdateOfRevisionContext_When_TaskIsExecuted() throws Exception {
		revisionContextsCreatorOnDefinitionsChangedEvent.execute(Mockito.mock(SchedulerContext.class));

		Mockito.verify(revisionService).createRevisionsContexts();
	}

	private CustomMatcher<SchedulerConfiguration> matchConfiguration() {
		return CustomMatcher.of((schedulerConfiguration) -> {
			Assert.assertTrue(schedulerConfiguration.isRemoveOnSuccess());
			Assert.assertEquals(TransactionMode.NOT_SUPPORTED, schedulerConfiguration.getTransactionMode());
			Assert.assertEquals(5, schedulerConfiguration.getMaxRetryCount());
			Assert.assertFalse(schedulerConfiguration.isSynchronous());
		});
	}
}