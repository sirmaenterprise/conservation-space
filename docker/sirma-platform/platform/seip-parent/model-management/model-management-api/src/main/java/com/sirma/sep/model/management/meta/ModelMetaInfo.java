package com.sirma.sep.model.management.meta;

import java.util.HashMap;
import java.util.List;
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

	private String dataType;

	private Object defaultValue;

	private boolean visible = true;

	private ModelMetaInfoValidation validationModel;

	private Map<String, String> labels;

	private Map<String, String> descriptions;

	private List<Map<String, String>> options;

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

	public ModelMetaInfo setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataType() {
		if (this.dataType == null) {
			this.dataType = this.type;
		}
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public ModelMetaInfo setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
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

	public Map<String, String> getDescriptions() {
		if (descriptions == null) {
			descriptions = new HashMap<>();
		}
		return descriptions;
	}

	public ModelMetaInfo setDescriptions(Map<String, String> descriptions) {
		this.descriptions = descriptions;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<Map<String, String>> getOptions() {
		return options;
	}

	public ModelMetaInfo setOptions(List<Map<String, String>> options) {
		this.options = options;
		return this;
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
		return visible == that.visible &&
				order == that.order &&
				Objects.equals(id, that.id) &&
				Objects.equals(uri, that.uri) &&
				Objects.equals(type, that.type) &&
				Objects.equals(id, that.dataType) &&
				Objects.equals(defaultValue, that.defaultValue) &&
				Objects.equals(validationModel, that.validationModel) &&
				Objects.equals(labels, that.labels) &&
				Objects.equals(descriptions, that.descriptions) &&
				Objects.equals(options, that.options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, uri, type, dataType, defaultValue, visible, validationModel, labels, descriptions,
				order, options);
	}
}
