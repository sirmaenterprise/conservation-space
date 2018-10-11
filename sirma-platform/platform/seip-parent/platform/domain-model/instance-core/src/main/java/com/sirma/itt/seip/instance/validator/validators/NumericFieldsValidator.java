package com.sirma.itt.seip.instance.validator.validators;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.instance.validation.FieldValidationContext;
import com.sirma.itt.seip.instance.validation.PropertyFieldValidator;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validator.errors.FieldValidationErrorBuilder;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Validator for fields that are defined numeric types. These can be either:
 * <ul>
 * <li>int</li>
 * <li>long values</li>
 * <li>double</li>
 * <li>float</li>
 * </ul>
 * <p/>
 * First we validate the type of the field behind the serializable value using class cast and then we use a regexp to
 * validate the actual format (for example n..10, should be int maximum 10 chars long.
 * <p/>
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@Extension(target = PropertyFieldValidator.TARGET_NAME, order = 6)
public class NumericFieldsValidator extends PropertyFieldValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	static {
		NUMBER_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
		NUMBER_FORMAT.setGroupingUsed(false);
	}

	@Inject
	private FieldValidationErrorBuilder builder;

	@Override
	public boolean isApplicable(FieldValidationContext context) {
		return isNumeric(context.getPropertyDefinition().getDataType().getName()) && context.getValue() != null;
	}

	// Numeric values are also marked as "text" fields so we need to check #PropertyDefinition.getDataType().getName()
	// as well. This predicate checks if the property data type is numeric.
	private static boolean isNumeric(String type) {
		return ValidationTypes.INT.toString().equals(type) || ValidationTypes.LONG.toString().equals(type)
				|| ValidationTypes.FLOAT.toString().equals(type) || ValidationTypes.DOUBLE.toString().equals(type);
	}

	@Override
	public Stream<PropertyValidationError> validate(FieldValidationContext context) {
		try {
			Pair<String, String> nameRegEx = regExGenerator.getPattern(context.getPropertyDefinition().getType(), null);
			String value = getValueAsString(context);
			if (!value.matches(nameRegEx.getFirst())) {
				return Stream
						.of(builder.buildNumericFieldError(context.getPropertyDefinition(), nameRegEx.getSecond()));
			}
		} catch (ClassCastException e) {
			LOGGER.warn("Could not cast to class [{}]", context.getValue().getClass(), e);
			return Stream.of(builder.buildNumericFieldError(context.getPropertyDefinition()));
		}
		return Stream.empty();
	}

	private static String getValueAsString(FieldValidationContext context) {
		Serializable value = context.getValue();
		if (value instanceof Double) {
			return NUMBER_FORMAT.format(Double.class.cast(value).doubleValue()).replace(",", ".");
		}

		return Objects.toString(value);
	}
}