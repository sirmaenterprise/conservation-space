package com.sirma.itt.seip.wildfly.batch;

import static org.jboss.as.controller.client.helpers.ClientConstants.COMPOSITE;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.STEPS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel.JobRepositoryType;
import com.sirma.sep.instance.batch.provisioning.BatchConfigurationProvisioner;
import com.sirma.sep.instance.batch.provisioning.BatchProvisioningException;

/**
 * Test the {@link WildflyBatchConfigurationProvisioner}.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflyBatchConfigurationProvisionerTest {

	@Mock
	private WildflyControllerService controller;

	@InjectMocks
	private BatchConfigurationProvisioner provisioner = new WildflyBatchConfigurationProvisioner();

	@Test
	public void should_provisionBatchSubsystem_fromConfigurations()
			throws RollbackedException, BatchProvisioningException {
		BatchConfigurationModel model = new BatchConfigurationModel();
		model.setRepositoryType(JobRepositoryType.JDBC);
		model.setJndiName("jndiName");
		model.setThreadFactoryGroupName("group-name");
		model.setThreadFactoryName("threadFactoryName");
		model.setThreadFactoryNamePattern("namePattern");
		model.setThreadFactoryPriority(1);
		model.setThreadPoolKeepAliveInSeconds(200);
		model.setThreadPoolSize(15);
		model.setThreadPoolThreadFactoryName("threadFactoryName");

		ModelNode result = getSuccessResult();
		ModelNode factoryAlreadyExistsResult = getFailedResult();
		when(controller.execute(any(ModelNode.class))).thenReturn(factoryAlreadyExistsResult, result);

		provisioner.provision(model);

		ArgumentCaptor<ModelNode> argument = ArgumentCaptor.forClass(ModelNode.class);
		verify(controller, times(2)).execute(argument.capture());
		assertEquals(getCorrectNode(model, true), argument.getValue());
	}

	@Test
	public void should_provisionBatchSubsystem_fromConfigurations_withoutThreadFactory()
			throws RollbackedException, BatchProvisioningException {
		BatchConfigurationModel model = new BatchConfigurationModel();
		model.setRepositoryType(JobRepositoryType.JDBC);
		model.setJndiName("jndiName");

		ModelNode result = getSuccessResult();
		when(controller.execute(any(ModelNode.class))).thenReturn(result);

		provisioner.provision(model);

		verify(controller).execute(getCorrectNode(model, false));
	}

	@Test(expected = BatchProvisioningException.class)
	public void should_throwException_onFailedResult() throws RollbackedException, BatchProvisioningException {
		BatchConfigurationModel model = new BatchConfigurationModel();
		model.setRepositoryType(JobRepositoryType.JDBC);
		model.setJndiName("jndiName");

		ModelNode result = getFailedResult();
		when(controller.execute(any(ModelNode.class))).thenReturn(result);

		provisioner.provision(model);
	}

	@Test(expected = BatchProvisioningException.class)
	public void should_throwException_onExecutionException() throws RollbackedException, BatchProvisioningException {
		BatchConfigurationModel model = new BatchConfigurationModel();
		model.setRepositoryType(JobRepositoryType.JDBC);
		model.setJndiName("jndiName");

		when(controller.execute(any(ModelNode.class))).thenThrow(new RollbackedException());

		provisioner.provision(model);
	}

	private static ModelNode getSuccessResult() {
		ModelNode result = mock(ModelNode.class);
		ModelNode success = new ModelNode(ClientConstants.SUCCESS);
		when(result.get(ClientConstants.OUTCOME)).thenReturn(success);
		return result;
	}

	private static ModelNode getFailedResult() {
		ModelNode result = mock(ModelNode.class);
		ModelNode success = new ModelNode("failure");
		when(result.get(ClientConstants.OUTCOME)).thenReturn(success);
		when(result.get(ClientConstants.FAILURE_DESCRIPTION)).thenReturn(new ModelNode("failure"));
		return result;
	}

	/**
	 * Get the correct node from the given {@link BatchConfigurationModel}. The correct node is the
	 * node in the correct format with the correct values for the current wildfly application server
	 * (Currently wildfly 9, format might or might not change with new versions).
	 * 
	 * @param model
	 *            the configuration model
	 * @param includeThreadFactory
	 *            whether the thread factory should be included
	 * @return the correct node
	 */
	private static ModelNode getCorrectNode(BatchConfigurationModel model, boolean includeThreadFactory) {
		ModelNode root = new ModelNode();
		root.get(OP).set(COMPOSITE);
		ModelNode steps = root.get(STEPS);

		ModelNode jobRepositoryTypeNode = steps.add();
		ModelNode jobRepositoryTypeNodeAddress = jobRepositoryTypeNode.get(OP_ADDR);
		jobRepositoryTypeNodeAddress.add(ClientConstants.SUBSYSTEM, "batch");
		writeAttribute(jobRepositoryTypeNode, "job-repository-type", model.getRepositoryType().getValue());

		ModelNode jndiNameNode = steps.add();
		ModelNode jndiNameNodeAddress = jndiNameNode.get(OP_ADDR);
		jndiNameNodeAddress.add(ClientConstants.SUBSYSTEM, "batch");
		jndiNameNodeAddress.add("job-repository", model.getRepositoryType().getValue());
		writeAttribute(jndiNameNode, "jndi-name", model.getJndiName());

		if (includeThreadFactory) {
			ModelNode threadFactoryNode = steps.add();
			threadFactoryNode.get(ClientConstants.OP).set(ClientConstants.ADD);

			ModelNode threadFactoryNodeAddress = threadFactoryNode.get(OP_ADDR);
			threadFactoryNodeAddress.add(ClientConstants.SUBSYSTEM, "batch");
			threadFactoryNodeAddress.add("thread-factory", model.getThreadFactoryName());

			threadFactoryNode = steps.add();
			addThreadFactoryAddress(model.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "group-name", model.getThreadFactoryGroupName());

			threadFactoryNode = steps.add();
			addThreadFactoryAddress(model.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "priority", Integer.toString(model.getThreadFactoryPriority()));

			threadFactoryNode = steps.add();
			addThreadFactoryAddress(model.getThreadFactoryName(), threadFactoryNode);
			writeAttribute(threadFactoryNode, "thread-name-pattern", model.getThreadFactoryNamePattern());

			ModelNode threadPoolNode = steps.add();
			addThreadPoolAddress(threadPoolNode);
			writeAttribute(threadPoolNode, "thread-factory", model.getThreadPoolThreadFactoryName());

		}
		ModelNode threadPoolNode = steps.add();
		addThreadPoolAddress(threadPoolNode);
		ModelNode keepAliveNode = new ModelNode();
		keepAliveNode.get("time").set(model.getThreadPoolKeepAliveInSeconds());
		keepAliveNode.get("unit").set("SECONDS");
		threadPoolNode.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		threadPoolNode.get(ClientConstants.NAME).set("keepalive-time");
		threadPoolNode.get(ClientConstants.VALUE).set(keepAliveNode);

		threadPoolNode = steps.add();
		addThreadPoolAddress(threadPoolNode);
		writeAttribute(threadPoolNode, "max-threads", Integer.toString(model.getThreadPoolSize()));

		return root;
	}

	private static void writeAttribute(ModelNode node, String name, String value) {
		node.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		node.get(ClientConstants.NAME).set(name);
		node.get(ClientConstants.VALUE).set(value);
	}

	private static void addThreadFactoryAddress(String threadFactoryName, ModelNode threadFactoryNode) {
		ModelNode threadFactoryNodeAddress = threadFactoryNode.get(OP_ADDR);
		threadFactoryNodeAddress.add(ClientConstants.SUBSYSTEM, "batch");
		threadFactoryNodeAddress.add("thread-factory", threadFactoryName);
	}

	private static void addThreadPoolAddress(ModelNode threadPoolNode) {
		ModelNode threadPoolNodeAddress = threadPoolNode.get(OP_ADDR);
		threadPoolNodeAddress.add(ClientConstants.SUBSYSTEM, "batch");
		threadPoolNodeAddress.add("thread-pool", "batch");
	}
}
