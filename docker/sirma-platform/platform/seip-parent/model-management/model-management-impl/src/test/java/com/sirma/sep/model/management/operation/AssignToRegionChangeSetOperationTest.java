package com.sirma.sep.model.management.operation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.meta.ModelMetaInfo;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * @author Mihail Radkov
 */
public class AssignToRegionChangeSetOperationTest {

	private AssignToRegionChangeSetOperation operation = new AssignToRegionChangeSetOperation();
	private Models models = new Models();
	private ModelField field;

	@Before
	public void init() {
		ModelsMetaInfo modelsMetaInfo = new ModelsMetaInfo();
		modelsMetaInfo.setFields(Collections.singletonList(new ModelMetaInfo().setId("displayType")));
		models.setModelsMetaInfo(modelsMetaInfo);
		field = new ModelField().setModelsMetaInfo(models.getModelsMetaInfo());
	}

	@Test
	public void shouldValidateOperation() {
		boolean isValid = operation.validate(models, field, getChangeSet().setOldValue("previousRegion").setNewValue("newRegion"));
		assertTrue(isValid);
	}

	@Test
	public void shouldNotExecuteOperationForTheSameRegionId() {
		field.setRegionId("newRegion");
		boolean isValid = operation.validate(models, field, getChangeSet().setOldValue("previousRegion").setNewValue("newRegion"));
		assertFalse(isValid);
	}

	@Test(expected = ChangeSetValidationFailed.class)
	public void shouldDisallowInvalidModelTypeForNewValue() {
		operation.validate(models, field, getChangeSet().setOldValue("previousRegion").setNewValue(Collections.EMPTY_MAP));
	}

	@Test
	public void shouldAllowEmptyNewValue() {
		field.setRegionId("region");
		boolean isValid = operation.validate(models, field, getChangeSet().setOldValue("previousRegion").setNewValue(null));
		assertTrue(isValid);
	}

	private static ModelChangeSet getChangeSet() {
		return new ModelChangeSet().setSelector("definition=PR0001/field=title").setOperation("assignToRegion");
	}
}
