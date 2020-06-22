package com.sirma.itt.seip.definition.validator;

import static com.sirma.itt.seip.definition.ValidationMessageUtils.hasError;
import static com.sirma.itt.seip.definition.validator.InvalidIdValidator.InvalidIdMessageBuilder.NON_ASCII_DEFINITION_ID;
import static com.sirma.itt.seip.definition.validator.InvalidIdValidator.InvalidIdMessageBuilder.NON_ASCII_ID;
import static com.sirma.itt.seip.definition.validator.InvalidIdValidator.InvalidIdMessageBuilder.NON_WORD_CHARACTERS_DEFINITION_ID;
import static com.sirma.itt.seip.definition.validator.InvalidIdValidator.InvalidIdMessageBuilder.NON_WORD_CHARACTERS_ID;
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
import com.sirma.itt.seip.domain.validation.ValidationMessage;

public class InvalidIdValidatorTest {

	private InvalidIdValidator validator = new InvalidIdValidator();

	@Test
	public void should_NotAllowNonWordCharactersInDefinitionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("@b");

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_WORD_CHARACTERS_DEFINITION_ID, "@b"));
	}

	@Test
	public void should_NotAllowNonWordCharactersInFieldId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("test");

		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setIdentifier("@");

		definition.getFields().add(field);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_WORD_CHARACTERS_ID, "test", "<field>", "@"));
	}

	@Test
	public void should_NotAllowNonAsciiDefinitionIdentifiers() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("µ1");

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_ASCII_DEFINITION_ID, "µ1", "µ"));
	}

	@Test
	public void should_NotAllowNonAsciiCharactersInFieldControlId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier("f1");

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("µ1");
		field.setControlDefinition(control);

		definition.getFields().add(field);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_ASCII_ID, "d1", "<control>", "µ1", "µ"));
	}

	@Test
	public void should_NotAllowNonAsciiCharactersInFieldConditionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setIdentifier("f1");

		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
		condition.setIdentifier("µ1");

		field.setConditions(new ArrayList<>());
		field.getConditions().add(condition);

		definition.getFields().add(field);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_ASCII_ID, "d1", "<condition>", "µ1", "µ"));
	}

	@Test
	public void shouldNotAllowNonAsciiCharactersInRegionId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("µ1");

		definition.getRegions().add(region);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_ASCII_ID, "d1", "<region>", "µ1", "µ"));
	}

	@Test
	public void shouldNotAllowNonAsciiCharactersInRegionControlId() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.setIdentifier("r1");
		definition.getRegions().add(region);

		ControlDefinitionImpl control = new ControlDefinitionImpl();
		control.setIdentifier("µ1");
		region.setControlDefinition(control);

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, NON_ASCII_ID, "d1", "<control>", "µ1", "µ"));
	}
}
