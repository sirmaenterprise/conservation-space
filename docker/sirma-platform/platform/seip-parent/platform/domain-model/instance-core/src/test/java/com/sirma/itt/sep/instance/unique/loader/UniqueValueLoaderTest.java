package com.sirma.itt.sep.instance.unique.loader;

import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.sep.instance.unique.persistence.UniqueField;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

/**
 * Tests for {@link UniqueValueLoader}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueLoaderTest {

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private UniqueValueLoader uniqueValueLoader;

    @Test
    public void should_ScheduleAction() {

        String definitionId = "definition-id";
        String fieldUri = "field-uri";

        UniqueField uniqueField = Mockito.mock(UniqueField.class);
        Mockito.when(uniqueField.getFieldUri()).thenReturn(fieldUri);
        Mockito.when(uniqueField.getDefinitionId()).thenReturn(definitionId);

        SchedulerConfiguration schedulerConfiguration = Mockito.mock(SchedulerConfiguration.class);
        Mockito.when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.TIMED))
                .thenReturn(schedulerConfiguration);

        //Invoke tested method.
        uniqueValueLoader.registerUniqueValues(Collections.singleton(uniqueField));

        ArgumentCaptor<SchedulerContext> contextArgumentCaptor = ArgumentCaptor.forClass(SchedulerContext.class);
        Mockito.verify(schedulerService)
                .schedule(Matchers.eq(UniqueValueLoaderAction.ACTION_NAME), Matchers.eq(schedulerConfiguration),
                          contextArgumentCaptor.capture());

        //verifying context
        SchedulerContext context = contextArgumentCaptor.getValue();
        Assert.assertEquals(definitionId, context.get(UniqueValueLoaderAction.KEY_DEFINITION_ID));
        Assert.assertEquals(fieldUri, context.get(UniqueValueLoaderAction.KEY_FIELD_URI));

        //verifying configuration
        Mockito.verify(schedulerConfiguration).setMaxActivePerGroup(definitionId, 1);
        Mockito.verify(schedulerConfiguration).setRemoveOnSuccess(true);
        Mockito.verify(schedulerConfiguration).setContinueOnError(true);
    }
}
