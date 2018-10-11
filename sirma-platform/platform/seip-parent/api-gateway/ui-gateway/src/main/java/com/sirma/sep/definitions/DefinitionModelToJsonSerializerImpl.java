package com.sirma.sep.definitions;

import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.ANY;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.DATE;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.DATETIME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.DEFAULT_HEADERS;
import static com.sirma.itt.seip.rest.utils.JSON.addIfNotNull;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.BINDINGS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CALCULATION_RULE_ID;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CALCULATION_VALIDATOR_ID_VALUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CODELIST_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONDITION;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONDITION_EXPRESSION;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONDITION_RENDER_AS;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONDITION_RULES;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONTEXT_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONTROL_FIELDS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONTROL_ID;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONTROL_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.CONTROL_PARAMS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DATA_TYPE_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DEFAULT_VALUE_PATTERN;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DEFINITION_ID_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DEFINITION_LABEL_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.DISPLAY_TYPE_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.ERROR_VALUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FIELDS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FIELD_VALIDATION_MANDATORY_LBL;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FIELD_VALIDATION_UNIQUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FILTERS;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.FUNCTIONS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.IDENTIFIER_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.ID_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.INSTANCE_TYPE_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.IS_DATA_PROPERTY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.IS_MANDATORY_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.LABEL_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.LEVEL_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.MANDATORY_VALIDATOR_ID_VALUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.MAX_LENGTH_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.MESSAGES_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.MESSAGE_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.MULTIVALUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PATTERN_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PREVIEW_EMPTY_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PROPERY_BINDING_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.PROPERY_FUNCTION_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.REGEX_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.RELATED_FIELDS;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.RELATED_FIELDS_VALIDATOR_ID_VALUE;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.TOOLTIP;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.UNIQUE_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.URI;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.VALIDATION_MODEL_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.VALIDATORS_KEY;
import static com.sirma.sep.definitions.DefinitionModelSerializationConstants.VIEW_MODEL_KEY;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * Contains logic that converts {@link DefinitionModel} to JSON. Primary used in definition model writers. Example of
 * generated JSON can be found in the test resources directory in definition-model-test.json file.
 *
 * @author A. Kunchev
 */
@Singleton
public class DefinitionModelToJsonSerializerImpl implements DefinitionModelToJsonSerializer {

	private static final Set<String> NO_REGEX_DATA_TYPES = new HashSet<>(
			Arrays.asList(DATE, DATETIME, ANY, DataTypeDefinition.URI));

	private static final Predicate<Identity> EXCLUDE_HEADERS = identity -> !DEFAULT_HEADERS
			.contains(identity.getIdentifier());

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private StateService stateService;

	@Inject
	private StateTransitionManager stateTransitionManager;

	@Inject
	private DefinitionHelper definitionHelper;

	/**
	 * Note that you should pass the generator with the container in which should be stored the definition model.<br />
	 * Execute {@link JsonGenerator#writeStartObject()} or {@link JsonGenerator#writeStartArray()} before passing the
	 * generator. Also keep in mind that you are responsible for the container closing.
	 */
	@Override
	public void serialize(DefinitionModel model, Instance instance, String operation, JsonGenerator generator) {
		serialize(model, instance, operation, null, generator);
	}

	@Override
	public void serialize(DefinitionModel model, Instance instance, String operation, Set<String> fieldsToSerialize,
			JsonGenerator generator) {
		JsonObjectBuilder validationModel = Json.createObjectBuilder();
		JsonArrayBuilder viewModelFields = Json.createArrayBuilder();

		Set<String> mandatoryForState = getMandatoryFieldsForState(instance, operation);
		Collection<Ordinal> fields = definitionHelper.collectAllFields(model);

		Predicate<Identity> selectedFields = EXCLUDE_HEADERS;
		if (fieldsToSerialize != null) {
			selectedFields = EXCLUDE_HEADERS.and(identity -> fieldsToSerialize.contains(identity.getIdentifier()));
		}

		serializeInternal(fields, mandatoryForState, viewModelFields, validationModel, selectedFields);

		generator.write(VALIDATION_MODEL_KEY, validationModel.build());
		generator.writeStartObject(VIEW_MODEL_KEY).write(FIELDS_KEY, viewModelFields.build()).writeEnd();

		writeAdditionalData(instance, model, generator);
	}

	private Set<String> getMandatoryFieldsForState(Instance instance, String operation) {
		if (instance == null || StringUtils.isBlank(operation)) {
			return Collections.emptySet();
		}

		String primaryState = stateService.getPrimaryState(instance);
		return stateTransitionManager.getRequiredFields(instance, primaryState, operation);
	}

	private void serializeInternal(Collection<?> fields, Set<String> stateMandatory, JsonArrayBuilder viewModelFields,
			JsonObjectBuilder validationModel, Predicate<Identity> fieldsFilter) {
		for (Object definition : fields) {
			if (definition instanceof RegionDefinition) {
				RegionDefinition regionDefinition = (RegionDefinition) definition;
				JsonArrayBuilder validators = Json.createArrayBuilder();
				addConditionsValidator((Conditional) definition, validators);

				JsonObjectBuilder region = Json.createObjectBuilder().add(IDENTIFIER_KEY,
						regionDefinition.getIdentifier());
				addIfNotNull(region, LABEL_KEY, regionDefinition.getLabel());
				addIfNotNull(region, DISPLAY_TYPE_KEY, regionDefinition.getDisplayType().name());

				JsonArrayBuilder regionFields = Json.createArrayBuilder();
				serializeInternal(regionDefinition.getFields(), stateMandatory, regionFields, validationModel,
						fieldsFilter);
				region.add(FIELDS_KEY, regionFields.build());
				region.add(VALIDATORS_KEY, validators.build());
				viewModelFields.add(region.build());
			} else if (fieldsFilter.test((PropertyDefinition) definition)) {
				PropertyDefinition propertyDefinition = (PropertyDefinition) definition;
				writeFieldValidationModel(propertyDefinition, validationModel);
				writePropertyDefinition(propertyDefinition, viewModelFields, stateMandatory);
			}
		}
	}

	private static void writeFieldValidationModel(PropertyDefinition property, JsonObjectBuilder validationModel) {

		JsonObjectBuilder builder = Json.createObjectBuilder()
				// implement when there is a server side validations
				.add(MESSAGES_KEY, Json.createArrayBuilder().build());

		validationModel.add(property.getIdentifier(), builder.build());
	}

	private void writePropertyDefinition(PropertyDefinition definition, JsonArrayBuilder viewModelFields,
			Set<String> stateMandatory) {
		String identifier = definition.getIdentifier();
		String uri = definition.getUri();
		boolean isMandatoryForState = stateMandatory.contains(identifier) || stateMandatory.contains(uri);
		boolean isMandatoryByDefinition = definition.isMandatory();
		boolean isMandatory = isMandatoryForState || isMandatoryByDefinition;
		JsonObjectBuilder field = Json.createObjectBuilder().add(PREVIEW_EMPTY_KEY, definition.isPreviewEnabled())
				.add(IDENTIFIER_KEY, identifier).add(IS_MANDATORY_KEY, isMandatory);

		addIfNotNull(field, MAX_LENGTH_KEY, definition.getMaxLength());
		addIfNotNull(field, CODELIST_KEY, definition.getCodelist());
		addIfNotNull(field, FILTERS, definition.getFilters());
		addIfNotNull(field, DISPLAY_TYPE_KEY, definition.getDisplayType().name());
		addIfNotNull(field, DATA_TYPE_KEY, definition.getDataType().getName());
		addIfNotNull(field, LABEL_KEY, definition.getLabel());
		addIfNotNull(field, MULTIVALUE, definition.isMultiValued());
		addIfNotNull(field, TOOLTIP, definition.getTooltip());
		addIfNotNull(field, URI, PropertyDefinition.resolveUri().apply(definition));
		addIfNotNull(field, IS_DATA_PROPERTY, PropertyDefinition.isObjectProperty().negate().test(definition));

		addValidators(definition, field, isMandatoryForState, isMandatoryByDefinition);
		writeControlDefinition(definition, field, stateMandatory);
		viewModelFields.add(field.build());
	}

	private void addValidators(PropertyDefinition definition, JsonObjectBuilder field, boolean isMandatoryForState,
			boolean isMandatoryByDefinition) {
		JsonArrayBuilder validators = Json.createArrayBuilder();
		addValidatorForMandatory(definition, validators, isMandatoryForState, isMandatoryByDefinition);
		addRegExValidator(definition, validators);
		addRelatedFieldValidator(definition, validators);
		addConditionsValidator(definition, validators);
		addCalculationValidator(definition, validators);
		addUniqueValidator(definition, validators);
		field.add(VALIDATORS_KEY, validators.build());
	}

	private static void addCalculationValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		ControlDefinition control = definition.getControlDefinition();
		if (control == null) {
			return;
		}

		if (control.paramsStream().filter(param -> DEFAULT_VALUE_PATTERN.equalsIgnoreCase(param.getType()))
				.count() == 0) {
			return;
		}

		JsonArrayBuilder bindingsValue = Json.createArrayBuilder();
		control.paramsStream().filter(param -> PROPERY_BINDING_KEY.equals(param.getIdentifier()))
				.forEach(param -> bindingsValue.add(param.getName()));

		JsonArrayBuilder functionsValue = Json.createArrayBuilder();
		control.paramsStream().filter(param -> PROPERY_FUNCTION_KEY.equals(param.getIdentifier()))
				.forEach(param -> functionsValue.add(param.getName()));

		JsonObjectBuilder context = Json.createObjectBuilder().add(DEFINITION_ID_KEY, definition.getParentPath())
				.add(BINDINGS_KEY, bindingsValue).add(FUNCTIONS_KEY, functionsValue);

		JsonObjectBuilder rule = Json.createObjectBuilder().add(ID_KEY, CALCULATION_RULE_ID).add(CONTEXT_KEY, context);

		JsonArrayBuilder conditionsJson = Json.createArrayBuilder();
		conditionsJson.add(rule);
		JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, CALCULATION_VALIDATOR_ID_VALUE);
		validator.add(CONDITION_RULES, conditionsJson);
		validators.add(validator);
	}

	private static void addConditionsValidator(Conditional definition, JsonArrayBuilder validators) {
		if (CollectionUtils.isEmpty(definition.getConditions())) {
			return;
		}
		List<Condition> conditions = definition.getConditions();
		JsonArrayBuilder conditionsJson = Json.createArrayBuilder();
		boolean hasRules = false;
		for (Condition condition : conditions) {
			if (isOptionalOrMandatory(condition)) {
				continue;
			}
			JsonObjectBuilder rule = Json.createObjectBuilder().add(ID_KEY, condition.getIdentifier())
					.add(CONDITION_RENDER_AS, condition.getRenderAs())
					.add(CONDITION_EXPRESSION, condition.getExpression());
			conditionsJson.add(rule);
			hasRules = true;
		}
		if (hasRules) {
			JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, CONDITION).add(CONDITION_RULES,
					conditionsJson);
			validators.add(validator);
		}
	}

	private void addValidatorForMandatory(PropertyDefinition definition, JsonArrayBuilder validators,
			boolean isMandatoryForState, boolean isMandatoryByDefinition) {
		boolean isMandatory = isMandatoryForState || isMandatoryByDefinition;
		List<Condition> conditions = definition.getConditions();
		Condition mandatoryCondition = null;
		for (Condition condition : conditions) {
			if (isOptionalOrMandatory(condition)) {
				mandatoryCondition = condition;
				break;
			}
		}

		String mandatoryMessage = labelProvider.getValue(FIELD_VALIDATION_MANDATORY_LBL);

		if (mandatoryCondition != null) {
			JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, MANDATORY_VALIDATOR_ID_VALUE)
					.add(LEVEL_KEY, ERROR_VALUE).add(MESSAGE_KEY, mandatoryMessage);
			JsonArrayBuilder conditionsJson = Json.createArrayBuilder();
			JsonObjectBuilder rule = Json.createObjectBuilder().add(ID_KEY, mandatoryCondition.getIdentifier())
					.add(CONDITION_RENDER_AS, mandatoryCondition.getRenderAs())
					.add(CONDITION_EXPRESSION, mandatoryCondition.getExpression());
			conditionsJson.add(rule);
			validator.add(CONDITION_RULES, conditionsJson);
			validators.add(validator);
		} else if (isMandatory) {
			JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, MANDATORY_VALIDATOR_ID_VALUE)
					.add(LEVEL_KEY, ERROR_VALUE);
			addIfNotNull(validator, MESSAGE_KEY, mandatoryMessage);
			if (isMandatoryByDefinition) {
				validator.add("isMandatoryByDefinition", isMandatoryByDefinition);
			}
			if (isMandatoryForState) {
				validator.add("isMandatoryForState", isMandatoryForState);
			}
			validators.add(validator.build());
		}
	}

	private static boolean isOptionalOrMandatory(Condition condition) {
		return "MANDATORY".equalsIgnoreCase(condition.getRenderAs())
				|| "OPTIONAL".equalsIgnoreCase(condition.getRenderAs());
	}

	private void addUniqueValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		if (PropertyDefinition.isUniqueProperty().test(definition)) {
			JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, UNIQUE_KEY).add(LEVEL_KEY,
					ERROR_VALUE);
			addIfNotNull(validator, MESSAGE_KEY, labelProvider.getValue(FIELD_VALIDATION_UNIQUE));
			validators.add(validator.build());
		}
	}

	private void addRegExValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		String type = definition.getType();
		Integer codelist = definition.getCodelist();
		if (codelist != null || type == null || NO_REGEX_DATA_TYPES.contains(type)) {
			return;
		}

		String fieldRncPattern = definition.getRnc();
		RegExGenerator regExGenerator = new RegExGenerator(labelProvider::getValue);
		Pair<String, String> pattern = regExGenerator.getPattern(type, fieldRncPattern);
		writeValidationObject(validators, REGEX_KEY, ERROR_VALUE, pattern.getSecond(), pattern.getFirst());
	}

	private static void addRelatedFieldValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		ControlDefinition control = definition.getControlDefinition();
		if (control == null || !RELATED_FIELDS.equals(control.getIdentifier())) {
			return;
		}

		Set<String> controlParamTypes = new HashSet<>();
		control.paramsStream().forEach(element -> {
			if (element.getType() == null) {
				element.setType(RELATED_FIELDS);
			}
			controlParamTypes.add(element.getType());
		});

		controlParamTypes.forEach(type -> {
			JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, RELATED_FIELDS_VALIDATOR_ID_VALUE)
					.add(LEVEL_KEY, ERROR_VALUE);
			writeParams(control, validator, type);
			validators.add(validator.build());
		});

	}

	private static void writeValidationObject(JsonArrayBuilder validators, String id, String level, String message,
			String pattern) {
		JsonObjectBuilder validator = Json.createObjectBuilder().add(ID_KEY, id).add(LEVEL_KEY, level);
		addIfNotNull(validator, MESSAGE_KEY, message);
		if (pattern != null) {
			validator.add(CONTEXT_KEY, Json.createObjectBuilder().add(PATTERN_KEY, pattern).build());
		}
		validators.add(validator.build());
	}

	private void writeControlDefinition(PropertyDefinition definition, JsonObjectBuilder field,
			Set<String> stateMandatory) {
		ControlDefinition control = definition.getControlDefinition();
		if (control == null) {
			return;
		}

		field.add(CONTROL_ID, control.getIdentifier());
		JsonArrayBuilder controlModelArray = Json.createArrayBuilder();

		if (control.getControlParams().isEmpty()) {
			createControlModel(control.getIdentifier(), control, stateMandatory, controlModelArray);
		} else {
			// Make set with all control types without duplication
			Set<String> controlParamTypes = new HashSet<>();
			control.paramsStream().forEach(element -> {
				if (element.getType() == null) {
					element.setType(control.getIdentifier());
				}
				controlParamTypes.add(element.getType());
			});

			controlParamTypes.forEach(type -> createControlModel(type, control, stateMandatory, controlModelArray));
		}

		field.add(CONTROL_KEY, controlModelArray.build());
	}

	private void createControlModel(String type, ControlDefinition control, Set<String> stateMandatory,
			JsonArrayBuilder controlModelArray) {
		if (StringUtils.isBlank(type)) {
			throw new EmfRuntimeException(control.toString());
		}
		JsonObjectBuilder controlModel = Json.createObjectBuilder().add(IDENTIFIER_KEY, type);
		JsonArrayBuilder controlFields = Json.createArrayBuilder();
		control.forEach(element -> writePropertyDefinition(element, controlFields, stateMandatory));
		controlModel.add(CONTROL_FIELDS_KEY, controlFields.build());
		JsonObjectBuilder params = Json.createObjectBuilder();
		writeParams(control, params, type);
		controlModel.add(CONTROL_PARAMS_KEY, params.build());
		controlModelArray.add(controlModel);
	}

	private static void writeParams(ControlDefinition control, JsonObjectBuilder builder, String type) {
		control.paramsStream().filter(element -> type.equals(element.getType()))
				.forEach(param -> builder.add(param.getName().toLowerCase(), param.getValue()));
	}

	private void writeAdditionalData(Instance instance, DefinitionModel model, JsonGenerator generator) {
		generator.write(DEFINITION_ID_KEY, model.getIdentifier());
		addIfNotNull(generator, DEFINITION_LABEL_KEY, definitionHelper.getDefinitionLabel(model));
		if (instance != null) {
			addIfNotNull(generator, INSTANCE_TYPE_KEY, instance.type().getCategory());
		}
	}

}