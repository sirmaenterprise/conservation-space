package com.sirma.itt.seip.instance.version;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines step that should be executed, when creating instances versions. The extensions are executed in defined order.
 *
 * @author A. Kunchev
 */
public interface VersionStep extends Named, Plugin {

	String TARGET_NAME = "versionStep";

	/**
	 * Executes the step.
	 *
	 * @param context
	 *            contains all the information needed for the versioning
	 */
	void execute(VersionContext context);

}
