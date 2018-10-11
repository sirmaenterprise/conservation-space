package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;

public class DuplicateControlFieldIdValidatorTest {

	private DuplicateControlFieldIdValidator validator = new DuplicateControlFieldIdValidator();

	@Test
	public void should_NotAllowDuplicateControlFieldIds() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("c1");

		PropertyDefinitionProxy controlField = new PropertyDefinitionProxy();
		controlField.setIdentifier("f1");
		control.getFields().add(controlField);

		PropertyDefinitionProxy controlField2 = new PropertyDefinitionProxy();
		controlField2.setIdentifier("f1");
		control.getFields().add(controlField2);

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setControlDefinition(control);

		definition.getFields().add(field);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("Found duplicate field IDs in control c1"));
		assertTrue(errors.get(1).contains("Found duplicate field IDs in region model d1"));
	}

	@Test
	public void should_NotAllowDuplicateFieldIds_InsideRegion() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("c1");

		PropertyDefinitionProxy controlField = new PropertyDefinitionProxy();
		controlField.setIdentifier("f1");
		control.getFields().add(controlField);

		PropertyDefinitionProxy controlField2 = new PropertyDefinitionProxy();
		controlField2.setIdentifier("f1");
		control.getFields().add(controlField2);

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setControlDefinition(control);

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("r1");
		region.getFields().add(field);

		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("Found duplicate field IDs in control c1"));
		assertTrue(errors.get(1).contains("Found duplicate field IDs in region r1"));
	}

	@Test
	public void should_NotAllowDuplicateFieldIds_InsideNestedRegion() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("c1");

		PropertyDefinitionProxy controlField = new PropertyDefinitionProxy();
		controlField.setIdentifier("f1");
		control.getFields().add(controlField);

		PropertyDefinitionProxy controlField2 = new PropertyDefinitionProxy();
		controlField2.setIdentifier("f1");
		control.getFields().add(controlField2);

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setControlDefinition(control);

		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("Found duplicate field IDs in control c1"));
		assertTrue(errors.get(1).contains("Found duplicate field IDs in regions controls d1"));
	}

}
