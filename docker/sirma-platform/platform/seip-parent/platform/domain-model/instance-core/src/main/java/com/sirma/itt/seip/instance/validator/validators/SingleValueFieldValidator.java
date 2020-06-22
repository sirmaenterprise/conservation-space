package com.sirma.itt.seip.instance.validator.validators;

import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Validates if a a property that is not multivalued has only one value.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 25/01/2018
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 13)
public class SingleValueFieldValidator extends PropertyFieldValidator {

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		if (collectValues(context).count() > 1) {
			return Stream.of(builder.buildSingleValueError(context.getPropertyDefinition()));
		}
		return Stream.empty();
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return !context.getPropertyDefinition().isMultiValued();
	}
}