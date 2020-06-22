package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Field validator that passes before the generic text validator and checks user name for valid format if user instance is saved
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/09/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 4.5)
public class UsernameValidator extends PropertyFieldValidator {

	@Inject
	private ResourceService resourceService;

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		Serializable userName = context.getValue();
		if (userName instanceof String && resourceService.validateUserName((String) userName)) {
			return Stream.empty();
		}
		return Stream.of(builder.buildCustomError(context.getPropertyDefinition(), "invalid_user_name", userName));
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return context.getInstance().type().is("user")
				&& context.getPropertyDefinition().getName().equals(ResourceProperties.USER_ID);
	}
}
