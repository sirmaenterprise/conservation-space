package com.sirma.sep.model.management;

import com.sirma.sep.model.management.operation.ChangeSetValidationFailed;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Model changes listener for failed to apply change sets.<br>
 * Used by {@link ModelChangeSetOperationManager} to notify the callers when a change failed to pass validation or
 * failed to apply due to some other reason.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 24/10/2018
 */
public interface OnFailedChangeSetListener {

	/**
	 * Called when change set failed to apply due to the provided exception
	 *
	 * @param failedReason the reason the change to fail
	 * @param changeSetInfo the failed change
	 * @return true if the change validation exception is not fatal and could continue with actual change set
	 * application and false for fatal errors and the change should not be applied
	 */
	boolean changeFailed(ChangeSetValidationFailed failedReason, ModelChangeSetInfo changeSetInfo);

	/**
	 * Returns an listener instance that just throws the accepted exception
	 *
	 * @return always failing listener
	 */
	static OnFailedChangeSetListener failingListener() {
		return (changeSetValidationFailed, changeSetInfo) -> {
			throw changeSetValidationFailed;
		};
	}
}
