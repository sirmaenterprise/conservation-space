package com.sirma.itt.sep.instance.unique.observer;

import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link UniqueValuesObserver}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValuesObserverTest {

    @Spy
    private TransactionSupport transactionSupport = new TransactionSupportFake();

    @Mock
    private UniqueValueValidationService uniqueValueValidationService;

    @InjectMocks
    private UniqueValuesObserver observer;

    @Test
    public void should_UpdateUniqueFields_When_DefinitionAreReloaded() {
        observer.onDefinitionReload(Mockito.mock(DefinitionsChangedEvent.class));

        Mockito.verify(transactionSupport).invokeOnSuccessfulTransactionInTx(Matchers.any());
        Mockito.verify(uniqueValueValidationService).updateUniqueFields();
    }

    @Test
    public void should_UnregisterValues_When_InstanceIsDeleted() {
        String instanceId = "instanceId";
        Instance instance = Mockito.mock(Instance.class);
        AfterInstanceDeleteEvent event = Mockito.mock(AfterInstanceDeleteEvent.class);
        Mockito.when(event.getInstance()).thenReturn(instance);
        Mockito.when(instance.getId()).thenReturn(instanceId);

        observer.onInstanceDelete(event);

        Mockito.verify(transactionSupport).invokeOnSuccessfulTransactionInTx(Matchers.any());
        Mockito.verify(uniqueValueValidationService).unRegisterUniqueValues(instanceId);
    }
}