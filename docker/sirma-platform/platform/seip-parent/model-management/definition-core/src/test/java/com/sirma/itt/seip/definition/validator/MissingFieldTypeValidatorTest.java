package com.sirma.itt.seip.definition.validator;

import static com.sirma.itt.seip.definition.ValidationMessageUtils.hasError;
import static com.sirma.itt.seip.definition.validator.MissingFieldTypeValidator.MissingFieldTypeMessageBuilder.MISSING_FIELD_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.model.DataType;

public class MissingFieldTypeValidatorTest {

	private MissingFieldTypeValidator validator = new MissingFieldTypeValidator();

	@Test
	public void should_NotAllowFieldWithoutType() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		definition.getFields().add(field1);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELD_TYPE, "d1", "f1"));
	}

	@Test
	public void should_NotAllowFieldWithoutType_LocatedInRegion() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("r1");
		region.getFields().add(field1);

		definition.getRegions().add(region);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELD_TYPE, "d1", "f1"));
	}

	@Test
	public void should_NotAllowFieldWithoutDataType() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setType("1");
		definition.getFields().add(field1);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, MISSING_FIELD_TYPE, "d1", "f1"));
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

		List<ValidationMessage> errors = validator.validate(definition);
		assertTrue(errors.isEmpty());
	}

}
