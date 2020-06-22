package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.ModelActionExecution;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelActionExecution}.
 *
 * @author Boyan Tonchev.
 */
public class RestoreModelActionExecutionChangeSetOperation
		extends RestoreModelNodeChangeSetOperation<ModelActionExecution> {
	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelActionExecution;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelActionExecution targetNode,
			ModelChangeSet changeSet) {
		ModelAction action = targetNode.getContext();
		String actionExecutionId = targetNode.getId();

		ModelActionExecution modelActionExecution = action.getActionExecution(actionExecutionId);
		if (modelActionExecution == null) {
			return Stream.empty();
		}
		// to restore the node we restore all attributes and delete the node if empty
		return Stream.concat(restoreModelNodeAttributes(modelActionExecution),
							 Stream.of(RemoveModelNodeChangeSetOperation.createChange(changeSet.getPath())));
	}
}
