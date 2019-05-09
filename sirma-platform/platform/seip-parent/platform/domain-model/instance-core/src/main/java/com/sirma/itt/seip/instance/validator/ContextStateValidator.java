package com.sirma.itt.seip.instance.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

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
		// do not validate context rights, when the user is administrator or the operation does not change the context
		if (!securityContextManager.isAuthenticatedAsAdmin() && canOperationChangeContext(validationContext)) {
			Instance instance = validationContext.getInstance();
			contextService.getContext(instance).map(InstanceReference::toInstance).ifPresent(context -> {
				contextValidationHelper.canCreateOrUploadIn(context).ifPresent(validationContext::addErrorMessage);
			});
		}
	}

	private static boolean canOperationChangeContext(ValidationContext validationContext) {
		return CHANGE_CONTEXT_OPERATIONS.stream()
				.anyMatch(validationContext.getOperation().getOperation()::equalsIgnoreCase);
	}
}