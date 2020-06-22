package com.sirma.itt.seip.wildfly.batch;

import static org.jboss.as.controller.client.helpers.ClientConstants.COMPOSITE;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.STEPS;

import javax.inject.Inject;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel.JobRepositoryType;
import com.sirma.sep.instance.batch.provisioning.BatchConfigurationProvisioner;
import com.sirma.sep.instance.batch.provisioning.BatchProvisioningException;

/**
 * Responsible for provisioning all resources needed for wildfly's batch subsystem.
 * 
 * @author nvelkov
 */
public class WildflyBatchConfigurationProvisioner implements BatchConfigurationProvisioner {

	private static final String BATCH = "batch";

	@Inject
	private WildflyControllerService controller;

	@Override
	public void provision(BatchConfigurationModel configurationModel) throws BatchProvisioningException {
		ModelNode root = new ModelNode();
		root.get(OP).set(COMPOSITE);
		ModelNode steps = root.get(STEPS);

		ModelNode jobRepositoryTypeNode = steps.add();
		ModelNode jobRepositoryTypeNodeAddress = jobRepositoryTypeNode.get(OP_ADDR);
		jobRepositoryTypeNodeAddress.add(ClientConstants.SUBSYSTEM, BATCH);
		writeAttribute(jobRepositoryTypeNode, "job-repository-type", configurationModel.getRepositoryType().getValue());

		addJndiName(configurationModel, steps);

		if (StringUtils.isNotBlank(configurationModel.getThreadFactoryName())) {
			addThreadFactoryNode(configurationModel.getThreadFactoryName(), steps);
			// Normally all those attribute writes can be added to the ADD step instead of doing it
			// step by step but since wildfly's add/remove resources requires server restarts we're
			// better off doing it like that.
			addThreadFactoryGroupName(configurationModel, steps);
			addThreadFactoryPriority(configurationModel, steps);
			addThreadFactoryNamePattern(configurationModel, steps);
		}

		// Unlike the thread factory, the thread pool is always there and is mandatory so no need to
		// create it beforehand, just handle it's attributes.
		addThreadPoolThreadFactoryName(configurationModel, steps);
		addThreadPoolKeepAlive(configurationModel, steps);
		addThreadPoolSize(configurationModel, steps);

		try {
			ModelNode result = controller.execute(root);
			if (!isSuccessful(result)) {
				throw new BatchProvisioningException(getFailureDescription(result));
			}
		} catch (RollbackedException e) {
			throw new BatchProvisioningException("Error during remote operation!", e);
		}

	}

	private static void addThreadPoolSize(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (configurationModel.getThreadPoolSize() != 0) {
			ModelNode threadPoolNode = steps.add();
			addThreadPoolAddress(threadPoolNode);
			writeAttribute(threadPoolNode, "max-threads", Integer.toString(configurationModel.getThreadPoolSize()));
		}
	}

	private static void addThreadPoolKeepAlive(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (configurationModel.getThreadPoolKeepAliveInSeconds() != 0) {
			ModelNode threadPoolNode = steps.add();
			addThreadPoolAddress(threadPoolNode);
			ModelNode keepAliveNode = new ModelNode();
			keepAliveNode.get("time").set(configurationModel.getThreadPoolKeepAliveInSeconds());
			keepAliveNode.get("unit").set("SECONDS");
			threadPoolNode.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
			threadPoolNode.get(ClientConstants.NAME).set("keepalive-time");
			threadPoolNode.get(ClientConstants.VALUE).set(keepAliveNode);
		}
	}

	private static void addThreadPoolThreadFactoryName(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (configurationModel.getThreadPoolThreadFactoryName() != null) {
			ModelNode threadPoolNode = steps.add();
			addThreadPoolAddress(threadPoolNode);
			writeAttribute(threadPoolNode, "thread-factory", configurationModel.getThreadPoolThreadFactoryName());
		}
	}

	private static void addThreadFactoryNamePattern(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (StringUtils.isNotBlank(configurationModel.getThreadFactoryNamePattern())) {
			ModelNode threadFactoryNode = steps.add();
			addThreadFactoryAddress(configurationModel.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "thread-name-pattern", configurationModel.getThreadFactoryNamePattern());
		}
	}

	private static void addThreadFactoryPriority(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (configurationModel.getThreadFactoryPriority() != 0) {
			ModelNode threadFactoryNode = steps.add();
			addThreadFactoryAddress(configurationModel.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "priority",
					Integer.toString(configurationModel.getThreadFactoryPriority()));
		}
	}

	private static void addThreadFactoryGroupName(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (StringUtils.isNotBlank(configurationModel.getThreadFactoryGroupName())) {
			ModelNode threadFactoryNode = steps.add();
			addThreadFactoryAddress(configurationModel.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "group-name", configurationModel.getThreadFactoryGroupName());
		}
	}

	/**
	 * Add the jndi name to the job-repository if the job repository type is jdbc.
	 * 
	 * @param configurationModel
	 *            the configuration model
	 * @param steps
	 *            the steps
	 */
	private static void addJndiName(BatchConfigurationModel configurationModel, ModelNode steps) {
		if (JobRepositoryType.JDBC.equals(configurationModel.getRepositoryType())) {
			ModelNode jndiNameNode = steps.add();
			ModelNode jndiNameNodeAddress = jndiNameNode.get(OP_ADDR);
			jndiNameNodeAddress.add(ClientConstants.SUBSYSTEM, BATCH);
			jndiNameNodeAddress.add("job-repository", configurationModel.getRepositoryType().getValue());
			writeAttribute(jndiNameNode, "jndi-name", configurationModel.getJndiName());
		}
	}

	/**
	 * Add the thread factory node to the steps if it doesn't already exist. If the thread factory
	 * exists no need to recreate it again. We can't do that anyways because wildfly requires a
	 * reload after we remove the factory if we want to add it again with the same name, so we have
	 * to handle the factory creation here and all it's attributes separately.
	 * 
	 * @param threadFactoryName
	 *            the thread factory name
	 * @param steps
	 *            the steps
	 * @throws BatchProvisioningException
	 *             if an error has occured during the remote operation
	 */
	private void addThreadFactoryNode(String threadFactoryName, ModelNode steps) throws BatchProvisioningException {
		ModelNode threadFactoryNodeCheck = new ModelNode();
		addThreadFactoryAddress(threadFactoryName, threadFactoryNodeCheck);
		if (!resourceExists(threadFactoryNodeCheck)) {
			ModelNode threadFactoryNode = steps.add();
			threadFactoryNode.get(ClientConstants.OP).set(ClientConstants.ADD);
			addThreadFactoryAddress(threadFactoryName, threadFactoryNode);
		}
	}

	private static void addThreadFactoryAddress(String threadFactoryName, ModelNode threadFactoryNode) {
		ModelNode threadFactoryNodeAddress = threadFactoryNode.get(OP_ADDR);
		threadFactoryNodeAddress.add(ClientConstants.SUBSYSTEM, BATCH);
		threadFactoryNodeAddress.add("thread-factory", threadFactoryName);
	}

	private static void addThreadPoolAddress(ModelNode threadPoolNode) {
		ModelNode threadPoolNodeAddress = threadPoolNode.get(OP_ADDR);
		threadPoolNodeAddress.add(ClientConstants.SUBSYSTEM, BATCH);
		threadPoolNodeAddress.add("thread-pool", BATCH);
	}

	private static void writeAttribute(ModelNode node, String name, String value) {
		node.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		node.get(ClientConstants.NAME).set(name);
		node.get(ClientConstants.VALUE).set(value);
	}

	private static boolean isSuccessful(ModelNode result) {
		return ClientConstants.SUCCESS.equals(result.get(ClientConstants.OUTCOME).asString());
	}

	private static String getFailureDescription(ModelNode result) {
		return result.get(ClientConstants.FAILURE_DESCRIPTION).asString();
	}

	private boolean resourceExists(ModelNode resource) throws BatchProvisioningException {
		resource.get(OP).set(READ_RESOURCE_OPERATION);
		try {
			return isSuccessful(controller.execute(resource));
		} catch (RollbackedException e) {
			throw new BatchProvisioningException("Error during remote operation!", e);
		}
	}
}
