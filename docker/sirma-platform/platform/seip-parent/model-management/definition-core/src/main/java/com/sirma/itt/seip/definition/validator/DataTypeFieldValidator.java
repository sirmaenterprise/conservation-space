package com.sirma.itt.seip.definition.validator;

import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Checks if uri attribute of data type fields, are related to data type property.
 * If related uri is object property or it is not exist properly error message will be returned.
 *
 * @author Boyan Tonchev.
 */
public class DataTypeFieldValidator implements DefinitionValidator {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		DataTypeFieldMessageBuilder messageBuilder = new DataTypeFieldMessageBuilder(definition);
		definition.fieldsStream()
				// Data property + uri
				.filter(PropertyDefinition.isObjectProperty().negate().and(PropertyDefinition.hasUri()))
				.forEach(property -> this.validate(property, messageBuilder));
		return messageBuilder.getMessages();
	}

	private void validate(PropertyDefinition propertyDefinition, DataTypeFieldMessageBuilder messageBuilder) {
		String uri = propertyDefinition.getUri();
		PropertyInstance property = semanticDefinitionService.getProperty(uri);
		if (property == null) {
			String propertyName = propertyDefinition.getName();
			if (semanticDefinitionService.getRelation(uri) != null) {
				messageBuilder.objectPropertyAsDataProperty(propertyName, uri);
			} else {
				messageBuilder.unregisteredUri(propertyName, uri);
			}
		}
	}

	public class DataTypeFieldMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String OBJECT_PROPERTY_AS_DATA = "definition.validation.property.object.as.data";
		public static final String UNREGISTERED_PROPERTY_URI = "definition.validation.property.unregistered.uri";

		public DataTypeFieldMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void objectPropertyAsDataProperty(String propertyName, String uri) {
			error(getId(), OBJECT_PROPERTY_AS_DATA).setParams(getId(), propertyName, uri);
		}

		private void unregisteredUri(String propertyName, String uri) {
			error(getId(), UNREGISTERED_PROPERTY_URI).setParams(getId(), propertyName, uri);
		}
	}
}
