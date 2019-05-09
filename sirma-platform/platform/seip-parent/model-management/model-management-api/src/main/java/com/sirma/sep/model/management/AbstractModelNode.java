package com.sirma.sep.model.management;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic implementation of {@link ModelNode} providing means to assign model node identifier, parent and labels.
 * <p>
 * It's generalized to allow to define the type of the current node and it's parent type and to provide proper method chaining.
 *
 * @param <M> type of the current {@link ModelNode}
 * @param <C> type of the context of the current {@link ModelNode}
 * @author Mihail Radkov
 */
public abstract class AbstractModelNode<M extends ModelNode, C extends ModelNode> implements ModelNode, Walkable, Copyable<M> {

	private String id;

	private String parent;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private boolean deployed;

	private Map<String, ModelAttribute> attributes;

	@JsonIgnore
	private M parentReference;

	@JsonIgnore
	private C context;

	@JsonIgnore
	private DetachedModelNodesStore detachedModelNodesStore = DetachedModelNodesStore.NO_OP_INSTANCE;

	@JsonIgnore
	private ModelsMetaInfo modelsMetaInfo;

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
		addAttribute(getLabelAttributeKey(), new HashMap<>(labels));
		return (M) this;
	}

	@Override
	public Map<String, String> getLabels() {
		return findAttributeValue(getLabelAttributeKey());
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
	public M getParentReference() {
		return parentReference;
	}

	public M setParentReference(M parentReference) {
		this.parentReference = parentReference;
		return (M) this;
	}

	@Override
	public boolean hasParent() {
		return parentReference != null;
	}

	@Override
	public boolean isDeployed() {
		return deployed;
	}

	@Override
	public void setAsDeployed() {
		deployed = true;
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
	public Optional<ModelAttribute> findAttribute(String name) {
		ModelAttribute attribute = getAttributesInternal().get(name);
		if (attribute != null) {
			return Optional.of(attribute);
		}

		if (getParentReference() != null) {
			return getParentReference().findAttribute(name);
		}

		return Optional.empty();
	}

	@Override
	public Optional<ModelAttribute> getAttribute(String name) {
		return Optional.ofNullable(getAttributesInternal().get(name));
	}

	@Override
	public boolean hasAttribute(String name) {
		return getAttributesInternal().containsKey(name);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If attribute existed for the given name, it is moved to another {@link Collection} of detached attributes that are
	 * available during {@link #walk(Path)}.
	 */
	@Override
	public ModelAttribute removeAttribute(String name) {
		ModelAttribute detached = getAttributesInternal().remove(name);
		detachedModelNodesStore.addDetached(detached);
		return detached;
	}

	@Override
	public Object walk(Path step) {
		if (step.getName().equals(ModelAttribute.MODEL_TYPE)) {
			return step.proceed(getOrCreateAttribute(step.getValue()));
		}
		throw new IllegalArgumentException("Invalid path step " + step);
	}

	protected <T> T findAttributeValue(String name) {
		Optional<ModelAttribute> attribute = findAttribute(name);
		if (attribute.isPresent()) {
			return (T) attribute.get().getValue();
		}
		return null;
	}

	protected <T> T getAttributeValue(String name) {
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

	@Override
	public ModelAttribute addAttribute(String name, Object value) {
		if (value != null) {
			return getAttributesInternal().computeIfAbsent(name, this::createAttribute).setValue(value);
		}
		return null;
	}

	protected ModelAttribute getOrCreateAttribute(String name) {
		if (resolveAttributeMetaInfo(name) == null) {
			// do not create invalid or not supported attribute
			// this mainly happens on changes in the meta model and not patched changes
			return null;
		}
		return getAttributesInternal().computeIfAbsent(name, this::createAttribute);
	}

	private ModelAttribute createAttribute(String name) {
		// Remove from detached if re-inserted
		detachedModelNodesStore.removeDetached(this, ModelAttribute.MODEL_TYPE, name);
		return new ModelAttribute(name).setContext(this)
				.setMetaInfoProvider(this::resolveAttributeMetaInfo);
	}

	protected <T extends AbstractModelNode> T copyNodeTo(T target) {
		target.setId(getId());
		target.setParent(getParent());
		target.setModelsMetaInfo(modelsMetaInfo);
		copyAttributesTo(target);
		if (isDeployed()) {
			target.setAsDeployed();
		}
		return target;
	}

	protected void copyAttributesTo(AbstractModelNode<? extends ModelNode, ? extends ModelNode> target) {
		getAttributes().forEach(attribute -> target.addAttribute(attribute.getName(), cloneValue(attribute.getValue())));
	}

	protected static Object cloneValue(Object value) {
		if (value instanceof Map) {
			return new LinkedHashMap((Map) value);
		}
		return value;
	}

	protected ModelMetaInfo resolveAttributeMetaInfo(String attributeName) {
		return getAttributesMetaInfo().get(attributeName);
	}

	@Override
	@JsonIgnore
	public Path getPath() {
		String localId = Objects.requireNonNull(getId(), "Node id is required to build a node path");
		if (getContext() != null) {
			return getContext().getPath().append(Path.create(getTypeName(), localId));
		}
		return Path.create(getTypeName(), localId);
	}

	/**
	 * Returns the path name for the current node. This should be distinct for each Model node implementation.
	 *
	 * @return the model note type
	 */
	protected abstract String getTypeName();

	@Override
	@JsonIgnore
	public C getContext() {
		return context;
	}

	@Override
	@JsonIgnore
	public boolean isDetached() {
		return detachedModelNodesStore.hasDetached(getPath());
	}

	@Override
	@JsonIgnore
	public boolean isDetached(ModelNode context, String type, String name) {
		return detachedModelNodesStore.hasDetached(context, type, name);
	}

	@Override
	public boolean isDetached(ModelAttribute attribute) {
		return detachedModelNodesStore.hasDetached(attribute.getPath());
	}

	public M setContext(C context) {
		this.context = context;
		return (M) this;
	}

	public M setModelsMetaInfo(ModelsMetaInfo modelsMetaInfo) {
		this.modelsMetaInfo = modelsMetaInfo;
		return (M) this;
	}

	protected ModelsMetaInfo getModelsMetaInfo() {
		return this.modelsMetaInfo;
	}

	protected M setDetachedModelNodesStore(DetachedModelNodesStore detachedModelNodesStore) {
		this.detachedModelNodesStore = detachedModelNodesStore;
		return (M) this;
	}

	protected DetachedModelNodesStore getDetachedModelNodesStore() {
		return detachedModelNodesStore;
	}

	@Override
	@JsonIgnore
	public boolean detach() {
		if (getContext() == null) {
			return false;
		}
		Function<String, Optional<M>> removeFunction = getRemoveFunction();
		return removeFunction != null && removeFunction.apply(getId()).isPresent();
	}

	/**
	 * The implementor may provide a function that deletes the current node by name. This is optional operation and will
	 * be called only if the node has a context instance to delete from.
	 *
	 * @return delete function for nodes of the current type
	 */
	protected abstract Function<String, Optional<M>> getRemoveFunction();
}
