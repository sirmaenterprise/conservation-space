package com.sirma.sep.content.idoc.extract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.save.event.AfterInstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Tests for {@link ViewContentExtractor}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewContentExtractorTest {

    private static final String EMF_ID = "emf-id";

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private ViewContentExtractor viewContentExtractor;

    @Test
    public void should_NameBeCorrect() {
        viewContentExtractor.afterInstanceSave(createAfterInstanceSaveEvent());

        Mockito.verify(schedulerService)
                .schedule(Matchers.eq(ScheduleViewContentExtraction.NAME), Matchers.argThat(configurationMatcher()),
                          Matchers.argThat(contextMatcher()));
    }

	private static AfterInstanceSaveEvent createAfterInstanceSaveEvent() {
        Instance instance = Mockito.mock(Instance.class);
        Mockito.when(instance.getId()).thenReturn(EMF_ID);
        return new AfterInstanceSaveEvent(instance, instance, Operation.NO_OPERATION);
    }

    private static CustomMatcher<SchedulerConfiguration> configurationMatcher() {
        return CustomMatcher.of((SchedulerConfiguration configuration) -> {
            assertTrue(configuration.isRemoveOnSuccess());
            assertTrue(configuration.isPersistent());
            assertEquals(TransactionMode.NOT_SUPPORTED, configuration.getTransactionMode());
            assertEquals(5, configuration.getMaxRetryCount());
            assertTrue(configuration.isIncrementalDelay());
            assertEquals(5, configuration.getMaxActivePerGroup());
            assertEquals(SchedulerEntryType.TIMED, configuration.getType());
        });
    }

    private static CustomMatcher<SchedulerContext> contextMatcher() {
        return CustomMatcher.of((SchedulerContext context) -> assertEquals(EMF_ID, context.get(
                ScheduleViewContentExtraction.INSTANCE_ID)));
    }
}
