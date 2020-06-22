package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeCompare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.definition.MergeableBase;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Implementation class for {@link PropertyDefinition} that represents a single unique field.
 *
 * @author BBonev
 */
public class FieldDefinitionImpl extends MergeableBase<FieldDefinitionImpl>implements PropertyDefinition, Serializable,
		Copyable<FieldDefinitionImpl>, BidirectionalMapping, WritablePropertyDefinition, Sealable, Comparable<PropertyDefinition> {

	private static final long serialVersionUID = -7974317054578346092L;

	@Tag(1)
	protected String name;

	@Tag(2)
	protected String value;

	@Tag(3)
	protected String type;

	@Tag(4)
	protected Integer codelist;

	@Tag(5)
	protected String rnc;

	@Tag(6)
	protected String labelId;

	@Tag(7)
	protected String tooltipId;

	@Tag(8)
	protected Boolean mandatory;

	@Tag(9)
	protected DisplayType displayType;

	@Tag(10)
	protected Boolean override;

	@Tag(11)
	protected DataTypeDefinition dataType;

	@Tag(12)
	protected Boolean multiValued;

	@Tag(13)
	protected Boolean mandatoryEnforced;

	@Tag(14)
	protected Integer maxLength;

	@Tag(15)
	protected String container;

	@Tag(16)
	protected Boolean previewEmpty;

	@Tag(17)
	protected Integer order;

	@Tag(18)
	protected String dmsType;

	@Tag(19)
	protected Set<String> filters;

	protected transient String xmlFilterValue;

	protected transient LabelProvider labelProvider;

	@Tag(20)
	protected List<Condition> conditions;

	@Tag(21)
	protected Integer hash;

	@Tag(22)
	protected Long prototypeId;

	@Tag(23)
	protected String uri;

	@Tag(24)
	protected Boolean unique;

	private transient Set<String> dependentFields;

	private boolean sealed;

	/**
	 * @deprecated not needed anymore
	 */
	@Deprecated
	private transient String source;

	@Override
	public Integer getCodelist() {
		return codelist;
	}

	@Override
	public DataTypeDefinition getDataType() {
		return dataType;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
	}

	@Override
	public DisplayType getDisplayType() {
		return displayType;
	}

	@Override
	public Long getId() {
		return getPrototypeId();
	}

	@Override
	public String getTooltip() {
		if (labelProvider != null) {
			return labelProvider.getPropertyTooltip(this);
		}
		return tooltipId;
	}

	@Override
	public String getLabel() {
		if (labelProvider != null) {
			return labelProvider.getPropertyLabel(this);
		}
		return labelId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRnc() {
		return rnc;
	}

	@Override
	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Boolean isMandatory() {
		if (mandatory == null) {
			return Boolean.FALSE;
		}
		return mandatory;
	}

	@Override
	public Boolean isMandatoryEnforced() {
		return mandatoryEnforced;
	}

	@Override
	public Boolean isMultiValued() {
		if (multiValued == null) {
			return Boolean.FALSE;
		}
		return multiValued;
	}

	@Override
	public Boolean isOverride() {
		return override;
	}

	@Override
	public Boolean isProtected() {
		return displayType != null ? displayType != DisplayType.EDITABLE : Boolean.FALSE;
	}

	@Override
	public void setCodelist(Integer value) {
		if (!isSealed()) {
			codelist = value;
		}
	}

	@Override
	public void setDisplayType(DisplayType value) {
		if (!isSealed()) {
			displayType = value;
		}
	}

	@Override
	public void setId(Long id) {
		setPrototypeId(id);
	}

	@Override
	public void setMandatory(Boolean value) {
		if (!isSealed()) {
			mandatory = value;
		}
	}

	@Override
	public void setMandatoryEnforced(Boolean mandatoryEnforced) {
		if (!isSealed()) {
			this.mandatoryEnforced = mandatoryEnforced;
		}
	}

	@Override
	public void setMultiValued(Boolean multiValued) {
		if (!isSealed()) {
			this.multiValued = multiValued;
		}
	}

	@Override
	public void setName(String value) {
		if (!isSealed()) {
			name = value;
		}
	}

	@Override
	public void setOverride(Boolean override) {
		if (!isSealed()) {
			this.override = override;
		}
	}

	@Override
	public void setRnc(String value) {
		if (!isSealed()) {
			rnc = value;
		}
	}

	@Override
	public void setType(String value) {
		if (!isSealed()) {
			type = value;
		}
	}

	@Override
	public void setDataType(DataTypeDefinition typeDefinition) {
		if (!isSealed()) {
			dataType = typeDefinition;
		}
	}

	@Override
	public void setValue(String value) {
		if (!isSealed()) {
			this.value = value;
		}
	}

	@Override
	public Integer getMaxLength() {
		return maxLength;
	}

	@Override
	public void setMaxLength(Integer maxLength) {
		if (!isSealed()) {
			this.maxLength = maxLength;
		}
	}

	@Override
	public Long getRevision() {
		return -1L;
	}

	@Override
	public void setRevision(Long revision) {
		// nothing to do here
	}

	@Override
	public String getParentPath() {
		return null;
	}

	@Override
	public void setParentPath(String parentPath) {
		// nothing to do here
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FieldDefinition [");
		builder.append("name=");
		builder.append(name);
		builder.append(", container=");
		builder.append(container);
		builder.append(", value=");
		builder.append(value);
		builder.append(", type=");
		builder.append(type);
		builder.append(", dmsType=");
		builder.append(dmsType);
		builder.append(", uri=");
		builder.append(uri);
		builder.append(", unique=");
		builder.append(unique);
		builder.append(", order=");
		builder.append(order);
		builder.append(", codelist=");
		builder.append(codelist);
		builder.append(", filters=");
		builder.append(filters);
		builder.append(", rnc=");
		builder.append(rnc);
		builder.append(", labelId=");
		builder.append(labelId);
		builder.append(", mandatory=");
		builder.append(mandatory);
		builder.append(", displayType=");
		builder.append(displayType);
		builder.append(", override=");
		builder.append(override);
		builder.append(", multiValued=");
		builder.append(multiValued);
		builder.append(", mandatoryEnforced=");
		builder.append(mandatoryEnforced);
		builder.append(", previewEmpty=");
		builder.append(previewEmpty);
		builder.append(", maxLength=");
		builder.append(maxLength);
		builder.append(", labelProvider=");
		builder.append(labelProvider == null ? "null" : "not null");
		builder.append("]");
		return builder.toString();
	}

	@Override
	public FieldDefinitionImpl createCopy() {
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.name = name;
		definition.value = value;
		definition.type = type;
		definition.codelist = codelist;
		definition.rnc = rnc;
		definition.labelId = labelId;
		definition.tooltipId = tooltipId;
		definition.mandatory = mandatory;
		definition.displayType = displayType;
		definition.override = override;
		definition.multiValued = multiValued;
		definition.mandatoryEnforced = mandatoryEnforced;
		definition.maxLength = maxLength;
		definition.container = container;
		definition.previewEmpty = previewEmpty;
		definition.order = order;
		definition.dmsType = dmsType;
		definition.prototypeId = prototypeId;
		definition.uri = uri;
		definition.unique = unique;
		definition.labelProvider = labelProvider;
		definition.source = source;

		if (dataType instanceof DataType) {
			DataType dataTypeDef = (DataType) dataType;
			definition.dataType = dataTypeDef.createCopy();
		}

		if (getFilters() != null) {
			definition.setFilters(new LinkedHashSet<>(getFilters()));
		}

		if (getConditions() != null) {
			definition.setConditions(new ArrayList<>(getConditions().size()));
			for (Condition condition : getConditions()) {
				definition.getConditions().add(((ConditionDefinitionImpl) condition).createCopy());
			}
		}
		return definition;
	}

	@Override
	public Boolean getPreviewEmpty() {
		return previewEmpty;
	}

	@Override
	public void setPreviewEmpty(Boolean previewEmpty) {
		if (!isSealed()) {
			this.previewEmpty = previewEmpty;
		}
	}

	@Override
	public Boolean isPreviewEnabled() {
		return getPreviewEmpty();
	}

	@Override
	public String getLabelId() {
		return labelId;
	}

	@Override
	public void setLabelId(String labelId) {
		if (!isSealed()) {
			this.labelId = labelId;
		}
	}

	@Override
	public String getTooltipId() {
		return tooltipId;
	}

	@Override
	public void setTooltipId(String tooltipId) {
		if (!isSealed()) {
			this.tooltipId = tooltipId;
		}
	}

	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	@SuppressWarnings("unchecked")
	public FieldDefinitionImpl mergeFrom(FieldDefinitionImpl source) {
		name = MergeHelper.replaceIfNull(name, source.getName());
		value = MergeHelper.replaceIfNull(value, source.getDefaultValue());
		type = MergeHelper.replaceIfNull(type, source.getType());
		codelist = MergeHelper.replaceIfNull(codelist, source.getCodelist());
		rnc = MergeHelper.replaceIfNull(rnc, source.getRnc());
		labelId = MergeHelper.replaceIfNull(labelId, source.getLabelId());
		tooltipId = MergeHelper.replaceIfNull(tooltipId, source.getTooltipId());

		mandatory = MergeHelper.replaceIfNull(mandatory, source.mandatory);

		displayType = MergeHelper.replaceIfNull(displayType, source.getDisplayType());
		override = MergeHelper.replaceIfNull(override, source.override);
		multiValued = MergeHelper.replaceIfNull(multiValued, source.multiValued);
		mandatoryEnforced = MergeHelper.replaceIfNull(mandatoryEnforced, source.mandatoryEnforced);
		maxLength = MergeHelper.replaceIfNull(maxLength, source.getMaxLength());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		order = MergeHelper.replaceIfNull(order, source.getOrder());
		previewEmpty = MergeHelper.replaceIfNull(previewEmpty, source.previewEmpty);
		dmsType = MergeHelper.replaceIfNull(dmsType, source.getDmsType());
		uri = MergeHelper.replaceIfNull(uri, source.getUri());
		unique = MergeHelper.replaceIfNull(unique, source.isUnique());

		// if we have some filters then we keep them from overriding
		if (source.getFilters() != null && (filters == null || filters.isEmpty())) {
			filters = new LinkedHashSet<>(source.getFilters());
		}

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(source.getConditions()), EmfMergeableFactory.CONDITION_DEFINITION);

		return this;
	}

	@Override
	public String getIdentifier() {
		return getName();
	}

	@Override
	public void setIdentifier(String identifier) {
		setName(identifier);
	}

	@Override
	public void initBidirection() {
		if (getConditions() != null) {
			for (Condition definition : getConditions()) {
				ConditionDefinitionImpl definitionImpl = (ConditionDefinitionImpl) definition;
				definitionImpl.setFieldDefinition(this);
			}
		}
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return null;
	}

	@Override
	public void setControlDefinition(ControlDefinition controlDefinition) {
		// nothing to do here
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public void setOrder(Integer order) {
		if (!isSealed()) {
			this.order = order;
		}
	}

	@Override
	public String getDmsType() {
		return dmsType;
	}

	@Override
	public void setDmsType(String dmsType) {
		if (!isSealed()) {
			this.dmsType = dmsType;
		}
	}

	@Override
	public void setDefaultProperties() {
		if (isSealed()) {
			// already sealed should not modify
			return;
		}
		if (mandatory == null) {
			mandatory = Boolean.FALSE;
		}
		if (mandatoryEnforced == null) {
			mandatoryEnforced = Boolean.FALSE;
		}
		if (multiValued == null) {
			multiValued = Boolean.FALSE;
		}
		if (previewEmpty == null) {
			previewEmpty = Boolean.TRUE;
		}
		if (StringUtils.isBlank(dmsType)) {
			dmsType = DefaultProperties.NOT_USED_PROPERTY_VALUE;
		}
		if (displayType == null) {
			displayType = DisplayType.HIDDEN;
		}
		// make sure the field is set with the proper case.
		if (StringUtils.isBlank(uri)
				|| EqualsHelper.nullSafeEquals(uri, DefaultProperties.NOT_USED_PROPERTY_VALUE, true)) {
			uri = DefaultProperties.NOT_USED_PROPERTY_VALUE;
		}
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
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		if (!isSealed()) {
			this.container = container;
		}
	}

	@Override
	public Set<String> getFilters() {
		return filters;
	}

	@Override
	public void setFilters(Set<String> filters) {
		if (!isSealed()) {
			this.filters = filters;
		}
	}

	/**
	 * Getter method for xmlFilterValue.
	 *
	 * @return the xmlFilterValue
	 */
	public String getXmlFilterValue() {
		return xmlFilterValue;
	}

	/**
	 * Setter method for xmlFilterValue.
	 *
	 * @param xmlFilterValue
	 *            the xmlFilterValue to set
	 */
	public void setXmlFilterValue(String xmlFilterValue) {
		if (!isSealed()) {
			this.xmlFilterValue = xmlFilterValue;
			if (xmlFilterValue != null) {
				String[] split = xmlFilterValue.split("\\s*,\\s*");
				setFilters(new LinkedHashSet<>(Arrays.asList(split)));
			}
		}
	}

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	@Override
	public void setConditions(List<Condition> conditions) {
		if (!isSealed()) {
			this.conditions = conditions;
		}
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		if (!isSealed()) {
			this.hash = hash;
		}
	}

	@Override
	public Long getPrototypeId() {
		return prototypeId;
	}

	@Override
	public void setPrototypeId(Long prototypeId) {
		if (!isSealed()) {
			this.prototypeId = prototypeId;
		}
	}

	@Override
	public boolean hasChildren() {
		return getControlDefinition() != null && getControlDefinition().hasChildren();
	}

	@Override
	public Node getChild(String aName) {
		if (hasChildren()) {
			return getControlDefinition().getChild(aName);
		}
		return null;
	}

	@Override
	public Boolean getMandatory() {
		return mandatory;
	}

	@Override
	public Boolean getOverride() {
		return override;
	}

	@Override
	public Boolean getMultiValued() {
		return multiValued;
	}

	/**
	 * Getter method for mandatoryEnforced.
	 *
	 * @return the mandatoryEnforced
	 */
	public Boolean getMandatoryEnforced() {
		return mandatoryEnforced;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public void setUri(String uri) {
		if (!isSealed()) {
			this.uri = uri;
		}
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		if (isSealed()) {
			// already sealed so should not continue
			return;
		}

		if (getConditions() != null) {
			conditions = Collections.unmodifiableList(Sealable.seal(getConditions()));
		}
		if (getFilters() != null) {
			filters = Collections.unmodifiableSet(getFilters());
		}
		sealed = true;
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
	public int hashCode() {
		return Objects.hash(getIdentifier());
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

	@Override
	public Set<String> getDependentFields() {
		if (dependentFields != null) {
			// already calculated return them
			return dependentFields;
		}
		// not calculated, yet. calculate them and store the result in the instance

		Set<String> processed = new HashSet<>();
		if (getRnc() != null && !(getRnc().startsWith("$") || getRnc().startsWith("#"))) {
			processed.addAll(DefinitionUtil.getRncFields(getRnc()));
		}
		if (getConditions() != null) {
			processed.addAll(getConditions()
					.stream()
						.map(Condition::getExpression)
						.flatMap(exp -> DefinitionUtil.getRncFields(exp).stream())
						.collect(Collectors.toSet()));
		}
		dependentFields = Collections.unmodifiableSet(processed);
		return dependentFields;
	}

	@Override
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	@Override
	public Boolean isUnique() {
		return unique;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
