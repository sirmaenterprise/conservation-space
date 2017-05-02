package com.sirma.itt.cmf.beans.definitions.impl;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.util.CmfMergeableFactory;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.model.AllowedChildDefinitionImpl;
import com.sirma.itt.seip.definition.model.BaseRegionDefinition;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.model.StateTransitionImpl;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.AllowedChildDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.Mergeable;
import com.sirma.itt.seip.domain.definition.MergeableTopLevelDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.WritablePropertyDefinition;

/**
 * Default implementation for {@link GenericDefinition} interface. The implementation could handle parent and reference
 * definition merging.
 *
 * @author BBonev
 */
public class GenericDefinitionImpl extends BaseRegionDefinition<GenericDefinitionImpl>
		implements GenericDefinition, MergeableTopLevelDefinition<GenericDefinitionImpl>, Cloneable {
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
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<>();

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
	@SuppressWarnings("findbugs:NM_FIELD_NAMING_CONVENTION")
	protected Boolean Abstract = Boolean.FALSE;
	/** The transitions. */
	@Tag(11)
	protected List<TransitionDefinition> transitions = new LinkedList<>();
	/** The state transitions. */
	@Tag(12)
	protected List<StateTransition> stateTransitions = new LinkedList<>();
	/** The sub definitions. */
	@Tag(14)
	protected List<GenericDefinition> subDefinitions = new LinkedList<>();
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
		if (!isSealed()) {
			parentDefinitionId = value;
		}
	}

	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		if (!isSealed()) {
			this.dmsId = dmsId;
		}
	}

	@Override
	public PathElement getParentElement() {
		return getParentDefinition();
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		if (!isSealed()) {
			this.container = container;
		}
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
		if (!isSealed()) {
			Abstract = _abstract;
		}
	}

	@Override
	public boolean isAbstract() {
		Boolean a = getAbstract();
		if (a != null) {
			return a.booleanValue();
		}
		return false;
	}

	@Override
	public boolean hasChildren() {
		return !getSubDefinitions().isEmpty() || !getRegions().isEmpty() || super.hasChildren();
	}

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

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public void setRevision(Long revision) {
		if (!isSealed()) {
			this.revision = revision;
		}
	}

	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		if (allowedChildren == null) {
			allowedChildren = new LinkedList<>();
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
		if (!isSealed()) {
			this.allowedChildren = allowedChildren;
		}
	}

	@Override
	public List<TransitionDefinition> getTransitions() {
		if (transitions == null) {
			transitions = new LinkedList<>();
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
		if (!isSealed()) {
			this.transitions = transitions;
		}
	}

	@Override
	public List<StateTransition> getStateTransitions() {
		if (stateTransitions == null) {
			stateTransitions = new LinkedList<>();
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
		if (!isSealed()) {
			this.stateTransitions = stateTransitions;
		}
	}

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
		if (!isSealed()) {
			this.type = type;
		}
	}

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
		if (!isSealed()) {
			this.subDefinitions = subDefinitions;
		}
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
		if (!isSealed()) {
			this.lastModifiedDate = lastModifiedDate;
		}
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
		if (!isSealed()) {
			this.creationDate = creationDate;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public GenericDefinitionImpl mergeFrom(GenericDefinitionImpl source) {
		super.mergeFrom(source);

		// update local fields using the source fields regardless of the source field position
		fieldsStream().forEach(field -> mergeFields(field, source));

		creationDate = MergeHelper.replaceIfNull(creationDate, source.getCreationDate());
		type = MergeHelper.replaceIfNull(type, source.getType());
		lastModifiedDate = MergeHelper.replaceIfNull(lastModifiedDate, source.getLastModifiedDate());
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		Abstract = MergeHelper.replaceIfNull(Abstract, source.isAbstract());

		subDefinitions = MergeHelper.mergeLists(MergeHelper.convertToMergable(subDefinitions),
				MergeHelper.convertToMergable(source.getSubDefinitions()), CmfMergeableFactory.GENERIC_DEFINITION);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getTransitions()),
				MergeHelper.convertToMergable(source.getTransitions()), EmfMergeableFactory.TRANSITION_DEFINITION);

		if (getAllowedChildren().isEmpty() && !source.getAllowedChildren().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getAllowedChildren()),
					MergeHelper.convertToMergable(source.getAllowedChildren()), EmfMergeableFactory.ALLOWED_CHILDREN);
		}
		if (getStateTransitions().isEmpty() && !source.getStateTransitions().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getStateTransitions()),
					MergeHelper.convertToMergable(source.getStateTransitions()), EmfMergeableFactory.STATE_TRANSITION);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	private static void mergeFields(PropertyDefinition field, GenericDefinitionImpl source) {
		Optional<PropertyDefinition> optional = source.getField(field.getIdentifier());
		if (optional.isPresent()) {
			((Mergeable<PropertyDefinition>) field).mergeFrom(optional.get());
		}
	}

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
		if (!isSealed()) {
			this.parentDefinition = parentDefinition;
		}
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
		if (!isSealed()) {
			this.referenceDefinition = referenceDefinition;
			mergeTemplate(referenceDefinition);
		}
	}

	/**
	 * Merge template definition to the local definition. The method copies the properties from the template to the
	 * local level
	 *
	 * @param genericDefinition
	 *            the document definition impl
	 */
	private void mergeTemplate(GenericDefinition genericDefinition) {
		if (genericDefinition instanceof GenericDefinitionImpl) {
			GenericDefinitionImpl definitionImpl = (GenericDefinitionImpl) genericDefinition;
			setExpression(MergeHelper.replaceIfNull(getExpression(), definitionImpl.getExpression()));

			MergeHelper.mergeTemplate(this, definitionImpl);
		}
	}

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
		if (!isSealed()) {
			this.referenceId = referenceId;
		}
	}

	@Override
	public String getPurpose() {
		return purpose;
	}

	@Override
	public void setPurpose(String purpose) {
		if (!isSealed()) {
			this.purpose = purpose;
		}
	}

	@Override
	public GenericDefinitionImpl clone() {
		GenericDefinitionImpl copy = new GenericDefinitionImpl();
		copy.Abstract = Abstract;
		copy.container = container;
		copy.expression = expression;
		copy.identifier = identifier;
		copy.parentDefinitionId = parentDefinitionId;
		copy.purpose = purpose;
		copy.referenceId = referenceId;
		copy.revision = revision;
		copy.type = type;

		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition).cloneProxy();
			copy.getFields().add(clone);
		}
		for (RegionDefinition regionDefinition : getRegions()) {
			RegionDefinitionImpl clone = ((RegionDefinitionImpl) regionDefinition).clone();
			copy.getRegions().add(clone);
		}
		for (TransitionDefinition transitionDefinition : getTransitions()) {
			TransitionDefinitionImpl clone = ((TransitionDefinitionImpl) transitionDefinition).clone();
			copy.getTransitions().add(clone);
		}
		for (StateTransition stateTransition : getStateTransitions()) {
			StateTransitionImpl clone = ((StateTransitionImpl) stateTransition).clone();
			copy.getStateTransitions().add(clone);
		}
		for (GenericDefinition stateTransition : getSubDefinitions()) {
			GenericDefinitionImpl clone = ((GenericDefinitionImpl) stateTransition).clone();
			copy.getSubDefinitions().add(clone);
		}

		copy.initBidirection();
		return copy;
	}

	@Override
	public void seal() {
		if (!isSealed()) {

			allowedChildren = Collections.unmodifiableList(Sealable.seal(getAllowedChildren()));
			stateTransitions = Collections.unmodifiableList(Sealable.seal(getStateTransitions()));
			subDefinitions = Collections.unmodifiableList(Sealable.seal(getSubDefinitions()));
			transitions = Collections.unmodifiableList(Sealable.seal(getTransitions()));

			super.seal();
		}
	}
}
