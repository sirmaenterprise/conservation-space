package com.sirma.sep.model.management.operation;

import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;

import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelAttributeType;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Test for {@link ModifyMapAttributeChangeSetOperation}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 10/08/2018
 */
public class ModifyMapAttributeChangeSetOperationTest {

	@Test(expected = ChangeSetCollisionException.class)
	public void validate_shouldFailOnValueAdditionCollision() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setNewValue(createStringMap("en=New en value")).setSelector("/test=someAttribute");
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void validate_shouldAcceptValueAddition() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setNewValue(createStringMap("bg=Bg value"));
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void validate_shouldAcceptRepetitiveValueAddition() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setNewValue(createStringMap("en=En value"));
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void applyChange_shouldAcceptValueAddition() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setNewValue(createStringMap("bg=Bg value"));

		operation.applyChange(new Models(), attribute, changeSet);

		Map<String, String> value = (Map<String, String>) attribute.getValue();
		assertEquals(2, value.size());
		assertEquals("En value", value.get("en"));
		assertEquals("Bg value", value.get("bg"));
	}

	@Test(expected = ChangeSetCollisionException.class)
	public void validate_shouldFailOnValueRemovalCollision() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=Some other en value")).setSelector("/test=someAttribute");
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test(expected = ChangeSetValidationFailed.class)
	public void validate_shouldFailOnInvalidValueProvided() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(new Integer(12)).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(new Double(5.)).setSelector("/test=someAttribute");
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void validate_shouldAcceptValueRemoval() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=En value"));
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void validate_shouldAcceptRepetitiveValueRemoval() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=En value"));
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void applyChange_shouldAcceptValueRemoval() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=En value"));

		operation.applyChange(new Models(), attribute, changeSet);

		Map<String, String> value = (Map<String, String>) attribute.getValue();
		assertEquals(1, value.size());
		assertNull(value.get("en"));
		assertEquals("Bg value", value.get("bg"));
	}

	@Test
	public void validate_shouldAcceptValueUpdate() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=En value")).setNewValue(createStringMap("en=New en value"));
		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void applyChange_shouldAcceptValueUpdate() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue(createStringMap("en=En value")).setNewValue(createStringMap("en=New en value"));

		operation.applyChange(new Models(), attribute, changeSet);

		Map<String, String> value = (Map<String, String>) attribute.getValue();
		assertEquals(2, value.size());
		assertEquals("New en value", value.get("en"));
		assertEquals("Bg value", value.get("bg"));
	}

	@Test
	public void validate_shouldAcceptValueUpdate_fromSingleEnValue() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue("En value").setNewValue("New en value");

		operation.validate(new Models(), attribute, changeSet);
	}

	@Test(expected = ChangeSetCollisionException.class)
	public void validate_shouldNotAcceptValueUpdate_fromSingleNonEnValue() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue("Bg value").setNewValue("New bg value").setSelector("/test=someAttribute");

		operation.validate(new Models(), attribute, changeSet);
	}

	@Test
	public void applyChange_shouldAcceptValueUpdate_fromSingleEnValue() throws Exception {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setOldValue("En value").setNewValue("New en value");

		operation.applyChange(new Models(), attribute, changeSet);

		Map<String, String> value = (Map<String, String>) attribute.getValue();
		assertEquals(2, value.size());
		assertEquals("New en value", value.get("en"));
		assertEquals("Bg value", value.get("bg"));
	}

	@Test
	public void shouldNormalizeUpperCaseLanguageKeys() {
		ModifyMapAttributeChangeSetOperation operation = new ModifyMapAttributeChangeSetOperation();
		ModelAttribute attribute = getAttribute().setValue(createStringMap("en=En value", "bg=Bg value")).setName("someAttribute");
		ModelChangeSet changeSet = new ModelChangeSet().setNewValue(createStringMap("EN=New EN value", "BG=New BG value"));

		operation.applyChange(new Models(), attribute, changeSet);

		Map<String, String> value = (Map<String, String>) attribute.getValue();
		assertEquals(2, value.size());
		assertEquals("New EN value", value.get("en"));
		assertEquals("New BG value", value.get("bg"));
	}

	private static ModelAttribute getAttribute() {
		return new ModelAttribute().setMetaInfoProvider(s -> getMetaInfo());
	}

	private static ModelMetaInfo getMetaInfo() {
		ModelMetaInfo modelMetaInfo = new ModelMetaInfo();
		modelMetaInfo.setType(ModelAttributeType.MULTI_LANG_STRING);
		return modelMetaInfo;

	}
}
