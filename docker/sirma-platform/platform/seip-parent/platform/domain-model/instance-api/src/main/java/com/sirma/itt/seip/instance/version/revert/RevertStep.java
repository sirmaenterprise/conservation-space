package com.sirma.itt.seip.instance.version.revert;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines step that should be executed, when reverting versions. The extensions are executed in defined order.
 *
 * @author A. Kunchev
 */
public interface RevertStep extends Plugin, Named {

	String EXTENSION_NAME = "revertStep";

	/**
	 * Specifies main behaviour for the step.
	 *
	 * @param context
	 *            {@link RevertContext} object, used to store the data required for the revert process. Also used to
	 *            share data between the different steps
	 */
	void invoke(RevertContext context);

	/**
	 * Specifies behaviour for error recovery, if any occurs while executing the revert process.
	 *
	 * @param context
	 *            {@link RevertContext} object, used to store the data required for the revert process. Also used to
	 *            share data between the different steps
	 */
	default void rollback(RevertContext context) {
		// FSS
	}

}
