package com.sirma.itt.seip.instance.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.ObjectInstance;

/**
 * Tests for {@link PropertiesEvaluationHelper}.
 *
 * @author smustafov
 */
public class PropertiesEvaluationHelperTest {

	@Test
	public void testPopulateProperties_shouldInitializeBooleanProperty() {
		Instance model = new ObjectInstance();
		List<PropertyDefinition> fields = new ArrayList<>();
		PropertyDefinitionMock definition = getPropertyDefinitionMock("flag", DataTypeDefinition.BOOLEAN);
		fields.add(definition);

		DatabaseIdManager idManager = mock(DatabaseIdManager.class);
		ExpressionsManager expressionsManager = mock(ExpressionsManager.class);
		when(expressionsManager.createDefaultContext(any(Instance.class), eq(null), eq(null)))
				.thenReturn(new ExpressionContext());

		PropertiesEvaluationHelper.populateProperties(model, fields, expressionsManager, false, idManager);

		Assert.assertEquals(Boolean.FALSE, model.getProperties().get("flag"));
	}

	@Test
	public void testPopulateProperties_notEmptyValue() {
		Instance model = new ObjectInstance();
		List<PropertyDefinition> fields = new ArrayList<>();
		PropertyDefinitionMock definition = getPropertyDefinitionMock("flag", DataTypeDefinition.BOOLEAN);
		fields.add(definition);

		DatabaseIdManager idManager = mock(DatabaseIdManager.class);
		ExpressionsManager expressionsManager = mock(ExpressionsManager.class);
		ExpressionContext expressionContext = new ExpressionContext();
		when(expressionsManager.createDefaultContext(any(Instance.class), eq(null), eq(null)))
				.thenReturn(expressionContext);
		when(expressionsManager.evaluate(definition, expressionContext)).thenReturn(Boolean.TRUE);

		PropertiesEvaluationHelper.populateProperties(model, fields, expressionsManager, false, idManager);

		Assert.assertEquals(Boolean.TRUE, model.getProperties().get("flag"));
	}

	@Test
	public void testPopulateProperties_enabledPreviewEmptyValue() {
		Instance model = new ObjectInstance();
		List<PropertyDefinition> fields = new ArrayList<>();
		PropertyDefinitionMock definition = getPropertyDefinitionMock("field", DataTypeDefinition.TEXT);
		definition.setPreviewEmpty(Boolean.TRUE);
		fields.add(definition);

		DatabaseIdManager idManager = mock(DatabaseIdManager.class);
		ExpressionsManager expressionsManager = mock(ExpressionsManager.class);
		ExpressionContext expressionContext = new ExpressionContext();
		when(expressionsManager.createDefaultContext(any(Instance.class), eq(null), eq(null)))
				.thenReturn(expressionContext);

		PropertiesEvaluationHelper.populateProperties(model, fields, expressionsManager, false, idManager);

		Assert.assertTrue(model.getProperties().containsKey("field"));
		Assert.assertNull(model.getProperties().get("field"));
	}

	@Test
	public void testPopulateProperties_disabledPreviewEmptyValue() {
		Instance model = new ObjectInstance();
		List<PropertyDefinition> fields = new ArrayList<>();
		PropertyDefinitionMock definition = getPropertyDefinitionMock("field", DataTypeDefinition.TEXT);
		definition.setPreviewEmpty(Boolean.FALSE);
		fields.add(definition);

		DatabaseIdManager idManager = mock(DatabaseIdManager.class);
		ExpressionsManager expressionsManager = mock(ExpressionsManager.class);
		ExpressionContext expressionContext = new ExpressionContext();
		when(expressionsManager.createDefaultContext(any(Instance.class), eq(null), eq(null)))
				.thenReturn(expressionContext);

		PropertiesEvaluationHelper.populateProperties(model, fields, expressionsManager, false, idManager);

		Assert.assertTrue(model.getProperties().isEmpty());
	}

	private PropertyDefinitionMock getPropertyDefinitionMock(String name, String type) {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(dataTypeDefinition.getName()).thenReturn(type);
		definition.setName(name);
		definition.setType(type);
		definition.setDataType(dataTypeDefinition);
		return definition;
	}

	public class PropertyDefinitionMock implements WritablePropertyDefinition, Serializable {

		private static final long serialVersionUID = -9171382611182739445L;
		private String name;
		private String defaultValue;
		private DataTypeDefinition dataType;
		private Boolean isOverride;
		private Boolean isMultiValued;
		private Boolean isMandatory;
		private Integer length;
		private Boolean isProtected;
		private Boolean isMandatoryEnforced;
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
		private Boolean previewEmpty;
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

}
