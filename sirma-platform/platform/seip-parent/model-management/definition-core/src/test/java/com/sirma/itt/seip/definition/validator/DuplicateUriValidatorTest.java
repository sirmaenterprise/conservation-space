package com.sirma.itt.seip.definition.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;

public class DuplicateUriValidatorTest {

	private DuplicateUriValidator validator = new DuplicateUriValidator();

	@Test
	public void should_NotAllowDuplicateFieldUri() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setUri("http://1.com");
		definition.getFields().add(field1);

		PropertyDefinitionProxy field2 = new PropertyDefinitionProxy();
		field2.setIdentifier("f2");
		field2.setUri("http://1.com");
		definition.getFields().add(field2);

		List<String> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());

		assertTrue(errors.get(0).contains("(duplicate use of uri) : http://1.com"));
	}

	@Test
	public void should_AllowFieldsWithDifferentUris() {
		GenericDefinitionImpl definition = new GenericDefinitionImpl();
		definition.setIdentifier("d1");

		PropertyDefinitionProxy field1 = new PropertyDefinitionProxy();
		field1.setIdentifier("f1");
		field1.setUri("http://1.com");
		definition.getFields().add(field1);

		PropertyDefinitionProxy field2 = new PropertyDefinitionProxy();
		field2.setIdentifier("f2");
		field2.setUri("http://2.com");
		definition.getFields().add(field2);

		List<String> errors = validator.validate(definition);

		assertTrue(errors.isEmpty());
	}

}
