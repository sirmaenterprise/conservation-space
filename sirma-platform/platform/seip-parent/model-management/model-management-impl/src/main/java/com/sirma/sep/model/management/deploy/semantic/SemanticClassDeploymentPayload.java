package com.sirma.sep.model.management.deploy.semantic;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * DTO for transferring the deployment payload of single {@link com.sirma.sep.model.management.ModelClass} as maps of added and
 * removed properties.
 *
 * @author Mihail Radkov
 */
public class SemanticClassDeploymentPayload {

	private final String classId;
	private final Map<String, ModelMetaInfo> classMetaInfo;
	private final Map<String, List<Serializable>> toRemove = new LinkedHashMap<>();
	private final Map<String, List<Serializable>> toAdd = new LinkedHashMap<>();

	/**
	 * Constructs the payload with the provided class identifier and its related meta information.
	 *
	 * @param classId - identifier of the class for which the payload relates to
	 * @param classMetaInfo meta information about the class properties
	 */
	public SemanticClassDeploymentPayload(String classId,
			Map<String, ModelMetaInfo> classMetaInfo) {
		this.classId = classId;
		this.classMetaInfo = classMetaInfo;
	}

	public String getClassId() {
		return classId;
	}

	public Map<String, ModelMetaInfo> getClassMetaInfo() {
		return classMetaInfo;
	}

	public Map<String, List<Serializable>> getToRemove() {
		return toRemove;
	}

	public SemanticClassDeploymentPayload toRemove(String key, Serializable value) {
		if (value != null) {
			toRemove.computeIfAbsent(key, r -> new LinkedList<>()).add(value);
		}
		return this;
	}

	public Map<String, List<Serializable>> getToAdd() {
		return toAdd;
	}

	public SemanticClassDeploymentPayload toAdd(String key, Serializable value) {
		if (value != null) {
			toAdd.computeIfAbsent(key, r -> new LinkedList<>()).add(value);
		}
		return this;
	}

	public boolean isEmpty() {
		return toRemove.isEmpty() && toAdd.isEmpty();
	}
}
