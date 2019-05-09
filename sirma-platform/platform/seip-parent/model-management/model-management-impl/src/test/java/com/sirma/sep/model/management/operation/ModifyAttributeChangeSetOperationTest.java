package com.sirma.sep.model.management.operation;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Test for {@link ModifyAttributeChangeSetOperation}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/07/2018
 */
public class ModifyAttributeChangeSetOperationTest {

	private Models models;
	private ModifyAttributeChangeSetOperation operation;
	private ModelField field;

	@Before
	public void setUp() {
		models = new Models();
		models.setModelsMetaInfo(getModelsMetaInfo());
		operation = new ModifyAttributeChangeSetOperation();
		field = new ModelField().setModelsMetaInfo(models.getModelsMetaInfo());
	}

	@Test(expected = ChangeSetValidationFailed.class)
	public void validate_shouldFailIfValuesDoesNotMatch() throws Exception {
		operation.validate(models, createDisplayTypeAttribute().setValue("initialValue"),
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType")
						.setOldValue("someValue").setNewValue("previousValue"));
	}

	@Test
	public void validate_shouldNotFailIfValueIsNull() throws Exception {
		operation.validate(models, createDisplayTypeAttribute(),
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType")
						.setOldValue("someValue").setNewValue("previousValue"));
	}

	@Test
	public void validate_shouldNotFailIfValueIsNotEqualToPreviousButIsSameAsTheNewOne() throws Exception {
		operation.validate(models, createDisplayTypeAttribute(),
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType")
						.setOldValue("someValue"));
	}

	@Test
	public void validate_shouldPassIfValuesMatch() throws Exception {
		operation.validate(models, createDisplayTypeAttribute(),
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType"));
	}

	@Test
	public void applyChange_shouldSetTheNewValue() throws Exception {
		ModelDefinition definition = new ModelDefinition().setId("PR0001").setModelsMetaInfo(models.getModelsMetaInfo());
		ModelAttribute modelAttribute = (ModelAttribute) definition.select("field=title/attribute=displayType");
		operation.applyChange(models, modelAttribute,
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType")
						.setNewValue("SYSTEM"));

		// the change adds new attribute instance to the definition so we need to select it again
		modelAttribute = (ModelAttribute) definition.select("field=title/attribute=displayType");
		assertEquals("SYSTEM", modelAttribute.getValue());
	}

	private ModelAttribute createDisplayTypeAttribute() {
		return new ModelAttribute().setName("displayType")
				.setContext(field)
				.setMetaInfoProvider(field.getAttributesMetaInfo()::get);
	}

	@Test
	public void validate_shouldNotFailWhenDefaultValueIsNullAndIfPreviousValueIsEqualToDefaultValue() throws Exception {
		// override the default value in the meta model
		models.getModelsMetaInfo().getFieldsMapping().get("displayType").setDefaultValue("defaultValue");

		ModelField field = new ModelField().setModelsMetaInfo(models.getModelsMetaInfo());
		operation.validate(models, createDisplayTypeAttribute().setContext(field).setMetaInfoProvider(field.getAttributesMetaInfo()::get),
				new ModelChangeSet().setSelector("definition=PR0001/field=title/attribute=displayType")
						.setOldValue("defaultValue").setNewValue("someValue"));
	}

	private ModelsMetaInfo getModelsMetaInfo() {
		ModelsMetaInfo modelsMetaInfo = new ModelsMetaInfo();

		ModelMetaInfo displayType = new ModelMetaInfo().setId("displayType");
		modelsMetaInfo.setFields(Collections.singletonList(displayType));

		return modelsMetaInfo;
	}
}
