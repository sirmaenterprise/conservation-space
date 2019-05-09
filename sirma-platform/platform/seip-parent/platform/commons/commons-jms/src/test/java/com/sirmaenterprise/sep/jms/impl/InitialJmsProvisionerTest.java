package com.sirmaenterprise.sep.jms.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.DeploymentException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;
import com.sirmaenterprise.sep.jms.provision.InitialJmsProvisioner;
import com.sirmaenterprise.sep.jms.provision.JmsProvisioner;
import com.sirmaenterprise.sep.jms.provision.JmsSubsystemConfigurations;
import com.sirmaenterprise.sep.jms.provision.JmsSubsystemModel;

/**
 * Test the initial jms provisioning.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class InitialJmsProvisionerTest {

	@Mock
	private JmsProvisioner jmsProvisioner;

	private Instance<JmsProvisioner> jmsProvisionerInstance;

	@Mock
	private JmsDefinitionProvider jmsProvider;

	@Mock
	private JmsSubsystemConfigurations jmsSubsystemConfigurations;

	@InjectMocks
	private InitialJmsProvisioner provisioner;

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		jmsProvisionerInstance = new InstanceProxyMock<>(jmsProvisioner);
		ReflectionUtils.setFieldValue(provisioner, "jmsProvisionerInstance", jmsProvisionerInstance);
		when(jmsSubsystemConfigurations.getPersistenceLocation()).thenReturn(new ConfigurationPropertyMock<>(new File(".")));
	}

	/**
	 * When there are no queues defined in the system, the subsystem shouldn't be provisioned and
	 * the controller shouldn't be called at all.
	 *
	 * @throws DeploymentException
	 *             thrown on successful execution so the deployment terminates
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	public void should_not_provision_subsystem_when_noQueues() throws DeploymentException, RollbackedException {
		provisioner.provisionJmsSubsystem();
		Mockito.verifyZeroInteractions(jmsProvisioner);
	}

	/**
	 * The jms provisioner shouldnt be called when a jms provisioner isn't available.
	 *
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	public void should_doNothing_whenNoJmsProvisioner() throws RollbackedException {
		jmsProvisionerInstance = Mockito.mock(Instance.class);
		ReflectionUtils.setFieldValue(provisioner, "jmsProvisionerInstance", jmsProvisionerInstance);
		when(jmsProvisionerInstance.isUnsatisfied()).thenReturn(true);
		provisioner.provisionJmsSubsystem();
		Mockito.verifyZeroInteractions(jmsProvisioner);
	}

	/**
	 * When there are queues defined in the system, the subsystem should be provisioned alongside
	 * all the queues.
	 *
	 * @throws DeploymentException
	 *             thrown on successful execution so the deployment terminates
	 * @throws RollbackedException
	 *             thrown on unsuccessful controller execution
	 */
	@Test
	public void should_provision_subsystem() throws DeploymentException, RollbackedException {
		Map<String, DestinationDefinition> addresses = new HashMap<>();
		addresses.put("queue", new DestinationDefinition().setType(DestinationType.QUEUE).setAddress("queue"));
		addresses.put("topic", new DestinationDefinition().setType(DestinationType.TOPIC).setAddress("topic"));
		Mockito.when(jmsProvider.getAddresses()).thenReturn(addresses);
		provisioner.provisionJmsSubsystem();

		verify(jmsProvisioner).provisionSubsystem(Matchers.any(JmsSubsystemModel.class));
		verify(jmsProvisioner, times(2)).provisionDestination(any());
	}
}
