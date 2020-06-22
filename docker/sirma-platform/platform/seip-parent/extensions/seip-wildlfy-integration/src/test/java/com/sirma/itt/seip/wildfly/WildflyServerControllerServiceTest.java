package com.sirma.itt.seip.wildfly;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.ServerControllerService;

/**
 * Test the {@link WildflyServerControllerService}.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflyServerControllerServiceTest {
	@Mock
	private WildflyControllerService controllerService;

	@InjectMocks
	private ServerControllerService serverControllerService = new WildflyServerControllerService();

	/**
	 * Test the undeployment of all deployed applications.
	 *
	 * @throws RollbackedException
	 *             if a rollbackedexcepton occurs
	 */
	@Test
	public void should_undeployDeployments() throws RollbackedException {
		mockControllerGetDeploymentNames();

		ArgumentCaptor<ModelNode> captor = ArgumentCaptor.forClass(ModelNode.class);
		serverControllerService.undeployAllDeployments();

		Mockito.verify(controllerService, Mockito.atLeast(2)).execute(captor.capture());

		String result = captor
				.getValue()
					.get("steps")
					.get(0)
					.get(ClientConstants.OP_ADDR)
					.get(0)
					.get(ClientConstants.DEPLOYMENT).asString();

		Assert.assertEquals("firstDeployment",result);
	}

	/**
	 * Test the deplyoment status check when the deployment has been finished.
	 *
	 * @throws RollbackedException
	 *             if a rollbackedexcepton occurs
	 */
	@Test
	public void should_verifyDeploymentStatus_when_finished() throws RollbackedException {
		mockControllerGetDeploymentState("success");

		ArgumentCaptor<ModelNode> captor = ArgumentCaptor.forClass(ModelNode.class);
		boolean result = serverControllerService.isDeploymentFinished();
		Assert.assertTrue(result);
		Mockito.verify(controllerService, Mockito.atLeastOnce()).execute(captor.capture());

		String operation = captor.getValue().get(ClientConstants.OP).asString();
		Assert.assertEquals(ClientConstants.READ_ATTRIBUTE_OPERATION, operation);

		String name = captor.getValue().get(ClientConstants.NAME).asString();
		Assert.assertEquals("server-state", name);
	}

	/**
	 * Test the deployment status check when the deployment has failed.
	 *
	 * @throws RollbackedException
	 *             if a rollbackedexcepton occurs
	 */
	@Test
	public void should_verifyDeploymentStatus_when_failed() throws RollbackedException {
		mockControllerGetDeploymentState("failed");
		boolean result = serverControllerService.isDeploymentFinished();
		Assert.assertFalse(result);
	}

	/**
	 * Test the deployment status check when the deployment has failed with an exception.
	 *
	 * @throws RollbackedException
	 *             if a rollbackedexception occurs
	 */
	@Test
	public void should_verifyDeploymentStatus_when_exceptionOccurs() throws RollbackedException {
		Mockito.when(controllerService.execute(Matchers.any(ModelNode.class))).thenThrow(new RollbackedException());
		boolean result = serverControllerService.isDeploymentFinished();
		Assert.assertFalse(result);
	}

	private void mockControllerGetDeploymentNames() throws RollbackedException {
		ModelNode result = Mockito.mock(ModelNode.class);

		ModelNode deployments = Mockito.mock(ModelNode.class);
		Mockito.when(deployments.asList())
				.thenReturn(Arrays.asList(new ModelNode("firstDeployment"), new ModelNode("secondDeployment")));
		Mockito.when(result.get(ClientConstants.RESULT)).thenReturn(deployments);
		Mockito.when(result.isDefined()).thenReturn(true);

		when(controllerService.execute(Matchers.any(ModelNode.class))).thenReturn(result);
	}

	private void mockControllerGetDeploymentState(String status) throws RollbackedException {
		ModelNode result = Mockito.mock(ModelNode.class);

		Mockito.when(result.get(ClientConstants.OUTCOME)).thenReturn(new ModelNode(status));

		when(controllerService.execute(Matchers.any(ModelNode.class))).thenReturn(result);
	}
}
