package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelControl;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelControl}.
 *
 * @author Stella Djulgerova
 */
public class RestoreModelControlChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelControl> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelControl;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelControl targetNode, ModelChangeSet changeSet) {
		ModelField modelField = targetNode.getContext();
		String controlId = targetNode.getId();

		ModelControl modelControl = modelField.getControlsMap().get(controlId);
		if (modelControl == null) {
			return Stream.empty();
		}
		// to restore the control we delete the node
		return Stream.of(RemoveModelNodeChangeSetOperation.createChange(changeSet.getPath()));
	}
}