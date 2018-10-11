package com.sirma.itt.seip.wildfly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.DeploymentException;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;
import com.sirmaenterprise.sep.jms.provision.JmsSubsystemModel;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test the wildfly hornetq jms provisioning.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflyHornetQJMSProvisionerTest {

	@Mock
	private WildflyControllerService controller;

	@InjectMocks
	private WildflyHornetQJMSProvisioner provisioner;

	/**
	 * If the extension isn't already provisioned and there are queues defined in the application, it should be.
	 *
	 * @throws DeploymentException
	 *             thrown on successful execution so the deployment terminates
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	@SuppressWarnings("boxing")
	public void should_provision_extension_if_notAlreadyDefined()
			throws DeploymentException, RollbackedException {
		// First response states that the messaging extension is not already defined, second is the
		// response from the
		// extension provision, third states that the subsystem extension is already defined, the
		// next four ones are for the acceptors and the last one for the queue
		mockControllerResponses(false, null, true, "sep-core", "sep-core", true, true, true, true, true);
		provisioner.provisionSubsystem(new JmsSubsystemModel());
		ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(controller, Mockito.times(10)).execute(controllerCaptor.capture());

		ModelNode extensionAlreadyDefinedCheckModel = controllerCaptor.getAllValues().get(0);
		ModelNode extensionProvisioningModel = controllerCaptor.getAllValues().get(1);

		Assert.assertEquals(ClientConstants.READ_RESOURCE_OPERATION,
				extensionAlreadyDefinedCheckModel.get(ClientConstants.OP).asString());
		Assert.assertEquals("org.jboss.as.messaging",
				extensionAlreadyDefinedCheckModel.get(ClientConstants.OP_ADDR).get(0).get("extension").asString());

		Assert.assertEquals(ClientConstants.ADD, extensionProvisioningModel.get(ClientConstants.OP).asString());
		Assert.assertEquals("org.jboss.as.messaging",
				extensionProvisioningModel.get(ClientConstants.OP_ADDR).get(0).get("extension").asString());
	}

	/**
	 * If the there are changes found in the currently deployed model and in the passed model the provisioner should
	 * trigger changes in the model to update the deployed model.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	@SuppressWarnings("boxing")
	public void should_provision_ChangesToModel_differentValue() throws RollbackedException, IOException {
		// First response states that the messaging extension is not already defined, second is the
		// response from the
		// extension provision, third states that the subsystem extension is already defined, the
		// next four ones are for the acceptors and the last one for the queue
		mockControllerResponses(true, true, "sep-core", true, true, true, true, true, true);
		JmsSubsystemModel model = new JmsSubsystemModel();
		model.setTopicsClientId("sep-test");
		try {
			provisioner.provisionSubsystem(model);
		}catch (DeploymentException e) {
			ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
			Mockito.verify(controller, Mockito.times(10)).execute(controllerCaptor.capture());

			ModelNode writeAttributeModel = controllerCaptor.getAllValues().get(9);
			compareModels("model-changes-changedValue.json", writeAttributeModel);
		}
	}

	/**
	 * If the there are changes found in the currently deployed model and in the passed model the provisioner should
	 * trigger changes in the model to update the deployed model.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	@SuppressWarnings("boxing")
	public void should_provision_ChangesToModel_missingValue() throws RollbackedException, IOException {
		// First response states that the messaging extension is not already defined, second is the
		// response from the
		// extension provision, third states that the subsystem extension is already defined, the
		// next four ones are for the acceptors and the last one for the queue
		mockControllerResponses(true, true, "undefined", true, true, true, true, true, true);
		JmsSubsystemModel model = new JmsSubsystemModel();
		model.setTopicsClientId("sep-test");
		try {
			provisioner.provisionSubsystem(model);
		}catch (DeploymentException e) {
			ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
			Mockito.verify(controller, Mockito.times(10)).execute(controllerCaptor.capture());

			ModelNode writeAttributeModel = controllerCaptor.getAllValues().get(9);
			compareModels("model-changes-addValue.json", writeAttributeModel);
		}
	}

	/**
	 * If the there are changes found in the currently deployed model and in the passed model the provisioner should
	 * trigger changes in the model to update the deployed model.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	@SuppressWarnings("boxing")
	public void should_provision_ChangesToModel_removedValue() throws RollbackedException, IOException {
		// First response states that the messaging extension is not already defined, second is the
		// response from the
		// extension provision, third states that the subsystem extension is already defined, the
		// next four ones are for the acceptors and the last one for the queue
		mockControllerResponses(true, true, "sep-core", "sep-core", true, true, true, true, true, true);
		JmsSubsystemModel model = new JmsSubsystemModel();
		model.setTopicsClientId(null);
		try {
			provisioner.provisionSubsystem(model);
		}catch (DeploymentException e) {
			ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
			Mockito.verify(controller, Mockito.times(10)).execute(controllerCaptor.capture());

			ModelNode writeAttributeModel = controllerCaptor.getAllValues().get(9);
			compareModels("model-changes-removedValue.json", writeAttributeModel);
		}
	}

	/**
	 * If the subsystem isn't already provisioned, and there are queues defined in the application, it should be. This
	 * operation requires a server reload so an exception must be thrown.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 * @throws IOException
	 *             thrown if the test file can't be read
	 */
	@Test
	public void should_provision_subsystem_if_notAlreadyDefined() throws RollbackedException, IOException {
		// First response states that the messaging extension is already defined, second states that
		// the subsystem extension is not already defined, the
		// next four ones are for the acceptors and the next one for the queue, last is the
		// response from the subsystem provision
		mockControllerResponses(true, false, true, true, true, true, true, true);
		try {
			provisioner.provisionSubsystem(new JmsSubsystemModel());
		} catch (DeploymentException e) {
			ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
			Mockito.verify(controller, Mockito.times(8)).execute(controllerCaptor.capture());

			ModelNode subsystemProvisioningModel = controllerCaptor.getAllValues().get(7);
			compareModels("subsystem_provisioning_model.json", subsystemProvisioningModel);
		}
	}

	private static void compareModels(String expected, ModelNode actual) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(WildflyHornetQJMSProvisionerTest.class.getResourceAsStream(expected), writer, "UTF-8");
		String expectedResult = writer.toString();
		JsonAssert.assertJsonEquals(expectedResult, actual.toJSONString(false));
	}

	/**
	 * If the acceptors aren't already provisioned, they should be.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 * @throws IOException
	 *             thrown if the test file can't be read
	 */
	@Test
	@SuppressWarnings("boxing")
	public void should_provision_acceptors_if_not_AlreadyDefined() throws RollbackedException, IOException {
		// First response states that the messaging extension is already defined, second states that
		// the subsystem extension is already defined, the
		// next four ones are for the acceptors and the next one for the queue, last is the
		// response from the subsystem provision
		mockControllerResponses(true, true, "sep-core", "sep-core", false, false, false, false, true, true);
		provisioner.provisionSubsystem(new JmsSubsystemModel());
		ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(controller, Mockito.times(10)).execute(controllerCaptor.capture());

		ModelNode subsystemProvisioningModel = controllerCaptor.getAllValues().get(9);
		compareModels("acceptors_provisioning_model.json", subsystemProvisioningModel);
	}
	
	/**
	 * All queues that are defined in the application should be provisioned. This doesn't require
	 * any reloads.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 * @throws IOException
	 *             thrown if the test file can't be read
	 * @throws DeploymentException
	 *             thrown on successful execution so the deployment terminates
	 */
	@Test
	public void should_provision_queue_if_notAlreadyDefined()
			throws RollbackedException, IOException, DeploymentException {
		testDestinationProvisioning("java.queueName", "queue_provisioning_model.json", DestinationType.QUEUE);
	}

	/**
	 * All topics that are defined in the application should be provisioned. This doesn't require
	 * any reloads.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 * @throws IOException
	 *             thrown if the test file can't be read
	 * @throws DeploymentException
	 *             thrown on successful execution so the deployment terminates
	 */
	@Test
	public void should_provision_topic_if_notAlreadyDefined()
			throws RollbackedException, IOException, DeploymentException {
		testDestinationProvisioning("java.topicName", "topic_provisioning_model.json", DestinationType.TOPIC);
	}

	@Test
	public void resolveDestinationDefinition() throws Exception {
		ModelNode modelNode = ModelNode.fromJSONStream(WildflyHornetQJMSProvisionerTest.class.getResourceAsStream("destinationDefinition.json"));
		when(controller.execute(any())).thenReturn(modelNode);

		Optional<DestinationDefinition> resolve = provisioner.resolve("java:/jms.queue.MoveContent");
		Assert.assertNotNull(resolve);
		assertTrue(resolve.isPresent());
		DestinationDefinition definition = resolve.get();
		assertEquals(10, definition.getMessageCounterHistoryDayLimit());
		assertEquals(15728640, definition.getMaxSize());
		assertEquals(2097152, definition.getPageSize());
		assertEquals(43200000, definition.getMaxRedeliveryDelay());
		assertEquals(19, definition.getMaxRedeliveryAttempts());
		assertEquals(1500, definition.getRedeliveryDelay());
		assertEquals(1.5, definition.getRedeliveryMultiplier(), 0.0001);
		assertEquals("jms.queue.MoveContent_DLQ", definition.getDeadLetterAddress());
		assertEquals("jms.queue.MoveContent_EQ", definition.getExpiryAddress());
	}

	@Test
	public void resolveAllDestinationDefinitions() throws Exception {
		ModelNode destinationDef = ModelNode.fromJSONStream(WildflyHornetQJMSProvisionerTest.class.getResourceAsStream("destinationDefinition.json"));
		ModelNode genericDestinationDef = ModelNode.fromJSONStream(WildflyHornetQJMSProvisionerTest.class.getResourceAsStream("genericDestinationDefinition.json"));
		ModelNode subSystemDef = ModelNode.fromJSONStream(WildflyHornetQJMSProvisionerTest.class.getResourceAsStream("jmsSubSystemDefinition.json"));
		when(controller.execute(any())).thenReturn(subSystemDef, genericDestinationDef, destinationDef);

		Collection<DestinationDefinition> resolve = provisioner.resolveAll();
		Assert.assertNotNull(resolve);
		assertEquals(2, resolve.size());
		boolean foundGenericConfig = false;
		for (DestinationDefinition definition : resolve) {
			if ("jms.queue.DLQ".equals(definition.getDeadLetterAddress())) {
				foundGenericConfig = true;
			}
		}
		assertTrue("Should return the generic definition", foundGenericConfig);
	}

	private void testDestinationProvisioning(String destinationName, String resultResourceFileName,
			DestinationType destinationType) throws IOException, RollbackedException {
		// first one is for the topic already defined check, second is the response from
		// the subsystem provision
		when(controller.execute(any(ModelNode.class))).thenReturn(mockModelNodeAlreadyDefined(false),
				mockModelNodeAlreadyDefined(true));
		try {
			DestinationDefinition definition = new DestinationDefinition();
			definition.setType(destinationType);
			definition.setAddress(destinationName);
			provisioner.provisionDestination(definition);
		} catch (RollbackedException e) {
			// won't happen
		}
		ArgumentCaptor<ModelNode> controllerCaptor = ArgumentCaptor.forClass(ModelNode.class);
		Mockito.verify(controller, Mockito.times(2)).execute(controllerCaptor.capture());

		ModelNode subsystemProvisioningModel = controllerCaptor.getAllValues().get(1);
		compareModels(resultResourceFileName, subsystemProvisioningModel);
	}
	
	private static ModelNode mockModelNodeAlreadyDefined(Object alreadyDefined) {
		String status = "failed";
		String result = "";
		if (alreadyDefined instanceof Boolean) {
			status = (Boolean) alreadyDefined ? ClientConstants.SUCCESS : "failed";
		} else if (alreadyDefined != null) {
			if ("undefined".equals(alreadyDefined)) {
				result = ", \"result\": null";
			}else {
				result = ", \"result\":\"" + alreadyDefined + "\"";
			}
			status = ClientConstants.SUCCESS;
		}
		return ModelNode.fromJSONString("{\"outcome\":\"" + status + "\"" +result + "}");
	}
	
	private void mockControllerResponses(Object... alreadyDefinedResponses) throws RollbackedException {
		ModelNode[] controllerResponses = new ModelNode[alreadyDefinedResponses.length];
		for (int i = 0; i < alreadyDefinedResponses.length; i++) {
			if (alreadyDefinedResponses[i] != null) {
				controllerResponses[i] = mockModelNodeAlreadyDefined(alreadyDefinedResponses[i]);
			} else {
				controllerResponses[i] = null;
			}
		}
		when(controller.execute(any(ModelNode.class))).thenReturn(controllerResponses[0],
				Arrays.copyOfRange(controllerResponses, 1, controllerResponses.length));
	}
}
