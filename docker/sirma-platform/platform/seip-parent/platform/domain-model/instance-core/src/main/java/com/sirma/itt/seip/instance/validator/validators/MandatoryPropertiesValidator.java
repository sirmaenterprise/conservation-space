package com.sirma.itt.seip.instance.validator.validators;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Class that has logic that checks if given field is mandatory and if so checks if the corresponding
 * {@link com.sirma.itt.seip.domain.instance.Instance} object has value for it.
 * <p/>
 * The mandatory fields are gathered and set in the {@link com.sirma.itt.seip.instance.validation.ValidationContext}
 * beforehand.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 10)
public class MandatoryPropertiesValidator extends PropertyFieldValidator {

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		Set<String> mandatoryFields = context.getMandatoryFields();
		Set<String> optionalFields = context.getOptionalFields();
		String fieldName = context.getPropertyDefinition().getName();
		if (optionalFields.contains(fieldName)) {
			return Stream.empty();
		} else if ((mandatoryFields.contains(fieldName) || context.getPropertyDefinition().isMandatory())
				&& !collectValues(context).findAny().isPresent()) {
			return Stream.of(builder.buildMandatoryFieldError(context.getPropertyDefinition()));
		}
		return Stream.empty();
	}

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return context.getMandatoryFields().contains(context.getPropertyDefinition().getName())
				|| context.getPropertyDefinition().isMandatory();
	}
}
