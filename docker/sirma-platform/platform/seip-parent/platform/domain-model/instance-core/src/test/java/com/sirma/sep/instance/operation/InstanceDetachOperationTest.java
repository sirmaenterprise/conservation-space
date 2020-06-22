package com.sirma.sep.instance.operation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for {@link InstanceDetachOperation}.
 *
 * @author A. Kunchev
 */
public class InstanceDetachOperationTest {

	@InjectMocks
	private InstanceDetachOperation operation;

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
		operation = new InstanceDetachOperation();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSupportedOperations() {
		Set<String> supportedOperations = operation.getSupportedOperations();
		assertTrue(supportedOperations.contains("detach"));
		assertTrue(supportedOperations.contains("detachObject"));
		assertTrue(supportedOperations.contains("detachDocument"));
	}

	@Test
	public void execute() {
		Context<String, Object> context = new Context<>(3);
		context.put("instance", new EmfInstance());
		context.put("operation", new Operation("detach"));
		context.put("instance_array", new Instance[] { new EmfInstance() });

		InstanceEventProvider<Instance> eventProvider = mock(InstanceEventProvider.class);
		when(eventProvider.createDetachEvent(any(), any()))
				.thenReturn(new InstanceDetachedEvent<Instance>(new EmfInstance(), new EmfInstance()));
		when(serviceRegistry.getEventProvider(any())).thenReturn(eventProvider);

		operation.execute(context);

		verify(contextService).bindContext(any(), any());
		verify(eventService).fire(any());
	}
}
