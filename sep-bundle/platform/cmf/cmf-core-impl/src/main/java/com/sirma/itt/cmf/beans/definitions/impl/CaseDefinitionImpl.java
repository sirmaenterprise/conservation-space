package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default {@link CaseDefinition} implementation.
 *
 * @author BBonev
 */
public class CaseDefinitionImpl extends BaseRegionDefinition<CaseDefinitionImpl> implements
		Serializable, CaseDefinition, BidirectionalMapping, Mergeable<CaseDefinitionImpl>,
		MergeableTopLevelDefinition<CaseDefinitionImpl> {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4833910638626518171L;
	/**
	 * The date on which the entity (e.g. Registration) was last modified. <br/>
	 */
	@Tag(1)
	protected Date lastModifiedDate;
	/**
	 * The date object was created. <br/>
	 */
	@Tag(2)
	protected Date creationDate;
	/**
	 * Versionable field for determining case revision <br/>
	 */
	@Tag(3)
	protected Long revision;

	/**
	 * The case id. <b>NOTE</b>: This value is moved to the parent class, but cannot be removed for
	 * backward compatibility.
	 */
	@Tag(4)
	protected String oldidentifier;

	/** The section defs. */
	@Tag(5)
	protected List<SectionDefinition> sectionDefinitions = new LinkedList<SectionDefinition>();

	/** The workflow ids. */
	@Tag(6)
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<AllowedChildDefinition>();

	/** The parent case id. */
	@Tag(7)
	protected String parentDefinitionId;
	/** The dms id. */
	@Tag(8)
	protected String dmsId;

	/** The container. */
	@Tag(9)
	protected String container;

	/** The Abstract. */
	@Tag(10)
	protected Boolean Abstract = Boolean.FALSE;
	/** The transitions. */
	@Tag(11)
	protected List<TransitionDefinition> transitions = new LinkedList<TransitionDefinition>();
	/** The state transitions. */
	@Tag(12)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();

	/**
	 * Inits the bidirection.
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		if (sectionDefinitions != null) {
			for (SectionDefinition definition : sectionDefinitions) {
				SectionDefinitionImpl impl = (SectionDefinitionImpl) definition;
				impl.setCaseDefinition(this);
				impl.initBidirection();
			}
		}
		if (getAllowedChildren() != null) {
			for (AllowedChildDefinition definition : getAllowedChildren()) {
				AllowedChildDefinitionImpl impl = (AllowedChildDefinitionImpl) definition;
				impl.setParentDefinition(this);
				impl.initBidirection();
			}
		}
	}

	/**
	 * Gets the value of the sectionDefinitions property.
	 *
	 * @return the section defs possible object is {@link List<SectionDef> }
	 */
	@Override
	public List<SectionDefinition> getSectionDefinitions() {
		if (sectionDefinitions == null) {
			sectionDefinitions = new LinkedList<SectionDefinition>();
		}
		return sectionDefinitions;
	}

	/**
	 * Sets the value of the sectionDefinitions property.
	 *
	 * @param value
	 *            allowed object is {@link List<SectionDef> }
	 */
	public void setSectionDefinitions(List<SectionDefinition> value) {
		sectionDefinitions = value;
	}

	/**
	 * Gets the value of the parentDefinitionId property.
	 *
	 * @return the parent case id possible object is {@link String }
	 */
	@Override
	public String getParentDefinitionId() {
		return parentDefinitionId;
	}

	/**
	 * Sets the value of the parentDefinitionId property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setParentDefinitionId(String value) {
		parentDefinitionId = value;
	}

	/**
	 * Gets the last modified date.
	 *
	 * @return the last modified date
	 */
	@Override
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	/**
	 * Sets the last modified date.
	 *
	 * @param lastModifiedDate
	 *            the new last modified date
	 */
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * Gets the creation date.
	 *
	 * @return the creation date
	 */
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets the creation date.
	 *
	 * @param creationDate
	 *            the new creation date
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Gets the revision.
	 *
	 * @return the revision
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * Sets the revision.
	 *
	 * @param revision
	 *            the new revision
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * Getter method for dmsId.
	 *
	 * @return the dmsId
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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CaseDefinition [super=");
		builder.append(super.toString());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", parentDefinitionId=");
		builder.append(parentDefinitionId);
		builder.append(", abstract=");
		builder.append(Abstract);
		builder.append(", lastModifiedDate=");
		builder.append(lastModifiedDate);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", container=");
		builder.append(container);
		builder.append(", sectionDefinitions=");
		builder.append(sectionDefinitions);
		builder.append(", workflowIds=");
		builder.append(getAllowedChildren());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	/**
	 * Getter method for container.
	 *
	 * @return the container
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public CaseDefinitionImpl mergeFrom(CaseDefinitionImpl source) {
		super.mergeFrom(source);
		creationDate = MergeHelper.replaceIfNull(creationDate, source.getCreationDate());
		lastModifiedDate = MergeHelper
				.replaceIfNull(lastModifiedDate, source.getLastModifiedDate());
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		Abstract = MergeHelper.replaceIfNull(Abstract, source.isAbstract());

		sectionDefinitions = MergeHelper.mergeLists(
				MergeHelper.convertToMergable(sectionDefinitions),
				MergeHelper.convertToMergable(source.getSectionDefinitions()),
				CmfMergeableFactory.SECTION_DEFINITION);
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
		return !getSectionDefinitions().isEmpty() || !getRegions().isEmpty() || super.hasChildren();
	}

	@Override
	public Node getChild(String name) {
		Node find = PathHelper.find(getSectionDefinitions(), name);
		if (find == null) {
			find = super.getChild(name);
			if (find == null) {
				for (RegionDefinition regionDefinition : getRegions()) {
					if (regionDefinition.hasChildren()) {
						find = regionDefinition.getChild(name);
						if (find != null) {
							break;
						}
					}
				}
			}
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
	 * @param allowedChildren
	 *            the allowedChildren to set
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
	 * @param stateTransitions the stateTransitions to set
	 */
	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

}
