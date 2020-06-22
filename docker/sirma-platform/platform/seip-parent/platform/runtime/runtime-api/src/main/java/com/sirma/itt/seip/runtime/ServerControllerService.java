/**
 *
 */
package com.sirma.itt.seip.runtime;

/**
 * Callback called to control servers. This may be implemented for different servers. A single instance of this should
 * be provided!.
 *
 * @author BBonev
 */
public interface ServerControllerService {

	/**
	 * Checks if is deployment has finished.
	 *
	 * @return true if the deployment has finished
	 */
	boolean isDeploymentFinished();

	/**
	 * Undeploy all deployed applications on the server.
	 */
	void undeployAllDeployments();
}
