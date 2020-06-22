/**
 *
 */
package com.sirma.itt.seip.wildfly;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.ServerControllerService;

/**
 * Wildfly server controller that uses the internal CLI interface to control the server. The checker relays on that no
 * CLI operations could be performed while in deployment mode.
 *
 * @author BBonev
 */
@Singleton
public class WildflyServerControllerService implements ServerControllerService {

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

	@Override
	public void undeployAllDeployments() {
		ModelNode compositeOperation = new ModelNode();
		compositeOperation.get(ClientConstants.OP).set(ClientConstants.COMPOSITE);

		ModelNode steps = compositeOperation.get("steps");
		for (String deploymentName : getDeploymentNames()) {
			ModelNode undeployOperation = new ModelNode();
			undeployOperation.get(ClientConstants.OP).set(ClientConstants.DEPLOYMENT_UNDEPLOY_OPERATION);
			undeployOperation.get(ClientConstants.OP_ADDR).add(ClientConstants.DEPLOYMENT, deploymentName);
			steps.add(undeployOperation);
		}

		try {
			LOGGER.info("Executing undeploy operation: " + compositeOperation.asString());
			ModelNode result = controllerService.execute(compositeOperation);
			LOGGER.info("Undeploy operation returned " + result.asString());
		} catch (RollbackedException e) {
			LOGGER.trace("Failed to undeploy deployed applications", e);
		}

	}

	private Set<String> getDeploymentNames() {
		ModelNode op = new ModelNode();
		op.get(ClientConstants.OP).set(ClientConstants.READ_CHILDREN_NAMES_OPERATION);
		op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
		try {
			ModelNode result = controllerService.execute(op);
			Set<String> names = new HashSet<>();
			if (!result.isDefined()) {
				return CollectionUtils.emptySet();
			}
			for (ModelNode deployment : result.get(ClientConstants.RESULT).asList()) {
				names.add(deployment.asString());
			}
			return names;
		} catch (RollbackedException e) {
			LOGGER.trace("Failed to get deployment names", e);
		}
		return CollectionUtils.emptySet();
	}
}
