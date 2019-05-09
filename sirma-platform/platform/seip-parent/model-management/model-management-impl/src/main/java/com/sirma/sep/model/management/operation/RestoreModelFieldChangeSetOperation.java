package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelField}. The model field is removed from
 * it's containing definition and unregistered from the containing region if applicable.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2018
 */
public class RestoreModelFieldChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelField> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelField;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelField targetNode, ModelChangeSet changeSet) {
		ModelDefinition parentDefinition = targetNode.getContext();
		// notify the owning definition for the region removal
		ModelField modelField = parentDefinition.getFieldsMap().get(targetNode.getId());
		if (modelField == null) {
			return Stream.empty();
		}
		String newRegionId = null;
		ModelField parentReference = targetNode.getParentReference();
		if (parentReference != null) {
			newRegionId = parentReference.findRegionId();
		}
		return Stream.concat(AssignToRegionChangeSetOperation.createChange(changeSet.getPath(), newRegionId),
				restoreModelNodeAttributes(modelField));
	}
}
