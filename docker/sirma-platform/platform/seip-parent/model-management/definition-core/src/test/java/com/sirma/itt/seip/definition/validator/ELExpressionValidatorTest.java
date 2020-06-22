package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Tests for {@link ELExpressionValidator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 07/09/2017
 */
public class ELExpressionValidatorTest {

	private ELExpressionValidator validator;

	@Before
	public void init() {
		validator = new ELExpressionValidator();
	}

	@Test
	public void testFieldValidation() {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setIdentifier("testField");
		field.setValue("");
		field.setRnc("");

		GenericDefinition definition = new GenericDefinitionImpl();
		definition.getFields().add(field);

		assertTrue(validator.validate(definition).isEmpty());

		field.setValue("${eval(${get([test])})}");
		assertTrue(validator.validate(definition).isEmpty());

		field.setRnc("${eval(${get([test])})}");
		assertTrue(validator.validate(definition).isEmpty());

		field.setRnc("${eval(${get([test]})}");
		assertFalse(validator.validate(definition).isEmpty());

		field.setValue("${eval($get([test])})}");
		assertFalse(validator.validate(definition).isEmpty());

		field.setRnc(null);
		assertFalse(validator.validate(definition).isEmpty());

		field.setValue(null);
		assertTrue(validator.validate(definition).isEmpty());
	}

	@Test
	public void test_validate_without_control() {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setControlDefinition(null);

		GenericDefinition definition = new GenericDefinitionImpl();
		definition.getFields().add(field);

		assertTrue(validator.validate(definition).isEmpty());
	}

	@Test
	public void test_validate_invalidExpression() {
		// Missing closing }
		PropertyDefinitionMock field = mockFieldWithExpression("{${sequence(text)}");

		GenericDefinition definition = new GenericDefinitionImpl();
		definition.getFields().add(field);

		assertFalse(validator.validate(definition).isEmpty());

		field = mockFieldWithExpression("{${sequence(text)]}");
		definition.getFields().clear();
		definition.getFields().add(field);

		assertFalse(validator.validate(definition).isEmpty());
	}

	@Test
	public void test_validate_validExpression() {
		PropertyDefinitionMock field = mockFieldWithExpression("{${sequence(text)[more text]|label}}");

		GenericDefinition definition = new GenericDefinitionImpl();
		definition.getFields().add(field);

		assertTrue(validator.validate(definition).isEmpty());
	}

	@Test
	public void test_validate_definitionModel() {
		PropertyDefinitionMock field = mockFieldWithExpression("{${seq(text)[more text]|label}}");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setFields(Collections.singletonList(field));

		GenericDefinition definition = new GenericDefinitionImpl();
		definition.getRegions().add(region);

		assertTrue(validator.validate(definition).isEmpty());
	}

	@Test
	public void test_validate_regionModel() {
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
