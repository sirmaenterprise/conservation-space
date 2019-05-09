package com.sirma.sep.model.management;

import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Generic class for storing different types of model attributes.
 *
 * @author Mihail Radkov
 */
public class ModelAttribute {

	static final String MODEL_TYPE = "attribute";

	private String name;

	private Object value;

	@JsonIgnore
	private ModelNode context;

	@JsonIgnore
	private Function<String, ModelMetaInfo> metaInfoProvider;

	/**
	 * Creates an attribute path node using the given attribute name
	 *
	 * @param name the attribute name to create
	 * @return an attribute path node
	 */
	public static Path createPath(String name) {
		return Path.create(MODEL_TYPE, name);
	}

	public ModelAttribute() {
		//	just default constructor
	}

	public ModelAttribute(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public ModelAttribute setName(String name) {
		this.name = name;
		return this;
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getType() {
		return getMetaInfoInternal().getType();
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getDataType() {
		return getMetaInfoInternal().getDataType();
	}

	public Object getValue() {
		return value;
	}

	public ModelAttribute setValue(Object value) {
		this.value = value;
		return this;
	}

	public ModelNode getContext() {
		return context;
	}

	public ModelAttribute setContext(ModelNode context) {
		this.context = context;
		return this;
	}

	public ModelAttribute setMetaInfoProvider(Function<String, ModelMetaInfo> metaInfoProvider) {
		this.metaInfoProvider = metaInfoProvider;
		return this;
	}

	@JsonIgnore
	public ModelMetaInfo getMetaInfo() {
		return metaInfoProvider.apply(getName());
	}

	@JsonIgnore
	public Path getPath() {
		if (getContext() != null) {
			return getContext().getPath().append(Path.create(MODEL_TYPE, getName()));
		}
		return Path.create(MODEL_TYPE, getName());
	}

	/**
	 * Detaches the current node from it's context and from any other referenced nodes.
	 *
	 * @return true if the node was detached and false if called on already detached node or the node is not part of
	 * any context
	 */
	@JsonIgnore
	public boolean detach() {
		if (getContext() == null) {
			return false;
		}
		return getContext().removeAttribute(getName()) != null;
	}

	/**
	 * Check if the current attribute is detached from the context
	 *
	 * @return true if detached
	 */
	@JsonIgnore
	public boolean isDetached() {
		return getContext().isDetached(this);
	}

	/**
	 * Check if the current attribute value is empty
	 *
	 * @return true if detached
	 */
	@JsonIgnore
	public boolean isEmpty() {
		if (value instanceof String) {
			return ((String) value).isEmpty();
		}
		return value == null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModelAttribute that = (ModelAttribute) o;
		return Objects.equals(name, that.name) && Objects.equals(value, that.value);
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	private ModelMetaInfo getMetaInfoInternal() {
		ModelMetaInfo metaInfo = getMetaInfo();
		if (metaInfo == null) {
			if (getContext() == null) {
				throw new IllegalStateException("No model meta info and context for " + getName());
			}
			throw new IllegalStateException("No model meta info for " + getContext().getId() + "." + getName());
		}
		return metaInfo;
	}
}
