package com.sirma.itt.seip.definition.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;

/**
 * Checks if definition range of given field is compatible with the defined range in semantics.
 *
 * @author Boyan Tonchev.
 */
public class RangeFieldValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RangeFieldValidator.class);

	private static final String START_LOG_ERROR_MESSAGE = "=================  Errors are found in definition %s  =================";
	private static final String END_LOG_ERROR_MESSAGE = "=====================================================================================";

	private static final String MISMATCH_ERROR_MESSAGE = "There is incorrect property range %1s in property: %3s. It have to be: %2s or subclass of it!";
	private static final String NOT_REGISTERED_URI_ERROR_MESSAGE = "Uri: %1s of property: %2s is not registered!";
	private static final String RELATION_NOT_FOUND_ERROR_MESSAGE = "Uri: %1s of property: %2s is not found!";
	private static final String NOT_REGISTERED_SEMANTIC_RANGE = "Semantic range: %1s of uri %2s not found!";
	private static final String CAN_NOT_USE_DATA_PROPERTY_AS_OBJECT = "Uri %1s of property %2s is defined as data property and can't be used as object property!";

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private TypeConverter typeConverter;

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		return this.validate((DefinitionModel) model);
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		if (model instanceof GenericDefinition) {
			List<String> errorMessages = model.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty().and(PropertyDefinition.hasUri()))
					.flatMap(this::validate)
					.collect(Collectors.toList());
			logErrorMessages(model.getIdentifier(), errorMessages);
			return errorMessages;
		}
		return Collections.emptyList();
	}

	private Stream<String> validate(PropertyDefinition propertyDefinition) {
		return Optional.ofNullable(propertyDefinition.getControlDefinition())
				.flatMap(controlDefinition -> controlDefinition.getParam("range"))
				.map(controlParam -> {
					String propertyUri = propertyDefinition.getUri();
					String propertyName = propertyDefinition.getName();
					PropertyInstance relation = semanticDefinitionService.getRelation(propertyUri);
					if (relation == null) {
						PropertyInstance property = semanticDefinitionService.getProperty(propertyUri);
						if (property != null) {
							return Stream.of(getErrorMessage(CAN_NOT_USE_DATA_PROPERTY_AS_OBJECT, propertyUri, propertyName));
						}
						return Stream.of(getErrorMessage(RELATION_NOT_FOUND_ERROR_MESSAGE, propertyUri, propertyName));
					} else {
						return validateTypesFromPropertyRange(propertyName, propertyUri, controlParam.getValue(),
															  relation.getRangeClass());
					}
				})
				.orElse(Stream.empty());
	}

	private Stream<String> validateTypesFromPropertyRange(String propertyName, String propertyUri,
			String rangesFromDefinition, String semanticRange) {
		if (StringUtils.isBlank(rangesFromDefinition)) {
			return Stream.empty();
		}
		return Optional.ofNullable(semanticDefinitionService.getClassInstance(semanticRange))
				.map(classInstance -> Arrays.stream(rangesFromDefinition.split(","))
						.map(String::trim)
						.filter(StringUtils::isNotBlank)
						.map(type -> validateTypeFromPropertyRange(type, propertyName, propertyUri, semanticRange,
																   classInstance))
						.filter(StringUtils::isNotBlank))
				.orElse(Stream.of(getErrorMessage(NOT_REGISTERED_SEMANTIC_RANGE, semanticRange, propertyUri)));
	}

	private String validateTypeFromPropertyRange(String type, String propertyName, String propertyUri,
			String semanticRange, ClassInstance classInstance) {
		try {
			String typeFullUri = typeConverter.convert(Uri.class, type).toString();
			if (!typeFullUri.equals(classInstance.getId()) && !classInstance.hasSubType(typeFullUri)) {
				return getErrorMessage(MISMATCH_ERROR_MESSAGE, type, propertyName, semanticRange);
			}
		} catch (IllegalStateException e) {
			return getErrorMessage(NOT_REGISTERED_URI_ERROR_MESSAGE, propertyUri, propertyName);
		}
		return "";
	}

	private static String getErrorMessage(String errorMessagePattern, String... args) {
		return String.format(errorMessagePattern, args);
	}

	private void logErrorMessages(String definitionId, List<String> errorMessages) {
		if (errorMessages.isEmpty()) {
			return;
		}
		String errorFoundMessage = String.format(START_LOG_ERROR_MESSAGE, definitionId);
		LOGGER.error(errorFoundMessage);
		errorMessages.forEach(LOGGER::error);
		LOGGER.error(END_LOG_ERROR_MESSAGE);
	}
}
