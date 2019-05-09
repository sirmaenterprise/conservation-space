package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for restoring a single model attribute.
 * <p>
 * If the provided attribute is the last one in its context then that context will be removed.
 *
 * @author Mihail Radkov
 */
public class RestoreModelAttributeChangeSetOperation extends RestoreModelAttributeInternalChangeSetOperation {

	@Override
	public String getName() {
		return RestoreModelNodeChangeSetOperation.OPERATION_NAME;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		ModelNode targetNodeContext = targetNode.getContext();
		return Stream.concat(
				// First remove the attribute
				super.applyChange(models, targetNode, changeSet),
				// And then remove the context node if this was the last attribute
				Stream.of(RemoveNodeChangeSetOperation.createChange(targetNodeContext.getPath())));
	}

}
