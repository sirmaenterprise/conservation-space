package com.sirma.sep.model.management;

import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Models listener for successfully applied changes to a model instance. <br>
 * Used by {@link ModelChangeSetOperationManager} to notify the callers when a change is applied successfully
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @see ModelChangeSetOperationManager
 * @since 24/10/2018
 */
public interface OnSuccessfulChangeSetLister {

	/**
	 * Called when a change set is successfully applied to the given model
	 *
	 * @param models the model that got modified by the given change
	 * @param changeSetInfo the applied change
	 */
	void changeApplied(Models models, ModelChangeSetInfo changeSetInfo);
}
