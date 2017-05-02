package com.sirma.itt.seip.permissions.validator;

import javax.inject.Inject;

import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Validates saved search instances when they are being updated. The validity rule is that the user that is updating
 * them must have sufficient permissions on them.
 *
 * @author nvelkov
 */
@Extension(target = Validator.TARGET_NAME, order = 1)
public class SavedSearchPermissionsValidator implements Validator {
	private static final String SAVED_SEARCH_RDFTYPE = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#SavedSearch";
	private static final String INSTANCE_VALIDATION_PERMISSIONS = "instance.validation.permissions";

	@Inject
	private AuthorityService authorityService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	public void validate(ValidationContext validationContext) {
		Instance instance = validationContext.getInstance();
		Operation operation = validationContext.getOperation();
		String instanceRdfType = instance.get(DefaultProperties.SEMANTIC_TYPE, "").toString();
		boolean isOperationCorrect = instanceRdfType.equals(SAVED_SEARCH_RDFTYPE)
				&& Operation.isOperationAs(operation, ActionTypeConstants.EDIT_DETAILS);
		if (isOperationCorrect && !authorityService.isActionAllowed(instance, operation.getOperation(), null)) {
			String message = labelProvider.getValue(INSTANCE_VALIDATION_PERMISSIONS);
			validationContext.addMessage(MessageType.ERROR, message);
		}
	}
}
