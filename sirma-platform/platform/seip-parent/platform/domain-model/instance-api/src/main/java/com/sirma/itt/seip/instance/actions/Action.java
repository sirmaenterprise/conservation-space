package com.sirma.itt.seip.instance.actions;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * {@link Action} defines means for executing business function in unified manner.
 *
 * @param <A> the type of the request
 * @author BBonev
 */
public interface Action<A extends ActionRequest> extends Named, Plugin {

	String TARGET_NAME = "actions";

	/**
	 * Performs validation over actions, before their execution.
	 *
	 * @param request the request to execute
	 */
	default void validate(A request) {
		// nothing to do here
	}

	/**
	 * Check if the target (affected) instance should be locked before performing the action itself to prevent
	 * concurrent action invocations.<br>
	 * By default locking is <b>enabled</b>.
	 *
	 * @param request the action request
	 * @return true if the instance should be locked before action invocation
	 */
	default boolean shouldLockInstanceBeforeAction(A request) {
		return true;
	}

	/**
	 * Perform the action using the given request and producing a response.
	 *
	 * @param request the request to execute
	 * @return the result object, should not be <code>null</code>.
	 */
	Object perform(A request);

}
