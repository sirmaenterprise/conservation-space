package com.sirma.itt.objects.web.definitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * TODO THIS CLASS WILL BE REMOVED WHEN THE ALL BRANCHES FOR THIS FUNCTIONALITIES ARE MERGED.
 * <p>
 * Helper methods for working with instance definitions: loading, sorting, copying.
 *
 * @author svelikov
 */
@ApplicationScoped
public class DefinitionHelperOld {

	private static final String MULTIVALUE = "multivalue";
	private static final String VALUE_LABEL = "valueLabel";
	private static final String CONTROL = "control";
	private static final String VALIDATORS = "validators";
	private static final String MAX_LENGTH = "maxLength";
	private static final String FILTERS = "filters";
	private static final String PREVIEW_EMPTY = "previewEmpty";
	private static final String IS_MANDATORY = "isMandatory";
	private static final String VALIDATION_FIELD_MANDATORY_LBL = "validation.field.mandatory";
	private static final String CODELIST = "codelist";
	private static final String LABEL = "label";
	private static final String DISPLAY_TYPE = "displayType";
	private static final String IDENTIFIER = "identifier";
	private static final String MESSAGES = "messages";
	private static final String DATA_TYPE = "dataType";
	private static final String DEFAULT_VALUE = "defaultValue";
	private static final String VALUE = "value";
	private static final String VALIDATION_MODEL = "validationModel";
	private static final String VIEW_MODEL = "viewModel";
	private static final String FIELDS = "fields";
	private static final String CONTROL_PARAMS = "controlParams";
	private static final String CONTROL_FIELDS = "controlFields";

	@Inject
	protected DefinitionService definitionService;

	@Inject
	protected StateTransitionManager transitionManager;

	@Inject
	private StateService stateService;

	@Inject
	private CodelistService codelistService;

	/**
	 * Load an instance definition.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param instanceType
	 *            the instance type
	 * @return the definition model
	 */
	public DefinitionModel loadDefinition(String definitionId, String instanceType) {
		boolean nullOrEmptyDefinitionId = StringUtils.isBlank(definitionId);
		boolean nullOrEmptyType = StringUtils.isBlank(instanceType);
		if (nullOrEmptyDefinitionId || nullOrEmptyType) {
			BadRequestException e = new BadRequestException("Missing required parameters for definition load");
			if (nullOrEmptyDefinitionId) {
				e.getMessages().put("definitionId", "[definitionId] is required");
			}
			if (nullOrEmptyType) {
				e.getMessages().put("instanceType", "[instanceType] is required");
			}
			throw e;
		}

		return definitionService.find(definitionId);
	}

	/**
	 * Converts a definition model and instance model to external model in json format to be used for ui representation.
	 *
	 * @param externalModel
	 *            instance properties mapping: property id to value
	 * @param definitionFields
	 *            definition fields including regions
	 * @param labelProvider
	 *            label provider
	 * @param mandatoryFieldIds
	 *            All mandatory fields by definition for current state.
	 * @return the definition model in json format
	 */
	public JSONObject toJsonModel(Map<String, ?> externalModel, List<Ordinal> definitionFields,
			LabelProvider labelProvider, Set<String> mandatoryFieldIds) {
		if (externalModel == null || definitionFields == null || labelProvider == null) {
			throw new InternalServerErrorException("Can not build definition json model");
		}
		JSONObject models = new JSONObject();
		JSONObject viewModel = new JSONObject();
		JSONObject validationModel = new JSONObject();
		JsonUtil.addToJson(viewModel, FIELDS, new JSONArray());
		buildModel(viewModel, validationModel, definitionFields, externalModel, mandatoryFieldIds, labelProvider);
		JsonUtil.addToJson(models, VIEW_MODEL, viewModel);
		JsonUtil.addToJson(models, VALIDATION_MODEL, validationModel);
		return models;
	}

	/**
	 * Extracts the definition fields sorting them by the order attribute.
	 *
	 * @param model
	 *            the definition model
	 * @return the field definitions sorted by order
	 */
	public List<Ordinal> collectAllFields(DefinitionModel model) {
		if (model == null) {
			throw new InternalServerErrorException("Can not load definition fields due to missing definition model");
		}

		List<Ordinal> result = new LinkedList<>(model.getFields());
		if (model instanceof RegionDefinitionModel) {
			result.addAll(getRegionsCopy((RegionDefinitionModel) model));
		}

		DefinitionUtil.sort(result);
		return result;
	}

	/**
	 * Determine the required fields that need to be marked as such.
	 *
	 * @param model
	 *            the definition model
	 * @param mandatoryFieldIds
	 *            contains all mandatory fields by definition and for given state
	 * @return the required fields by definition
	 */
	public List<Ordinal> collectMandatoryFields(DefinitionModel model, Set<String> mandatoryFieldIds) {
		// maybe we should skip non editable fields
		return model.fieldsStream().filter(f -> isRequiredOrRepresentable(mandatoryFieldIds, f)).collect(Collectors.toList());
	}

	/**
	 * Check for every definition field if is mandatory for the current state and returns their identifiers.
	 *
	 * @param model
	 *            The definition model.
	 * @param instance
	 *            Current instance to be used for checking the state.
	 * @param operation
	 *            Current transition for which to check the mandatory fields.
	 * @return A set containing all mandatory fields for given state.
	 */
	public Set<String> getMandatoryFieldIds(DefinitionModel model, Instance instance, String operation) {
		if (model == null || instance == null || operation == null) {
			throw new InternalServerErrorException(
					"Can not load definition fields due to missing definition model or operation");
		}
		String primaryState = stateService.getPrimaryState(instance);
		return transitionManager.getRequiredFields(instance, primaryState, operation);
	}

	private static boolean isRequiredOrRepresentable(Set<String> requiredFields, PropertyDefinition f) {
		boolean requiredForTransition = requiredFields.contains(f.getIdentifier());
		boolean requiredByDefault = f.isMandatory();
		boolean isRepresentable = !DefaultProperties.NON_REPRESENTABLE_FIELDS.contains(f.getIdentifier());
		return (requiredForTransition || requiredByDefault) && isRepresentable;
	}

	private JSONObject buildModel(JSONObject viewModel, JSONObject validationModel, List<?> sortedFields,
			Map<String, ?> externalModel, Set<String> mandatoryFieldIds, LabelProvider labelProvider) {
		JSONArray viewFields = JsonUtil.getJsonArray(viewModel, FIELDS);
		for (Object propertyDefinition : sortedFields) {
			if (propertyDefinition instanceof RegionDefinition) {
				JSONObject regionJSON = regionDefinitionToJSON((RegionDefinition) propertyDefinition, labelProvider);

				JSONArray validatorsArray = new JSONArray();
				addConditionsValidator((Conditional) propertyDefinition, validatorsArray);
				JsonUtil.addToJson(regionJSON, VALIDATORS, validatorsArray);

				viewFields.put(regionJSON);
				buildModel(regionJSON, validationModel, ((RegionDefinition) propertyDefinition).getFields(),
						externalModel, mandatoryFieldIds, labelProvider);
			} else {
				JSONObject propertyJSON = propertyDefinitionToJSON((PropertyDefinition) propertyDefinition,
						mandatoryFieldIds, labelProvider);
				viewFields.put(propertyJSON);
				JSONObject fieldValidationModelJSON = createFieldValidationModel(
						(PropertyDefinition) propertyDefinition, externalModel);
				JsonUtil.addToJson(validationModel, ((PropertyDefinition) propertyDefinition).getIdentifier(),
						fieldValidationModelJSON);
			}
		}
		return viewModel;
	}

	/**
	 * Creates a field validation model that can be used for storing, validation and sending back for instance update or
	 * create.
	 *
	 * @param propertyDefinition
	 *            the property definition
	 * @param externalModel
	 *            the external model
	 * @return the JSON object
	 */
	private JSONObject createFieldValidationModel(PropertyDefinition propertyDefinition, Map<String, ?> externalModel) {
		JSONObject object = new JSONObject();
		String identifier = propertyDefinition.getIdentifier();

		Object value = externalModel.get(identifier);

		if (value == null && DataTypeDefinition.BOOLEAN.equals(propertyDefinition.getDataType().getName())) {
			value = Boolean.FALSE;
		}

		JsonUtil.addToJson(object, VALUE, value);
		JsonUtil.addToJson(object, DEFAULT_VALUE, value);
		JsonUtil.addToJson(object, DATA_TYPE, propertyDefinition.getDataType().getName());
		JsonUtil.addToJson(object, MESSAGES, new JSONObject());

		boolean hasCodelist = propertyDefinition.getCodelist() != null;
		if (hasCodelist) {
			String valueLabel = null;
			if (value instanceof Collection) {
				List<String> values = new ArrayList<>();
				values.addAll((Collection<? extends String>) value);
				StringBuilder labels = new StringBuilder();
				for (String code : values) {
					CodeValue cv = codelistService.getCodeValue(propertyDefinition.getCodelist(), code);
					labels.append(codelistService.getDescription(cv)).append(", ");
				}
				int length = labels.length();
				if (length > 2) {
					labels.delete(length - 2, length);
				}
				valueLabel = labels.toString();
			} else if (value instanceof String) {
				CodeValue cv = codelistService.getCodeValue(propertyDefinition.getCodelist(), (String) value);
				valueLabel = codelistService.getDescription(cv);
			}
			JsonUtil.addToJson(object, VALUE_LABEL, valueLabel);
		}
		return object;
	}

	/**
	 * Converts a property definition to json.
	 *
	 * @param property
	 *            the property definition
	 * @param mandatoryFieldIds
	 *            all mandatory fields by definition and state
	 * @return the JSON object
	 */
	private JSONObject propertyDefinitionToJSON(PropertyDefinition property, Set<String> mandatoryFieldIds,
			LabelProvider labelProvider) {
		JSONObject object = new JSONObject();
		String identifier = property.getIdentifier();
		JsonUtil.addToJson(object, IDENTIFIER, identifier);

		addControl(property, object, mandatoryFieldIds, labelProvider);
		JsonUtil.addToJson(object, DATA_TYPE, property.getDataType().getName());
		Boolean multiValued = property.isMultiValued();
		if (multiValued != null && multiValued) {
			JsonUtil.addToJson(object, MULTIVALUE, multiValued);
		}
		JsonUtil.addToJson(object, DISPLAY_TYPE, property.getDisplayType().name());
		JsonUtil.addToJson(object, LABEL, labelProvider.getLabel(property.getLabelId()));
		JsonUtil.addToJson(object, CODELIST, property.getCodelist());
		JsonUtil.addToJson(object, DEFAULT_VALUE, property.getDefaultValue());
		JsonUtil.addToJson(object, PREVIEW_EMPTY, property.isPreviewEnabled());
		JsonUtil.addToJson(object, MAX_LENGTH, property.getMaxLength());
		JsonUtil.addToJson(object, FILTERS, property.getFilters());
		JsonUtil.addToJson(object, "uri", property.getUri());
		JsonUtil.addToJson(object, "tooltip", property.getTooltip());
		JsonUtil.addToJson(object, "isDataPropery", PropertyDefinition.isObjectProperty().negate().test(property));
		JSONArray validatorsArray = new JSONArray();
		JsonUtil.addToJson(object, VALIDATORS, validatorsArray);
		boolean mandatoryForCurrentState = mandatoryFieldIds.contains(identifier);
		Boolean mandatoryByDefinition = property.isMandatory();
		boolean isMandatory = mandatoryForCurrentState || mandatoryByDefinition;
		JsonUtil.addToJson(object, IS_MANDATORY, isMandatory);
		addMandatoryValidator(validatorsArray, isMandatory, labelProvider);
		addRegexValidator(property, validatorsArray, labelProvider);
		addRelatedCodelistValidator(property, validatorsArray);
		addConditionsValidator(property, validatorsArray);
		return object;
	}

	private void addControl(PropertyDefinition propertyDefinition, JSONObject object, Set<String> mandatoryFieldIds,
			LabelProvider labelProvider) {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition != null) {
			JSONObject control = new JSONObject();
			JsonUtil.addToJson(object, CONTROL, control);
			JsonUtil.addToJson(control, IDENTIFIER, controlDefinition.getIdentifier());
			// fill in control parameters
			JSONObject params = new JSONObject();
			JsonUtil.addToJson(control, CONTROL_PARAMS, params);
			controlDefinition.paramsStream().forEach(controlParam ->
				JsonUtil.addToJson(params, controlParam.getName().toLowerCase(), controlParam.getValue())
			);
			// fill in control fields
			JSONArray fields = new JSONArray();
			controlDefinition.getFields().forEach(fieldDefinition -> {
				JSONObject controlField = new JSONObject();
				JsonUtil.addToJson(controlField, "name", fieldDefinition.getName());
				JsonUtil.addToJson(controlField, "label", labelProvider.getLabel(fieldDefinition.getLabelId()));
				fields.put(controlField);
			});
			JsonUtil.addToJson(control, CONTROL_FIELDS, fields);
			List<PropertyDefinition> controlFieldsDefinitions = controlDefinition.getFields();
			for (PropertyDefinition definition : controlFieldsDefinitions) {
				propertyDefinitionToJSON(definition, mandatoryFieldIds, labelProvider);
			}
		}
	}

	/**
	 * Builds a validator definition (for related codelists) and put it in the validators array.
	 *
	 * @param propertyDefinition
	 *            - definition for given field
	 * @param validatorsArray
	 *            - array with validators
	 */
	private static void addRelatedCodelistValidator(PropertyDefinition propertyDefinition, JSONArray validatorsArray) {

		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
		if (controlDefinition != null) {
			JSONObject relatedCodelistDefinition = new JSONObject();
			JsonUtil.addToJson(relatedCodelistDefinition, "id", "relatedCodelistFilter");
			JsonUtil.addToJson(relatedCodelistDefinition, "level", "error");

			controlDefinition.paramsStream().forEach(controllParam -> JsonUtil.addToJson(relatedCodelistDefinition,
				 controllParam.getName().toLowerCase(),controllParam.getValue()));
			validatorsArray.put(relatedCodelistDefinition);
		}
	}

	/**
	 * Builds a validator for conditions and put it in the validators array.
	 *
	 * @param definition
	 *            - property definition for given field
	 * @param validatorsArray
	 *            - array with validators
	 */
	private static void addConditionsValidator(Conditional definition, JSONArray validatorsArray) {
		if (CollectionUtils.isEmpty(definition.getConditions())) {
			return;
		}
		List<Condition> conditions = definition.getConditions();
		JSONArray conditionsArray = new JSONArray();

		for (Condition condition : conditions) {
			JSONObject rule = new JSONObject();
			JsonUtil.addToJson(rule, "id", condition.getIdentifier());
			JsonUtil.addToJson(rule, "renderAs", condition.getRenderAs());
			JsonUtil.addToJson(rule, "expression", condition.getExpression());
			conditionsArray.put(rule);
		}
		JSONObject validator = new JSONObject();
		JsonUtil.addToJson(validator, "id", "condition");
		JsonUtil.addToJson(validator, "rules", conditionsArray);
		validatorsArray.put(validator);
	}

	/**
	 * Builds a validator definition and put it in the validators array.
	 *
	 * <pre>
	 * <code>
	 * {
	 * 		id: 'regex',
	 * 		context: {
	 * 			pattern: '[\\s\\S]{1,20}'
	 * 		},
	 * 		message: 'This field should be max 20 characters length',
	 * 		level: 'error'
	 * }
	 * </code>
	 * </pre>
	 */
	private static void putValidatorDefinition(JSONArray validatorsArray, String validatorType,
			JSONObject validatorContext, String message, String errorLevel) {
		JSONObject validatorDefinition = new JSONObject();
		JsonUtil.addToJson(validatorDefinition, "id", validatorType);
		if (validatorContext != null) {
			JsonUtil.addToJson(validatorDefinition, "context", validatorContext);
		}
		JsonUtil.addToJson(validatorDefinition, "message", message);
		JsonUtil.addToJson(validatorDefinition, "level", errorLevel);
		validatorsArray.put(validatorDefinition);
	}

	private static void addMandatoryValidator(JSONArray validatorsArray, boolean isMandatory,
			LabelProvider labelProvider) {
		if (isMandatory) {
			putValidatorDefinition(validatorsArray, "mandatory", null,
					labelProvider.getValue(VALIDATION_FIELD_MANDATORY_LBL), "error");
		}
	}

	/**
	 * If a field has a type which might be converted to regex pattern then a validator definition is built and added to
	 * the field's model.
	 *
	 * @param propertyDefinition
	 *            Current property definition for which to extract and fill the type validation regex and message.
	 * @param validatorsArray
	 * @param labelProvider
	 */
	private static void addRegexValidator(PropertyDefinition propertyDefinition, JSONArray validatorsArray,
			LabelProvider labelProvider) {
		String type = propertyDefinition.getType();
		boolean isDatetime = type.startsWith("date");
		if (isDatetime) {
			return;
		}
		RegExGenerator regExGenerator = new RegExGenerator(labelProvider == null ? null : labelProvider::getValue);
		String fieldRncPattern = propertyDefinition.getRnc();
		Pair<String, String> pattern = regExGenerator.getPattern(type, fieldRncPattern);
		String regex = pattern.getFirst();
		if (regex != null) {
			JSONObject validatorContext = new JSONObject();
			JsonUtil.addToJson(validatorContext, "pattern", regex);
			putValidatorDefinition(validatorsArray, "regex", validatorContext, pattern.getSecond(), "error");
		}
	}

	/**
	 * Converts a region definition to json.
	 *
	 * @param regionDefinition
	 *            the region definition
	 * @return the JSON object
	 */
	private static JSONObject regionDefinitionToJSON(RegionDefinition regionDefinition, LabelProvider labelProvider) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, IDENTIFIER, regionDefinition.getIdentifier());
		// for some reason some of the regions does not have a display type
		// JsonUtil.addToJson(object, "displayType", regionDefinition.getDisplayType().name())
		// JsonUtil.addToJson(object, DATA_TYPE, REGION)
		JsonUtil.addToJson(object, LABEL, labelProvider.getLabel(regionDefinition.getLabelId()));
		JsonUtil.addToJson(object, DISPLAY_TYPE, regionDefinition.getDisplayType().name());
		JsonUtil.addToJson(object, FIELDS, new JSONArray());
		return object;
	}

	/**
	 * Creates a region definition copy to be used when fields would be modified or reordered.
	 *
	 * @param model
	 *            the region definition
	 * @return the region definition copy
	 */
	private static List<RegionDefinition> getRegionsCopy(RegionDefinitionModel model) {
		List<RegionDefinition> regionsCopy = new ArrayList<>(model.getRegions().size());
		for (RegionDefinition region : model.getRegions()) {
			regionsCopy.add(((RegionDefinitionImpl) region).createCopy());
		}
		return regionsCopy;
	}
}