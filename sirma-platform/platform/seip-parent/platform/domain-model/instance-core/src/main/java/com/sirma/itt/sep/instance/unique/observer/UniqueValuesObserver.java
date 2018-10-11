package com.sirma.itt.sep.instance.unique.observer;

import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Registers observers of a certain tenant events.
 *
 * @author Boyan Tonchev.
 */
public class UniqueValuesObserver {

    @Inject
    private TransactionSupport transactionSupport;

    @Inject
    private UniqueValueValidationService uniqueValueValidationService;

    /**
     * Observes for reload definition event. Update registered unique fields.
     *
     * @param event
     *         event fired after definitions are reloaded.
     */
    public void onDefinitionReload(@Observes DefinitionsChangedEvent event) {
        transactionSupport.invokeOnSuccessfulTransactionInTx(uniqueValueValidationService::updateUniqueFields);
    }

    /**
     * Observes for instance delete event. Removes all registered unique values for deleted instance.
     *
     * @param event
     *         - event fired after instance is deleted.
     */
    public void onInstanceDelete(@Observes AfterInstanceDeleteEvent event) {
        String instanceId = (String) event.getInstance().getId();
        transactionSupport.invokeOnSuccessfulTransactionInTx(
                () -> uniqueValueValidationService.unRegisterUniqueValues(instanceId));
    }
}
