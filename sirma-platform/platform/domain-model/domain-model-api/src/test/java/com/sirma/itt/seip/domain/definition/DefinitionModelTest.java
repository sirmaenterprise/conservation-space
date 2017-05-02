/*
 *
 */
package com.sirma.itt.seip.domain.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;

/**
 * Test for the default methods in {@link DefinitionModel}
 *
 * @author BBonev
 */
public class DefinitionModelTest {

	private DefinitionModel model;

	@Before
	public void beforeMethod() {
		model = new DefinitionModelDummy();
		model.getFields().add(new PropertyDefinitionDummy("property1", "emf:property1"));
		model.getFields().add(new PropertyDefinitionDummy("property2", "emf:property2"));
		model.getFields().add(new PropertyDefinitionDummy("property3", "emf:property3"));
		model.getFields().add(new PropertyDefinitionDummy("property4", "emf:property4"));
		ControlDefinitionDummy control = new ControlDefinitionDummy();
		control.getFields().add(new PropertyDefinitionDummy("controlProperty1", "emf:controlProperty1"));
		control.getFields().add(new PropertyDefinitionDummy("controlProperty2", "emf:controlProperty2"));
		model.getFields().add(new PropertyDefinitionDummy("property5", control, "emf:property5"));
	}

	@Test
	public void fieldsStream() throws Exception {
		assertEquals(5L, model.fieldsStream().count());
	}

	@Test
	public void getField() throws Exception {
		assertFalse("Should not find null property", model.getField(null).isPresent());
		assertFalse("Field property32 should not be found", model.getField("property32").isPresent());
		assertTrue("Field property3 should be present", model.getField("property3").isPresent());
	}

	@Test
	public void findField() throws Exception {
		assertFalse("Should not find anything for null filter", model.findField(null).isPresent());
		assertFalse("Field property32 should not be found",
				model.findField(PropertyDefinition.hasUri("emf:property32")).isPresent());
		assertFalse("Field null should not be found", model.findField(PropertyDefinition.hasUri(null)).isPresent());
		assertTrue("Field property3 should be present",
				model.findField(PropertyDefinition.hasUri("emf:property3")).isPresent());
	}

	@Test
	public void fieldsAsMap() throws Exception {
		Map<String, PropertyDefinition> mapping = model.getFieldsAsMap();
		assertNotNull(mapping);
		assertFalse(mapping.isEmpty());
		assertEquals(5, mapping.size());
		assertFalse(mapping.containsKey("controlProperty1"));
		assertTrue(mapping.containsKey("property4"));
	}

	@Test
	public void fieldsStreamAndControl() throws Exception {
		assertEquals(7L, model.fieldsStream().flatMap(PropertyDefinition::stream).count());
	}

	@Test
	public void testIterator() throws Exception {
		int count = 0;
		for (@SuppressWarnings("unused")
		PropertyDefinition property : model) {
			count++;
		}
		assertEquals(5, count);
	}

	@Test
	public void test_getFieldsAndDependencies() throws Exception {
		PropertyDefinitionDummy propertyDefinition = (PropertyDefinitionDummy) model.getField("property1").get();
		propertyDefinition.setConditions(Arrays.asList(new ConditionDummy("+[property2] AND -[property3]")));

		Set<String> dependentFields = model
				.getFieldsAndDependencies(Arrays.asList("property1"))
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());

		assertTrue(dependentFields.contains("property1"));
		assertTrue(dependentFields.contains("property2"));
		assertTrue(dependentFields.contains("property3"));
		assertEquals(3, dependentFields.size());
	}

	/**
	 * @author BBonev
	 */
	private static class DefinitionModelDummy implements DefinitionModel {

		private static final long serialVersionUID = 1L;
		private List<PropertyDefinition> fields = new LinkedList<>();

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public void setHash(Integer hash) {
			// not needed
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public void setIdentifier(String identifier) {
			// not needed
		}

		@Override
		public List<PropertyDefinition> getFields() {
			return fields;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}
	}

	/**
	 * @author BBonev
	 */
	private static class PropertyDefinitionDummy implements WritablePropertyDefinition {
		private static final Pattern RNC_FIELD_PATTERN = Pattern.compile("(?<!\\d+)\\[([\\w:]+?)\\]", Pattern.CANON_EQ);
		private String identifier;
		private ControlDefinition control;
		private String uri;
		private List<Condition> conditions;

		public PropertyDefinitionDummy(String identifier, String uri) {
			this(identifier, null, uri);
		}

		public PropertyDefinitionDummy(String identifier, ControlDefinition control, String uri) {
			this.identifier = identifier;
			this.control = control;
			this.uri = uri;
		}

		@Override
		public Integer getOrder() {
			return null;
		}

		@Override
		public PathElement getParentElement() {
			return null;
		}

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public String getLabel() {
			return getLabelId();
		}

		@Override
		public String getTooltip() {
			return getTooltipId();
		}

		@Override
		public String getLabelId() {
			return null;
		}

		@Override
		public String getTooltipId() {
			return null;
		}

		@Override
		public void setLabelProvider(LabelProvider labelProvider) {
			// nothing
		}

		@Override
		public List<Condition> getConditions() {
			return conditions;
		}

		@Override
		public void setContainer(String container) {
			// not used
		}

		@Override
		public void setDataType(DataTypeDefinition typeDefinition) {
			// not used
		}

		@Override
		public void setMultiValued(Boolean multiValued) {
			// not used
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public void setId(Long id) {
			// not used
		}

		@Override
		public String getName() {
			return getIdentifier();
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public DataTypeDefinition getDataType() {
			return null;
		}

		@Override
		public Boolean isOverride() {
			return null;
		}

		@Override
		public Boolean isMultiValued() {
			return null;
		}

		@Override
		public Boolean isMandatory() {
			return null;
		}

		@Override
		public Boolean isMandatoryEnforced() {
			return null;
		}

		@Override
		public Boolean isProtected() {
			return null;
		}

		@Override
		public Integer getMaxLength() {
			return null;
		}

		@Override
		public DisplayType getDisplayType() {
			return null;
		}

		@Override
		public Boolean isPreviewEnabled() {
			return null;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getParentPath() {
			return null;
		}

		@Override
		public Integer getCodelist() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

		@Override
		public ControlDefinition getControlDefinition() {
			return control;
		}

		@Override
		public String getRnc() {
			return null;
		}

		@Override
		public String getDmsType() {
			return null;
		}

		@Override
		public Set<String> getFilters() {
			return Collections.emptySet();
		}

		@Override
		public String getContainer() {
			return null;
		}

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public Long getPrototypeId() {
			return null;
		}

		@Override
		public String getUri() {
			return uri;
		}

		@Override
		public void initBidirection() {
			// not used
		}

		@Override
		public void setCodelist(Integer value) {
			// not used
		}

		@Override
		public void setDisplayType(DisplayType value) {
			// not used
		}

		@Override
		public void setMandatory(Boolean value) {
			// not used
		}

		@Override
		public void setMandatoryEnforced(Boolean mandatoryEnforced) {
			// not used
		}

		@Override
		public void setName(String value) {
			setIdentifier(value);
		}

		@Override
		public void setOverride(Boolean override) {
			// not used
		}

		@Override
		public void setRnc(String value) {
			// not used
		}

		@Override
		public void setType(String value) {
			// not used
		}

		@Override
		public void setValue(String value) {
			// not used
		}

		@Override
		public void setMaxLength(Integer maxLength) {
			// not used
		}

		@Override
		public void setRevision(Long revision) {
			// not used
		}

		@Override
		public void setParentPath(String parentPath) {
			// not used
		}

		@Override
		public void setPreviewEmpty(Boolean previewEmpty) {
			// not used
		}

		@Override
		public void setLabelId(String labelId) {
			// not used
		}

		@Override
		public void setTooltipId(String tooltipId) {
			// not used
		}

		@Override
		public void setControlDefinition(ControlDefinition controlDefinition) {
			control = controlDefinition;
		}

		@Override
		public void setOrder(Integer order) {
			// not used
		}

		@Override
		public void setDmsType(String dmsType) {
			// not used
		}

		@Override
		public void setDefaultProperties() {
			// not used
		}

		@Override
		public void setFilters(Set<String> filters) {
			// not used
		}

		@Override
		public void setConditions(List<Condition> conditions) {
			this.conditions = conditions;
		}

		@Override
		public void setHash(Integer hash) {
			// not used
		}

		@Override
		public void setPrototypeId(Long prototypeId) {
			// not used
		}

		@Override
		public void setUri(String uri) {
			this.uri = uri;
		}

		@Override
		public Set<String> getDependentFields() {
			Set<String> processed = new HashSet<>();
			if (getRnc() != null && !(getRnc().startsWith("$") || getRnc().startsWith("#"))) {
				processed.addAll(getRncFields(getRnc()));
			}
			if (getConditions() != null) {
				processed.addAll(getConditions()
						.stream()
							.map(Condition::getExpression)
							.flatMap(exp -> getRncFields(exp).stream())
							.collect(Collectors.toSet()));
			}
			return Collections.unmodifiableSet(processed);
		}

		public static Set<String> getRncFields(String expression) {
			if (org.apache.commons.lang3.StringUtils.isBlank(expression)) {
				return Collections.emptySet();
			}
			Set<String> fields = new LinkedHashSet<>();
			Matcher matcher = RNC_FIELD_PATTERN.matcher(expression);
			while (matcher.find()) {
				fields.add(matcher.group(1));
			}
			return fields;
		}
	}

	/**
	 * @author BBonev
	 */
	private static class ControlDefinitionDummy implements ControlDefinition {

		private static final long serialVersionUID = 1L;
		private List<PropertyDefinition> fields = new LinkedList<>();

		@Override
		public PathElement getParentElement() {
			return null;
		}

		@Override
		public String getPath() {
			return null;
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public void setIdentifier(String identifier) {
			// not used
		}

		@Override
		public List<PropertyDefinition> getFields() {
			return fields;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public void setHash(Integer hash) {
			// not used
		}

		@Override
		public JSONObject toJSONObject() {
			return null;
		}

		@Override
		public void fromJSONObject(JSONObject jsonObject) {
			// not used
		}

		@Override
		public List<ControlParam> getControlParams() {
			return Collections.emptyList();
		}

		@Override
		public List<ControlParam> getUiParams() {
			return Collections.emptyList();
		}
	}

	/**
	 * @author BBonev
	 */
	private static class ConditionDummy implements Condition {

		private final String expression;

		/**
		 * Instantiates a new condition dummy.
		 *
		 * @param expression
		 *            the expression
		 */
		public ConditionDummy(String expression) {
			this.expression = expression;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public void setIdentifier(String identifier) {
			// not used for now
		}

		@Override
		public String getRenderAs() {
			return null;
		}

		@Override
		public String getExpression() {
			return expression;
		}

	}
}
