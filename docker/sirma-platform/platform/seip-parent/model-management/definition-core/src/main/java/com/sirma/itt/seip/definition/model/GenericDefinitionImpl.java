package com.sirma.itt.seip.definition.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.MergeableTopLevelDefinition;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Default implementation for {@link GenericDefinition} interface. The implementation could handle parent and reference
 * definition merging.
 *
 * @author BBonev
 */
public class GenericDefinitionImpl extends BaseRegionDefinition<GenericDefinitionImpl> implements GenericDefinition,
		MergeableTopLevelDefinition<GenericDefinitionImpl>, Copyable<GenericDefinitionImpl> {

	private static final long serialVersionUID = 8421832475725154466L;

	@Tag(1)
	protected Date lastModifiedDate;

	@Tag(2)
	protected Date creationDate;

	@Tag(3)
	protected Long revision;

	@Tag(4)
	protected String type;

	@Tag(6)
	protected List<AllowedChildDefinition> allowedChildren = new LinkedList<>();

	@Tag(7)
	protected String parentDefinitionId;

	@Tag(8)
	protected String dmsId;

	@Tag(9)
	protected String container;

	@Tag(10)
	@SuppressWarnings("squid:S00116")
	protected Boolean Abstract = Boolean.FALSE;

	@Tag(11)
	protected List<TransitionDefinition> transitions = new LinkedList<>();

	@Tag(12)
	protected List<StateTransition> stateTransitions = new LinkedList<>();

	@Tag(14)
	protected List<GenericDefinition> subDefinitions = new LinkedList<>();

	private transient GenericDefinition parentDefinition;

	@Tag(15)
	protected String referenceId;

	@Tag(16)
	protected String purpose;

	@Tag(17)
	protected List<TransitionGroupDefinition> transitionGroups = new LinkedList<>();

	@Tag(18)
	protected List<PropertyDefinition> configurations;

	// Used internally to keep the original source of the definition
	private transient String sourceFile;
	private transient String sourceContent;
	private transient String previousIdentifier;

	@Override
	public void initBidirection() {
		super.initBidirection();

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
	 * @param isAbstract the abstract to set
	 */
	public void setAbstract(Boolean isAbstract) {
		if (!isSealed()) {
			Abstract = isAbstract;
		}
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
		return !getRegions().isEmpty() || super.hasChildren();
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
	public List<TransitionGroupDefinition> getTransitionGroups() {
		if (transitionGroups == null) {
			transitionGroups = new LinkedList<>();
		}
		return transitionGroups;
	}

	/**
	 * Setter method for transition groups.
	 *
	 * @param transitionGroups
	 *            the transition groups to set
	 */
	public void setTransitionGroups(List<TransitionGroupDefinition> transitionGroups) {
		if (!isSealed()) {
			this.transitionGroups = transitionGroups;
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
	public List<PropertyDefinition> getConfigurations() {
		if (configurations == null) {
			configurations = new LinkedList<>();
		}

		return configurations;
	}

	public void setConfigurations(List<PropertyDefinition> configurations) {
		if (!isSealed()) {
			this.configurations = configurations;
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
	 * @param lastModifiedDate the lastModifiedDate to set
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
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		expression = MergeHelper.replaceIfNull(expression, source.getExpression());

		mergeFieldsAndRegions(source);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getConfigurations()),
				MergeHelper.convertToMergable(source.getConfigurations()), EmfMergeableFactory.FIELD_DEFINITION);

		creationDate = MergeHelper.replaceIfNull(creationDate, source.getCreationDate());
		type = MergeHelper.replaceIfNull(type, source.getType());
		lastModifiedDate = MergeHelper.replaceIfNull(lastModifiedDate, source.getLastModifiedDate());
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		identifier = MergeHelper.replaceIfNull(identifier, source.getIdentifier());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		purpose = MergeHelper.replaceIfNull(purpose, source.getPurpose());
		Abstract = MergeHelper.replaceIfNull(Abstract, source.isAbstract());

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getTransitions()),
				MergeHelper.convertToMergable(source.getTransitions()), EmfMergeableFactory.TRANSITION_DEFINITION);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getTransitionGroups()),
				MergeHelper.convertToMergable(source.getTransitionGroups()),
				EmfMergeableFactory.TRANSITION_GROUP_DEFINITION);

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

	private void mergeFieldsAndRegions(GenericDefinitionImpl parent) {
		// The manual merging here is needed to avoid issues from moving fields in and out of regions

		Map<String, PropertyDefinition> parentFields = toMap(parent.getFields());
		Map<String, PropertyDefinition> initialFields = toMap(getFields());

		mergeFields(parent, parentFields, initialFields);
		mergeRegions(parent, parentFields, initialFields);
	}

	private void mergeFields(GenericDefinitionImpl parent, Map<String, PropertyDefinition> parentFields,
			Map<String, PropertyDefinition> initialFields) {

		// Lookup maps
		Map<String, PropertyDefinition> parentRegionFields = collectRegionFields(parent);
		Map<String, PropertyDefinition> currentRegionFields = collectRegionFields(this);

		Map<String, PropertyDefinition> parentFieldsForMerge = new LinkedHashMap<>(parentFields);

		// Omit all fields that have been moved to a region in the current definition
		parentFieldsForMerge.values().removeIf(parentField -> currentRegionFields.containsKey(parentField.getIdentifier()));

		// Insert fields from parent regions in case some field have been moved out of a region
		parentRegionFields.values().forEach(parentRegionField -> {
			if (initialFields.containsKey(parentRegionField.getIdentifier())) {
				parentFieldsForMerge.put(parentRegionField.getIdentifier(), parentRegionField);
			}
		});

		// Merge the applicable fields
		MergeHelper.copyOrMergeLists(MergeHelper.convertToMergable(getFields()),
				MergeHelper.convertToMergable(new LinkedList<>(parentFieldsForMerge.values())));

		DefinitionUtil.sort(getFields());
	}

	private void mergeRegions(GenericDefinitionImpl parent, Map<String, PropertyDefinition> parentFields,
			Map<String, PropertyDefinition> initialFields) {

		// Mapping to determine if some field is moved to another region
		Map<String, String> fieldToRegion = collectFieldToRegion(this);

		// Merge everything via RegionDefinition's mergeFrom
		MergeHelper.mergeLists(MergeHelper.convertToMergable(getRegions()), MergeHelper.convertToMergable(parent.getRegions()),
				getRegionFactory());

		// And then clean up duplications
		getRegions().forEach(region -> {
			List<PropertyDefinition> regionFields = region.getFields();

			// Remove those region fields that have been defined as root fields in the current definition
			regionFields.removeIf(field -> initialFields.containsKey(field.getIdentifier()));

			// Remove those region fields that have been moved from one region to another
			regionFields.removeIf(field -> fieldToRegion.containsKey(field.getIdentifier()) && !fieldToRegion.get(field.getIdentifier())
					.equals(region.getIdentifier()));

			// Merge those fields that have been moved in a region (mergeLists cannot handle it)
			regionFields.forEach(regionField -> {
				String regionFieldId = regionField.getIdentifier();
				if (parentFields.containsKey(regionFieldId)) {
					((Mergeable) regionField).mergeFrom(parentFields.get(regionFieldId));
				}
			});

			DefinitionUtil.sort(region.getFields());
		});
	}

	private static Map<String, PropertyDefinition> collectRegionFields(GenericDefinition definition) {
		return definition.getRegions().stream()
				.flatMap(region -> region.getFields().stream())
				.collect(propertyCollector());
	}

	private static Map<String, String> collectFieldToRegion(GenericDefinition definition) {
		Map<String, String> fieldToRegion = new HashMap<>();
		definition.getRegions()
				.forEach(region -> region.getFields()
						.forEach(property -> fieldToRegion.put(property.getIdentifier(), region.getIdentifier())));
		return fieldToRegion;
	}

	private static Map<String, PropertyDefinition> toMap(List<PropertyDefinition> fields) {
		return fields.stream().collect(propertyCollector());
	}

	private static Collector<PropertyDefinition, ?, Map<String, PropertyDefinition>> propertyCollector() {
		return Collectors.toMap(PropertyDefinition::getIdentifier, Function.identity(), (p1, p2) -> p2, LinkedHashMap::new);
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
		builder.append(", configurations=");
		builder.append(configurations);
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
	public GenericDefinitionImpl createCopy() {
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
		for (PropertyDefinition configuration : getConfigurations()) {
			WritablePropertyDefinition clone = ((PropertyDefinitionProxy) configuration).cloneProxy();
			copy.getConfigurations().add(clone);
		}
		for (RegionDefinition regionDefinition : getRegions()) {
			RegionDefinitionImpl clone = ((RegionDefinitionImpl) regionDefinition).createCopy();
			copy.getRegions().add(clone);
		}
		for (TransitionDefinition transitionDefinition : getTransitions()) {
			TransitionDefinitionImpl clone = ((TransitionDefinitionImpl) transitionDefinition).createCopy();
			copy.getTransitions().add(clone);
		}
		for (TransitionGroupDefinition transitionGroupDefinition : getTransitionGroups()) {
			TransitionGroupDefinitionImpl clone = ((TransitionGroupDefinitionImpl) transitionGroupDefinition)
					.createCopy();
			copy.getTransitionGroups().add(clone);
		}
		for (StateTransition stateTransition : getStateTransitions()) {
			StateTransitionImpl clone = ((StateTransitionImpl) stateTransition).createCopy();
			copy.getStateTransitions().add(clone);
		}

		copy.initBidirection();
		copy.setSourceFile(sourceFile);
		return copy;
	}

	@Override
	public void seal() {
		if (!isSealed()) {

			allowedChildren = Collections.unmodifiableList(Sealable.seal(getAllowedChildren()));
			stateTransitions = Collections.unmodifiableList(Sealable.seal(getStateTransitions()));
			transitions = Collections.unmodifiableList(Sealable.seal(getTransitions()));

			super.seal();
		}
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String getSourceContent() {
		return sourceContent;
	}

	public void setSourceContent(String content) {
		this.sourceContent = content;
	}

	public String getPreviousIdentifier() {
		return previousIdentifier;
	}

	public void setPreviousIdentifier(String previousIdentifier) {
		this.previousIdentifier = previousIdentifier;
	}
}
