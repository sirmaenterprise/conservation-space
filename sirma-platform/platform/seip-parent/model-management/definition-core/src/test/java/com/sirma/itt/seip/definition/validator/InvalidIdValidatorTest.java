package com.sirma.itt.seip.definition.validator;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;

public class InvalidIdValidatorTest {

	private InvalidIdValidator validator = new InvalidIdValidator();

	@Test
	public void should_NotAllowNonWordCharactersInDefinitionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("@");

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non word character"));
	}

	@Test
	public void should_NotAllowNonWordCharactersInFieldId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("test");

		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setIdentifier("@");

		definition.getFields().add(field);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non word character in ID='@'"));
	}

	@Test
	public void should_NotAllowNonAsciiCharactersInFieldControlId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier("f1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("µ");
		field.setControlDefinition(control);

		definition.getFields().add(field);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non ASCII character in ID='µ'"));
	}

	@Test
	public void should_NotAllowNonAsciiCharactersInFieldConditionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier("f1");

		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setIdentifier("µ");

		field.setConditions(new ArrayList<>());
		field.getConditions().add(condition);

		definition.getFields().add(field);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non ASCII character in ID='µ'"));
	}

	@Test
	public void shouldNotAllowNonAsciiCharactersInRegionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("µ");

		definition.setRegions(new ArrayList<>());
		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non ASCII character in ID='µ'"));
	}

	@Test
	public void shouldNotAllowNonAsciiCharactersInRegionControlId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("r1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("µ");
		region.setControlDefinition(control);

		definition.setRegions(new ArrayList<>());
		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("Found non ASCII character in ID='µ'"));
	}
}
