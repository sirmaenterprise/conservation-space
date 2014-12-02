package com.sirma.itt.cmf.beans.definitions.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default implementation for {@link GenericDefinition} interface. The implementation could handle
 * parent and reference definition merging.
 *
 * @author BBonev
 */
public class GenericDefinitionImpl extends BaseRegionDefinition<GenericDefinitionImpl> implements
		GenericDefinition, BidirectionalMapping, MergeableTopLevelDefinition<GenericDefinitionImpl> {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8421832475725154466L;
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

	/** Versionable field for determining case revision. */
	@Tag(3)
	protected Long revision;

	/** The type. */
	@Tag(4)
	protected String type;

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
	/** The sub definitions. */
	@Tag(14)
	protected List<GenericDefinition> subDefinitions = new LinkedList<GenericDefinition>();
	/** The parent definition. */
	private transient GenericDefinition parentDefinition;
	/** The reference definition. */
	private transient GenericDefinition referenceDefinition;
	/** The reference id. */
	@Tag(15)
	protected String referenceId;
	/** The purpose. */
	@Tag(16)
	protected String purpose;

	/**
	 * Inits the bidirection.
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();
		if (getSubDefinitions() != null) {
			for (GenericDefinition definition : getSubDefinitions()) {
				GenericDefinitionImpl impl = (GenericDefinitionImpl) definition;
				impl.setParentDefinition(this);
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
	 * Gets the parent element.
	 *
	 * @return the parent element
	 */
	@Override
	public PathElement getParentElement() {
		return getParentDefinition();
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
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
	public boolean isAbstract() {
		Boolean a = getAbstract();
		if (a != null) {
			return a;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return !getSubDefinitions().isEmpty() || !getRegions().isEmpty() || super.hasChildren();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		Node find = PathHelper.find(getSubDefinitions(), name);
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
	 * {@inheritDoc}
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
	 * @param stateTransitions
	 *            the stateTransitions to set
	 */
	public void setStateTransitions(List<StateTransition> stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GenericDefinition> getSubDefinitions() {
		if (subDefinitions == null) {
			subDefinitions = new LinkedList<>();
		}
		return subDefinitions;
	}

	/**
	 * Sets the sub definitions.
	 *
	 * @param subDefinitions
	 *            the new sub definitions
	 */
	public void setSubDefinitions(List<GenericDefinition> subDefinitions) {
		this.subDefinitions = subDefinitions;
	}

	/**
	 * Getter method for lastModifiedDate.
	 *
	 * @return the lastModifiedDate
	 */
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
	 * Getter method for creationDate.
	 *
	 * @return the creationDate
	 */
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
	@SuppressWarnings("unchecked")
	public GenericDefinitionImpl mergeFrom(GenericDefinitionImpl source) {
		super.mergeFrom(source);

		creationDate = MergeHelper.replaceIfNull(creationDate, source.getCreationDate());
		type = MergeHelper.replaceIfNull(type, source.getType());
		lastModifiedDate = MergeHelper
				.replaceIfNull(lastModifiedDate, source.getLastModifiedDate());
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		Abstract = MergeHelper.replaceIfNull(Abstract, source.isAbstract());

		subDefinitions = MergeHelper.mergeLists(MergeHelper.convertToMergable(subDefinitions),
				MergeHelper.convertToMergable(source.getSubDefinitions()),
				CmfMergeableFactory.GENERIC_DEFINITION);

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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GenericDefinitionImpl [identifier=");
		builder.append(identifier);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", parentDefinitionId=");
		builder.append(parentDefinitionId);
		builder.append(", referenceId=");
		builder.append(referenceId);
		builder.append(", purpose=");
		builder.append(purpose);
		builder.append(", Abstract=");
		builder.append(Abstract);
		builder.append(", container=");
		builder.append(container);
		builder.append(", transitions=");
		builder.append(transitions);
		builder.append(", stateTransitions=");
		builder.append(stateTransitions);
		builder.append(", allowedChildren=");
		builder.append(allowedChildren);
		builder.append(", fields=");
		builder.append(fields);
		builder.append(", regions=");
		builder.append(regions);
		builder.append(", subDefinitions=");
		builder.append(subDefinitions);
		builder.append(", expression=");
		builder.append(expression);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", hash=");
		builder.append(hash);
		builder.append(", lastModifiedDate=");
		builder.append(lastModifiedDate);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for parentDefinition.
	 *
	 * @return the parentDefinition
	 */
	public GenericDefinition getParentDefinition() {
		return parentDefinition;
	}

	/**
	 * Setter method for parentDefinition.
	 *
	 * @param parentDefinition
	 *            the parentDefinition to set
	 */
	public void setParentDefinition(GenericDefinition parentDefinition) {
		this.parentDefinition = parentDefinition;
	}

	/**
	 * Getter method for referenceDefinition.
	 *
	 * @return the referenceDefinition
	 */
	public GenericDefinition getReferenceDefinition() {
		return referenceDefinition;
	}

	/**
	 * Setter method for referenceDefinition.
	 *
	 * @param referenceDefinition
	 *            the referenceDefinition to set
	 */
	public void setReferenceDefinition(GenericDefinition referenceDefinition) {
		this.referenceDefinition = referenceDefinition;
		mergeTemplate(referenceDefinition);
	}

	/**
	 * Merge template definition to the local definition. The method copies the properties from the
	 * template to the local level
	 *
	 * @param genericDefinition
	 *            the document definition impl
	 */
	private void mergeTemplate(GenericDefinition genericDefinition) {
		if (genericDefinition instanceof GenericDefinitionImpl) {
			GenericDefinitionImpl definitionImpl = (GenericDefinitionImpl) genericDefinition;
			setExpression(MergeHelper
					.replaceIfNull(getExpression(), definitionImpl.getExpression()));

			MergeHelper.mergeTemplate(this, definitionImpl);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Sets the reference id.
	 *
	 * @param referenceId
	 *            the new reference id
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
}
