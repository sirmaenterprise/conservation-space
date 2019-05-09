package com.sirma.itt.seip.instance.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;

/**
 * Test for {@link ActionExecutor}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionExecutorTest {

	@InjectMocks
	private ActionExecutor executor = mock(ActionExecutor.class, CALLS_REAL_METHODS);

	@Mock
	private EventService eventService;

	@Mock
	private Action<ActionRequest> action;

	@Mock
	private ActionRequest request;

	@Test
	public void execute_eventsFired_actionCalled() {
		when(request.getTargetReference()).thenReturn(buildInstance().toReference());
		executor.execute(action, request);
		verify(eventService).fire(any());
		verify(action).perform(request);
		verify(eventService).fireNextPhase(any());
	}

	private static EmfInstance buildInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setReference(mock(InstanceReference.class));
		return instance;
	}
}
