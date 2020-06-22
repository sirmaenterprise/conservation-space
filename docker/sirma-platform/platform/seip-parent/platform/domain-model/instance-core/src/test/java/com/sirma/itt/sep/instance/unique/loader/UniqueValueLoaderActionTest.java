package com.sirma.itt.sep.instance.unique.loader;

import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link UniqueValueLoaderAction}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueLoaderActionTest {

    @Mock
    private UniqueValueValidationService uniqueValueValidationService;

    @InjectMocks
    private UniqueValueLoaderAction uniqueValueLoaderAction;

    @Test
    public void should_StartRegistrationOfOldestUniqueValues() throws Exception {
        String definitionId = "DT0007";
        String fieldUri = "chd:numericN10";
        SchedulerContext context = new SchedulerContext();
        context.put(UniqueValueLoaderAction.KEY_DEFINITION_ID, definitionId);
        context.put(UniqueValueLoaderAction.KEY_FIELD_URI, fieldUri);

        uniqueValueLoaderAction.execute(context);

        Mockito.verify(uniqueValueValidationService).registerOldUniqueValues(definitionId, fieldUri);
    }
}