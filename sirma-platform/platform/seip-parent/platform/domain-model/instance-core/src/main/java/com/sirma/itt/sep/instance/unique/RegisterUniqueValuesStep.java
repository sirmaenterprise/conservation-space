package com.sirma.itt.sep.instance.unique;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.plugin.Extension;

import javax.inject.Inject;

/**
 * Save step for update unique property values of saved instance.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = InstanceSaveStep.NAME, order = 50)
public class RegisterUniqueValuesStep implements InstanceSaveStep {

    private static final String NAME = "registerUniqueValueInstanceStep";

    @Inject
    private UniqueValueValidationService uniqueValueValidationService;

    @Inject
    private RevisionService revisionService;

    @Override
    public void beforeSave(InstanceSaveContext saveContext) {
        Instance instance = saveContext.getInstance();
        //we skip the revision instance as from requirement.
        if (revisionService.isRevision(instance)) {
            return;
        }

        uniqueValueValidationService.registerUniqueValues(instance);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
