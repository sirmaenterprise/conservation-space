package com.sirma.sep.model.management.operation;

import com.sirma.sep.model.management.Path;

/**
 * Base operation for removing {@link com.sirma.sep.model.ModelNode} or {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/12/2018
 */
public abstract class RemoveNodeChangeSetOperation<M> implements ModelChangeSetOperation<M> {

	public static final String OPERATION_NAME = "remove";

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
	 * Creates are intermediate change set for restore operation of a node or attribute identified by the given path.
	 *
	 * @param changeSet
	 *            a {@link ModelChangeSet} instance from which to create intermediate {@link ModelChangeSetInfo}
	 * @return intermediate change set info instance for restore operation
	 */
	public static ModelChangeSetInfo createChange(ModelChangeSet changeSet) {
		return ModelChangeSetInfo.createIntermediate(changeSet, OPERATION_NAME);
	}

	@Override
	public String getName() {
		return OPERATION_NAME;
	}
}
