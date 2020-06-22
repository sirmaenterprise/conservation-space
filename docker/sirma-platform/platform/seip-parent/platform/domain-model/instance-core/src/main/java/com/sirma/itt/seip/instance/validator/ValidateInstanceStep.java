package com.sirma.itt.seip.instance.validator;

import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Handles the validation process before save. If any error is detected an exception for rollback is thrown to terminate
 * current transaction. If no error is detected step is passed without any instance modifications.
 *
 * @author bbanchev
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 1.2)
public class ValidateInstanceStep implements InstanceSaveStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceValidationService instanceValidationService;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		ValidationContext validationContext = new ValidationContext(saveContext.getInstance(),
				saveContext.getOperation());
		InstanceValidationResult validationResult = instanceValidationService.validate(validationContext);
		if (validationResult.hasPassed()) {
			LOGGER.trace("Instance {} passed validation.", validationContext.getInstance().getId());
			return;
		}

		String errors = validationResult
				.getErrorMessages()
					.stream()
					.map(PropertyValidationError::getMessage)
					.collect(joining(", "));
		if (saveContext.isValidationEnabled()) {
			throw new RollbackedRuntimeException(errors);
		}

		LOGGER.warn("Validation for instance {} failed with {}\n but was disabled with reason: {}.",
				saveContext.getInstance().getId(), errors, saveContext.getDisableValidationReason());
	}

	@Override
	public String getName() {
		return "validateInstanceStep";
	}
}