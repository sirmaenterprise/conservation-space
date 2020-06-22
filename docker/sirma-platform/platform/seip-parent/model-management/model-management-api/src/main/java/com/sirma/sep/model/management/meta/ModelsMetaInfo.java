package com.sirma.sep.model.management.meta;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holder for the supported types of models meta information.
 *
 * @author Mihail Radkov
 */
public class ModelsMetaInfo implements Sealable {

	@JsonIgnore
	private boolean isSealed = false;

	private Map<String, ModelMetaInfo> semantics;

	private Map<String, ModelMetaInfo> definitions;

	private Map<String, ModelMetaInfo> properties;

	private Map<String, ModelMetaInfo> fields;

	private Map<String, ModelMetaInfo> regions;

	private Map<String, ModelMetaInfo> headers;

	private Map<String, ModelMetaInfo> actions;

	private Map<String, ModelMetaInfo> actionGroups;

	private Map<String, ModelMetaInfo> controls;

	private Map<String, ModelMetaInfo> controlParams;

	private Map<String, ModelMetaInfo> actionExecutions;

	public Collection<ModelMetaInfo> getSemantics() {
		return semantics.values();
	}

	public void setSemantics(List<ModelMetaInfo> semantics) {
		checkIfSealed();
		this.semantics = toMap(semantics, ModelMetaInfo::getUri);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getSemanticsMapping() {
		return semantics;
	}

	public Collection<ModelMetaInfo> getDefinitions() {
		return definitions.values();
	}

	public void setDefinitions(List<ModelMetaInfo> definitions) {
		checkIfSealed();
		this.definitions = toMap(definitions, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getDefinitionsMapping() {
		return definitions;
	}

	public Collection<ModelMetaInfo> getProperties() {
		return properties.values();
	}

	public void setProperties(List<ModelMetaInfo> properties) {
		checkIfSealed();
		this.properties = toMap(properties, ModelMetaInfo::getUri);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getPropertiesMapping() {
		return properties;
	}

	public Collection<ModelMetaInfo> getFields() {
		return fields.values();
	}

	public void setFields(List<ModelMetaInfo> fields) {
		checkIfSealed();
		this.fields = toMap(fields, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getFieldsMapping() {
		return fields;
	}

	public Collection<ModelMetaInfo> getRegions() {
		return regions.values();
	}

	public void setRegions(List<ModelMetaInfo> regions) {
		checkIfSealed();
		this.regions = toMap(regions, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getRegionsMapping() {
		return regions;
	}

	public Collection<ModelMetaInfo> getHeaders() { return headers.values(); }

	public void setHeaders(List<ModelMetaInfo> headers) {
		checkIfSealed();
		this.headers = toMap(headers, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getHeadersMapping() { return headers; }

	public Collection<ModelMetaInfo> getActions() {
		return actions.values();
	}

	public void setActions(List<ModelMetaInfo> actions) {
		checkIfSealed();
		this.actions = toMap(actions, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getActionsMapping() {
		return actions;
	}

	public Collection<ModelMetaInfo> getActionGroups() {
		return actionGroups.values();
	}

	public void setActionGroups(List<ModelMetaInfo> actionGroups) {
		checkIfSealed();
		this.actionGroups = toMap(actionGroups, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getActionGroupsMapping() {
		return actionGroups;
	}

	public Collection<ModelMetaInfo> getControls() {
		return controls.values();
	}

	public void setControls(List<ModelControlMetaInfo> controls) {
		checkIfSealed();
		this.controls = toMap(controls, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getControlsMapping() {
		return controls;
	}

	public Collection<ModelMetaInfo> getControlParams() {
		return controlParams.values();
	}

	public void setControlParams(List<ModelMetaInfo> controlParams) {
		checkIfSealed();
		this.controlParams = toMap(controlParams, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getControlParamsMapping() {
		return controlParams;
	}

	public Collection<ModelMetaInfo> getActionExecutions() {
		return actionExecutions.values();
	}

	public void setActionExecutions(List<ModelMetaInfo> actionExecutions) {
		checkIfSealed();
		this.actionExecutions = toMap(actionExecutions, ModelMetaInfo::getId);
	}

	@JsonIgnore
	public Map<String, ModelMetaInfo> getActionExecutionsMapping() {
		return actionExecutions;
	}

	@Override
	public void seal() {
		isSealed = true;
	}

	@Override
	public boolean isSealed() {
		return isSealed;
	}

	private void checkIfSealed() {
		if (isSealed()) {
			throw new IllegalStateException("Meta information is sealed and cannot be modified");
		}
	}

	private static Map<String, ModelMetaInfo> toMap(List<? extends ModelMetaInfo> metaInfoList,
			Function<ModelMetaInfo, String> idResolver) {
		assignOrdering(metaInfoList);
		return Collections.unmodifiableMap(metaInfoList.stream().collect(CollectionUtils.toIdentityMap(idResolver, LinkedHashMap::new)));
	}

	private static void assignOrdering(List<? extends ModelMetaInfo> metaInfoList) {
		for (int index = 0; index < metaInfoList.size(); index++) {
			metaInfoList.get(index).setOrder(index);
		}
	}
}
