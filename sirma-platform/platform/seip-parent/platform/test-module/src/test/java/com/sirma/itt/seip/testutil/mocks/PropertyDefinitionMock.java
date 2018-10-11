/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * @author BBonev
 */
public class PropertyDefinitionMock implements WritablePropertyDefinition {

	private String name;
	private String defaultValue;
	private DataTypeDefinition dataType;
	private Boolean isOverride = Boolean.FALSE;
	private Boolean isMultiValued = Boolean.FALSE;
	private Boolean isMandatory = Boolean.FALSE;
	private Integer length;
	private Boolean isProtected = Boolean.FALSE;
	private Boolean isMandatoryEnforced = Boolean.FALSE;
	private DisplayType displayType;
	private Long revision;
	private Integer codelist;
	private String type;
	private ControlDefinition controlDefinition;
	private String rnc;
	private String dmsType;
	private Set<String> filters = new HashSet<>();
	private String container;
	private Integer hash;
	private Long propertyId;
	private String uri;
	private Integer order;
	private String labelId;
	private String tooltip;
	private String tooltipId;
	private List<Condition> conditions = new LinkedList<>();
	private Long id;
	private String parentPath;
	private Boolean previewEmpty = Boolean.FALSE;
	private LabelProvider labelProvider;
	private Boolean unique;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public DataTypeDefinition getDataType() {
		return dataType;
	}

	@Override
	public Boolean isOverride() {
		return isOverride;
	}

	public void setIsOverride(Boolean isOverride) {
		this.isOverride = isOverride;
	}

	@Override
	public Boolean isMultiValued() {
		return isMultiValued;
	}

	@Override
	public Boolean isMandatory() {
		return isMandatory;
	}

	@Override
	public Boolean isMandatoryEnforced() {
		return isMandatoryEnforced;
	}

	@Override
	public Boolean isProtected() {
		return isProtected;
	}

	public void setIsProtected(Boolean isProtected) {
		this.isProtected = isProtected;
	}

	@Override
	public Integer getMaxLength() {
		return length;
	}

	@Override
	public DisplayType getDisplayType() {
		return displayType;
	}

	@Override
	public Boolean isPreviewEnabled() {
		return previewEmpty;
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
		return codelist;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public ControlDefinition getControlDefinition() {
		return controlDefinition;
	}

	@Override
	public String getRnc() {
		return rnc;
	}

	@Override
	public String getDmsType() {
		return dmsType;
	}

	@Override
	public Set<String> getFilters() {
		return filters;
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public Long getPrototypeId() {
		return propertyId;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public Boolean isUnique() {
		return unique;
	}

	@Override
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String s) {
		return null;
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
	public String getLabel() {
		if (labelProvider != null) {
			return labelProvider.getLabel(labelId);
		}
		return labelId;
	}

	@Override
	public String getTooltip() {
		if (labelProvider != null) {
			return labelProvider.getLabel(labelId);
		}
		return tooltip;
	}

	@Override
	public String getLabelId() {
		return labelId;
	}

	@Override
	public String getTooltipId() {
		return tooltipId;
	}

	@Override
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public void initBidirection() {
		// empty
	}

	@Override
	public void setCodelist(Integer value) {
		codelist = value;
	}

	@Override
	public void setDisplayType(DisplayType value) {
		displayType = value;
	}

	@Override
	public void setMandatory(Boolean value) {
		isMandatory = value;
	}

	@Override
	public void setMandatoryEnforced(Boolean mandatoryEnforced) {
		isMandatoryEnforced = mandatoryEnforced;
	}

	@Override
	public void setMultiValued(Boolean multiValued) {
		isMultiValued = multiValued;
	}

	@Override
	public void setName(String value) {
		name = value;
	}

	@Override
	public void setOverride(Boolean override) {
		isOverride = override;
	}

	@Override
	public void setRnc(String value) {
		rnc = value;
	}

	@Override
	public void setType(String value) {
		type = value;
		if (dataType == null) {
			DataTypeDefinitionMock dataTypeDefinitionMock = new DataTypeDefinitionMock(value);
			setDataType(dataTypeDefinitionMock);
		}
	}

	@Override
	public void setDataType(DataTypeDefinition typeDefinition) {
		dataType = typeDefinition;
	}

	@Override
	public void setValue(String value) {
		defaultValue = value;
	}

	@Override
	public void setMaxLength(Integer maxLength) {
		length = maxLength;
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
		this.previewEmpty = previewEmpty;
	}

	@Override
	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	@Override
	public void setTooltipId(String tooltipId) {
		this.tooltipId = tooltipId;
	}

	@Override
	public void setControlDefinition(ControlDefinition controlDefinition) {
		this.controlDefinition = controlDefinition;
	}

	@Override
	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public void setDmsType(String dmsType) {
		this.dmsType = dmsType;
	}

	@Override
	public void setDefaultProperties() {
		// empty
	}

	@Override
	public void setFilters(Set<String> filters) {
		this.filters = filters;
	}

	@Override
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	@Override
	public void setPrototypeId(Long prototypeId) {
		propertyId = prototypeId;
	}

	@Override
	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

}
