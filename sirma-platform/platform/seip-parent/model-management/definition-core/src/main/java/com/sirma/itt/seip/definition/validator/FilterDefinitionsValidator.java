package com.sirma.itt.seip.definition.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Checks if filter for definitions is added to field which can't be filtered.
 * For example field with uri "emf:type" can't be filtering it is used by system.
 *
 * @author Boyan Tonchev.
 */
public class FilterDefinitionsValidator implements DefinitionValidator {

	private static final Set<String> nonFilterableUris = new HashSet<>(2);

	static {
		nonFilterableUris.add("emf:type");
		nonFilterableUris.add("emf:status");
	}

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		FilterValidatorMessageBuilder messageBuilder = new FilterValidatorMessageBuilder(definition);
		definition.fieldsStream()
				.filter(PropertyDefinition.hasUri())
				.filter(propertyDefinition -> nonFilterableUris.contains(PropertyDefinition.resolveUri().apply(propertyDefinition)))
				.filter(FilterDefinitionsValidator::hasDefinitionsFilter)
				.forEach(propertyDefinition -> addMessage(propertyDefinition, messageBuilder));
		return messageBuilder.getMessages();
	}

	private static void addMessage(PropertyDefinition propertyDefinition, FilterValidatorMessageBuilder messageBuilder) {
		messageBuilder.filteringNotSupported(propertyDefinition.getName(), PropertyDefinition.resolveUri().apply(propertyDefinition));
	}

	private static boolean hasDefinitionsFilter(PropertyDefinition propertyDefinition) {
		return CollectionUtils.isNotEmpty(propertyDefinition.getFilters());
	}

	public static class FilterValidatorMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String FIELD_WITH_NON_SUPPORTED_FILTERING = "definition.validation.field.non.supported.filtering";

		public FilterValidatorMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void filteringNotSupported(String fieldName, String uri) {
			error(getId(), FIELD_WITH_NON_SUPPORTED_FILTERING, getId(), fieldName, uri);
		}
	}
}
