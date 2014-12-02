
package com.sirma.itt.cmf.beans.definitions.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.MergeableTopLevelDefinition;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransition;

/**
 * Default implementation of the task definition template.
 * 
 * @author BBonev
 */
public class TaskDefinitionTemplateImpl extends BaseRegionDefinition<TaskDefinitionTemplateImpl>
		implements Serializable, BidirectionalMapping,
		MergeableTopLevelDefinition<TaskDefinitionTemplateImpl>, TaskDefinitionTemplate, Cloneable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6505775715118086538L;
	/** The task id. */
	@Tag(1)
	protected String identifier;
	/** The task id. */
	@Tag(2)
	protected String parentTaskId;
	/** The transitions. */
	@Tag(3)
	protected List<TransitionDefinition> transitions;
	/** The container. */
	@Tag(4)
	protected String container;
	/** The standalone. */
	@Tag(5)
	protected Boolean standalone = false;
	/** The dms type. */
	@Tag(6)
	protected String dmsType;
	/** The reference id. It's used only to copy the value from the template to top level definition */
	private transient String referenceId;
	/** The allowed children. */
	@Tag(7)
	protected List<AllowedChildDefinition> allowedChildren;
	/** The state transitions. */
	@Tag(8)
	protected List<StateTransition> stateTransitions = new LinkedList<StateTransition>();
	/** The Abstract. It's used only to copy the value from the template to top level definition */
	private transient Boolean Abstract = Boolean.FALSE;

	/**
	* {@inheritDoc}
	*/
	@Override
	public String getParentTaskId() {
		return parentTaskId;
	}

	/**
	 * Setter method for parentTaskId.
	 *
	 * @param parentTaskId
	 *            the parentTaskId to set
	 */
	public void setParentTaskId(String parentTaskId) {
		this.parentTaskId = parentTaskId;
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

	@Override
	@SuppressWarnings("unchecked")
	public TaskDefinitionTemplateImpl mergeFrom(TaskDefinitionTemplateImpl source) {
		super.mergeFrom(source);

		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		parentTaskId = MergeHelper.replaceIfNull(parentTaskId, source.getParentTaskId());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		dmsType = MergeHelper.replaceIfNull(dmsType, source.getDmsType());
		Abstract = MergeHelper.replaceIfNull(Abstract, source.isAbstract());

		transitions = MergeHelper.mergeLists(MergeHelper.convertToMergable(transitions),
				MergeHelper.convertToMergable(source.getTransitions()),
				EmfMergeableFactory.TRANSITION_DEFINITION_TEMPLATE);
		if (getStateTransitions().isEmpty() && !source.getStateTransitions().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getStateTransitions()),
					MergeHelper.convertToMergable(source.getStateTransitions()),
					EmfMergeableFactory.STATE_TRANSITION);
		}
		if (getAllowedChildren().isEmpty() && !source.getAllowedChildren().isEmpty()) {
			MergeHelper.mergeLists(MergeHelper.convertToMergable(getAllowedChildren()),
					MergeHelper.convertToMergable(source.getAllowedChildren()),
					EmfMergeableFactory.ALLOWED_CHILDREN);
		}
		initBidirection();
		return this;
	}

	@Override
	public void initBidirection() {
		super.initBidirection();

		if (transitions != null) {
			for (TransitionDefinition transitionDefinition : transitions) {
				TransitionDefinitionImpl templateImpl = (TransitionDefinitionImpl) transitionDefinition;
				templateImpl.setOwningDefinition(this);
				templateImpl.initBidirection();
			}
		}
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskDefinitionTemplateImpl [identifier=");
		builder.append(identifier);
		builder.append(", parentTaskId=");
		builder.append(parentTaskId);
		builder.append(", container=");
		builder.append(container);
		builder.append(", Abstract=");
		builder.append(Abstract);
		builder.append(", transitions=");
		builder.append(transitions);
		builder.append(", super=");
		builder.append(super.toString());
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
	public String getParentDefinitionId() {
		return getParentTaskId();
	}

	@Override
	public String getDmsId() {
		return null;
	}

	@Override
	public void setDmsId(String dmsId) {

	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		super.setIdentifier(identifier);
	}

	@Override
	public TaskDefinitionTemplateImpl clone() {
		TaskDefinitionTemplateImpl copy = new TaskDefinitionTemplateImpl();
		copy.identifier = identifier;
		copy.parentTaskId = parentTaskId;
		copy.container = container;
		copy.standalone = standalone;
		copy.dmsType = dmsType;
		copy.Abstract = Abstract;

		for (PropertyDefinition propertyDefinition : getFields()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) propertyDefinition).cloneProxy();
			copy.getFields().add(clone);
		}
		copy.setRegions(new LinkedList<RegionDefinition>());
		for (RegionDefinition regionDefinition : getRegions()) {
			RegionDefinitionImpl clone = ((RegionDefinitionImpl) regionDefinition).clone();
			copy.getRegions().add(clone);
		}
		copy.transitions = new LinkedList<TransitionDefinition>();
		for (TransitionDefinition transitionDefinition : getTransitions()) {
			TransitionDefinitionImpl clone = ((TransitionDefinitionImpl) transitionDefinition)
					.clone();
			copy.transitions.add(clone);
		}

		copy.initBidirection();
		return copy;
	}

	@Override
	public Long getRevision() {
		return 0L;
	}

	@Override
	public void setRevision(Long revision) {
		// not used
	}

	@Override
	@SuppressWarnings("unchecked")
	protected MergeableInstanceFactory<Mergeable<Object>> getRegionFactory() {
		return EmfMergeableFactory.REGION_DEFINITION_TEMPLATE;
	}

	/**
	 * Getter method for standalone.
	 * 
	 * @return the standalone
	 */
	public Boolean getStandalone() {
		if (standalone == null) {
			return false;
		}
		return standalone;
	}

	/**
	 * Setter method for standalone.
	 * 
	 * @param standalone
	 *            the standalone to set
	 */
	public void setStandalone(Boolean standalone) {
		this.standalone = standalone;
	}

	/**
	 * Getter method for dmsType.
	 *
	 * @return the dmsType
	 */
	public String getDmsType() {
		return dmsType;
	}

	/**
	 * Setter method for dmsType.
	 *
	 * @param dmsType the dmsType to set
	 */
	public void setDmsType(String dmsType) {
		this.dmsType = dmsType;
	}

	/**
	 * Getter method for referenceId.
	 * 
	 * @return the referenceId
	 */
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Setter method for referenceId.
	 * 
	 * @param referenceId
	 *            the referenceId to set
	 */
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
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
	 * @param allowedChildren
	 *            the allowedChildren to set
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
