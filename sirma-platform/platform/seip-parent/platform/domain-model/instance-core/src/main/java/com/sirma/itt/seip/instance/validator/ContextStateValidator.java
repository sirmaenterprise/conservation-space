package com.sirma.itt.seip.instance.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Validate the state of the instance's context to see if the operation can be performed in the context's current
 * state. Only operation through which we can alter the state are checked.
 *
 * @author nvelkov
 */
@Extension(target = Validator.TARGET_NAME)
public class ContextStateValidator implements Validator {

	private static final Set<String> CHANGE_CONTEXT_OPERATIONS = new HashSet<>(Arrays.asList("CREATE", "MOVE"));

	@Inject
	private InstanceValidationService contextValidationHelper;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private InstanceContextService contextService;

	@Override
	public void validate(ValidationContext validationContext) {
		// we do not have to validate the context rights if we already have are logged as administrator or out of create operation scope
		if (!securityContextManager.isAuthenticatedAsAdmin() && canOperationChangeContext(validationContext)) {
			Instance instance = validationContext.getInstance();
			Instance context = contextService
					.getContext(instance)
					.map(InstanceReference::toInstance)
					.orElse(null);
			if (context != null) {
				Optional<String> errorMessage = contextValidationHelper.canCreateOrUploadIn(context);
				errorMessage.ifPresent(s -> validationContext.addMessage(MessageType.ERROR, s));
			}
		}
	}

	private boolean canOperationChangeContext(ValidationContext validationContext) {
		if (validationContext.getOperation() == null || validationContext.getOperation().getOperation() == null) {
			return false;
		}
		return CHANGE_CONTEXT_OPERATIONS.stream()
				.anyMatch(validationContext.getOperation().getOperation()::equalsIgnoreCase);
	}
}
