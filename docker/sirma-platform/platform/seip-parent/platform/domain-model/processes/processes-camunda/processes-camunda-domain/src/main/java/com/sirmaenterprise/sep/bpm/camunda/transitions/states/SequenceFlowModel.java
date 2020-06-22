package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getCamundaProperties;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel.SequenceFlowModelProperties.KEY_LEAVING_OBJECT_STATE;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.extractBusinessId;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.isSkipped;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The {@link SequenceFlowModel} is wrapper for the sequence outgoing model of single activity.<br>
 *
 * @author bbanchev
 */
public class SequenceFlowModel implements Serializable {

	/**
	 * Supported keys for {@link SequenceFlowModel} properties
	 * 
	 * @author bbanchev
	 */
	public static class SequenceFlowModelProperties {
		/** Describes a key for status of the object that has this transition as outgoing. */
		public static final String KEY_LEAVING_OBJECT_STATE = "leavingObjectState";

		private SequenceFlowModelProperties() {
			// constants class without public constructor
		}
	}

	private static final long serialVersionUID = -7432234431011098011L;

	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_TRANSITION_ID = "transitionId";
	private static final String JSON_KEY_IDENTIFIER = "identifier";
	private static final String JSON_KEY_PROPERTIES = "properties";
	private static final String KEY_MANDATORY_TYPES = "mandatoryTypes";

	// condition expressions constants
	private static final String JSON_KEY_CONDITION = "condition";
	private static final String JSON_KEY_SCOPE_ID = "scopeId";
	private static final String JSON_KEY_VALUE = "value";
	private static final String JSON_KEY_LANGUAGE = "language";

	private static final List<Condition> NO_CONDITIONS = Collections.emptyList();

	boolean loopCharacteristics = false;

	private Map<String, SequenceFlowEntry> entries = new LinkedHashMap<>(2, 2);
	private Map<String, SequenceFlowEntry> skipped = new LinkedHashMap<>(1, 2);

	/**
	 * Builds a manual flow based on operation and desired status at the end of the operation.
	 *
	 * @param action
	 *            the operation that could be executed on the instance.
	 * @param properties
	 *            the custom properties to set for transition
	 */
	public SequenceFlowModel(String action, Map<String, Serializable> properties) {
		SequenceFlowEntry entry = new SequenceFlowEntry(action, action, null, properties);
		appendSequenceFlowToModel(this, () -> entry, entry.getId(), null);
	}

	SequenceFlowModel() {
		// used by the parser
	}

	/**
	 * Adds a new activity to a single sequence flow in the bpm.
	 *
	 * @param sequenceFlow
	 *            the sequence flow that activity belongs to
	 * @param activity
	 *            the activity id to add. Might be null which means the transition is added empty
	 */
	void add(SequenceFlow sequenceFlow, String activity) {
		appendSequenceFlowToModel(this, () -> convertSequenceFlowToEntry(sequenceFlow), sequenceFlow.getId(), activity);
	}

	private static SequenceFlowEntry convertSequenceFlowToEntry(SequenceFlow sequenceFlow) {
		CamundaProperties camundaProperties = getCamundaProperties(sequenceFlow.getExtensionElements());
		ConditionExpression expression = extractCondition(sequenceFlow);
		if (camundaProperties == null) {
			return new SequenceFlowEntry(sequenceFlow.getId(), sequenceFlow.getName(), expression,
					Collections.emptyMap());
		}
		Map<String, Serializable> properties = new LinkedHashMap<>(camundaProperties.getCamundaProperties().size());
		for (CamundaProperty camundaProperty : camundaProperties.getCamundaProperties()) {
			CollectionUtils.addNonNullValue(properties, camundaProperty.getCamundaName(),
					camundaProperty.getCamundaValue());
		}
		return new SequenceFlowEntry(sequenceFlow.getId(), sequenceFlow.getName(), expression, properties);
	}

	/**
	 * Gets the model as map of sequence to list of activities that are reached.
	 *
	 * @return the model
	 */
	Set<SequenceFlowEntry> getModel() {
		return new LinkedHashSet<>(entries.values());
	}

	/**
	 * Check if current model contains transition by its id (binded to the same value as an operation id).
	 *
	 * @param operationId
	 *            the operation id
	 * @return true, if transition is contained in the model
	 */
	public boolean containsTransition(String operationId) {
		// what if there are two transitions for operation?
		return getTransition(operationId) != null;
	}

	/**
	 * Gets a transition by its id (binded to the same value as an operation id).
	 *
	 * @param operationId
	 *            the operation id
	 * @return the transition or null if transition is not available
	 */
	public SequenceFlowEntry getTransition(String operationId) {
		Optional<Entry<String, SequenceFlowEntry>> foundTransition = entries
				.entrySet()
					.stream()
					.filter(entry -> EqualsHelper.nullSafeEquals(extractBusinessId(entry.getKey()), operationId, true))
					.findFirst();
		if (foundTransition.isPresent()) {
			return foundTransition.get().getValue();
		}
		return null;
	}

	/**
	 * Return the list of transitions as {@link Stream}
	 * 
	 * @return the {@link SequenceFlowEntry} stream part the model
	 */
	public Stream<SequenceFlowEntry> getTransitions() {
		return entries.values().stream();
	}

	/**
	 * Converts current model to {@link StateTransition} model.
	 *
	 * @param currentState
	 *            the current state to build transition as {@link StateTransition#getFromState()} value
	 * @param filterByTransition
	 *            predicate used for filtering the transitions {@link com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService#createTransitionConditionFilter(Instance)}
	 * @return the list if transitions
	 */
	List<StateTransition> toStateTransitionModel(String currentState, Predicate<SequenceFlowEntry> filterByTransition) {
		return entries
				.values()
					.stream()
					.filter(flow -> filterByTransition == null ? true : filterByTransition.test(flow))
					.map(flow -> convertFlowEntryToStateTransition(flow, currentState))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
	}

	private static StateTransition convertFlowEntryToStateTransition(SequenceFlowEntry entry, String currentState) {
		// skip non interesting transitions
		if (entry.getName() == null) {
			return null;
		}
		BPMStateTransition transition = new BPMStateTransition();
		transition.setIdentifier(entry.getId().trim());
		// completed state by specification
		transition.setToState(Objects.toString(entry.getProperty(KEY_LEAVING_OBJECT_STATE), "COMPLETED"));
		transition.setFromState(currentState);
		transition.setTransitionId(extractBusinessId(entry.getId()));
		transition.setConditions(NO_CONDITIONS);
		return transition;
	}

	/**
	 * Serialize the model as compatible json object that could be transformed again to {@link SequenceFlowModel} by the
	 * method {@link #deserialize(Serializable)}.
	 *
	 * @return the string
	 */
	public String serialize() {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		builder.add(loopCharacteristics);
		entries.entrySet().stream().forEach(entry -> {
			JsonObjectBuilder entryJson = convertSequenceFlowEntryToJSON(entry);
			if (entryJson != null) {
				builder.add(entryJson);
			}
		});
		return builder.build().toString();
	}

	/**
	 * Deserialize a {@link SequenceFlowModel} serialized as json string.
	 *
	 * @param value
	 *            the json string value
	 * @return the sequence flow model or null if the provided value does not contain model
	 */
	public static SequenceFlowModel deserialize(Serializable value) {
		if (!(value instanceof String) || StringUtils.isBlank(Objects.toString(value, null))) {
			return null;
		}
		SequenceFlowModel model = new SequenceFlowModel();
		try (StringReader reader = new StringReader(value.toString());
				JsonReader jsonReader = Json.createReader(reader)) {
			JsonArray transitions = jsonReader.readArray();
			for (JsonValue jsonValue : transitions) {
				if (jsonValue instanceof JsonObject) {
					SequenceFlowEntry entry = convertJSONToSequenceFlowEntry((JsonObject) jsonValue);
					appendSequenceFlowToModel(model, () -> entry, entry.getId(), null);
				} else if (jsonValue.getValueType() == ValueType.TRUE || jsonValue.getValueType() == ValueType.FALSE) {
					model.loopCharacteristics = Boolean.parseBoolean(jsonValue.toString());
				}

			}
		}
		return model;
	}

	private static void appendSequenceFlowToModel(SequenceFlowModel model,
			Supplier<SequenceFlowEntry> sequenceFlowEntrySupplier, String flowId, String activityId) {
		if (flowId.isEmpty()) {
			return;
		}
		if (isSkipped(flowId)) {
			model.skipped.put(flowId, sequenceFlowEntrySupplier.get());
			return;
		}
		SequenceFlowEntry sequenceFlowEntry = sequenceFlowEntrySupplier.get();
		model.entries.putIfAbsent(flowId, sequenceFlowEntry);
		if (StringUtils.isNotBlank(activityId) && !model.loopCharacteristics) {
			model.entries.get(flowId).add(activityId, extractBusinessId(activityId));
		}
	}

	private static JsonObjectBuilder convertSequenceFlowEntryToJSON(Entry<String, SequenceFlowEntry> entry) {
		JsonObjectBuilder transition = Json.createObjectBuilder();
		SequenceFlowEntry flowEntry = entry.getValue();
		if (flowEntry.getName() == null) {
			return null;
		}
		transition.add(JSON_KEY_IDENTIFIER, flowEntry.getId().trim());
		transition.add(JSON_KEY_TRANSITION_ID, extractBusinessId(flowEntry.getId().trim()));
		transition.add(JSON_KEY_NAME, flowEntry.getName().trim());
		ConditionExpression condition = flowEntry.getCondition();
		if (condition != null) {
			JsonObjectBuilder conditionJson = Json.createObjectBuilder();
			if (condition instanceof ConditionScript) {
				String language = ((ConditionScript) condition).getLanguage();
				conditionJson.add(JSON_KEY_LANGUAGE, StringUtils.isNotBlank(language) ? language : "");
			}
			String scopeId = condition.getScopeId();
			conditionJson.add(JSON_KEY_SCOPE_ID, StringUtils.isNotBlank(scopeId) ? scopeId : "");
			conditionJson.add(JSON_KEY_VALUE, condition.getValue());
			transition.add(JSON_KEY_CONDITION, conditionJson);
		}
		transition.add(JSON_KEY_PROPERTIES, JSON.convertToJsonObject(flowEntry.getProperties()));
		JsonArrayBuilder builder = Json.createArrayBuilder();
		flowEntry.getCheckpoints().keySet().stream().forEach(builder::add);
		transition.add(KEY_MANDATORY_TYPES, builder);

		return transition;
	}

	private static SequenceFlowEntry convertJSONToSequenceFlowEntry(JsonObject transition) {
		SequenceFlowEntry flowEntry = new SequenceFlowEntry(transition.getString(JSON_KEY_IDENTIFIER),
				transition.getString(JSON_KEY_NAME, transition.getString(JSON_KEY_IDENTIFIER)),
				extractCondition(transition), JSON.jsonToMap(transition.getJsonObject(JSON_KEY_PROPERTIES)));
		JsonArray types = transition.getJsonArray(KEY_MANDATORY_TYPES);
		if (types != null) {
			for (JsonValue type : types) {
				String activityId = ((JsonString) type).getString();
				flowEntry.add(activityId, extractBusinessId(activityId));
			}
		}
		return flowEntry;
	}

	private static ConditionExpression extractCondition(SequenceFlow sequenceFlow) {
		org.camunda.bpm.model.bpmn.instance.ConditionExpression conditionExpression = sequenceFlow
				.getConditionExpression();
		if (conditionExpression == null) {
			return null;
		}
		String language = conditionExpression.getLanguage();
		String scopeId = conditionExpression.getScope() != null ? conditionExpression.getId() : "";
		String value = conditionExpression.getRawTextContent();
		if (language != null) {
			String camundaResourceValue = conditionExpression.getCamundaResource();
			if (StringUtils.isNotBlank(camundaResourceValue)) {
				value = camundaResourceValue;
			}
			return new ConditionScript(scopeId, value, language);
		}
		return new ConditionExpression(scopeId, value);
	}

	private static ConditionExpression extractCondition(JsonObject transition) {
		if (transition.containsKey(JSON_KEY_CONDITION)) {
			JsonValue condition = transition.get(JSON_KEY_CONDITION);
			if (condition instanceof JsonObject) {
				String scopeId = ((JsonObject) condition).getString(JSON_KEY_SCOPE_ID, "");
				String value = ((JsonObject) condition).getString(JSON_KEY_VALUE, "");
				String language = ((JsonObject) condition).getString(JSON_KEY_LANGUAGE, "");
				if (StringUtils.isNotBlank(language)) {
					return new ConditionScript(scopeId, value, language);
				}
				return new ConditionExpression(scopeId, value);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return entries.toString();
	}

}