package com.sirma.itt.seip.instance.validator;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.instance.validation.Validator;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirmaenterprise.sep.instance.validator.exceptions.InstanceValidationException;

/**
 * Chaining proxy implementation for all validators when validating an instance.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class ChainingValidatorService implements Validator {

	@Inject
	@ExtensionPoint(value = Validator.TARGET_NAME)
	private Iterable<Validator> validators;

	@Override
	public void validate(ValidationContext validationContext) {
		validators.forEach(validator -> validator.validate(validationContext));
		if (CollectionUtils.isNotEmpty(validationContext.getMessages())) {
			String errorMessage = validationContext
					.getMessages()
						.stream()
						.map(Message::getMessage)
						.collect(Collectors.joining(", "));
			throw new InstanceValidationException(errorMessage);
		}
	}

}
