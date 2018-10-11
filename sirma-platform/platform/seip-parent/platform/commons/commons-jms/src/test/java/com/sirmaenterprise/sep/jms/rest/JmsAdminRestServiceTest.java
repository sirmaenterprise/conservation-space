package com.sirmaenterprise.sep.jms.rest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirmaenterprise.sep.jms.impl.receiver.JmsReceiverManager;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;
import com.sirmaenterprise.sep.jms.provision.JmsProvisioner;

/**
 * Test for {@link JmsAdminRestService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/05/2018
 */
public class JmsAdminRestServiceTest {
	@InjectMocks
	private JmsAdminRestService adminService;

	@Mock
	private JmsReceiverManager jmsReceiverManager;
	@Mock
	private JmsProvisioner provisioner;
	@Spy
	private InstanceProxyMock<JmsProvisioner> jmsProvisioner = new InstanceProxyMock<>();

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		jmsProvisioner.set(provisioner);
	}

	@Test
	public void getAllDefinitions() throws Exception {
		when(provisioner.resolveAll()).thenReturn(
				Arrays.asList(new DestinationDefinition(), new DestinationDefinition()));

		Collection<DestinationDefinition> definitions = adminService.getAllDefinitions();

		verify(provisioner).resolveAll();
		assertEquals(2, definitions.size());
	}

	@Test
	public void getDefinition() throws Exception {
		String queueName = "jms.queue.TestQueue";
		String jndiQueueName = "java:/" + queueName;
		when(provisioner.resolve(jndiQueueName)).thenReturn(Optional.of(new DestinationDefinition().setAddress(jndiQueueName)));

		DestinationDefinition definition = adminService.getDefinition(queueName);
		assertNotNull(definition);
	}

	@Test(expected = NotFoundException.class)
	public void getDefinition_shouldFailForMissingQueue() throws Exception {
		String queueName = "java:/jms.queue.TestQueue";
		when(provisioner.resolve(queueName)).thenReturn(Optional.empty());

		adminService.getDefinition(queueName);
	}

	@Test
	public void updateDefinition() throws Exception {
		String queueName = "jms.queue.TestQueue";
		String jndiQueueName = "java:/" + queueName;
		when(provisioner.resolve(jndiQueueName)).thenReturn(Optional.of(new DestinationDefinition().setAddress(jndiQueueName)));

		DestinationDefinition definition = adminService.updateDefinition(queueName, new DestinationDefinition());
		verify(provisioner).provisionDestination(any());
		assertNotNull(definition);
	}

	@Test
	public void getInfo() throws Exception {
		adminService.getInfo();
		verify(jmsReceiverManager).getInfo();
	}

	@Test
	public void restart() throws Exception {
		adminService.restart();
		verify(jmsReceiverManager).restart();
	}

}
