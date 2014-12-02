package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default {@link WorkflowDefinition} implementation
 *
 * @author BBonev
 */
public class WorkflowDefinitionImpl extends BaseRegionDefinition<WorkflowDefinitionImpl> implements
		Serializable, BidirectionalMapping, WorkflowDefinition,
		MergeableTopLevelDefinition<WorkflowDefinitionImpl> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8818349347394727282L;

	/** The workflow definition id. */
	@Tag(1)
	protected String identifier;
	/**
	 * Versionable field for determining case revision <br/>
	 */
	@Tag(2)
	protected Long revision;

	/** The parent case id. */
	@Tag(3)
	protected String parentDefinitionId;
	/**
	 * The date on which the entity (e.g. Registration) was last modified. <br/>
	 */
	@Tag(4)
	protected Date lastModifiedDate;
	/**
	 * The date object was created. <br/>
	 */
	@Tag(5)
	protected Date creationDate;

	/** The dms id. */
	@Tag(6)
	protected String dmsId;

	/** The container. */
	@Tag(7)
	protected String container;

	/** The Abstract. */
	@Tag(8)
	protected Boolean Abstract = Boolean.FALSE;

	/** The tasks. */
	@Tag(9)
	protected List<TaskDefinitionRef> tasks;

	/** The allowed children. */
	@Tag(10)
	protected List<AllowedChildDefinition> allowedChildren;
	/** The transitions. */
	@Tag(11)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();
	/** The state transitions. */
	@Tag(12)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentDefinitionId() {
		return parentDefinitionId;
	}

	/**
	 * Setter method for parentDefinitionId.
	 *
	 * @param parentDefinitionId
	 *            the parentDefinitionId to set
	 */
	public void setParentDefinitionId(String parentDefinitionId) {
		this.parentDefinitionId = parentDefinitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * Setter method for lastModifiedDate.
	 *
	 * @param lastModifiedDate
	 *            the lastModifiedDate to set
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Setter method for creationDate.
	 *
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * Setter method for dmsId.
	 *
	 * @param dmsId
	 *            the dmsId to set
	 */
	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * Setter method for container.
	 *
	 * @param container
	 *            the container to set
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TaskDefinitionRef> getTasks() {
		if (tasks == null) {
			tasks = new LinkedList<TaskDefinitionRef>();
		}
		return tasks;
	}

	/**
	 * Setter method for tasks.
	 *
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(List<TaskDefinitionRef> tasks) {
		this.tasks = tasks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public WorkflowDefinitionImpl mergeFrom(WorkflowDefinitionImpl source) {
		super.mergeFrom(source);

		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		lastModifiedDate = MergeHelper
				.replaceIfNull(lastModifiedDate, source.getLastModifiedDate());
		creationDate = MergeHelper.replaceIfNull(creationDate, source.getCreationDate());
		container = MergeHelper.replaceIfNull(container, source.getContainer());

		tasks = MergeHelper.mergeLists(MergeHelper.convertToMergable(tasks),
				MergeHelper.convertToMergable(source.getTasks()),
				CmfMergeableFactory.TASK_DEFINITION_REF);
		MergeHelper.mergeLists(MergeHelper.convertToMergable(getTransitions()),
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
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();

		if (tasks != null) {
			for (TaskDefinitionRef definitionRef : tasks) {
				TaskDefinitionRefImpl definitionRefImpl = (TaskDefinitionRefImpl) definitionRef;
				definitionRefImpl.setWorkflowDefinition(this);
				definitionRefImpl.initBidirection();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return null;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkflowDefinitionImpl [identifier=");
		builder.append(identifier);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", parentDefinitionId=");
		builder.append(parentDefinitionId);
		builder.append(", lastModifiedDate=");
		builder.append(lastModifiedDate);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", container=");
		builder.append(container);
		builder.append(", super=");
		builder.append(super.toString());
		builder.append(", tasks=");
		builder.append(tasks);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for abstract.
	 *
	 * @return the abstract
	 */
	public Boolean getAbstract() {
		return Abstract;
	}

	/**
	 * Setter method for abstract.
	 *
	 * @param _abstract
	 *            the abstract to set
	 */
	public void setAbstract(Boolean _abstract) {
		Abstract = _abstract;
	}

	@Override
	public boolean isAbstract() {
		Boolean a = getAbstract();
		if (a != null) {
			return a;
		}
		return false;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || !getTasks().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		Node find = PathHelper.find(getTasks(), name);
		if (find == null) {
			find = super.getChild(name);
		}
		return find;
	}

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
	 * Getter method for transitions.
	 * 
	 * @return the transitions
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
