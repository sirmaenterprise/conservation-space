package com.sirma.itt.seip.instance.actions;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * {@link Action} defines means for executing business function in unified manner.
 *
 * @param <A>
 *            the type of the request
 * @author BBonev
 */
public interface Action<A extends ActionRequest> extends Named, Plugin {

	String TARGET_NAME = "actions";

	/**
	 * Perform the action using the given request and producing a response.
	 *
	 * @param request
	 *            the request to execute
	 * @return the result object, should not be <code>null</code>.
	 */
	Object perform(A request);
}
