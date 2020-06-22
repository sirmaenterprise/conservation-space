package com.sirma.sep.model.management.operation;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Change set operation for modifying a {@link ModelField}'s {@link DefinitionModelAttributes#REGION_ID} effectively placing
 * the field in a region or removing from it.
 *
 * @author Mihail Radkov
 */
public class AssignToRegionChangeSetOperation implements ModelChangeSetOperation<ModelField> {

	public static final String OPERATION_NAME = "assignToRegion";

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelField;
	}

	@Override
	public boolean validate(Models models, ModelField targetField, ModelChangeSet changeSet) {
		ModelChangeSetOperation.super.validate(models, targetField, changeSet);
		validateChangeSet(changeSet);
		// Skip hierarchy resolving because we would not be able to override
		return !nullSafeEquals(targetField.getRegionId(), changeSet.getNewValue());
	}

	private static void validateChangeSet(ModelChangeSet changeSet) {
		// We currently care about the new value only.
		Object newValue = changeSet.getNewValue();
		if (newValue != null && !(newValue instanceof String)) {
			throw new ChangeSetValidationFailed(
					"New value for " + changeSet.getPath().prettyPrint() + " is of unexpected type "
							+ newValue.getClass().getSimpleName());
		}
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelField targetField, ModelChangeSet changeSet) {
		// Remove the region attribute + the field from the region (if any)
		removeRegion(targetField);

		Object newRegion = changeSet.getNewValue();
		if (newRegion != null) {
			targetField.setRegionId(newRegion.toString());
			ModelDefinition modelDefinition = targetField.getContext();
			// this will create new region in the current model definition if not already present
			// and will effectively override it from the parent definition if any
			ModelRegion modelRegion = (ModelRegion) modelDefinition.walk(ModelRegion.createPath(newRegion.toString()));
			modelRegion.addField(targetField.getId());
		}
		return Stream.empty();
	}

	private static void removeRegion(ModelField targetField) {
		String removedRegion = targetField.getRegionId();
		if (removedRegion != null) {
			targetField.setRegionId(null);
			ModelDefinition modelDefinition = targetField.getContext();
			ModelRegion modelRegion = modelDefinition.getRegionsMap().get(removedRegion);
			if (modelRegion != null) {
				modelRegion.removeField(targetField.getId());
			}
			modelDefinition.getRegions().removeIf(ModelRegion::isEmpty);
		}
	}

	public static Stream<ModelChangeSetInfo> createChange(Path selector, Object newValue) {
		return Stream.of(ModelChangeSetInfo.createIntermediate(selector, OPERATION_NAME, newValue, null));
	}

	@Override
	public String getName() {
		return OPERATION_NAME;
	}
}
