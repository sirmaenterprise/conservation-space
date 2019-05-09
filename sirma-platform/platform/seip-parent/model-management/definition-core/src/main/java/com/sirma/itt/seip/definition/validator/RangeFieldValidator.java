package com.sirma.itt.seip.definition.validator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Checks if definition range of given field is compatible with the defined range in semantics.
 *
 * @author Boyan Tonchev.
 */
public class RangeFieldValidator implements DefinitionValidator {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		RangeValidatorMessageBuilder messageBuilder = new RangeValidatorMessageBuilder(definition);
		definition.fieldsStream()
				.filter(PropertyDefinition.isObjectProperty().and(PropertyDefinition.hasUri()))
				.filter(RangeFieldValidator::propertyHasRangeControl)
				.forEach(propertyDefinition -> validateRange(propertyDefinition, messageBuilder));

		return messageBuilder.getMessages();
	}

	private static boolean propertyHasRangeControl(PropertyDefinition propertyDefinition) {
		return propertyDefinition.getControlDefinition() != null && propertyDefinition.getControlDefinition().getParam("range").isPresent();
	}

	private void validateRange(PropertyDefinition propertyDefinition, RangeValidatorMessageBuilder messageBuilder) {
		Optional<ControlParam> rangeParam = propertyDefinition.getControlDefinition().getParam("range");
		if (!rangeParam.isPresent()) {
			return;
		}

		String rangeValue = rangeParam.get().getValue();

		String propertyUri = propertyDefinition.getUri();
		String propertyName = propertyDefinition.getName();
		PropertyInstance relation = semanticDefinitionService.getRelation(propertyUri);

		// First validate if there is such property in the semantics
		if (relation == null) {
			validateMissingRelation(propertyDefinition, messageBuilder);
			return;
		}

		if (StringUtils.isBlank(rangeValue)) {
			return;
		}

		ClassInstance rangeClass = semanticDefinitionService.getClassInstance(relation.getRangeClass());
		if (rangeClass == null) {
			messageBuilder.unregisteredSemanticRange(propertyName, propertyUri, relation.getRangeClass());
			return;
		}

		extractRangeTypes(rangeValue).forEach(validateRangeType(propertyDefinition, rangeClass, messageBuilder));
	}

	private void validateMissingRelation(PropertyDefinition propertyDefinition, RangeValidatorMessageBuilder messageBuilder) {
		String propertyUri = propertyDefinition.getUri();
		PropertyInstance property = semanticDefinitionService.getProperty(propertyUri);
		if (property != null) {
			messageBuilder.usingDataPropertyAsObject(propertyDefinition.getName(), propertyUri);
		} else {
			messageBuilder.missingRelation(propertyDefinition.getName(), propertyUri);
		}
	}

	private Consumer<String> validateRangeType(PropertyDefinition propertyDefinition, ClassInstance rangeClass,
			RangeValidatorMessageBuilder messageBuilder) {
		return rangeType -> {
			String propertyUri = propertyDefinition.getUri();
			String propertyName = propertyDefinition.getName();
			try {
				String typeFullUri = typeConverter.convert(Uri.class, rangeType).toString();
				if (!typeFullUri.equals(rangeClass.getId()) && !rangeClass.hasSubType(typeFullUri)) {
					messageBuilder.relationMismatch(propertyName, rangeType, rangeClass.getId());
				}
			} catch (IllegalStateException e) {
				messageBuilder.unregisteredUri(propertyName, propertyUri, rangeType);
			}
		};
	}

	private static Stream<String> extractRangeTypes(String ranges) {
		return Arrays.stream(ranges.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank);
	}

	public static class RangeValidatorMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String USING_DATA_PROPERTY_AS_OBJECT = "definition.validation.data.property.as.object";
		public static final String MISSING_RELATION = "definition.validation.missing.relation";
		public static final String UNREGISTERED_SEMANTIC_RANGE = "definition.validation.unregistered.semantic.range";
		public static final String RELATION_MISMATCH = "definition.validation.relation.mismatch";
		public static final String UNREGISTERED_URI = "definition.validation.unregistered.uri";

		public RangeValidatorMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void usingDataPropertyAsObject(String fieldName, String propertyUri) {
			error(getId(), USING_DATA_PROPERTY_AS_OBJECT, getId(), fieldName, propertyUri);
		}

		private void missingRelation(String fieldName, String propertyUri) {
			error(getId(), MISSING_RELATION, getId(), fieldName, propertyUri);
		}

		private void unregisteredSemanticRange(String fieldName, String propertyUri, String missingRange) {
			error(getId(), UNREGISTERED_SEMANTIC_RANGE, getId(), fieldName, propertyUri, missingRange);
		}

		private void relationMismatch(String fieldName, String type, Serializable rangeClass) {
			error(getId(), RELATION_MISMATCH, getId(), fieldName, type, rangeClass);
		}

		private void unregisteredUri(String fieldName, String propertyUri, String unregisteredType) {
			error(getId(), UNREGISTERED_URI, getId(), fieldName, propertyUri, unregisteredType);
		}
	}
}
