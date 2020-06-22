package com.sirma.itt.sep.instance.unique.loader;

import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.sep.instance.unique.UniqueValueValidationService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This action will trigger registration of values for instances created
 * before some field to be marked as unique. Context of action contains a
 * couple of definitionId and fieldUri for which values have to be registered.
 *
 * @author Boyan Tonchev.
 */
@ApplicationScoped
@Named(UniqueValueLoaderAction.ACTION_NAME)
public class UniqueValueLoaderAction extends SchedulerActionAdapter {

    public static final String ACTION_NAME = "uniqueValueLoaderAction";
    static final String KEY_DEFINITION_ID = "definitionId";
    static final String KEY_FIELD_URI = "fieldUri";

    @Inject
    private UniqueValueValidationService uniqueValueValidationService;

    @Override
    public void execute(SchedulerContext context) throws Exception {
        String definitionId = (String) context.get(KEY_DEFINITION_ID);
        String fieldUri = (String) context.get(KEY_FIELD_URI);
        uniqueValueValidationService.registerOldUniqueValues(definitionId, fieldUri);
    }
}
