package com.sirma.itt.seip.definition.validator;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Validator class that checks for missing field type.
 *
 * @author BBonev
 */
public class MissingFieldTypeValidator implements DefinitionValidator {

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		MissingFieldTypeMessageBuilder messageBuilder = new MissingFieldTypeMessageBuilder(definition);

		findFieldsWithoutType(definition).forEach(propertyDefinition -> messageBuilder.missingFieldType(propertyDefinition.getName()));

		return messageBuilder.getMessages();
	}

	private static Stream<PropertyDefinition> findFieldsWithoutType(DefinitionModel model) {
		return model.fieldsStream().filter(propertyDefinition -> !validatePropertyDefinition(propertyDefinition));
	}

	private static boolean validatePropertyDefinition(PropertyDefinition propertyDefinition) {
		return StringUtils.isNotBlank(propertyDefinition.getType()) && propertyDefinition.getDataType() != null;
	}

	public class MissingFieldTypeMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String MISSING_FIELD_TYPE = "definition.validation.field.missing.type";

		public MissingFieldTypeMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void missingFieldType(String fieldName) {
			error(getId(), MISSING_FIELD_TYPE, getId(), fieldName);
		}
	}
}
