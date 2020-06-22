package com.sirma.itt.seip.instance.validator;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Execute validation based on the operation that is executed with the save process. This validation is mandatory and
 * they cannot be skipped.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 1.1)
public class OperationValidationStep implements InstanceSaveStep {

	@Inject
	private Validator validatorService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		validatorService.validate(new ValidationContext(saveContext.getInstance(), saveContext.getOperation()));
	}

	@Override
	public String getName() {
		return "operationValidation";
	}
}