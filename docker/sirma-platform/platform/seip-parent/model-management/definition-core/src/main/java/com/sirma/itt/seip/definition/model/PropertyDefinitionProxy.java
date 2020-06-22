package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeCompare;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Class that represents a join table between all base definitions and field definitions. It also acts as a proxy for
 * the target field definition.
 *
 * @author BBonev
 */
public class PropertyDefinitionProxy extends MergeableBase<PropertyDefinitionProxy>
		implements WritablePropertyDefinition, GenericProxy<WritablePropertyDefinition>, Serializable,
		JsonRepresentable, Sealable, Comparable<PropertyDefinition> {

	private static final long serialVersionUID = -1534468836975112785L;

	@Tag(1)
	protected String name;

	@Tag(2)
	protected Long revision;

	@Tag(3)
	protected String parentPath;

	@Tag(4)
	protected String container;

	@Tag(5)
	protected ControlDefinition controlDefinition;

	protected transient BaseDefinition<?> baseDefinition;

	@Tag(6)
	protected WritablePropertyDefinition target;

	private boolean sealed;

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
		if (!isSealed()) {
			this.baseDefinition = baseDefinition;
		}
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
		setName(identifier);
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
		if (!isSealed()) {
			getTarget().setId(id);
		}
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
	public Boolean getOverride() {
		return getTarget().getOverride();
	}

	@Override
	public Boolean isMultiValued() {
		return getTarget().isMultiValued();
	}

	@Override
	public Boolean getMultiValued() {
		return getTarget().getMultiValued();
	}

	@Override
	public Boolean isMandatory() {
		return getTarget().isMandatory();
	}

	@Override
	public Boolean getMandatory() {
		return getTarget().getMandatory();
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

	@Override
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

	@Override
	public WritablePropertyDefinition getTarget() {
		if (target == null) {
			target = new FieldDefinitionImpl();
		}
		return target;
	}

	@Override
	public void setTarget(WritablePropertyDefinition target) {
		if (!isSealed()) {
			this.target = target;
		}
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
		if (Boolean.TRUE.equals(isOverride())) {
			return this;
		}

		((FieldDefinitionImpl) getTarget()).mergeFrom((FieldDefinitionImpl) source.getTarget());
		name = MergeHelper.replaceIfNull(name, source.getName());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		parentPath = null;
		revision = MergeHelper.replaceIfNull(revision, source.getRevision());
		if (source.getControlDefinition() != null) {
			controlDefinition = MergeHelper.replaceIfNull(controlDefinition, new ControlDefinitionImpl());
			((ControlDefinitionImpl) controlDefinition)
					.mergeFrom((ControlDefinitionImpl) source.getControlDefinition());
		}
		return this;
	}

	@Override
	public PropertyDefinitionProxy createCopy() {
		PropertyDefinitionProxy copy = new PropertyDefinitionProxy();
		copy.container = container;
		copy.parentPath = parentPath;
		copy.name = name;
		copy.revision = revision;
		copy.target = ((FieldDefinitionImpl) getTarget()).createCopy();
		copy.target.setId(null);
		if (controlDefinition != null) {
			copy.controlDefinition = ((ControlDefinitionImpl) controlDefinition).createCopy();
		}
		return copy;
	}

	@Override
	public WritablePropertyDefinition cloneProxy() {
		return createCopy();
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
		if (!isSealed()) {
			name = value;
			getTarget().setName(value);
		}
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
		if (!isSealed()) {
			this.revision = revision;
		}
	}

	@Override
	public void setParentPath(String parentPath) {
		if (!isSealed()) {
			this.parentPath = parentPath;
		}
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
		if (!isSealed()) {
			this.controlDefinition = controlDefinition;
		}
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
		if (!isSealed()) {
			this.container = container;
			getTarget().setContainer(container);
		}
	}

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
		return getControlDefinition() != null && getControlDefinition().hasChildren();
	}

	@Override
	public Node getChild(String childName) {
		if (hasChildren()) {
			return getControlDefinition().getChild(childName);
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

	@Override
	public void setUnique(Boolean unique) {
		getTarget().setUnique(unique);
	}

	@Override
	public Boolean isUnique() {
		return getTarget().isUnique();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "label", getLabel());
		JsonUtil.addToJson(object, "tooltip", getTooltip());
		JsonUtil.addToJson(object, "name", getName());
		JsonUtil.addToJson(object, "defaultValue", getDefaultValue());
		JsonUtil.addToJson(object, "dataType",
				TypeConverterUtil.getConverter().convert(JSONObject.class, getDataType()));
		JsonUtil.addToJson(object, "displayType", getDisplayType());
		JsonUtil.addToJson(object, "codelist", getCodelist());
		JsonUtil.addToJson(object, "isMandatory", isMandatory());
		JsonUtil.addToJson(object, "previewEmpty", getPreviewEmpty());
		JSONObject controlDefinitionData = TypeConverterUtil.getConverter().convert(JSONObject.class,
				getControlDefinition());
		JsonUtil.addToJson(object, "controlDefinition", controlDefinitionData);
		JsonUtil.addToJson(object, "uri", getUri());
		JsonUtil.addToJson(object, "unique", isUnique());
		JsonUtil.addToJson(object, "multivalue", isMultiValued());
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public Set<String> getDependentFields() {
		return getTarget().getDependentFields();
	}

	@Override
	public void seal() {
		if (isSealed()) {
			// sealed so nothing to do more
			return;
		}

		Sealable.seal(target);
		Sealable.seal(controlDefinition);

		sealed = true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIdentifier());
	}

	@Override
	public int compareTo(PropertyDefinition o) {
		int compare = nullSafeCompare(getOrder(), o.getOrder());
		if (compare == 0) {
			return getName().compareTo(o.getName());
		}
		return compare;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof MergeableBase) {
			return EqualsHelper.nullSafeEquals(getIdentifier(), ((Identity) obj).getIdentifier());
		}

		return false;
	}

}
