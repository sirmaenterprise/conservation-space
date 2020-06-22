package com.sirma.itt.seip.instance.validator;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.EnumUtils;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.permissions.action.AuthorityService;

/**
 * Contains instance validations that will be run mainly from the {@link Validator} chain.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class InstanceValidationServiceImpl implements InstanceValidationService {
	private static final String MISSING_INSTANCE_LABEL_ID = "sep.uigateway.validation.context.missinginstance";
	private static final String WRONG_STATE_CREATE_LABEL_ID = "sep.uigateway.validation.context.create.wrongstate";
	private static final String WRONG_STATE_UPLOAD_LABEL_ID = "sep.uigateway.validation.context.upload.wrongstate";
	private static final String CREATE_IN_CONTEXT = "createInContext";
	private static final String PURPOSE_CREATE = "CREATE";

	@Inject
	private AuthorityService authorityService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstanceValidator instanceValidator;

	@Override
	public Optional<String> canCreateOrUploadIn(String instanceId, String purpose) {
		return validateInstanceState(instanceId, purpose);
	}

	@Override
	public Optional<String> canCreateOrUploadIn(Instance instance) {
		return validateInstanceState(instance, PURPOSE_CREATE);
	}

	@Override
	public InstanceValidationResult validate(ValidationContext context) {
		return instanceValidator.validate(context);
	}

	@Override
	public boolean canExistInContext(DefinitionModel instanceDefinition) {
		return canExistInContext(fetchExistingInContextValue(instanceDefinition));
	}

	@Override
	public boolean canExistWithoutContext(DefinitionModel instanceDefinition) {
		return canExistWithoutContext(fetchExistingInContextValue(instanceDefinition));
	}

	private boolean canExistInContext(String existingInContext) {
		return ExistingInContext.IN_CONTEXT.toString().equals(existingInContext)
				|| ExistingInContext.BOTH.toString().equals(existingInContext);
	}

	private boolean canExistWithoutContext(String existingInContext) {
		return ExistingInContext.WITHOUT_CONTEXT.toString().equals(existingInContext)
				|| ExistingInContext.BOTH.toString().equals(existingInContext);
	}

	private String fetchExistingInContextValue(DefinitionModel instanceDefinition) {
		if (!(instanceDefinition instanceof GenericDefinition)) {
			return ExistingInContext.BOTH.toString();
		}
		return ((GenericDefinition) instanceDefinition).getConfiguration(DefaultProperties.EXISTING_IN_CONTEXT)
				.map(PropertyDefinition::getDefaultValue)
				.filter(existingInContextValue -> EnumUtils.isValidEnum(ExistingInContext.class, existingInContextValue))
				.orElse(ExistingInContext.BOTH.toString());
	}

	private Optional<String> validateInstanceState(String instanceId, String purpose) {
		Optional<InstanceReference> reference = instanceTypeResolver.resolveReference(instanceId);
		return reference.map(instanceReference -> validateInstanceState(instanceReference.toInstance(), purpose))
				.orElseGet(() -> Optional.of(labelProvider.getValue(MISSING_INSTANCE_LABEL_ID)));
	}

	private Optional<String> validateInstanceState(Instance instance, String purpose) {
		if (instance != null) {
			if (authorityService.isActionAllowed(instance, CREATE_IN_CONTEXT, "")) {
				return Optional.empty();
			}
			return Optional.of(getWrongStateLabel(purpose));
		}
		return Optional.of(labelProvider.getValue(MISSING_INSTANCE_LABEL_ID));
	}

	private String getWrongStateLabel(String purpose) {
		if (PURPOSE_CREATE.equals(purpose)) {
			return labelProvider.getValue(WRONG_STATE_CREATE_LABEL_ID);
		}
		return labelProvider.getValue(WRONG_STATE_UPLOAD_LABEL_ID);
	}
}
