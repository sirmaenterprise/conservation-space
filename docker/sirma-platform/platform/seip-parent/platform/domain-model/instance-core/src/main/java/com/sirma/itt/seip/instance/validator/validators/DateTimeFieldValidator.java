package com.sirma.itt.seip.instance.validator.validators;

import java.util.Date;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Class that validates {@link PropertyDefinition} fields of type datetime and date. No need to check the actual format
 * here, we just need to check if the instance has a date object in it. The date format conversion is not done in the
 * instance.
 * <p>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 9)
public class DateTimeFieldValidator extends PropertyFieldValidator {

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		if (context.getValue() instanceof Date) {
			return Stream.empty();
		}
		return Stream.of(builder.buildDateTimeFieldError(context.getPropertyDefinition()));
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		String typeName = context.getPropertyDefinition().getDataType().getName();
		return (ValidationTypes.DATE_TIME.toString().equals(typeName) || ValidationTypes.DATE.toString()
				.equals(typeName)) && context.getValue() != null;
	}
}
