package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Internal model representing the current state transition with its name/id, the list of ordered endpoints and the
 * associated properties for the transition (properties are automatically extracted from the BPMN activity Camunda
 * properties extension.
 * 
 * @author bbanchev
 */
public class SequenceFlowEntry {

	private String id;
	private String name;
	private ConditionExpression condition;
	private Map<String, String> checkpoints = new LinkedHashMap<>(2, 2);
	private Map<String, Serializable> properties = new LinkedHashMap<>(2, 2);

	SequenceFlowEntry(String id, String name, ConditionExpression condition, Map<String, Serializable> properties) {
		this.id = id;
		this.name = name;
		this.condition = condition;
		if (properties != null) {
			this.properties = properties;
		}
	}

	void add(String activityId, String activityName) {
		String added = checkpoints.put(activityId, activityName);
		if (added != null) {
			throw new CamundaIntegrationRuntimeException(
					"Duplicate activity:" + activityId + " during flow: " + toString());
		}
	}

	/**
	 * Gets a list of activities that would be visited during this flow.
	 *
	 * @return the list of activities - might be empty, never null
	 */
	public Map<String, String> getCheckpoints() {
		return checkpoints;
	}

	/**
	 * Gets the name of flow - user friendly id.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the id of flow - uid in bpm.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets an additional setting's property.
	 *
	 * @param key
	 *            the key
	 * @return the property value
	 */
	public Serializable getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * Gets the condition expression
	 * 
	 * @see ConditionExpression
	 * @return the condition object
	 */
	public ConditionExpression getCondition() {
		return condition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SequenceFlowEntry)) {
			return false;
		}
		SequenceFlowEntry other = (SequenceFlowEntry) obj;
		return EqualsHelper.nullSafeEquals(id, other.id, true);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", checkpoints=");
		builder.append(checkpoints);
		builder.append(", extensions=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

}
