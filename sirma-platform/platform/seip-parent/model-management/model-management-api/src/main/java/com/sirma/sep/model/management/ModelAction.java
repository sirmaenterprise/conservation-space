package com.sirma.sep.model.management;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Function;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holds definition information of transitions. Transitions are description of actions which user can perform on an instance.
 *
 * @author Boyan Tonchev.
 */
public class ModelAction extends AbstractModelNode<ModelAction, ModelDefinition> implements Copyable<ModelAction> {

	static final String MODEL_TYPE = "action";

	private Map<String, ModelActionExecution> actionExecutions = new LinkedHashMap<>();

	@Override
	public ModelAction createCopy() {
		ModelAction copyOfModelAction = copyNodeTo(new ModelAction());
		getActionExecutions().stream()
				.map(ModelActionExecution::createCopy)
				.forEach(copyOfModelAction::addActionExecution);
		return copyOfModelAction;
	}

	public ModelActionExecution addActionExecution(ModelActionExecution actionExecution) {
		actionExecution.setContext(this);
		actionExecution.setModelsMetaInfo(getModelsMetaInfo());
		actionExecution.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(actionExecution);
		link(actionExecution);
		return actionExecutions.put(actionExecution.getId(), actionExecution);
	}

	@Override
	public Object walk(Path step) {
		if (ModelActionExecution.MODEL_TYPE.equals(step.getName())) {
			return step.proceed(getOrCreateActionExecution(step.getValue()));
		}
		return super.walk(step);
	}

	private ModelActionExecution getOrCreateActionExecution(String id) {
		return actionExecutions.computeIfAbsent(id, this::createActionExecution);
	}

	private ModelActionExecution createActionExecution(String id) {
		ModelActionExecution modelActionExecution = new ModelActionExecution();
		modelActionExecution.setModelsMetaInfo(getModelsMetaInfo());
		modelActionExecution.setId(id);
		modelActionExecution.setContext(this);
		modelActionExecution.setDetachedModelNodesStore(getDetachedModelNodesStore());
		getDetachedModelNodesStore().removeDetached(modelActionExecution);
		link(modelActionExecution);
		return modelActionExecution;
	}

	/**
	 * Remove all action executions.
	 */
	public void removeAllActionExecution() {
		getActionExecutionMap().keySet().forEach(this::removeActionExecution);
	}

	public Optional<ModelActionExecution> removeActionExecution(String actionExecutionId) {
		ModelActionExecution removedNode = actionExecutions.remove(actionExecutionId);
		getDetachedModelNodesStore().addDetached(removedNode);
		if (removedNode != null) {
			// we do not care if the parentActionExecution is null or not as if null we need to remove the reference
			linkWithChildrenActionExecutions(findParentActionExecution(actionExecutionId).orElse(null));
		}
		return Optional.ofNullable(removedNode);
	}

	public void relinkAllExecutions() {
		getActionExecutions().forEach(this::link);
	}

	private void link(ModelActionExecution actionExecution) {
		// Update parent action execution.
		findParentActionExecution(actionExecution.getId()).ifPresent(actionExecution::setParentReference);
		// Update all children action executions.
		linkWithChildrenActionExecutions(actionExecution);
	}

	/**
	 * Iterate over parent actions and looking for such action which have an executionAction with id <code>actionExecutionId</code>
	 *
	 * @param actionExecutionId - the id of action execution.
	 * @return found action or Optional.empty()
	 */
	private Optional<ModelActionExecution> findParentActionExecution(String actionExecutionId) {
		ModelAction parentAction = getParentReference();
		if (parentAction != null) {
			return parentAction.findActionExecutionById(actionExecutionId);
		}
		return Optional.empty();
	}

	/**
	 * Tries to find a {@link ModelActionExecution} corresponding to the given id either in this model definition or in the parent hierarchy
	 * if this one has a parent.
	 *
	 * @param actionExecutionId the action id to search for
	 * @return the action execution or {@link Optional#empty()} if there is no such action execution.
	 */
	private Optional<ModelActionExecution> findActionExecutionById(String actionExecutionId) {
		ModelActionExecution modelActionExecution = getActionExecutionMap().get(actionExecutionId);
		if (modelActionExecution != null) {
			return Optional.of(modelActionExecution);
		}
		if (hasParent()) {
			return getParentReference().findActionExecutionById(actionExecutionId);
		}
		return Optional.empty();
	}

	private void linkWithChildrenActionExecutions(ModelActionExecution modelActionExecution) {
		// find all child definition which has action with id as current.
		Stream<ModelAction> modelActions = findChildActions(getContext(), getId());
		modelActions.map(modelAction -> modelAction.getActionExecutionMap().get(modelActionExecution.getId()))
				.filter(Objects::nonNull)
				.forEach(childModelActionExecution -> childModelActionExecution.setParentReference(
						modelActionExecution));
	}

	/**
	 * Iterate over all child definitions and fetch all actions with id <code>actionId</code>
	 *
	 * @param modelDefinition - the definition whose children have to be searched.
	 * @param actionId        - the id of searched actions.
	 * @return stream with fond actions or empty stream.
	 */
	private Stream<ModelAction> findChildActions(ModelDefinition modelDefinition, String actionId) {
		if (modelDefinition == null) {
			return Stream.empty();
		}
		return modelDefinition.getChildren().stream().flatMap(definition -> {
			ModelAction action = definition.getActionsMap().get(actionId);
			if (action != null) {
				return Stream.of(action);
			}
			return findChildActions(definition, actionId);
		});
	}

	public Collection<ModelActionExecution> getActionExecutions() {
		return actionExecutions.values();
	}

	@JsonIgnore
	public Map<String, ModelActionExecution> getActionExecutionMap() {
		return actionExecutions;
	}

	public void setActionExecutions(Map<String, ModelActionExecution> actionExecutions) {
		this.actionExecutions = actionExecutions;
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelAction>> getRemoveFunction() {
		return getContext()::removeAction;
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getActionsMapping();
	}

	@JsonIgnore
	public ModelActionExecution getActionExecution(String actionExecutionId) {
		return actionExecutions.get(actionExecutionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), actionExecutions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ModelAction))
			return false;
		if (!super.equals(o))
			return false;
		ModelAction that = (ModelAction) o;
		return Objects.equals(actionExecutions, that.actionExecutions);
	}
}