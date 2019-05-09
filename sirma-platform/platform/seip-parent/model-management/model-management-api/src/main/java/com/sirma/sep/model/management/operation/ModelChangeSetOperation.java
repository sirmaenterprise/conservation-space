package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.itt.seip.Named;
import com.sirma.sep.model.management.Models;

/**
 * Represents an action that can be executed on model management node to apply changes represented by
 * {@link ModelChangeSet}. The operation should provide an input data validation if applicable in order to validate the
 * current state of the model before actual invocation of the action to modify the model.
 *
 * @param <M> supported model node type
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public interface ModelChangeSetOperation<M> extends Named {
	/**
	 * Tests if the given model node is applicable for the current operation
	 *
	 * @param target the model node to test
	 * @return true if it's accepted and can proceed with validation phase
	 */
	boolean isAccepted(Object target);

	/**
	 * Called to validate the model node and the change set that should be applied over the node. The validation could
	 * include also a check if applying the change will modify the model. For example someone already performed the same
	 * change over the model and the new change will do nothing. This is optional behaviour and reported via the
	 * return of the method. If implementation does not support such checks it should return always {@code true}.
	 * Returning {@code false} will cause skipping the apply phase
	 *
	 * @param models the root {@link Models} that houses the given model node
	 * @param targetNode is the target node that should be tested if it's compatible with the change set
	 * @param changeSet is the change set that needs to be applied if validation passes
	 * @return {@code true} indicating that if the change is applied then the model will be modified. if {@code false}
	 * the model will not be modified it also means that the model is in already in this state and the given change will
	 * do nothing to the model.
	 * @throws ChangeSetValidationFailed in case of integrity checks failed and can not proceed with model modification
	 * phase
	 */
	default boolean validate(Models models, M targetNode, ModelChangeSet changeSet) {
		if (models == null) {
			throw new ChangeSetValidationFailed("Missing Models instance argument");
		} else if (targetNode == null) {
			throw new ChangeSetValidationFailed("Missing target node argument");
		} else if (changeSet == null) {
			throw new ChangeSetValidationFailed("Missing change set argument");
		}
		return true;
	}

	/**
	 * Apply model change set to target model node.
	 *
	 * @param models The root instance that houses the target model node
	 * @param targetNode the target node that need to be modified by the given change set. The node is mainly resolved
	 * using <code>changeSet.getPath().walk(models)</code>
	 * @param changeSet the change set that need to be applied
	 * @return A stream of intermediate changes that should be applied after the current one or {@link Stream#empty()}
	 * if nothing else is needed
	 */
	Stream<ModelChangeSetInfo> applyChange(Models models, M targetNode, ModelChangeSet changeSet);

	/**
	 * Remove changes added by the given change set. The current model state should be the same as after calling
	 * {@link #applyChange(Models, Object, ModelChangeSet)} to the same node from the same model.
	 *
	 * @param models the root instance that houses the target model
	 * @param targetNode the model node that need to be reverted. It should have the same state just after invocation
	 * of {@link #applyChange(Models, Object, ModelChangeSet) applyChange}
	 * @param changeSet that change set that need to be reverted
	 */
	default void rollbackChange(Models models, M targetNode, ModelChangeSet changeSet) {
		throw new UnsupportedOperationException();
	}
}
