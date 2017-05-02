/**
 *
 */
package com.sirma.itt.seip.runtime;

/**
 * Callback called to check the server status. This may be implemented for different servers. A single instance of this
 * should be provided!.
 *
 * @author BBonev
 */
public interface ServerStatusChecker {

	/**
	 * Checks if is deployment has finished.
	 *
	 * @return true, if is deployment finished
	 */
	boolean isDeploymentFinished();
}
