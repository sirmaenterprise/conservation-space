package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.response;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.CONTENT_SOURCE;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.PRIMARY_CONTENT_ID;
import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties.isSystemProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.model.request.DynamicProperties;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.validation.InstanceValidationResult;
import com.sirma.itt.seip.instance.validation.InstanceValidationService;
import com.sirma.itt.seip.instance.validation.PropertyValidationError;
import com.sirma.itt.seip.instance.validation.PropertyValidationErrorTypes;
import com.sirma.itt.seip.instance.validation.ValidationContext;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.template.Template;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.template.TemplateSearchCriteria;
import com.sirma.itt.seip.template.TemplateService;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants;
import com.sirmaenterprise.sep.eai.spreadsheet.model.EAISystemProperties;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetEntry;

/**
 * Service responsible to validate the property source model and the parsed response model. In addition service contains
 * a set of other relevant model validation services.
 *
 * @author bbanchev
 */
@Singleton
class ModelValidationService {

	private static final String EXISTING_IN_CONTEXT_ERROR_MESSAGE = "validation.error.existing_in_context";
	private static final String EXISTING_WITHOUT_CONTEXT_ERROR_MESSAGE = "validation.error.existing_without_context";

	@Inject
	private InstanceValidationService validationService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private InstanceAccessEvaluator instanceAccessEvaluator;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private TemplateService templateService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private LabelProvider labelProvider;

	void validatePropertyModel(IntegrationData integrated, ErrorBuilderProvider errorBuilder)
			throws EAIReportableException {
		validatePropertyModelInternal(integrated, errorBuilder);
		checkInstancePurpose(integrated, errorBuilder);
		removeSystemProperties(integrated);
	}

	/**
	 * Validates the write permission for the class referred by the provided {@link EntityType}
	 *
	 * @param entityType
	 *            the tested source
	 * @throws EAIReportableException
	 *             on error with specific reason
	 */
	void validateCreatablePermissions(EntityType entityType) throws EAIReportableException {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(entityType.getUri());
		if (classInstance == null) {
			throw new EAIReportableException("Class instance '" + entityType.getUri() + "' could not be resolved");
		}
		Serializable uri = classInstance.getId();
		if (!instanceAccessEvaluator
				.canWrite(Objects.toString(typeConverter.convert(ShortUri.class, uri), uri.toString()))) {
			throw new EAIReportableException(
					"The current user doesn't have permissions to create object of type: " + entityType.getTitle());
		}
	}

	private void checkInstancePurpose(IntegrationData integrated, ErrorBuilderProvider errorBuilder)
			throws EAIReportableException {
		InstanceType instanceType = integrated.getIntegrated().type();
		if (instanceType.isCreatable() && instanceType.isUploadable()) {
			processCreatableAndUploadable(integrated, errorBuilder);
		} else if (instanceType.isCreatable()) {
			processCreatable(integrated, errorBuilder);
		} else if (instanceType.isUploadable()) {
			processUploadable(integrated, errorBuilder);
		} else {
			throw new EAIReportableException("The object is neither creatable or uploadable. Check system model!");
		}
	}

	private void processCreatableAndUploadable(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		// if there is contentSource but contentId is missing, we show a validation error message for missing file for
		// uploadable instance
		DynamicProperties sourceData = integrated.getSource().getProperties();
		String sourcePrimaryContentId = (String) sourceData.get(PRIMARY_CONTENT_ID);
		if (StringUtils.isNotBlank((String) sourceData.get(CONTENT_SOURCE))
				&& StringUtils.isBlank(sourcePrimaryContentId)) {
			errorBuilder.separator().append("There is no content file associated with the uploadable object.");
		} else if (StringUtils.isNotBlank(sourcePrimaryContentId)) {
			setPrimaryContentId(integrated, sourcePrimaryContentId);
			setContentProperties(integrated.getSource().getProperties(), sourcePrimaryContentId);
		}
	}

	private void setPrimaryContentId(IntegrationData integrated, Serializable primaryContentId) {
		Instance instance = integrated.getIntegrated();
		instance.add(PRIMARY_CONTENT_ID, primaryContentId);
		Template template = templateService.getTemplate(new TemplateSearchCriteria(instance.getIdentifier(),
				TemplatePurposes.UPLOADABLE, instance.getProperties()));
		if (template != null) {
			instance.add(LinkConstants.HAS_TEMPLATE, template.getCorrespondingInstance());
		}
	}

	private static void processCreatable(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		DynamicProperties sourceData = integrated.getSource().getProperties();
		if (StringUtils.isNotBlank((String) sourceData.get(PRIMARY_CONTENT_ID))) {
			errorBuilder.separator().append("There is content file associated with a creatable object.");
		}
	}

	private void processUploadable(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		DynamicProperties sourceData = integrated.getSource().getProperties();
		String sourcePrimaryContentId = (String) sourceData.get(PRIMARY_CONTENT_ID);
		if (StringUtils.isBlank(sourcePrimaryContentId)) {
			// add error message to validation report for missing primary content
			errorBuilder.separator().append("There is no content file associated with the uploadable object.");
		} else {
			setPrimaryContentId(integrated, sourcePrimaryContentId);
			setContentProperties(integrated.getSource().getProperties(), sourcePrimaryContentId);
		}
	}

	private void setContentProperties(DynamicProperties properties, String sourcePrimaryContentId) {
		ContentInfo content = instanceContentService.getContent(sourcePrimaryContentId, null);
		properties.put(EAISpreadsheetConstants.FILE_NAME_FIELD_URI, content.getName());
		properties.put(EAISpreadsheetConstants.FILE_SIZE_FIELD_URI, content.getLength());
		properties.put(EAISpreadsheetConstants.MIMETYPE_FIELD_URI, content.getMimeType());
	}

	private static void validatePropertyModelInternal(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		// validate if property is not part of the model
		String definitionId = integrated.getType().getIdentifier();
		EntityType type = integrated.getModelConfiguration().getTypeByDefinitionId(definitionId);
		Function<EntityProperty, String> propertyMapper = EntityProperty::getUri;
		Set<String> modelProperties = type.getProperties().stream().map(propertyMapper).collect(Collectors.toSet());
		Predicate<String> nonModelFilter = propertyId -> !modelProperties.contains(propertyId)
				&& !isSystemProperty(propertyId);
		SpreadsheetEntry source = integrated.getSource();
		Set<String> nonModelProperties = source
				.getProperties()
					.keySet()
					.stream()
					.filter(nonModelFilter)
					.collect(Collectors.toSet());
		if (!nonModelProperties.isEmpty()) {
			int errorsCount = nonModelProperties.size();
			errorBuilder
					.separator()
						.append("Propert")
						.append(errorsCount > 1 ? "ies " : "y ")
						.append(nonModelProperties)
						.append(errorsCount > 1 ? " are " : " is ")
						.append("not valid for ")
						.append(type.getTitle());
		}
	}

	private static void removeSystemProperties(IntegrationData data) {
		data.getSource().getProperties().remove(EAISystemProperties.IMPORT_STATUS);
		data.getSource().getProperties().remove(EAISystemProperties.CONTENT_SOURCE);
	}

	void validateInstanceModel(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		InstanceValidationResult instanceValidationResult = validationService
				.validate(new ValidationContext(integrated.getIntegrated(), integrated.getOperation()));
		if (instanceValidationResult.hasPassed()) {
			return;
		}
		// collect by type
		Map<String, List<PropertyValidationError>> errorsByTypes = new LinkedHashMap<>();
		for (PropertyValidationError error : instanceValidationResult.getErrorMessages()) {
			errorsByTypes.computeIfAbsent(error.getValidationType(), k -> new LinkedList<>()).add(error);
		}
		Set<Entry<String, List<PropertyValidationError>>> errorEntries = errorsByTypes.entrySet();
		for (Entry<String, List<PropertyValidationError>> entry : errorEntries) {
			errorBuilder.separator().append(processValidationError(entry));
		}
	}

	void validateExistingInContext(IntegrationData integrated, ErrorBuilderProvider errorBuilder) {
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(integrated.getIntegrated());
		if (integrated.getContext() == null) {
			if (!validationService.canExistWithoutContext(instanceDefinition)) {
				errorBuilder.separator().append(labelProvider.getValue(EXISTING_WITHOUT_CONTEXT_ERROR_MESSAGE));
			}
		} else if (!validationService.canExistInContext(instanceDefinition)) {
			errorBuilder.separator().append(labelProvider.getValue(EXISTING_IN_CONTEXT_ERROR_MESSAGE));
		}
	}

	private static String processValidationError(Entry<String, List<PropertyValidationError>> entry) {
		String validationType = entry.getKey();
		List<PropertyValidationError> errors = entry.getValue();
		Function<PropertyValidationError, Object[]> defaultArgs = p -> new Object[] { p.getValidationType(),
				p.getFieldName().getName(),
				(p.getFieldName().getUri() != null ? "(" + p.getFieldName().getUri() + ")" : "") };
		if (nullSafeEquals(validationType, PropertyValidationErrorTypes.MISSING_MANDATORY_PROPERTY)) {
			String concatMandatory = buildErrorMessage(errors, "%s%s", true, ",",
					p1 -> new Object[] { p1.getFieldName().getName(),
							(p1.getFieldName().getUri() != null ? "(" + p1.getFieldName().getUri() + ")" : "") });
			return String.format("Missing mandatory propert%s [%s].", errors.size() == 1 ? "y" : "ies",
					concatMandatory);
		}
		return buildErrorMessage(errors, "%s for %s%s", false, "; ", defaultArgs);
	}

	private static String buildErrorMessage(final List<PropertyValidationError> errors, final String message,
			final boolean useDefaultMsg, final String delimiter,
			final Function<PropertyValidationError, Object[]> args) {
		StringJoiner joiner = new StringJoiner(delimiter);
		errors.stream().map(error -> {
			if (useDefaultMsg || isDefaultMessage(error)) {
				return String.format(message, args.apply(error));
			}
			return error.getMessage();
		}).forEach(joiner::add);
		return joiner.toString();
	}

	private static boolean isDefaultMessage(PropertyValidationError error) {
		return error.getMessage().startsWith(PropertyValidationError.ERROR_MESSAGE_LABEL_PREFIX)
				|| PropertyValidationError.DEFAULT_MESSAGE.regionMatches(0, error.getMessage(), 0, 10);
	}

}
