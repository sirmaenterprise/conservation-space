package com.sirma.itt.pm.domain.definitions.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;

/**
 * Default project definition implementations.
 * 
 * @author BBonev
 */
public class ProjectDefinitionImpl extends BaseRegionDefinition<ProjectDefinitionImpl> implements
		ProjectDefinition, MergeableTopLevelDefinition<ProjectDefinitionImpl> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6061811109359868127L;

	/** The dms id. */
	@Tag(1)
	protected String dmsId;
	/** The parent definition id. */
	@Tag(2)
	protected String parentDefinitionId;
	/** The revision. */
	@Tag(3)
	protected Long revision;
	/** The container. */
	@Tag(4)
	protected String container;
	/** The last modified date. */
	@Tag(5)
	protected Date lastModifiedDate;
	/** The creation date. */
	@Tag(6)
	protected Date creationDate;
	/** The Abstract. */
	@Tag(7)
	protected Boolean Abstract = Boolean.FALSE;
	/** The transitions. */
	@Tag(8)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();
	/** The state transitions. */
	@Tag(9)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();
	/** The allowed children. */
	@Tag(10)
	protected List<AllowedChildDefinition> allowedChildren;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		for (TransitionDefinition transitionDefinition : getTransitions()) {
			TransitionDefinitionImpl templateImpl = (TransitionDefinitionImpl) transitionDefinition;
			templateImpl.setOwningDefinition(this);
			templateImpl.initBidirection();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ProjectDefinitionImpl mergeFrom(ProjectDefinitionImpl source) {
		super.mergeFrom(source);
		revision = MergeHelper.replaceIfNull(revision, source.revision);
		Abstract = MergeHelper.replaceIfNull(Abstract, source.Abstract);
		container = MergeHelper.replaceIfNull(container, source.container);
		parentDefinitionId = MergeHelper.replaceIfNull(parentDefinitionId,
				source.parentDefinitionId);
		dmsId = MergeHelper.replaceIfNull(dmsId, source.dmsId);
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
	public String getParentDefinitionId() {
		return parentDefinitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbstract() {
		if (Abstract != null) {
			return Abstract.booleanValue();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
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
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getCreationDate() {
		return creationDate;
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
	 * @param abstract1
	 *            the abstract to set
	 */
	public void setAbstract(Boolean abstract1) {
		Abstract = abstract1;
	}

	/**
	 * Setter method for lastNodifiedDate.
	 *
	 * @param lastModifiedDate
	 *            the lastNodifiedDate to set
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
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
	 * Setter method for parentDefinitionId.
	 *
	 * @param parentDefinitionId
	 *            the parentDefinitionId to set
	 */
	public void setParentDefinitionId(String parentDefinitionId) {
		this.parentDefinitionId = parentDefinitionId;
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

}
