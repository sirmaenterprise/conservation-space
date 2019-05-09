package com.sirma.itt.seip.instance.validator;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.revision.RevisionService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Verifies whether the instance can exist in context or context.
 *
 * Validator have not be executed for integrated instances, and revisions.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = Validator.TARGET_NAME, order = 1)
public class InstanceExistingInContextValidator implements Validator {

	private static final String EXISTING_IN_CONTEXT_ERROR_MESSAGE = "validation.error.existing_in_context";
	private static final String EXISTING_WITHOUT_CONTEXT_ERROR_MESSAGE = "validation.error.existing_without_context";

	@Inject
	private InstanceValidationService instanceValidationService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private RevisionService revisionService;

	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public void validate(ValidationContext validationContext) {
		Instance instance = validationContext.getInstance();
		if (revisionService.isRevision(instance)) {
			return;
		}

		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		if (instance.get(InstanceContextService.HAS_PARENT, fieldConverter) != null) {
			validateInContext(validationContext, instanceDefinition);
		} else {
			validateWithoutContext(validationContext, instanceDefinition);
		}
	}

	private void validateInContext(ValidationContext validationContext, DefinitionModel instanceDefinition) {
		if (!instanceValidationService.canExistInContext(instanceDefinition)) {
			validationContext.addErrorMessage(labelProvider.getValue(EXISTING_IN_CONTEXT_ERROR_MESSAGE));
		}
	}

	private void validateWithoutContext(ValidationContext validationContext, DefinitionModel instanceDefinition) {
		if (!instanceValidationService.canExistWithoutContext(instanceDefinition)) {
			validationContext.addErrorMessage(labelProvider.getValue(EXISTING_WITHOUT_CONTEXT_ERROR_MESSAGE));
		}
	}
}