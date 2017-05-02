/**
 *
 */
package com.sirma.itt.seip.wildfly;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.ServerStatusChecker;

/**
 * Wildfly server status checker that uses the internal CLI interface to check if the server is running. The checker
 * relays on that no CLI operations could be performed while in deployment mode.
 *
 * @author BBonev
 */
@ApplicationScoped
public class WildflyServerStatusChecker implements ServerStatusChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private WildflyControllerService controllerService;

	@Override
	public boolean isDeploymentFinished() {
		ModelNode request = new ModelNode();
		request.get(ClientConstants.OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
		request.get("name").set("server-state");

		try {
			ModelNode result = controllerService.execute(request);
			return !"failed".equals(result.get("outcome").asString());
		} catch (RollbackedException e) {
			LOGGER.trace("Failed to check server status", e);
			return false;
		}
	}

}
