package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelAction}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/11/2018
 */
public class RestoreModelActionChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelAction> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelAction;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAction targetNode, ModelChangeSet changeSet) {
		ModelDefinition parentDefinition = targetNode.getContext();
		String actionId = targetNode.getId();

		ModelAction action = parentDefinition.getActionsMap().get(actionId);
		if (action == null) {
			return Stream.empty();
		}
		// to restore the node we restore all attributes and delete the node if empty
		return Stream.concat(
				restoreModelNodeAttributes(action),
				Stream.of(RemoveModelNodeChangeSetOperation.createChange(changeSet.getPath())));
	}
}
