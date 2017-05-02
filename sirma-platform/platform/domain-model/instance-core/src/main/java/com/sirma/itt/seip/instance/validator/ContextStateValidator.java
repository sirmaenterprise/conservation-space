package com.sirma.itt.seip.instance.validator;

import java.util.Optional;

import javax.inject.Inject;

import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Validate the state of the instance's context to see if the create operation can be performed in the context's current
 * state.
 *
 * @author nvelkov
 */
@Extension(target = Validator.TARGET_NAME, order = 2)
public class ContextStateValidator implements Validator {

	@Inject
	private InstanceValidationService contextValidationHelper;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public void validate(ValidationContext validationContext) {
		if (!securityContextManager.isAuthenticatedAsAdmin()) {
			Instance instance = validationContext.getInstance();
			if (instance instanceof OwnedModel && ((OwnedModel) instance).getOwningInstance() != null) {
				Optional<String> errorMessage = contextValidationHelper
						.canCreateOrUploadIn(((OwnedModel) instance).getOwningInstance());
				if (errorMessage.isPresent()) {
					validationContext.addMessage(MessageType.ERROR, errorMessage.get());
				}
			}
		}
	}

}
