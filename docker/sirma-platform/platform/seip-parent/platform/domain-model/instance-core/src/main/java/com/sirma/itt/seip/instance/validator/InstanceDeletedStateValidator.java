package com.sirma.itt.seip.instance.validator;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Validates deleted state of the instance in the system. If there is an attempt to save any changes to already deleted
 * instance, the validation will not pass and to the validation context will be added error message that will interrupt
 * the save process.
 *
 * @author A. Kunchev
 */
@Extension(target = Validator.TARGET_NAME, order = 0.1)
public class InstanceDeletedStateValidator implements Validator {

	private static final String ERROR_MSG_BUNDLE_KEY = "validation.error.saving.deleted.object";

	@Inject
	private InstanceService instanceService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	public void validate(ValidationContext context) {
		Serializable id = context.getInstance().getId();
		if (instanceService.loadDeleted(id).filter(Instance::isDeleted).isPresent()) {
			context.addErrorMessage(labelProvider.getValue(ERROR_MSG_BUNDLE_KEY));
		}
	}
}