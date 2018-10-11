package com.sirma.sep.model.management.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Descriptor for a supported {@link com.sirma.sep.model.ModelNode}'s {@link com.sirma.sep.model.management.ModelAttribute} meta information.
 *
 * @author Mihail Radkov
 */
public class ModelMetaInfo {

	private String id;

	private String uri;

	private String type;

	private Object defaultValue;

	private ModelMetaInfoValidation validationModel;

	private Map<String, String> labels;

	private int order;

	public String getId() {
		return id;
	}

	public ModelMetaInfo setId(String id) {
		this.id = id;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public ModelMetaInfo setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public ModelMetaInfoValidation getValidationModel() {
		return validationModel;
	}

	public ModelMetaInfo setValidationModel(ModelMetaInfoValidation validationModel) {
		this.validationModel = validationModel;
		return this;
	}

	public Map<String, String> getLabels() {
		if (labels == null) {
			labels = new HashMap<>();
		}
		return labels;
	}

	public ModelMetaInfo setLabels(Map<String, String> labels) {
		this.labels = labels;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModelMetaInfo that = (ModelMetaInfo) o;
		return order == that.order &&
				Objects.equals(id, that.id) &&
				Objects.equals(uri, that.uri) &&
				Objects.equals(type, that.type) &&
				Objects.equals(defaultValue, that.defaultValue) &&
				Objects.equals(validationModel, that.validationModel) &&
				Objects.equals(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, uri, type, defaultValue, validationModel, labels, order);
	}
}
