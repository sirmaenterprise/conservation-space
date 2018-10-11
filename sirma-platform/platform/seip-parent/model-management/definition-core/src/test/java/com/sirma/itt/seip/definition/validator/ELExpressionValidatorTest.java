package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Tests for {@link ELExpressionValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 07/09/2017
 */
public class ELExpressionValidatorTest {
	private final class LabelDefinitionImplementation implements LabelDefinition {

		private String identifier;

		private final Map<String, String> labels = new HashMap<>();

		@Override
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public void setId(Long id) {
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public Map<String, String> getLabels() {
			return labels;
		}
	}

	@Test
	public void testFieldValidation() {
		ELExpressionValidator validator = new ELExpressionValidator();
		FieldDefinitionImpl model = new FieldDefinitionImpl();
		model.setIdentifier("testField");
		model.setValue("");
		model.setRnc("");
		assertTrue(validator.validate(model).isEmpty());

		model.setValue("${eval(${get([test])})}");
		assertTrue(validator.validate(model).isEmpty());

		model.setRnc("${eval(${get([test])})}");
		assertTrue(validator.validate(model).isEmpty());

		model.setRnc("${eval(${get([test]})}");
		assertFalse(validator.validate(model).isEmpty());

		model.setValue("${eval($get([test])})}");
		assertFalse(validator.validate(model).isEmpty());

		model.setRnc(null);
		assertFalse(validator.validate(model).isEmpty());

		model.setValue(null);
		assertTrue(validator.validate(model).isEmpty());
	}

	@Test
	public void testLabelValidation() {
		ELExpressionValidator validator = new ELExpressionValidator();
		LabelDefinitionImplementation model = new LabelDefinitionImplementation();
		model.setIdentifier("test.label");

		assertTrue(validator.validate(model).isEmpty());

		model.getLabels()
				.put("en", "${eval(<a class=\"${get([status])}\" href=\"${link(currentInstance)}\">"
						+ "<b>${id} ${CL([type])}" + " (${CL([status])})</b></a><br />актуализирана от: " + "<a href=\"${userLink(${get([modifiedBy])})}\">" + "${user(${get([modifiedBy])})}</a>,"
						+ " ${date([modifiedOn]).format(dd.MM.yyyy, HH:mm)})}");
		assertTrue(validator.validate(model).isEmpty());

		model.getLabels()
				.put("bg", "${eval(<a class=\"${get([status])}\" href=\"${link(currentInstance)}\">"
						+ "<b>${id} ${CL{[type])}" + " (${CL([status])})</b></a><br />актуализирана от: " + "<a href=\"${userLink(${get([modifiedBy])})}\">" + "${user(${get([modifiedBy])})}</a>,"
						+ " ${date([modifiedOn]).format(dd.MM.yyyy, HH:mm)})}");
		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void test_validate_without_control() {
		FieldDefinitionImpl model = new FieldDefinitionImpl();
		model.setControlDefinition(null);
		ELExpressionValidator validator = new ELExpressionValidator();
		assertTrue(validator.validate(model).isEmpty());
	}

	@Test
	public void test_validate_invalidExpression() {
		ELExpressionValidator validator = new ELExpressionValidator();
		PropertyDefinitionMock model = mockFieldWithExpression("{${sequence(text)}");
		assertFalse(validator.validate(model).isEmpty());
		model = mockFieldWithExpression("{${sequence(text)]}");
		assertFalse(validator.validate(model).isEmpty());
	}

	@Test
	public void test_validate_validExpression() {
		PropertyDefinitionMock model = mockFieldWithExpression("{${sequence(text)[more text]|label}}");
		ELExpressionValidator validator = new ELExpressionValidator();
		assertTrue(validator.validate(model).isEmpty());
	}

	@Test
	public void test_validate_definitionModel() {
		ELExpressionValidator validator = new ELExpressionValidator();
		RegionDefinitionImpl region = new RegionDefinitionImpl();
		PropertyDefinitionMock field = mockFieldWithExpression("{${seq(text)[more text]|label}}");
		region.setFields(Collections.singletonList(field));
		assertTrue(validator.validate(region).isEmpty());
	}

	@Test
	public void test_validate_regionModel() {
		ELExpressionValidator validator = new ELExpressionValidator();
		DefinitionMock genericDefinition = new DefinitionMock();
		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setControlDefinition(
				mockFieldWithExpression("{${sequence(text)[more $(something)text]|label}}").getControlDefinition());
		genericDefinition.setRegions(Collections.singletonList(region));
		assertTrue(validator.validate(genericDefinition).isEmpty());
	}

	private static PropertyDefinitionMock mockFieldWithExpression(String expression) {
		PropertyDefinitionMock model = new PropertyDefinitionMock();

		ControlParamImpl controlParam = new ControlParamImpl();
		controlParam.setIdentifier("template");
		controlParam.setName("template");
		controlParam.setType("default_value_pattern");
		controlParam.setValue(expression);

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setControlParams(Collections.singletonList(controlParam));

		model.setControlDefinition(control);
		return model;
	}
}