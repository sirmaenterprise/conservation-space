package com.sirma.itt.sep.instance.unique.loader;

import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.sep.instance.unique.persistence.UniqueField;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Schedules {@link UniqueValueLoaderAction} which will register unique values
 * of instances created before some field to be marked as unique.
 *
 * @author Boyan Tonchev.
 */
public class UniqueValueLoader {

    @Inject
    private SchedulerService schedulerService;

    /**
     * Schedules a {@link UniqueValueLoaderAction} for every {@link UniqueField} from <code>uniqueFields</code>.
     *
     * @param uniqueFields
     *         - collection with {@link UniqueField}.
     */
    public void registerUniqueValues(Collection<UniqueField> uniqueFields) {
        for (UniqueField uniqueField : uniqueFields) {
            scheduleLoading(uniqueField.getDefinitionId(), uniqueField.getFieldUri());
        }
    }

    /**
     * Schedules action to load and register unique values of instances created before some field to be marked as unique.
     * DefinitionId will be used as group name of action. This will give opportunity to start many parallel actions
     * for every different definition but no more than one per definition.
     * @param definitionId
     *         - the definition id where field is defined.
     * @param fieldUri
     *         - the field uri of filed.
     */
    private void scheduleLoading(String definitionId, String fieldUri) {
        SchedulerContext context = new SchedulerContext();
        context.put(UniqueValueLoaderAction.KEY_DEFINITION_ID, definitionId);
        context.put(UniqueValueLoaderAction.KEY_FIELD_URI, fieldUri);
        SchedulerConfiguration schedulerConfiguration = schedulerService.buildEmptyConfiguration(
                SchedulerEntryType.TIMED);
        schedulerConfiguration.setMaxActivePerGroup(definitionId, 1);
        schedulerConfiguration.setRemoveOnSuccess(true);
        schedulerConfiguration.setContinueOnError(true);
        schedulerService.schedule(UniqueValueLoaderAction.ACTION_NAME, schedulerConfiguration, context);
    }
}
