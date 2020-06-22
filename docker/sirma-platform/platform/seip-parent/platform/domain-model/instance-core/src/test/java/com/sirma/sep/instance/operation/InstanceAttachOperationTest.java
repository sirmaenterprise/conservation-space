package com.sirma.sep.instance.operation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for {@link InstanceAttachOperation}.
 *
 * @author A. Kunchev
 */
public class InstanceAttachOperationTest {

	@InjectMocks
	private InstanceAttachOperation operation;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private InstanceContextService contextService;
	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private EventService eventService;

	@Before
	public void setup() {
		operation = new InstanceAttachOperation();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSupportedOperations() {
		Set<String> supportedOperations = operation.getSupportedOperations();
		assertTrue(supportedOperations.contains("attach"));
		assertTrue(supportedOperations.contains("attachObject"));
		assertTrue(supportedOperations.contains("attachDocument"));
		assertTrue(supportedOperations.contains("addLibrary"));
	}

	@Test
	public void execute() {
		Context<String, Object> context = new Context<>(3);
		context.put("instance", new EmfInstance());
		context.put("operation", new Operation("attach"));
		context.put("instance_array", new Instance[] { new EmfInstance(), new EmfInstance() });

		InstanceEventProvider<Instance> eventProvider = mock(InstanceEventProvider.class);
		when(eventProvider.createAttachEvent(any(), any()))
				.thenReturn(new InstanceAttachedEvent<Instance>(new EmfInstance(), new EmfInstance()));
		when(serviceRegistry.getEventProvider(any())).thenReturn(eventProvider);

		operation.execute(context);

		verify(contextService, times(2)).bindContext(any(), any());
		verify(eventService, times(2)).fire(any());
	}
}
