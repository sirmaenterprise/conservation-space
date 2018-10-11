package com.sirma.itt.seip.instance.validator.validators;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Validates a text {@link PropertyDefinition} field. However the numbers in the definitions are also marked as text.
 * So this Validator checks:
 * <ul>
 * <li>string fields</li>
 * <li>integer and long values with specific format</li>
 * <li>float numbers with specific format</li>
 * <li>double values with specific format</li>
 * </ul>
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 5)
public class TextFieldValidator extends PropertyFieldValidator {

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return isFreeText(context.getPropertyDefinition()) && context.getValue() != null;
	}

	// Non code list text fields.
	private static boolean isFreeText(PropertyDefinition property) {
		return ValidationTypes.TEXT.toString().equals(property.getDataType().getName())
				&& !PropertyDefinition.hasCodelist().test(property);
	}

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		Pair<String, String> nameRegEx = regExGenerator.getPattern(context.getPropertyDefinition().getType(), null);
		Pattern pattern = Pattern.compile(nameRegEx.getFirst());

		return collectValues(context).flatMap(TextFieldValidator::convertMultiValue).map(String.class::cast)
				.filter(value -> !pattern.matcher(value).matches())
				.map(value -> builder.buildTextFieldError(context.getPropertyDefinition(), nameRegEx.getSecond()));
	}

	/**
	 * Title text fields for libraries can have a specific object them that holds the titles for all possible
	 * languages. This is done so that the UI can cache both languages and when the user switches the system's
	 * language he can see the changes instantly. We check ALL titles for correctness.
	 *
	 * @param value
	 * 		the value of the field that can be either a string or an {@link MultiLanguageValue} object.
	 * @return a stream with the string values of the field.
	 */
	private static Stream<Serializable> convertMultiValue(Serializable value) {
		if (value instanceof MultiLanguageValue) {
			return ((MultiLanguageValue) value).getAllValues();
		}
		return Stream.of(value);
	}
}
