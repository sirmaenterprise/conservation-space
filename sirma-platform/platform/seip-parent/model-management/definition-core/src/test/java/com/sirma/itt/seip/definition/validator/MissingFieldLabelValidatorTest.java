package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.DisplayType;

/**
 * Tests for MissingFieldLabelValidator
 * 
 * @author Stella D
 */
public class MissingFieldLabelValidatorTest {

	private MissingFieldLabelValidator validator = new MissingFieldLabelValidator();

	@Test
	public void should_NotAllowFieldWithoutLabel_LocatedInRegion() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		RegionDefinitionImpl region = new RegionDefinitionImpl();
		region.getFields().add(field1);

		definition.getRegions().add(region);

		List<String> errors = validator.validate(definition);
		assertFalse(errors.isEmpty());
		assertTrue(errors.get(0).contains("(missing labels):"));
	}

	@Test
	public void should_NotAllowMissingLabel() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("(missing labels):"));
	}

	@Test
	public void should_AllowMissingLabelForSystemFields() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setDisplayType(DisplayType.SYSTEM);
		field1.setConditions(Collections.emptyList());
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);
		assertTrue(errors.isEmpty());
	}

	@Test
	public void should_NotShowErrorIfLabelExist() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setLabelId("Test Label");

		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);
		assertTrue(errors.isEmpty());
	}

	@Test
	public void should_NotShowErrorIfSystemFieldHasLabel() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setDisplayType(DisplayType.SYSTEM);
		Condition condition = Mockito.mock(Condition.class);
		List<Condition> conditions = new LinkedList<>();
		conditions.add(condition);
		field1.setConditions(conditions);
		field1.setLabelId("Test Label");
		definition.getFields().add(field1);

		List<String> errors = validator.validate(definition);
		assertTrue(errors.isEmpty());
	}

}
