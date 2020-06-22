package com.sirma.itt.seip.instance.validator.errors;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.StringJoiner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that builds error messages for the different error types. We need this because the different errors objects
 * contain different error messages for which we may need to pass different set of data. For example for cl error we
 * want to pass the cl number as well.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 18/05/2017
 */
@ApplicationScoped
public class FieldValidationErrorBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected LabelProvider provider;

	/**
	 * Constructs a {@link BooleanFieldValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error
	 */
	public PropertyValidationError buildBooleanFieldError(PropertyDefinition property) {
		return new BooleanFieldValidationError(property, type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Constructs new {@link CodelistFieldValidationError}.
	 *
	 * @param property the {@link PropertyDefinition}
	 * @param codeValue used in the message to show the wrong code value
	 * @param codeValues the allowed code values
	 * @return the error.
	 */
	public PropertyValidationError buildCodelistFieldError(PropertyDefinition property, String codeValue,
			Map<String, CodeValue> codeValues) {
		Collection<CodeValue> values = codeValues.values();
		StringJoiner labelProvider = new StringJoiner(", ");
		for (CodeValue value : values) {
			labelProvider.add(value.getValue() + "=" + value.getProperties());
		}
		return new CodelistFieldValidationError(property,
				type -> getMessageOrDefault(type, property.getName(), codeValue, labelProvider.toString()), codeValues);
	}

	/**
	 * Constructs a new {@link DateTimeFieldValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error.
	 */
	public PropertyValidationError buildDateTimeFieldError(PropertyDefinition property) {
		return new DateTimeFieldValidationError(property, type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Constructs a {@link MandatoryFieldValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error.
	 */
	public PropertyValidationError buildMandatoryFieldError(PropertyDefinition property) {
		return new MandatoryFieldValidationError(property, type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Constructs a {@link MandatoryControlParamValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error.
	 */
	public PropertyValidationError buildMandatoryControlParamError(PropertyDefinition property) {
		return new MandatoryControlParamValidationError(property,
				type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Constructs a {@link TextFieldValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @param message this is the message returned from the {@link com.sirma.itt.seip.util.RegExGenerator}. Those messages
	 * are defined in label bundles. See the class for more information.
	 * @return the error.
	 */
	public PropertyValidationError buildTextFieldError(PropertyDefinition property, String message) {
		return new TextFieldValidationError(property,
				type -> getMessageOrDefault(type, property.getName(), property.getDataType().getName(), message));
	}

	/**
	 * Constructs a {@link UriFieldValidationError}.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error.
	 */
	public PropertyValidationError buildUriFieldError(PropertyDefinition property) {
		return new UriFieldValidationError(property, type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Constructs a new {@link com.sirma.itt.seip.instance.validator.validators.NumericFieldsValidator}. This error
	 * object is used when the number in the field is of correct type but the format is wrong. For example if the field
	 * is marked as int, n..3 (3 position) and the value is 12345.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @param message this is the message returned from the {@link com.sirma.itt.seip.util.RegExGenerator}. Those messages
	 * are defined in label bundles. See the class for more information.
	 * @return the error.
	 */
	public PropertyValidationError buildNumericFieldError(PropertyDefinition property, String message) {
		return new NumericFieldValidationError(property,
				type -> getMessageOrDefault(type, property.getName(), property.getDataType().getName(), message));
	}

	/**
	 * Constructs a new {@link com.sirma.itt.seip.instance.validator.validators.NumericFieldsValidator}. This error
	 * object is used when the number is from incorrect type.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @return the error.
	 */
	public PropertyValidationError buildNumericFieldError(PropertyDefinition property) {
		return new NumericFieldValidationError(property,
				type -> getMessageOrDefault(type, property.getName(), property.getDataType().getName()));
	}

	/**
	 * Constructs a new {@link CustomFieldValidationError}. The error can be used for plugging additional validation
	 * errors to be reported. The error message will be build by
	 * {@value PropertyValidationError#ERROR_MESSAGE_LABEL_PREFIX} concatenated with the given {@code errorType} to lower case.
	 *
	 * @param property the property definitions, used in the message to show for which field the validation has failed.
	 * @param errorType the error type to use for loading the error message
	 * @param messageArguments any message arguments that need to be passed if needed
	 * @return validation error instance
	 */
	public PropertyValidationError buildCustomError(PropertyDefinition property, String errorType,
			Object... messageArguments) {
		return new CustomFieldValidationError(property, errorType, type -> getMessageOrDefault(type, messageArguments));
	}

	/**
	 * Constructs a {@link PropertyValidationError} for unique field validation error.
	 *
	 * @param propertyDefinition - the property definition, used in the message to show for which field the validation has failed.
	 * @return validation error instance
	 */
	public PropertyValidationError buildUniqueValueError(PropertyDefinition propertyDefinition) {
		return buildCustomError(propertyDefinition, "unique_value", propertyDefinition.getLabel());
	}

	/**
	 * Builds a single value validation error.
	 *
	 * @param property the property definition for the field that failed the validation.
	 * @return the object that contains the message and its type.
	 */
	public PropertyValidationError buildSingleValueError(PropertyDefinition property) {
		return new SingleValueFieldValidationError(property, type -> getMessageOrDefault(type, property.getName()));
	}

	/**
	 * Retrieves an error message from the bundle. The error messages should be defined in the definitions.
	 *
	 * @param type The type of the error. It is used when constructing the error to set its type and also to construct
	 * the label id in the bundle.
	 * @param messageArguments the arguments for the message. Typically the messages are formatted string and we can insert values in
	 * them. <b>Here we should have at least the property name.</b>
	 * @return the formatted message with its data.
	 */
	private String getMessageOrDefault(String type, Object... messageArguments) {
		String labelId = PropertyValidationError.ERROR_MESSAGE_LABEL_PREFIX + type.toLowerCase();
		String label = provider.getLabel(labelId);
		if (StringUtils.isBlank(label) || labelId.equals(label)) {
			return getDefaultMessage(messageArguments[0]);
		}

		try {
			return String.format(label, messageArguments);
		} catch (MissingFormatArgumentException e) {
			LOGGER.warn("The configured message with id {} has incorrect number of arguments. "
					+ "Using default message instead.", labelId, e);
			// This catch is needed if for some reason the specified definition message is not correct and we don't want
			// to stop the validation but instead just construct the default error message. This can happen for example
			// if we have 3 parameters defined in the message and when we build it, we supply only two.
			return getDefaultMessage(messageArguments[0]);
		}
	}

	private static String getDefaultMessage(Object propertyName) {
		return String.format(PropertyValidationError.DEFAULT_MESSAGE, propertyName);
	}
}
