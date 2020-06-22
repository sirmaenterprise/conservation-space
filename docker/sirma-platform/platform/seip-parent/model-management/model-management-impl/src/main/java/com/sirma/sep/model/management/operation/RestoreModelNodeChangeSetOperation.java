package com.sirma.sep.model.management.operation;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Path;

/**
 * Base operation for restoring inheritance of different model nodes
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2018
 */
public abstract class RestoreModelNodeChangeSetOperation<M> implements ModelChangeSetOperation<M> {

	public static final String OPERATION_NAME = "restore";

	/**
	 * Creates are intermediate change set for restore operation of a node or attribute identified by the given path.
	 *
	 * @param selector a path to the node to restore
	 * @return intermediate change set info instance for restore operation
	 */
	public static ModelChangeSetInfo createChange(Path selector) {
		return ModelChangeSetInfo.createIntermediate(selector, OPERATION_NAME, null, null);
	}

	/**
	 * Returns a stream consisting of changes that trigger restore of all nodes attributes
	 *
	 * @param modelNode the node attributes to restore
	 * @return steam of changes
	 */
	public static Stream<ModelChangeSetInfo> restoreModelNodeAttributes(ModelNode modelNode) {
		return Stream.concat(getRestoreAttributesChangeSets(modelNode),
				Stream.of(RemoveModelNodeChangeSetOperation.createChange(modelNode.getPath())));
	}

	private static Stream<ModelChangeSetInfo> getRestoreAttributesChangeSets(ModelNode modelNode) {
		// operate on a copy of the attributes as during the streaming the other operations will try
		// to modify the same collection that is going to cause ConcurrentModificationException
		return new ArrayList<>(modelNode.getAttributes()).stream()
				.map(ModelAttribute::getPath)
				.map(RestoreModelAttributeInternalChangeSetOperation::createChange);
	}

	@Override
	public String getName() {
		return OPERATION_NAME;
	}
}
