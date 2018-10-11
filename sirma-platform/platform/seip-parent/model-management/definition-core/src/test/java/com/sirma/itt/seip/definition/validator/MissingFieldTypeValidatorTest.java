package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.model.DataType;

public class MissingFieldTypeValidatorTest {

	private MissingFieldTypeValidator validator = new MissingFieldTypeValidator();

	@Test
	public void should_NotAllowFieldWsithoutType() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("(missing types):"));
	}


	@Test
	public void should_NotAllowFieldWsithoutType_LocatedInRegion() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("r1");
		region.getFields().add(field1);

		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("(missing types):"));
	}

	@Test
	public void should_NotAllowFieldWsithoutDataType() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setType("1");
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("(missing types):"));
	}

	@Test
	public void should_AllowFieldsWithTypeAndDataType() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setType("1");
		field1.setDataType(new DataType());
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);

		assertTrue(errors.isEmpty());
	}

}
