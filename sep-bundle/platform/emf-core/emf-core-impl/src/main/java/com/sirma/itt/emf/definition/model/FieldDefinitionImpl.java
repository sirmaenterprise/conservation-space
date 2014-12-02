package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.MergeableBase;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Implementation class for {@link PropertyDefinition} that represents a single unique field.
 *
 * @author BBonev
 */
public class FieldDefinitionImpl extends MergeableBase<FieldDefinitionImpl> implements
		PropertyDefinition, Serializable, Cloneable,
		BidirectionalMapping, WritablePropertyDefinition {

	private static final Logger LOGGER = Logger.getLogger(FieldDefinitionImpl.class);
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7974317054578346092L;

	/** The name. */
	@Tag(1)
	protected String name;

	/** The value. */
	@Tag(2)
	protected String value;

	/** The type. */
	@Tag(3)
	protected String type;

	/** The codelist. */
	@Tag(4)
	protected Integer codelist;

	/** The rnc. */
	@Tag(5)
	protected String rnc;

	/** The label. */
	@Tag(6)
	protected String labelId;

	/** The label. */
	@Tag(7)
	protected String tooltipId;

	/** The mandatory. */
	@Tag(8)
	protected Boolean mandatory;

	/** The display type. */
	@Tag(9)
	protected DisplayType displayType;

	/** The override. */
	@Tag(10)
	protected Boolean override;

	/** The type definition. */
	@Tag(11)
	protected DataTypeDefinition dataType;

	/** The multi valued. */
	@Tag(12)
	protected Boolean multiValued;

	/** The mandatory enforced. */
	@Tag(13)
	protected Boolean mandatoryEnforced;

	@Tag(14)
	protected Integer maxLength;

	/** The container. */
	@Tag(15)
	protected String container;

	@Tag(16)
	protected Boolean previewEmpty;

	/** The order. */
	@Tag(17)
	protected Integer order;

	/** The dms type. */
	@Tag(18)
	protected String dmsType;

	@Tag(19)
	protected Set<String> filters;

	/** The xml values. */
	protected transient String xmlFilterValue;

	/** The label provider. */
	protected transient LabelProvider labelProvider;

	/** The conditions. */
	@Tag(20)
	protected List<Condition> conditions;

	/** The hash. */
	@Tag(21)
	protected Integer hash;

	/** The prototype id. */
	@Tag(22)
	protected Long prototypeId;

	/** The uri. */
	@Tag(23)
	protected String uri;

	/**
	 * Gets the value of the codelist property.
	 *
	 * @return the codelist possible object is {@link int }
	 */
	@Override
	public Integer getCodelist() {
		return codelist;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataTypeDefinition getDataType() {
		return dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultValue() {
		return getValue();
	}

	/**
	 * Gets the value of the displayType property.
	 *
	 * @return the display type possible object is {@link String }
	 */
	@Override
	public DisplayType getDisplayType() {
		return displayType;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public Long getId() {
		return getPrototypeId();
	}

	/**
	 * Gets the value of the label property.
	 *
	 * @return the label possible object is {@link String }
	 */
	@Override
	public String getLabel() {
		String labelid = getLabelId();
		if (labelProvider != null) {
			if (labelid == null) {
				LOGGER.warn("Requesting a label from a field '" + getIdentifier()
						+ "' that does not have a label");
				return labelid;
			}
			return labelProvider.getLabel(labelid);
		}
		return labelid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTooltip() {
		String tooltip = getTooltipId();
		if (labelProvider != null) {
			if (tooltip == null) {
				// most of the fields does not have a tooltip so no need to print it that often
				LOGGER.trace("Requesting a tooltip from a field '" + getIdentifier()
						+ "' that does not have a tooltip");
				return tooltip;
			}
			return labelProvider.getLabel(tooltip);
		}
		return tooltip;
	}

	/**
	 * Gets the value of the name property.
	 *
	 * @return the name possible object is {@link String }
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets the value of the rnc property.
	 *
	 * @return the rnc possible object is {@link String }
	 */
	@Override
	public String getRnc() {
		return rnc;
	}

	/**
	 * Gets the value of the type property.
	 *
	 * @return the type possible object is {@link String }
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Gets the value of the value property.
	 *
	 * @return the value possible object is {@link String }
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the value of the mandatory property.
	 *
	 * @return the boolean possible object is {@link Boolean }
	 */
	@Override
	public Boolean isMandatory() {
		if (mandatory == null) {
			return Boolean.FALSE;
		}
		return mandatory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isMandatoryEnforced() {
		return mandatoryEnforced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isMultiValued() {
		if (multiValued == null) {
			return Boolean.FALSE;
		}
		return multiValued;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isOverride() {
		return override;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isProtected() {
		return displayType != null ? displayType != DisplayType.EDITABLE : false;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setCodelist(Integer value) {
		codelist = value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setDisplayType(DisplayType value) {
		displayType = value;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(Long id) {
		setPrototypeId(id);
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setMandatory(Boolean value) {
		mandatory = value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setMandatoryEnforced(Boolean mandatoryEnforced) {
		this.mandatoryEnforced = mandatoryEnforced;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setMultiValued(Boolean multiValued) {
		this.multiValued = multiValued;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setName(String value) {
		name = value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setOverride(Boolean override) {
		this.override = override;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setRnc(String value) {
		rnc = value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setType(String value) {
		type = value;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setDataType(DataTypeDefinition typeDefinition) {
		dataType = typeDefinition;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Getter method for maxLength.
	 *
	 * @return the maxLength
	 */
	@Override
	public Integer getMaxLength() {
		return maxLength;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * Getter method for revision.
	 *
	 * @return the revision
	 */
	@Override
	public Long getRevision() {
		return -1l;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		// nothing to do here
	}

	/**
	 * Getter method for parentPath.
	 *
	 * @return the parentPath
	 */
	@Override
	public String getParentPath() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParentPath(String parentPath) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
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
	public FieldDefinitionImpl clone() {
		FieldDefinitionImpl definition = new FieldDefinitionImpl();
		definition.codelist = codelist;
		definition.displayType = displayType;
		definition.labelId = labelId;
		definition.mandatory = mandatory;
		definition.mandatoryEnforced = mandatoryEnforced;
		definition.maxLength = maxLength;
		definition.multiValued = multiValued;
		definition.name = name;
		definition.override = override;
		definition.container = container;
		definition.rnc = rnc;
		definition.type = type;
		definition.dmsType = dmsType;
		definition.uri = uri;
		definition.value = value;
		definition.order = order;
		definition.previewEmpty = previewEmpty;
		if (dataType instanceof DataType) {
			DataType dataTypeDef = (DataType) dataType;
			definition.dataType = dataTypeDef.clone();
		}
		definition.filters = new LinkedHashSet<String>();
		if (getFilters() != null) {
			definition.filters.addAll(getFilters());
		}
		return definition;
	}

	/**
	 * Getter method for previewEmpty.
	 *
	 * @return the previewEmpty
	 */
	public Boolean getPreviewEmpty() {
		return previewEmpty;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setPreviewEmpty(Boolean previewEmpty) {
		this.previewEmpty = previewEmpty;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isPreviewEnabled() {
		return getPreviewEmpty();
	}

	/**
	 * Getter method for labelId.
	 *
	 * @return the labelId
	 */
	@Override
	public String getLabelId() {
		return labelId;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	/**
	 * Getter method for tooltipId.
	 *
	 * @return the tooltipId
	 */
	@Override
	public String getTooltipId() {
		return tooltipId;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setTooltipId(String tooltipId) {
		this.tooltipId = tooltipId;
	}

	/**
	 * Sets the label provider.
	 *
	 * @param labelProvider
	 *            the new label provider
	 */
	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	@SuppressWarnings("unchecked")
	public FieldDefinitionImpl mergeFrom(FieldDefinitionImpl source) {
		// id = MergeHelper.replaceIfNull(id, source.getId());

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
		mandatoryEnforced = MergeHelper.replaceIfNull(mandatoryEnforced,
				source.mandatoryEnforced);
		maxLength = MergeHelper.replaceIfNull(maxLength, source.getMaxLength());
		container = MergeHelper.replaceIfNull(container, source.getContainer());
		order = MergeHelper.replaceIfNull(order, source.getOrder());
		previewEmpty = MergeHelper.replaceIfNull(previewEmpty, source.previewEmpty);
		dmsType = MergeHelper.replaceIfNull(dmsType, source.getDmsType());
		uri = MergeHelper.replaceIfNull(uri, source.getUri());

		// if we have some filters then we keep them from overriding
		if ((source.getFilters() != null) && ((filters == null) || filters.isEmpty())) {
			filters = new LinkedHashSet<String>(source.getFilters());
		}

		conditions = MergeHelper.mergeLists(MergeHelper.convertToMergable(conditions),
				MergeHelper.convertToMergable(source.getConditions()),
				EmfMergeableFactory.CONDITION_DEFINITION);

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

	/**
	 * Getter method for controlDefinition.
	 *
	 * @return the controlDefinition
	 */
	@Override
	public ControlDefinition getControlDefinition() {
		return null;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setControlDefinition(ControlDefinition controlDefinition) {
		// nothing to do here
	}

	/**
	 * Getter method for order.
	 *
	 * @return the order
	 */
	@Override
	public Integer getOrder() {
		return order;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * Getter method for dmsType.
	 *
	 * @return the dmsType
	 */
	@Override
	public String getDmsType() {
		return dmsType;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setDmsType(String dmsType) {
		this.dmsType = dmsType;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setDefaultProperties() {
		if (mandatory == null) {
			mandatory = Boolean.FALSE;
		}
		if (mandatoryEnforced == null) {
			mandatoryEnforced = Boolean.FALSE;
		}
		if (multiValued == null) {
			multiValued = Boolean.FALSE;
		}
		if (override == null) {
			override = Boolean.FALSE;
		}
		if (previewEmpty == null) {
			previewEmpty = Boolean.TRUE;
		}
		if (StringUtils.isNullOrEmpty(dmsType)) {
			dmsType = DefaultProperties.NOT_USED_PROPERTY_VALUE;
		}
		if (displayType == null) {
			displayType = DisplayType.HIDDEN;
		}
		// make sure the field is set with the proper case.
		if (StringUtils.isNullOrEmpty(uri) || EqualsHelper.nullSafeEquals(uri, DefaultProperties.NOT_USED_PROPERTY_VALUE, true)) {
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
	 * Getter method for filters.
	 *
	 * @return the filters
	 */
	@Override
	public Set<String> getFilters() {
		return filters;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setFilters(Set<String> filters) {
		this.filters = filters;
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
	 * @param xmlFilterValue the xmlFilterValue to set
	 */
	public void setXmlFilterValue(String xmlFilterValue) {
		this.xmlFilterValue = xmlFilterValue;
		if (xmlFilterValue != null) {
			String[] split = xmlFilterValue.split("\\s*,\\s*");
			setFilters(new LinkedHashSet<String>(Arrays.asList(split)));
		}
	}

	/**
	 * Getter method for conditions.
	 *
	 * @return the conditions
	 */
	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Getter method for hash.
	 *
	 * @return the hash
	 */
	@Override
	public Integer getHash() {
		return hash;
	}

	/**
	 * Setter method for hash.
	 *
	 * @param hash the hash to set
	 */
	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	/**
	 * Getter method for prototypeId.
	 *
	 * @return the prototypeId
	 */
	@Override
	public Long getPrototypeId() {
		return prototypeId;
	}

	/**
	 * Setter method for prototypeId.
	 *
	 * @param prototypeId the prototypeId to set
	 */
	@Override
	public void setPrototypeId(Long prototypeId) {
		this.prototypeId = prototypeId;
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

	/**
	 * Getter method for mandatory.
	 *
	 * @return the mandatory
	 */
	public Boolean getMandatory() {
		return mandatory;
	}

	/**
	 * Getter method for override.
	 *
	 * @return the override
	 */
	public Boolean getOverride() {
		return override;
	}

	/**
	 * Getter method for multiValued.
	 *
	 * @return the multiValued
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUri() {
		return uri;
	}

	/**
	 * Setter method for uri.
	 *
	 * @param uri the uri to set
	 */
	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}

}
