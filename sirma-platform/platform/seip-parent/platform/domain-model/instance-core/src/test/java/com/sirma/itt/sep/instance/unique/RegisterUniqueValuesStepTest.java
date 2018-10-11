package com.sirma.itt.sep.instance.unique;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.state.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

/**
 * Unit tests for {@link RegisterUniqueValuesStep}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterUniqueValuesStepTest {

    @Mock
    private UniqueValueValidationService uniqueValueValidationService;

    @Mock
    private RevisionService revisionService;

    @InjectMocks
    private RegisterUniqueValuesStep saveStep;

    @Test
    public void should_RegisterUniqueValues_When_InstanceIsSaved() {
        Instance instance = Mockito.mock(Instance.class);
        InstanceSaveContext saveContext = InstanceSaveContext.create(instance, Operation.NO_OPERATION);

        saveStep.beforeSave(saveContext);

        Mockito.verify(uniqueValueValidationService).registerUniqueValues(instance);
    }

    @Test
    public void should_NotRegisterUniqueValues_When_RevisionIsSaved() {
        Instance instance = Mockito.mock(Instance.class);
        InstanceSaveContext saveContext = InstanceSaveContext.create(instance, Operation.NO_OPERATION);
        Mockito.when(revisionService.isRevision(instance)).thenReturn(true);

        saveStep.beforeSave(saveContext);

        Mockito.verify(uniqueValueValidationService, Mockito.never()).registerUniqueValues(instance);
    }

    @Test
    public void should_ReturnCorrectName_When_MethodGetNameIsCalled() {
        Assert.assertEquals(saveStep.getName(), "registerUniqueValueInstanceStep");
    }
}