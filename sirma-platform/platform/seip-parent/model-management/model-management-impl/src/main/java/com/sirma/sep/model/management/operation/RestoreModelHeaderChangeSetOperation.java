package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelHeader;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring {@link ModelHeader}.
 *
 * @author Mihail Radkov
 */
public class RestoreModelHeaderChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelHeader> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelHeader;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelHeader targetNode, ModelChangeSet changeSet) {
		ModelDefinition parentDefinition = targetNode.getContext();
		String headerId = targetNode.getId();

		ModelHeader header = parentDefinition.getHeadersMap().get(headerId);
		if (header == null) {
			return Stream.empty();
		}
		// to restore the node we restore all attributes and delete the node if empty
		return Stream.concat(
				restoreModelNodeAttributes(header),
				Stream.of(RemoveModelNodeChangeSetOperation.createChange(changeSet.getPath())));
	}
}
