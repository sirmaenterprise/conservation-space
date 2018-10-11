package com.sirma.sep.instance.operation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.BeforeInstanceDeleteEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;

/**
 * Test for {@link BaseDeleteOperation}.
 *
 * @author A. Kunchev
 */
public class BaseDeleteOperationTest {

	@InjectMocks
	private BaseDeleteOperation operation;

	@Mock
	private ServiceRegistry serviceRegistry;

	@Mock
	private EventService eventService;

	@Mock
	private StateService stateService;

	@Before
	public void setup() {
		operation = new BaseDeleteOperation();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSupportedOperations_containsDelete() {
		assertTrue(operation.getSupportedOperations().contains("delete"));
	}

	@Test
	public void execute() {
		Context<String, Object> context = new Context<>(2);
		Instance instance = new EmfInstance();
		context.put("instance", instance);
		Operation deleteOperation = new Operation("delete");
		context.put("operation", deleteOperation);
		operation.execute(context);

		assertTrue(instance.isDeleted());
		verify(stateService).changeState(instance, deleteOperation);
		verify(eventService).fireNextPhase(any());
	}

	@Test
	public void createBeforeEvent_noProvider() {
		when(serviceRegistry.getEventProvider(any())).thenReturn(null);
		assertNull(operation.createBeforeEvent(new EmfInstance()));
	}

	@Test
	public void createBeforeEvent_withProvider_eventFired() {
		InstanceEventProvider<Instance> eventProvider = mock(InstanceEventProvider.class);
		when(eventProvider.createBeforeInstanceDeleteEvent(any()))
				.thenReturn(new BeforeInstanceDeleteEvent<>(new EmfInstance()));
		when(serviceRegistry.getEventProvider(any())).thenReturn(eventProvider);
		assertNotNull(operation.createBeforeEvent(new EmfInstance()));
		verify(eventService).fire(any());
	}
}
