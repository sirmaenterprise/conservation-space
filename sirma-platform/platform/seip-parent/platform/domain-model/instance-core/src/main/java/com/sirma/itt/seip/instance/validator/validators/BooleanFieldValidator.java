package com.sirma.itt.seip.instance.validator.validators;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

import javax.inject.Inject;
import java.util.stream.Stream;

/**
 * Class that validates a a boolean type field for correctness. Those fields should be a boolean object and
 * contain either "true" or "false".
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 8)
public class BooleanFieldValidator extends PropertyFieldValidator {

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		if (context.getValue() instanceof Boolean) {
			return Stream.empty();
		}
		return Stream.of(builder.buildBooleanFieldError(context.getPropertyDefinition()));
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return ValidationTypes.BOOLEAN.toString().equals(context.getPropertyDefinition().getDataType().getName())
				&& context.getValue() != null;
	}
}
