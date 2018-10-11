package com.sirma.sep.model.management.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holder for the supported types of models meta information.
 *
 * @author Mihail Radkov
 */
public class ModelsMetaInfo {

	private List<ModelMetaInfo> semantics;

	@JsonIgnore
	private Map<String, ModelMetaInfo> semanticsMapping;

	private List<ModelMetaInfo> definitions;

	@JsonIgnore
	private Map<String, ModelMetaInfo> definitionsMapping;

	private List<ModelMetaInfo> properties;

	@JsonIgnore
	private Map<String, ModelMetaInfo> propertiesMapping;

	private List<ModelMetaInfo> fields;

	@JsonIgnore
	private Map<String, ModelMetaInfo> fieldsMapping;

	private List<ModelMetaInfo> regions;

	@JsonIgnore
	private Map<String, ModelMetaInfo> regionsMapping;

	public List<ModelMetaInfo> getSemantics() {
		return semantics;
	}

	public void setSemantics(List<ModelMetaInfo> semantics) {
		this.semantics = Collections.unmodifiableList(semantics);
		this.semanticsMapping = toMap(this.semantics);
		assignOrdering(this.semantics);
	}

	public Map<String, ModelMetaInfo> getSemanticsMapping() {
		return semanticsMapping;
	}

	public List<ModelMetaInfo> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ModelMetaInfo> definitions) {
		this.definitions = Collections.unmodifiableList(definitions);
		this.definitionsMapping = toMap(this.definitions);
		assignOrdering(this.definitions);
	}

	public Map<String, ModelMetaInfo> getDefinitionsMapping() {
		return definitionsMapping;
	}

	public List<ModelMetaInfo> getProperties() {
		return properties;
	}

	public void setProperties(List<ModelMetaInfo> properties) {
		this.properties = Collections.unmodifiableList(properties);
		this.propertiesMapping = toMap(this.properties);
		assignOrdering(this.properties);
	}

	public Map<String, ModelMetaInfo> getPropertiesMapping() {
		return propertiesMapping;
	}

	public List<ModelMetaInfo> getFields() {
		return fields;
	}

	public void setFields(List<ModelMetaInfo> fields) {
		this.fields = Collections.unmodifiableList(fields);
		this.fieldsMapping = toMap(this.fields);
		assignOrdering(this.fields);
	}

	public Map<String, ModelMetaInfo> getFieldsMapping() {
		return fieldsMapping;
	}

	public List<ModelMetaInfo> getRegions() {
		return regions;
	}

	public void setRegions(List<ModelMetaInfo> regions) {
		this.regions = Collections.unmodifiableList(regions);
		this.regionsMapping = toMap(this.regions);
		assignOrdering(this.regions);
	}

	public Map<String, ModelMetaInfo> getRegionsMapping() {
		return regionsMapping;
	}

	private static Map<String, ModelMetaInfo> toMap(List<ModelMetaInfo> metaInfoList) {
		return Collections.unmodifiableMap(metaInfoList.stream().collect(Collectors.toMap(ModelMetaInfo::getId, Function.identity())));
	}

	private static void assignOrdering(List<ModelMetaInfo> metaInfoList) {
		for (int index = 0; index < metaInfoList.size(); index++) {
			metaInfoList.get(index).setOrder(index);
		}
	}

}
