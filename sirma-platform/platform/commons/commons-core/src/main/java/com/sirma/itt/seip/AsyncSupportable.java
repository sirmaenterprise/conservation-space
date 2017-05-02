package com.sirma.itt.seip;

/**
 * Provides means to define that the implementation could support asynchronous execution.
 *
 * @author BBonev
 */
public interface AsyncSupportable {

	/**
	 * Checks if the current rule supports asynchronous invocation.
	 *
	 * @return <code>true</code> if async invocation is supported
	 */
	boolean isAsyncSupported();

}