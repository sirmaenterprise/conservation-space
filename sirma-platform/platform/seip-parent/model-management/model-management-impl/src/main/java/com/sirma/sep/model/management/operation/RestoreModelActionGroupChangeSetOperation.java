package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelActionGroup;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring the inheritance of a {@link ModelActionGroup}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/11/2018
 */
public class RestoreModelActionGroupChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelActionGroup> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelActionGroup;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelActionGroup targetNode, ModelChangeSet changeSet) {
		ModelDefinition parentDefinition = targetNode.getContext();
		String actionId = targetNode.getId();

		ModelActionGroup actionGroup = parentDefinition.getActionGroupsMap().get(actionId);
		if (actionGroup == null) {
			return Stream.empty();
		}
		// to restore the node we restore all attributes and delete the node if empty
		return Stream.concat(
				restoreModelNodeAttributes(actionGroup),
				Stream.of(RemoveModelNodeChangeSetOperation.createChange(changeSet.getPath())));
	}
}
