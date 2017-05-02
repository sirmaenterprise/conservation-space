package com.sirma.itt.seip.instance.validator;

import java.util.Optional;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.permissions.action.AuthorityService;

/**
 * Contains instance validations that will be run mainly from the {@link Validator} chain.
 *
 * @author nvelkov
 */
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

	@Override
	public Optional<String> canCreateOrUploadIn(String instanceId, String purpose) {
		return validateInstanceState(instanceId, purpose);
	}

	@Override
	public Optional<String> canCreateOrUploadIn(Instance instance) {
		return validateInstanceState(instance, PURPOSE_CREATE);
	}

	private Optional<String> validateInstanceState(String instanceId, String purpose) {
		Optional<InstanceReference> reference = instanceTypeResolver.resolveReference(instanceId);
		if (reference.isPresent()) {
			return validateInstanceState(reference.get().toInstance(), purpose);
		}
		return Optional.of(labelProvider.getValue(MISSING_INSTANCE_LABEL_ID));
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
