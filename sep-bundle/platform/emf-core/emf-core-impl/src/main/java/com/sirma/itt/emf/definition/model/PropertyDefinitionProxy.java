package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Class that represents a join table between all base definitions and field definitions. It also
 * acts as a proxy for the target field definition.
 *
 * @author BBonev
 */
public class PropertyDefinitionProxy extends MergeableBase<PropertyDefinitionProxy> implements
		BidirectionalMapping, Mergeable<PropertyDefinitionProxy>, Cloneable,
		WritablePropertyDefinition, GenericProxy<WritablePropertyDefinition>, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1534468836975112785L;
	/** The name. */
	@Tag(1)
	protected String name;
	/** The revision. */
	@Tag(2)
	protected Long revision;
	/** The parent path. */
	@Tag(3)
	protected String parentPath;
	/** The container. */
	@Tag(4)
	protected String container;

	/** The control definition. */
	@Tag(5)
	protected ControlDefinition controlDefinition;

	/** The base definition. */
	protected transient BaseDefinition<?> baseDefinition;

	/** The target. */
	@Tag(6)
	protected WritablePropertyDefinition target;

	/**
	 * Getter method for baseDefinition.
	 *
	 * @return the baseDefinition
	 */
	public BaseDefinition<?> getBaseDefinition() {
		return baseDefinition;
	}

	/**
	 * Setter method for baseDefinition.
	 *
	 * @param baseDefinition
	 *            the baseDefinition to set
	 */
	public void setBaseDefinition(BaseDefinition<?> baseDefinition) {
		this.baseDefinition = baseDefinition;
	}

	@Override
	public Integer getOrder() {
		return getTarget().getOrder();
	}

	@Override
	public String getIdentifier() {
		return getName();
	}

	@Override
	public void setIdentifier(String identifier) {
		getTarget().setIdentifier(identifier);
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getParentPath();
	}

	@Override
	public Long getId() {
		return getTarget().getId();
	}

	@Override
	public void setId(Long id) {
		getTarget().setId(id);
	}

	@Override
	public String getLabel() {
		return getTarget().getLabel();
	}

	@Override
	public String getTooltip() {
		return getTarget().getTooltip();
	}

	@Override
	public String getLabelId() {
		return getTarget().getLabelId();
	}

	@Override
	public String getTooltipId() {
		return getTarget().getTooltipId();
	}

	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		getTarget().setLabelProvider(labelProvider);
	}

	@Override
	public List<Condition> getConditions() {
		return getTarget().getConditions();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDefaultValue() {
		return getTarget().getDefaultValue();
	}

	@Override
	public DataTypeDefinition getDataType() {
		return getTarget().getDataType();
	}

	@Override
	public Boolean isOverride() {
		return getTarget().isOverride();
	}

	@Override
	public Boolean isMultiValued() {
		return getTarget().isMultiValued();
	}

	@Override
	public Boolean isMandatory() {
		return getTarget().isMandatory();
	}

	@Override
	public Boolean isMandatoryEnforced() {
		return getTarget().isMandatoryEnforced();
	}

	@Override
	public Boolean isProtected() {
		return getTarget().isProtected();
	}

	@Override
	public Integer getMaxLength() {
		return getTarget().getMaxLength();
	}

	@Override
	public DisplayType getDisplayType() {
		return getTarget().getDisplayType();
	}

	@Override
	public Boolean isPreviewEnabled() {
		return getTarget().isPreviewEnabled();
	}

	/**
	 * Getter method for previewEmpty.
	 *
	 * @return the previewEmpty
	 */
	public Boolean getPreviewEmpty() {
		return getTarget().isPreviewEnabled();
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public String getParentPath() {
		return parentPath;
	}

	@Override
	public Integer getCodelist() {
		return getTarget().getCodelist();
	}

	@Override
	public String getType() {
		return getTarget().getType();
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return controlDefinition;
	}

	@Override
	public String getRnc() {
		return getTarget().getRnc();
	}

	@Override
	public String getDmsType() {
		return getTarget().getDmsType();
	}

	@Override
	public Set<String> getFilters() {
		return getTarget().getFilters();
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	@Override
	public WritablePropertyDefinition getTarget() {
		if (target == null) {
			target = new FieldDefinitionImpl();
		}
		return target;
	}

	/**
	 * Setter method for target.
	 *
	 * @param target
	 *            the target to set
	 */
	@Override
	public void setTarget(WritablePropertyDefinition target) {
		this.target = target;
	}

	@Override
	public void initBidirection() {
		if (controlDefinition != null) {
			ControlDefinitionImpl definitionImpl = (ControlDefinitionImpl) controlDefinition;
			definitionImpl.setParentEntity(this);
			definitionImpl.initBidirection();
		}
		getTarget().initBidirection();
	}

	@Override
	public PropertyDefinitionProxy mergeFrom(PropertyDefinitionProxy source) {
		// if (StringUtils.isNotNullOrEmpty(getTarget().getName())) {
		// if (StringUtils.isNotNullOrEmpty(source.getTarget().getName())) {
		((FieldDefinitionImpl) getTarget()).mergeFrom((FieldDefinitionImpl) source.getTarget());
		// } else {
		// // we does not have field into the parent definition -- this can't happen
		// }
		// } else {
		// target = source.getTarget();
		// }
		// if (source.getTarget() != null) {
		// target = MergeHelper.replaceIfNull(target, new FieldDefinitionImpl());
		// ((FieldDefinitionImpl) getTarget()).mergeFrom((FieldDefinitionImpl) source.getTarget());
		// }
		name = MergeHelper.replaceIfNull(name, source.getName());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		parentPath = null;
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		if (source.getControlDefinition() != null) {
			controlDefinition = MergeHelper.replaceIfNull(controlDefinition,
					new ControlDefinitionImpl());
			((ControlDefinitionImpl) controlDefinition).mergeFrom((ControlDefinitionImpl) source
					.getControlDefinition());
		}
		return this;
	}

	@Override
	public PropertyDefinitionProxy clone() {
		PropertyDefinitionProxy copy = new PropertyDefinitionProxy();
		copy.container = container;
		copy.parentPath = parentPath;
		copy.name = name;
		copy.revision = revision;
		copy.target = ((FieldDefinitionImpl) getTarget()).clone();
		copy.target.setId(null);
		if (controlDefinition != null) {
			copy.controlDefinition = ((ControlDefinitionImpl) controlDefinition).clone();
		}
		return copy;
	}

	@Override
	public WritablePropertyDefinition cloneProxy() {
		PropertyDefinitionProxy copy = new PropertyDefinitionProxy();
		copy.container = container;
		copy.parentPath = parentPath;
		copy.name = name;
		copy.revision = revision;
		copy.target = getTarget();
		if (controlDefinition != null) {
			copy.controlDefinition = ((ControlDefinitionImpl) controlDefinition).clone();
		}
		return copy;
	}

	@Override
	public void setCodelist(Integer value) {
		getTarget().setCodelist(value);
	}

	@Override
	public void setDisplayType(DisplayType value) {
		getTarget().setDisplayType(value);
	}

	@Override
	public void setMandatory(Boolean value) {
		getTarget().setMandatory(value);
	}

	@Override
	public void setMandatoryEnforced(Boolean mandatoryEnforced) {
		getTarget().setMandatoryEnforced(mandatoryEnforced);
	}

	@Override
	public void setMultiValued(Boolean multiValued) {
		getTarget().setMultiValued(multiValued);
	}

	@Override
	public void setName(String value) {
		name = value;
		getTarget().setName(value);
	}

	@Override
	public void setOverride(Boolean override) {
		getTarget().setOverride(override);
	}

	@Override
	public void setRnc(String value) {
		getTarget().setRnc(value);
	}

	@Override
	public void setType(String value) {
		getTarget().setType(value);
	}

	@Override
	public void setDataType(DataTypeDefinition typeDefinition) {
		getTarget().setDataType(typeDefinition);
	}

	@Override
	public void setValue(String value) {
		getTarget().setValue(value);
	}

	@Override
	public void setMaxLength(Integer maxLength) {
		getTarget().setMaxLength(maxLength);
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	@Override
	public void setPreviewEmpty(Boolean previewEmpty) {
		getTarget().setPreviewEmpty(previewEmpty);
	}

	@Override
	public void setLabelId(String labelId) {
		getTarget().setLabelId(labelId);
	}

	@Override
	public void setTooltipId(String tooltipId) {
		getTarget().setTooltipId(tooltipId);
	}

	@Override
	public void setControlDefinition(ControlDefinition controlDefinition) {
		this.controlDefinition = controlDefinition;
	}

	@Override
	public void setOrder(Integer order) {
		getTarget().setOrder(order);
	}

	@Override
	public void setDmsType(String dmsType) {
		getTarget().setDmsType(dmsType);
	}

	@Override
	public void setDefaultProperties() {
		getTarget().setDefaultProperties();
	}

	@Override
	public void setFilters(Set<String> filters) {
		getTarget().setFilters(filters);
	}

	@Override
	public void setConditions(List<Condition> conditions) {
		getTarget().setConditions(conditions);
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
		getTarget().setContainer(container);
	}

	/**
	 * Gets the container.
	 *
	 * @return the container
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * Getter method for xmlFilterValue.
	 *
	 * @return the xmlFilterValue
	 */
	public String getXmlFilterValue() {
		return ((FieldDefinitionImpl) getTarget()).getXmlFilterValue();
	}

	/**
	 * Setter method for xmlFilterValue.
	 *
	 * @param xmlFilterValue
	 *            the xmlFilterValue to set
	 */
	public void setXmlFilterValue(String xmlFilterValue) {
		((FieldDefinitionImpl) getTarget()).setXmlFilterValue(xmlFilterValue);
	}

	@Override
	public Integer getHash() {
		return getTarget().getHash();
	}

	@Override
	public void setHash(Integer hash) {
		getTarget().setHash(hash);
	}

	@Override
	public Long getPrototypeId() {
		return getTarget().getPrototypeId();
	}

	@Override
	public void setPrototypeId(Long prototypeId) {
		getTarget().setPrototypeId(prototypeId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\tPropertyDefinitionProxy [");
		builder.append("name=");
		builder.append(name);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", parentPath=");
		builder.append(parentPath);
		builder.append(", container=");
		builder.append(container);
		builder.append(", controlDefinition=");
		builder.append(controlDefinition);
		builder.append(", target=");
		builder.append(target);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean hasChildren() {
		return (getControlDefinition() != null) && getControlDefinition().hasChildren();
	}

	@Override
	public Node getChild(String name) {
		if (hasChildren()) {
			return getControlDefinition().getChild(name);
		}
		return null;
	}

	@Override
	public String getUri() {
		return getTarget().getUri();
	}

	@Override
	public void setUri(String uri) {
		getTarget().setUri(uri);
	}

}
