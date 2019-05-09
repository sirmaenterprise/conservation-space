package com.sirma.sep.model.management;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Holds information about a definition model.
 *
 * @author Mihail Radkov
 */
public class ModelDefinition extends AbstractModelNode<ModelDefinition, ModelNode> implements Copyable<ModelDefinition> {

	static final String MODEL_TYPE = "definition";

	@JsonIgnore
	private List<ModelDefinition> children;

	private Map<String, ModelField> fields;
	private Map<String, ModelRegion> regions;
	private Map<String, ModelHeader> headers;
	private Map<String, ModelAction> actions;
	private Map<String, ModelActionGroup> actionGroups;

	@Override
	public List<ModelDefinition> getChildren() {
		if (children == null) {
			children = new LinkedList<>();
		}
		return children;
	}

	@Override
	public void addChild(ModelNode child) {
		if (child instanceof ModelDefinition) {
			getChildren().add((ModelDefinition) child);
			((ModelDefinition) child).setParentReference(this);
		} else if (child != null) {
			throw new IllegalArgumentException(
					"Incompatible child type. Expected " + this.getClass().getSimpleName() + " but got "
							+ child.getClass().getSimpleName());
		}
	}

	public ModelDefinition setChildren(List<ModelDefinition> children) {
		this.children = children;
		return this;
	}

	@Override
	public ModelDefinition setId(String id) {
		addAttribute(DefinitionModelAttributes.IDENTIFIER, id);
		return super.setId(id);
	}

	public boolean isAbstract() {
		return getAttributeValue(DefinitionModelAttributes.ABSTRACT);
	}

	public ModelDefinition setAbstract(boolean anAbstract) {
		addAttribute(DefinitionModelAttributes.ABSTRACT, anAbstract);
		return this;
	}

	public String getRdfType() {
		return findAttributeValue(DefinitionModelAttributes.RDF_TYPE);
	}

	public ModelDefinition setRdfType(String rdfType) {
		addAttribute(DefinitionModelAttributes.RDF_TYPE, rdfType);
		return this;
	}

	@Override
	@JsonIgnore
	public Map<String, String> getLabels() {
		return super.getLabels();
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	/**
	 * Returns a collection of this model definition's fields that does not support addition.
	 * <p>
	 * To perform addition use {@link #addField(ModelField)} or directly use {@link #getFieldsMap()}
	 *
	 * @return collection of this definition's fields
	 */
	public Collection<ModelField> getFields() {
		return getFieldsMap().values();
	}

	@JsonIgnore
	public Map<String, ModelField> getFieldsMap() {
		if (fields == null) {
			fields = new LinkedHashMap<>();
		}
		return fields;
	}

	public ModelDefinition setFields(Map<String, ModelField> fields) {
		this.fields = fields;
		return this;
	}

	public ModelField addField(ModelField field) {
		field.setContext(this);
		field.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(field);
		relinkNodesOnNewNode(field.getId(), field, ModelDefinition::getFieldsMap);
		linkFieldToRegion(field);
		return getFieldsMap().put(field.getId(), field);
	}

	/**
	 * Unregister field from the current definition. If the field was overridden from parent definition that field
	 * will still be there. This only removes the overridden copy of the field from the current definition.
	 *
	 * @param fieldId the identifier of the field to remove
	 * @return the removed field if any
	 */
	public Optional<ModelField> removeField(String fieldId) {
		return removeNode(fieldId, ModelDefinition::getFieldsMap);
	}

	private <C extends AbstractModelNode<C, ?>> Optional<C> removeNode(String nodeId,
			Function<ModelDefinition, Map<String, C>> childStoreResolver) {
		Map<String, C> store = childStoreResolver.apply(this);
		C removedNode = store.remove(nodeId);
		getDetachedModelNodesStore().addDetached(removedNode);
		if (removedNode != null) {
			// if a node is removed successfully then relink any parent node with the children of the current node
			relinkNodesOnNodeRemoval(nodeId, childStoreResolver);
		}
		return Optional.ofNullable(removedNode);
	}

	private <C extends AbstractModelNode<C, ?>> void relinkNodesOnNewNode(String nodeId, C nodeToSet,
			Function<ModelDefinition, Map<String, C>> childStoreResolver) {
		Function<ModelDefinition, C> childResolver = def -> childStoreResolver.apply(def).get(nodeId);
		C parent = resolveParentNode(childResolver);
		// set the any found parent as such to the newly added field
		linkParentNode(parent, def -> nodeToSet).accept(this);
		// search the children graph for such definitions that they define the current node and skip their children
		Predicate<ModelDefinition> onlyDefinitionsWithOverriddenNode = def -> childStoreResolver.apply(def)
				.containsKey(nodeId);
		streamChildren(this, onlyDefinitionsWithOverriddenNode).forEach(linkParentNode(nodeToSet, childResolver));
	}

	private <C extends AbstractModelNode<C, ?>> void relinkNodesOnNodeRemoval(String nodeId,
			Function<ModelDefinition, Map<String, C>> childStoreResolver) {
		Function<ModelDefinition, C> childResolver = def -> childStoreResolver.apply(def).get(nodeId);
		C nodeToSet = resolveParentNode(childResolver);
		// we do not care if the node is null or not as if null we need to remove the reference
		relinkNodesOnNewNode(nodeId, nodeToSet, childStoreResolver);
	}

	private <C extends AbstractModelNode<C, ?>> C resolveParentNode(Function<ModelDefinition, C> childResolver) {
		if (!hasParent()) {
			return null;
		}
		ModelDefinition current = getParentReference();
		while (current != null) {
			C node = childResolver.apply(current);
			if (node != null) {
				return node;
			}
			current = current.getParentReference();
		}
		// the node is not present in the hierarchy above
		return null;
	}

	private static Stream<ModelDefinition> streamChildren(ModelDefinition node, Predicate<ModelDefinition> childFilter) {
		return node.getChildren().stream().flatMap(child -> {
			if (childFilter.test(child)) {
				// the child has the required field
				return Stream.of(child);
			}
			// search in the child children until the end or child with the node is found
			return streamChildren(child, childFilter);
		});
	}

	private static <C extends AbstractModelNode<C, ?>> Consumer<ModelDefinition> linkParentNode(C parent,
			Function<ModelDefinition, C> childResolver) {
		return def -> {
			C child = childResolver.apply(def);
			if (child == null || child == parent) {
				return;
			}
			child.setParentReference(parent);
			String parentId = null;
			if (parent != null) {
				parentId = parent.getId();
			}
			child.setParent(parentId);
		};
	}

	/**
	 * Returns a collection of this model definition's regions that does not support addition.
	 * <p>
	 * To perform addition use {@link #addRegion(ModelRegion)} or directly use {@link #getRegionsMap()}
	 *
	 * @return collection of this definition's regions
	 */
	public Collection<ModelRegion> getRegions() {
		return getRegionsMap().values();
	}

	@JsonIgnore
	public Map<String, ModelRegion> getRegionsMap() {
		if (regions == null) {
			regions = new LinkedHashMap<>();
		}
		return regions;
	}

	/**
	 * Unregister region from the current definition. If the region was overridden from parent definition that region
	 * will still be there. This only removes the overridden copy of the region from the current definition.
	 *
	 * @param regionId the identifier of the region to remove
	 * @return the removed region if any.
	 */
	public Optional<ModelRegion> removeRegion(String regionId) {
		return removeNode(regionId, ModelDefinition::getRegionsMap);
	}

	public void setRegions(Map<String, ModelRegion> regions) {
		this.regions = regions;
	}

	public ModelRegion addRegion(ModelRegion region) {
		region.setContext(this);
		region.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(region);
		relinkNodesOnNewNode(region.getId(), region, ModelDefinition::getRegionsMap);
		return getRegionsMap().put(region.getId(), region);
	}

	/**
	 * Returns a collection of this model definition's headers that does not support addition.
	 * <p>
	 * To perform addition use {@link #addHeader(ModelHeader)} or directly use {@link #getHeadersMap()}
	 * <p>
	 *
	 * @return collection of this definition's headers
	 */
	public Collection<ModelHeader> getHeaders() {
		return getHeadersMap().values();
	}

	public Map<String, ModelHeader> getHeadersMap() {
		if (headers == null) {
			headers = new LinkedHashMap<>();
		}
		return headers;
	}

	public void setHeaders(Map<String, ModelHeader> headers) {
		this.headers = headers;
	}

	public ModelHeader addHeader(ModelHeader header) {
		header.setContext(this);
		header.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(header);
		relinkNodesOnNewNode(header.getId(), header, ModelDefinition::getHeadersMap);
		return getHeadersMap().put(header.getId(), header);
	}

	/**
	 * Unregister header from the current definition. If the header was overridden from parent definition that header
	 * will still be there. This only removes the overridden copy of the header from the current definition.
	 *
	 * @param headerId the identifier of the header to remove
	 * @return the removed header if any
	 */
	public Optional<ModelHeader> removeHeader(String headerId) {
		return removeNode(headerId, ModelDefinition::getHeadersMap);
	}

	/**
	 * Returns a collection of this model definition's actions that does not support addition.
	 * <p>
	 * To perform addition use {@link #addAction(ModelAction)} or directly use {@link #getActionsMap()}
	 *
	 * @return collection of this definition's actions
	 */
	public Collection<ModelAction> getActions() {
		return getActionsMap().values();
	}

	@JsonIgnore
	public Map<String, ModelAction> getActionsMap() {
		if (actions == null) {
			actions = new LinkedHashMap<>();
		}
		return actions;
	}

	public void setActions(Map<String, ModelAction> actions) {
		this.actions = actions;
	}

	public ModelAction addAction(ModelAction action) {
		action.setContext(this);
		action.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(action);
		relinkNodesOnNewNode(action.getId(), action, ModelDefinition::getActionsMap);
		action.relinkAllExecutions();
		return getActionsMap().put(action.getId(), action);
	}

	/**
	 * Unregister action from the current definition. If the action was overridden from parent definition that action
	 * will still be there. This only removes the overridden copy of the action from the current definition.
	 *
	 * @param actionId the identifier of the action to remove
	 * @return the removed action if any
	 */
	public Optional<ModelAction> removeAction(String actionId) {
		Optional<ModelAction> modelAction = removeNode(actionId, ModelDefinition::getActionsMap);
		if (modelAction.isPresent()) {
			modelAction.get().removeAllActionExecution();
		}
		return modelAction;
	}

	/**
	 * Returns a collection of this model definition's actions groups that does not support addition.
	 * <p>
	 * To perform addition use {@link #addActionGroup(ModelActionGroup)} or directly use {@link #getActionGroupsMap()}
	 *
	 * @return collection of this definition's actions groups
	 */
	public Collection<ModelActionGroup> getActionGroups() {
		return getActionGroupsMap().values();
	}

	@JsonIgnore
	public Map<String, ModelActionGroup> getActionGroupsMap() {
		if (actionGroups == null) {
			actionGroups = new LinkedHashMap<>();
		}
		return actionGroups;
	}

	public void setActionGroups(Map<String, ModelActionGroup> actionGroups) {
		if (actionGroups == null) {
			actionGroups = new LinkedHashMap<>();
		}
		this.actionGroups = actionGroups;
	}

	public ModelActionGroup addActionGroup(ModelActionGroup actionGroup) {
		actionGroup.setContext(this);
		actionGroup.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(actionGroup);
		relinkNodesOnNewNode(actionGroup.getId(), actionGroup, ModelDefinition::getActionGroupsMap);
		return getActionGroupsMap().put(actionGroup.getId(), actionGroup);
	}

	/**
	 * Unregister action group from the current definition. If the action was overridden from parent definition that
	 * action group will still be there. This only removes the overridden copy of the action group from the current
	 * definition.
	 *
	 * @param actionGroupId the identifier of the action group to remove
	 * @return the removed action group if any
	 */
	public Optional<ModelActionGroup> removeActionGroup(String actionGroupId) {
		return removeNode(actionGroupId, ModelDefinition::getActionGroupsMap);
	}

	@JsonIgnore
	@Override
	public ModelDefinition getParentReference() {
		return super.getParentReference();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModelDefinition that = (ModelDefinition) o;
		return Objects.equals(fields, that.fields) && Objects.equals(regions, that.regions) && Objects.equals(headers, that.headers)
				&& Objects.equals(actions, that.actions) && Objects.equals(actionGroups, that.actionGroups);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fields, regions, headers, actions, actionGroups);
	}

	/**
	 * Tries to find a {@link ModelField} corresponding to the given name either in this model definition or in the parent hierarchy
	 * if this one has a parent.
	 *
	 * @param name the field name to search for
	 * @return the field or {@link Optional#empty()} if there is no such field
	 */
	public Optional<ModelField> findFieldByName(String name) {
		return find(name, getFieldsMap(), (id, parent) -> parent.findFieldByName(id));
	}

	/**
	 * Tries to find a {@link ModelAction} corresponding to the given id either in this model definition or in the parent hierarchy
	 * if this one has a parent.
	 *
	 * @param actionId the action id to search for
	 * @return the action or {@link Optional#empty()} if there is no such action.
	 */
	public Optional<ModelAction> findActionById(String actionId) {
		return find(actionId, getActionsMap(), (id, parent) -> parent.findActionById(id));
	}

	/**
	 * Tries to find a {@link ModelActionGroup} corresponding to the given id either in this model definition or in the parent hierarchy
	 * if this one has a parent.
	 *
	 * @param actionGroupId the action group id to search for
	 * @return the action group or {@link Optional#empty()} if there is no such action group.
	 */
	public Optional<ModelActionGroup> findActionGroupById(String actionGroupId) {
		return find(actionGroupId, getActionGroupsMap(), (id, parent) -> parent.findActionGroupById(id));
	}

	/**
	 * Tries to find a {@link ModelRegion} corresponding to the given name either in this model definition or in the parent hierarchy
	 * if this one has a parent.
	 *
	 * @param regionName the region name to search for
	 * @return the field or {@link Optional#empty()} if there is no such region
	 */
	public Optional<ModelRegion> findRegionByName(String regionName) {
		return find(regionName, getRegionsMap(), (id, parent) -> parent.findRegionByName(id));
	}

	/**
	 * Tries to find a {@link ModelAttribute} corresponding to the given header name either in this model definition or in the parent
	 * hierarchy if this one has a parent.
	 *
	 * @param headerName the header name to search for
	 * @return the field or {@link Optional#empty()} if there is no such header
	 */
	public Optional<ModelHeader> findHeaderByName(String headerName) {
		return find(headerName, getHeadersMap(), (id, parent) -> parent.findHeaderByName(id));
	}

	private <M> Optional<M> find(String id, Map<String, M> models,
			BiFunction<String, ModelDefinition, Optional<M>> parentResolver) {
		if (models.containsKey(id)) {
			return Optional.of(models.get(id));
		}
		if (hasParent()) {
			return parentResolver.apply(id, getParentReference());
		}
		return Optional.empty();
	}

	@Override
	public Object walk(Path step) {
		switch (step.getName()) {
			case ModelAttribute.MODEL_TYPE:
				return processAttributePathStep(step, this::getOrCreateAttribute);
			case ModelField.MODEL_TYPE:
				return processPathStep(step, this::getOrCreateField);
			case ModelRegion.MODEL_TYPE:
				return processPathStep(step, this::getOrCreateRegion);
			case ModelHeader.HEADER_TYPE:
				return processPathStep(step, this::getOrCreateHeader);
			case ModelAction.MODEL_TYPE:
				return processPathStep(step, this::getOrCreateAction);
			case ModelActionGroup.MODEL_TYPE:
				return processPathStep(step, this::getOrCreateActionGroup);
			default:
				throw new IllegalArgumentException("Invalid path step " + step);
		}
	}

	private Object processAttributePathStep(Path step, Function<String, Object> pathNodeCreator) {
		return step.proceed(pathNodeCreator.apply(step.getValue()));
	}

	private Object processPathStep(Path step, Function<String, Object> pathNodeCreator) {
		return step.proceed(pathNodeCreator.apply(step.getValue()));
	}

	private ModelField getOrCreateField(String id) {
		return getFieldsMap().computeIfAbsent(id, this::createField);
	}

	private ModelField createField(String id) {
		ModelField modelField = new ModelField();
		modelField.setId(id);
		modelField.setModelsMetaInfo(getModelsMetaInfo());
		modelField.setContext(this);
		modelField.setDetachedModelNodesStore(getDetachedModelNodesStore());
		relinkNodesOnNewNode(id, modelField, ModelDefinition::getFieldsMap);
		linkFieldToRegion(modelField);
		getDetachedModelNodesStore().removeDetached(modelField);
		return modelField;
	}

	private void linkFieldToRegion(ModelField modelField) {
		// find parent field if any and assign the created field to the same region
		if (hasParent()) {
			getParentReference().findFieldByName(modelField.getId())
					.ifPresent(parentField -> modelField.setRegionId(parentField.getRegionId()));
		}

		if (modelField.getRegionId() != null) {
			ModelRegion region = getOrCreateRegion(modelField.getRegionId());
			region.addField(modelField.getId());
		}
	}

	private ModelRegion getOrCreateRegion(String id) {
		return getRegionsMap().computeIfAbsent(id, this::createRegion);
	}

	private ModelRegion createRegion(String id) {
		ModelRegion modelRegion = new ModelRegion();
		modelRegion.setId(id);
		modelRegion.setModelsMetaInfo(getModelsMetaInfo());
		modelRegion.setContext(this);
		modelRegion.setDetachedModelNodesStore(getDetachedModelNodesStore());
		relinkNodesOnNewNode(id, modelRegion, ModelDefinition::getRegionsMap);
		getDetachedModelNodesStore().removeDetached(modelRegion);
		return modelRegion;
	}

	private ModelHeader getOrCreateHeader(String id) {
		return getHeadersMap().computeIfAbsent(id, this::createHeader);
	}

	private ModelHeader createHeader(String id) {
		ModelHeader header = new ModelHeader();
		header.setId(id);
		header.setModelsMetaInfo(getModelsMetaInfo());
		header.setContext(this);
		header.setDetachedModelNodesStore(getDetachedModelNodesStore());
		relinkNodesOnNewNode(id, header, ModelDefinition::getHeadersMap);
		getDetachedModelNodesStore().removeDetached(header);
		return header;
	}

	private ModelAction getOrCreateAction(String id) {
		return getActionsMap().computeIfAbsent(id, this::createAction);
	}

	private ModelAction createAction(String id) {
		ModelAction actionModel = new ModelAction();
		actionModel.setId(id);
		actionModel.setModelsMetaInfo(getModelsMetaInfo());
		actionModel.setContext(this);
		actionModel.setDetachedModelNodesStore(getDetachedModelNodesStore());
		relinkNodesOnNewNode(id, actionModel, ModelDefinition::getActionsMap);
		getDetachedModelNodesStore().removeDetached(actionModel);
		return actionModel;
	}

	private ModelActionGroup getOrCreateActionGroup(String id) {
		return getActionGroupsMap().computeIfAbsent(id, this::createActionGroup);
	}

	private ModelActionGroup createActionGroup(String id) {
		ModelActionGroup actionGroupModel = new ModelActionGroup();
		actionGroupModel.setId(id);
		actionGroupModel.setModelsMetaInfo(getModelsMetaInfo());
		actionGroupModel.setContext(this);
		actionGroupModel.setDetachedModelNodesStore(getDetachedModelNodesStore());
		relinkNodesOnNewNode(id, actionGroupModel, ModelDefinition::getActionGroupsMap);
		getDetachedModelNodesStore().removeDetached(actionGroupModel);
		return actionGroupModel;
	}

	@Override
	public ModelDefinition createCopy() {
		ModelDefinition copy = new ModelDefinition();
		copy.setId(getId());
		copy.setParent(getParent());
		copyAttributesTo(copy);
		copy.setDetachedModelNodesStore(getDetachedModelNodesStore());
		copy.setModelsMetaInfo(getModelsMetaInfo());

		getFields().stream().map(ModelField::createCopy).forEach(copy::addField);
		getRegions().stream().map(ModelRegion::createCopy).forEach(copy::addRegion);
		getHeaders().stream().map(ModelHeader::createCopy).forEach(copy::addHeader);
		getActions().stream().map(ModelAction::createCopy).forEach(copy::addAction);
		getActionGroups().stream().map(ModelActionGroup::createCopy).forEach(copy::addActionGroup);

		if (isDeployed()) {
			copy.setAsDeployed();
		}
		return copy;
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getDefinitionsMapping();
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected ModelDefinition setDetachedModelNodesStore(DetachedModelNodesStore detachedModelNodesStore) {
		super.setDetachedModelNodesStore(detachedModelNodesStore);
		getFields().forEach(field -> field.setDetachedModelNodesStore(detachedModelNodesStore));
		getRegions().forEach(modelRegion -> modelRegion.setDetachedModelNodesStore(detachedModelNodesStore));
		getHeaders().forEach(modelHeader -> modelHeader.setDetachedModelNodesStore(detachedModelNodesStore));
		getActions().forEach(modelAction -> modelAction.setDetachedModelNodesStore(detachedModelNodesStore));
		getActionGroups().forEach(actionGroup -> actionGroup.setDetachedModelNodesStore(detachedModelNodesStore));
		return this;
	}

	@Override
	protected Function<String, Optional<ModelDefinition>> getRemoveFunction() {
		return null;
	}
}
