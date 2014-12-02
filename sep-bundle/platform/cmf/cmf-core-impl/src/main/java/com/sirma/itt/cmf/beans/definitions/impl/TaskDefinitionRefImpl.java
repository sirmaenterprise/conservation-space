
package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default implementation for the task definition reference in a workflow.
 * 
 * @author BBonev
 */
public class TaskDefinitionRefImpl extends BaseRegionDefinition<BaseRegionDefinition<?>> implements
		Serializable, BidirectionalMapping, TaskDefinitionRef {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4157609821603423614L;

	/** The task id. */
	@Tag(1)
	protected String identifier;

	/** The start task. */
	@Tag(2)
	protected String purpose;

	/** The reference task id. */
	@Tag(3)
	protected String referenceTaskId;

	/** The transitions. */
	@Tag(4)
	protected List<TransitionDefinition> transitions;

	/** The workflow definition. */
	protected transient WorkflowDefinitionImpl workflowDefinition;

	protected transient TaskDefinitionTemplate definitionTemplate;
	/** The allowed children. */
	@Tag(5)
	protected List<AllowedChildDefinition> allowedChildren;
	/** The state transitions. */
	@Tag(6)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();


	/**
	* {@inheritDoc}
	*/
	@Override
	public String getReferenceTaskId() {
		return referenceTaskId;
	}

	/**
	 * Setter method for referenceTaskId.
	 *
	 * @param referenceTaskId
	 *            the referenceTaskId to set
	 */
	public void setReferenceTaskId(String referenceTaskId) {
		this.referenceTaskId = referenceTaskId;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public List<TransitionDefinition> getTransitions() {
		if (transitions == null) {
			transitions = new LinkedList<TransitionDefinition>();
		}
		return transitions;
	}

	/**
	 * Setter method for transitions.
	 *
	 * @param transitions
	 *            the transitions to set
	 */
	public void setTransitions(List<TransitionDefinition> transitions) {
		this.transitions = transitions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public BaseRegionDefinition<?> mergeFrom(BaseRegionDefinition<?> src) {
		if (src instanceof TaskDefinitionRefImpl) {
			TaskDefinitionRefImpl source = (TaskDefinitionRefImpl) src;
			super.mergeFrom(source);

			identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
			referenceTaskId = MergeHelper.replaceIfNull(referenceTaskId,
					source.getReferenceTaskId());
			purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());

			transitions = MergeHelper.mergeLists(MergeHelper.convertToMergable(transitions),
					MergeHelper.convertToMergable(source.getTransitions()),
					EmfMergeableFactory.TRANSITION_DEFINITION);
			if (getAllowedChildren().isEmpty() && !source.getAllowedChildren().isEmpty()) {
				MergeHelper.mergeLists(MergeHelper.convertToMergable(getAllowedChildren()),
						MergeHelper.convertToMergable(source.getAllowedChildren()),
						EmfMergeableFactory.ALLOWED_CHILDREN);
			}
			if (getStateTransitions().isEmpty() && !source.getStateTransitions().isEmpty()) {
				MergeHelper.mergeLists(MergeHelper.convertToMergable(getStateTransitions()),
						MergeHelper.convertToMergable(source.getStateTransitions()),
						EmfMergeableFactory.STATE_TRANSITION);
			}
		}
		return this;
	}

	/**
	 * Merge template.
	 *
	 * @param template
	 *            the template
	 */
	private void mergeTemplate(TaskDefinitionTemplate template) {
		if (template == null) {
			return;
		}
		TaskDefinitionTemplateImpl source = (TaskDefinitionTemplateImpl) template;
		// copy expression for template if any
		setExpression(MergeHelper.replaceIfNull(getExpression(), source.getExpression()));

		// copy fields and regions to local level from the template
		MergeHelper.mergeTemplate(this, template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();

		if (transitions != null) {
			for (TransitionDefinition transitionDefinition : transitions) {
				TransitionDefinitionImpl definitionImpl = (TransitionDefinitionImpl) transitionDefinition;
				definitionImpl.setOwningDefinition(this);
				definitionImpl.initBidirection();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return getWorkflowDefinition();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		super.setIdentifier(identifier);
	}

	/**
	 * Getter method for workflowDefinition.
	 *
	 * @return the workflowDefinition
	 */
	@Override
	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	/**
	 * Setter method for workflowDefinition.
	 *
	 * @param workflowDefinition
	 *            the workflowDefinition to set
	 */
	public void setWorkflowDefinition(WorkflowDefinitionImpl workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskDefinitionRefImpl [super=");
		builder.append(super.toString());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", purpose=");
		builder.append(purpose);
		builder.append(", referenceTaskId=");
		builder.append(referenceTaskId);
		builder.append(", transitions=");
		builder.append(transitions);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for definitionTemplate.
	 *
	 * @return the definitionTemplate
	 */
	public TaskDefinitionTemplate getDefinitionTemplate() {
		return definitionTemplate;
	}

	/**
	 * Setter method for definitionTemplate.
	 *
	 * @param definitionTemplate
	 *            the definitionTemplate to set
	 */
	public void setDefinitionTemplate(TaskDefinitionTemplate definitionTemplate) {
		this.definitionTemplate = definitionTemplate;
		mergeTemplate(definitionTemplate);
	}

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || !getRegions().isEmpty() || !getTransitions().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		Node child = PathHelper.find(getTransitions(), name);
		if (child == null) {
			child = super.getChild(name);
			if (child == null) {
				for (RegionDefinition regionDefinition : getRegions()) {
					if (regionDefinition.hasChildren()) {
						child = PathHelper.find(regionDefinition.getFields(), name);
						if (child != null) {
							break;
						}
					}
				}
			}
		}
		return child;
	}

	/**
	 * Getter method for allowedChildren.
	 *
	 * @return the allowedChildren
	 */
	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		if (allowedChildren == null) {
			allowedChildren = new LinkedList<AllowedChildDefinition>();
		}
		return allowedChildren;
	}

	/**
	 * Setter method for allowedChildren.
	 *
	 * @param allowedChildren the allowedChildren to set
	 */
	public void setAllowedChildren(List<AllowedChildDefinition> allowedChildren) {
		this.allowedChildren = allowedChildren;
	}

	/**
	 * Getter method for stateTransitions.
	 * 
	 * @return the stateTransitions
	 */
	@Override
	public List<StateTransition> getStateTransitions() {
		if (stateTransitions == null) {
			stateTransitions = new LinkedList<StateTransition>();
		}
		return stateTransitions;
	}

	/**
	 * Setter method for stateTransitions.
	 * 
	 * @param stateTransitions
	 *            the stateTransitions to set
	 */
	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

}
