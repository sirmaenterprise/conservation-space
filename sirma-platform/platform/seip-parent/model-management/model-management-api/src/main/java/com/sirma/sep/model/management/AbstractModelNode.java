package com.sirma.sep.model.management;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.ModelNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Basic implementation of {@link ModelNode} providing means to assign model node identifier, parent and labels.
 * <p>
 * It's generalized to allow to define the type of the current node and it's parent type and to provide proper method chaining.
 *
 * @param <M> type of the current {@link ModelNode}
 * @param <P> type of the parent of the current {@link ModelNode}
 * @author Mihail Radkov
 */
public abstract class AbstractModelNode<M extends ModelNode, P extends ModelNode> implements ModelNode {

	private String id;

	private String parent;

	private Map<String, ModelAttribute> attributes;

	@JsonIgnore
	private P parentReference;

	@Override
	public String getId() {
		return id;
	}

	public M setId(String id) {
		this.id = id;
		return (M) this;
	}

	@Override
	public String getParent() {
		return parent;
	}

	public M setParent(String parent) {
		this.parent = parent;
		return (M) this;
	}

	public M setLabels(Map<String, String> labels) {
		return addAttribute(getLabelAttributeKey(), ModelAttributeType.LABEL, new HashMap<>(labels));
	}

	@Override
	public Map<String, String> getLabels() {
		return getAttributeValue(getLabelAttributeKey());
	}

	protected abstract String getLabelAttributeKey();

	@Override
	public Collection<ModelAttribute> getAttributes() {
		return getAttributesInternal().values();
	}

	public M setAttributes(List<ModelAttribute> attributes) {
		this.attributes = attributes.stream()
				.collect(CollectionUtils.toIdentityMap(ModelAttribute::getName, LinkedHashMap::new));
		return (M) this;
	}

	@Override
	public P getParentReference() {
		return parentReference;
	}

	public M setParentReference(P parentReference) {
		this.parentReference = parentReference;
		return (M) this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AbstractModelNode that = (AbstractModelNode) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(parent, that.parent)
				&& Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, parent, attributes);
	}

	@Override
	public Optional<ModelAttribute> getAttribute(String name) {
		ModelAttribute attribute = getAttributesInternal().get(name);
		if (attribute != null) {
			return Optional.of(attribute);
		}

		if (getParentReference() != null) {
			return getParentReference().getAttribute(name);
		}

		return Optional.empty();
	}

	protected <T extends Serializable> T getAttributeValue(String name) {
		Optional<ModelAttribute> attribute = getAttribute(name);
		if (attribute.isPresent()) {
			return (T) attribute.get().getValue();
		}
		return null;
	}

	private Map<String, ModelAttribute> getAttributesInternal() {
		if (attributes == null) {
			attributes = new LinkedHashMap<>();
		}
		return attributes;
	}

	public M addAttribute(String name, String type, Serializable value) {
		if (value != null) {
			getAttributesInternal().put(name, new ModelAttribute().setName(name)
					.setType(type)
					.setValue(value));
		}
		return (M) this;
	}
}
