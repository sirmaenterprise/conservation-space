package com.sirma.sep.model.management.operation;

/**
 * Represents the possible statues of a {@link ModelChangeSetInfo}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 23/08/2018
 */
public enum ModelChangeSetStatus {
	/**
	 * New, not applied change. Initial status
	 */
	NEW,
	/**
	 * The change was validated and successfully applied to the runtime model
	 */
	APPLIED,
	/**
	 * The changes was successfully deployed and is now in archived state
	 */
	DEPLOYED,
	/**
	 * The change was rollbacked
	 */
	ROLLBACKED,
	/**
	 * The change fail to apply the the runtime model and will be skipped for future tries
	 */
	FAIL_TO_APPLY,
	/**
	 * The change failed to deploy
	 */
	FAIL_TO_DEPLOY,
	/**
	 * Intermediate non persistent change. Such changes should not be stored and used to increment the model version.
	 */
	INTERMEDIATE;
}
