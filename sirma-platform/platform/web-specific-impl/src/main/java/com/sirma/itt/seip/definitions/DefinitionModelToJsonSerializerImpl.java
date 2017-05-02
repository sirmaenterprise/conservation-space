package com.sirma.itt.seip.definitions;

import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CODELIST_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONDITION;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONDITION_EXPRESSION;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONDITION_RENDER_AS;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONDITION_RULES;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONTEXT_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONTROL_FIELDS_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONTROL_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.CONTROL_PARAMS_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.DATA_TYPE_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.DEFINITION_ID_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.DEFINITION_LABEL_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.DISPLAY_TYPE_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.ERROR_VALUE;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.FIELDS_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.FIELD_VALIDATION_MANDATORY_LBL;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.FILTERS;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.IDENTIFIER_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.ID_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.INSTANCE_TYPE_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.IS_DATA_PROPERTY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.IS_MANDATORY_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.LABEL_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.LEVEL_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.MANDATORY_VALIDATOR_ID_VALUE;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.MAX_LENGTH_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.MESSAGES_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.MESSAGE_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.MULTIVALUE;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.PATH_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.PATTERN_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.PREVIEW_EMPTY_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.REGEX_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.RELATED_FIELDS_VALIDATOR_ID_VALUE;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.TOOLTIP;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.URI;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.VALIDATION_MODEL_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.VALIDATORS_KEY;
import static com.sirma.itt.seip.definitions.DefinitionModelSerializationConstants.VIEW_MODEL_KEY;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.ANY;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.DATE;
import static com.sirma.itt.seip.domain.definition.DataTypeDefinition.DATETIME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.DEFAULT_HEADERS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_BREADCRUMB;
import static com.sirma.itt.seip.rest.utils.JSON.addIfNotNull;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionHelper;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.util.RegExGenerator;

/**
 * Contains logic that converts {@link DefinitionModel} to JSON. Primary used in definition model writers. Example of
 * generated JSON can be found in the test resources directory in definition-model-test.json file.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class DefinitionModelToJsonSerializerImpl implements DefinitionModelToJsonSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	private String mandatoryMessage;

	private RegExGenerator regExGenerator;

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
		mandatoryMessage = labelProvider.getValue(FIELD_VALIDATION_MANDATORY_LBL);
		regExGenerator = new RegExGenerator(labelProvider::getValue);

		Set<String> mandatoryForState = getMandatoryFieldsForState(instance, operation);
		Collection<Ordinal> fields = definitionHelper.collectAllFields(model);

		Predicate<Identity> selectedFields = EXCLUDE_HEADERS;
		if (fieldsToSerialize != null) {
			selectedFields = EXCLUDE_HEADERS.and(identity -> fieldsToSerialize.contains(identity.getIdentifier()));
		}

		serializeInternal(fields, mandatoryForState, viewModelFields, validationModel, selectedFields);

		generator.write(VALIDATION_MODEL_KEY, validationModel.build());
		generator.writeStartObject(VIEW_MODEL_KEY).write(FIELDS_KEY, viewModelFields.build()).writeEnd();

		writePath(instance, generator);
		writeAdditionalData(instance, model, generator);
	}

	private Set<String> getMandatoryFieldsForState(Instance instance, String operation) {
		if (instance == null || StringUtils.isNullOrEmpty(operation)) {
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

		JsonObjectBuilder builder = Json
				.createObjectBuilder()
					// implement when there is a server side validations
					.add(MESSAGES_KEY, Json.createArrayBuilder().build());

		validationModel.add(property.getIdentifier(), builder.build());
	}

	private void writePropertyDefinition(PropertyDefinition definition, JsonArrayBuilder viewModelFields,
			Set<String> stateMandatory) {
		String identifier = definition.getIdentifier();
		boolean isMandatory = stateMandatory.contains(identifier) || definition.isMandatory();
		JsonObjectBuilder field = Json
				.createObjectBuilder()
					.add(PREVIEW_EMPTY_KEY, definition.isPreviewEnabled())
					.add(IDENTIFIER_KEY, identifier)
					.add(IS_MANDATORY_KEY, isMandatory);

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

		addValidators(definition, field, isMandatory);
		writeControlDefinition(definition, field, stateMandatory);
		viewModelFields.add(field.build());
	}

	private void addValidators(PropertyDefinition definition, JsonObjectBuilder field, boolean isMandatory) {
		JsonArrayBuilder validators = Json.createArrayBuilder();
		addValidatorForMandatory(definition, validators, isMandatory);
		addRegExValidator(definition, validators);
		addRelatedFieldValidator(definition, validators);
		addConditionsValidator(definition, validators);
		field.add(VALIDATORS_KEY, validators.build());
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
			JsonObjectBuilder rule = Json
					.createObjectBuilder()
						.add(ID_KEY, condition.getIdentifier())
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
			boolean isMandatory) {
		List<Condition> conditions = definition.getConditions();
		Condition mandatoryCondition = null;
		for (Condition condition : conditions) {
			if (isOptionalOrMandatory(condition)) {
				mandatoryCondition = condition;
				break;
			}
		}

		if (mandatoryCondition != null) {
			JsonObjectBuilder validator = Json
					.createObjectBuilder()
						.add(ID_KEY, MANDATORY_VALIDATOR_ID_VALUE)
						.add(LEVEL_KEY, ERROR_VALUE)
						.add(MESSAGE_KEY, mandatoryMessage);
			JsonArrayBuilder conditionsJson = Json.createArrayBuilder();
			JsonObjectBuilder rule = Json
					.createObjectBuilder()
						.add(ID_KEY, mandatoryCondition.getIdentifier())
						.add(CONDITION_RENDER_AS, mandatoryCondition.getRenderAs())
						.add(CONDITION_EXPRESSION, mandatoryCondition.getExpression());
			conditionsJson.add(rule);
			validator.add(CONDITION_RULES, conditionsJson);
			validators.add(validator);
		} else if (isMandatory) {
			writeValidationObject(validators, MANDATORY_VALIDATOR_ID_VALUE, ERROR_VALUE, mandatoryMessage, null);
		}
	}

	private static boolean isOptionalOrMandatory(Condition condition) {
		return "MANDATORY".equalsIgnoreCase(condition.getRenderAs())
				|| "OPTIONAL".equalsIgnoreCase(condition.getRenderAs());
	}

	private void addRegExValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		String type = definition.getType();
		Integer codelist = definition.getCodelist();
		if (codelist != null || type == null || NO_REGEX_DATA_TYPES.contains(type)) {
			return;
		}

		String fieldRncPattern = definition.getRnc();
		Pair<String, String> pattern = regExGenerator.getPattern(type, fieldRncPattern);
		writeValidationObject(validators, REGEX_KEY, ERROR_VALUE, pattern.getSecond(), pattern.getFirst());
	}

	private static void addRelatedFieldValidator(PropertyDefinition definition, JsonArrayBuilder validators) {
		ControlDefinition control = definition.getControlDefinition();
		if (control == null || !"RELATED_FIELDS".equals(control.getIdentifier())) {
			return;
		}

		JsonObjectBuilder validator = Json
				.createObjectBuilder()
					.add(ID_KEY, RELATED_FIELDS_VALIDATOR_ID_VALUE)
					.add(LEVEL_KEY, ERROR_VALUE);
		writeParams(control, validator);
		validators.add(validator.build());
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

		JsonObjectBuilder controlModel = Json.createObjectBuilder().add(IDENTIFIER_KEY, control.getIdentifier());

		JsonArrayBuilder controlFields = Json.createArrayBuilder();
		control.forEach(element -> writePropertyDefinition(element, controlFields, stateMandatory));
		controlModel.add(CONTROL_FIELDS_KEY, controlFields.build());

		JsonObjectBuilder params = Json.createObjectBuilder();
		writeParams(control, params);
		controlModel.add(CONTROL_PARAMS_KEY, params.build());

		field.add(CONTROL_KEY, controlModel.build());
	}

	private static void writeParams(ControlDefinition control, JsonObjectBuilder builder) {
		control.paramsStream().forEach(param -> builder.add(param.getName().toLowerCase(), param.getValue()));
	}

	// THIS IS NOT FOR HERE AND SHOULD BE MOVED OR RETRIEVED WITH ANOTHER CALL
	private void writePath(Instance instance, JsonGenerator generator) {
		generator.writeStartArray(PATH_KEY);
		List<Instance> pathInstances = InstanceUtil.getParentPath(instance, true);
		instanceLoadDecorator.decorateResult(pathInstances);
		for (Instance pathInstance : pathInstances) {
			generator.writeStartObject();
			generator.write(ID_KEY, (String) pathInstance.getId());
			if (!JSON.addIfNotNull(generator, "type", pathInstance.type().getCategory())) {
				generator.write("type", "objectinstance");
				LOGGER.warn("Found type {} without category for instance {}", pathInstance.type().getId(),
						pathInstance.getId());
			}
			addIfNotNull(generator, "compactHeader", pathInstance.getString(HEADER_BREADCRUMB));
			generator.writeEnd();
		}
		generator.writeEnd();
	}

	private void writeAdditionalData(Instance instance, DefinitionModel model, JsonGenerator generator) {
		generator.write(DEFINITION_ID_KEY, model.getIdentifier());
		addIfNotNull(generator, DEFINITION_LABEL_KEY, definitionHelper.getDefinitionLabel(model));
		if (instance != null) {
			addIfNotNull(generator, INSTANCE_TYPE_KEY, instance.type().getCategory());
		}
	}

}