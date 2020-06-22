package com.sirma.itt.seip.definition.validator;

import static com.sirma.itt.seip.definition.ValidationMessageUtils.hasError;
import static com.sirma.itt.seip.definition.validator.DuplicateUriValidator.DuplicateUriMessageBuilder.DUPLICATED_URI;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

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

		List<ValidationMessage> errors = validator.validate(definition);

		assertFalse(errors.isEmpty());
		assertTrue(hasError(errors, DUPLICATED_URI, "d1", "http://1.com", "[f1, f2]"));
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

		List<ValidationMessage> errors = validator.validate(definition);

		assertTrue(errors.isEmpty());
	}

}
