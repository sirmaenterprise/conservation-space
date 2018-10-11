package com.sirma.itt.seip.instance.version;

/**
 * Defines methods for execution of the {@link VersionStep}s.
 *
 * @author A. Kunchev
 */
public interface VersionStepsExecutor {

	/**
	 * Executes the steps for instance versioning.
	 *
	 * @param context
	 *            the {@link VersionContext} which contains all the information needed for the instance versioning
	 */
	void execute(VersionContext context);

}
