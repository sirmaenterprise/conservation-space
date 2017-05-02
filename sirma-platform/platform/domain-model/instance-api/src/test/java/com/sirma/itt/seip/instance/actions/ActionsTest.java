package com.sirma.itt.seip.instance.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Test for {@link Actions}
 *
 * @author BBonev
 */
public class ActionsTest {

	@InjectMocks
	private Actions actions;

	private List<Action<ActionRequest>> actionsList = new LinkedList<>();

	@Spy
	private Plugins<Action<ActionRequest>> actionInstances = new Plugins<>("", actionsList);

	@Mock
	private InstanceTypeResolver resolver;

	@Mock
	private EventService eventService;

	@Mock
	private Action<ActionRequest> action;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		actionsList.clear();
		actionsList.add(action);

		when(action.getName()).thenReturn("actionName");

		when(resolver.resolveReference("emf:instance")).then(a ->
			{
				String id = a.getArgumentAt(0, String.class);
				Instance instance = new EmfInstance();
				instance.setId(id);
				return getReference(id, instance);
			});
		when(resolver.resolveReference("emf:nonExsistantInstance")).thenReturn(Optional.empty());
	}

	@Test
	public void callAction() {
		ActionRequest request = new Request("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");

		actions.callAction(request);

		verify(action).perform(request);
		verify(eventService).fire(any());
		verify(eventService).fireNextPhase(any());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void callUndefinedAction() {
		ActionRequest request = new Request("undefinedActionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");

		actions.callAction(request);
	}

	@Test
	public void callAction_forMissingInstance() {
		ActionRequest request = new Request("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:nonExsistantInstance");

		actions.callAction(request);

		verify(action).perform(request);
		verify(eventService, never()).fire(any());
		verify(eventService).fireNextPhase(eq(null));
	}

	@Test
	public void callAction_missingTargetId() {
		ActionRequest request = new Request("actionName");
		request.setUserOperation("doSomething");

		actions.callAction(request);
		verify(action).perform(request);
		verify(eventService, never()).fire(any());
		verify(eventService).fireNextPhase(eq(null));
	}

	@Test
	public void callAction_missingInstance() {
		ActionRequest request = new Request("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:missingInstance");

		when(resolver.resolveReference("emf:missingInstance")).then(a -> getReference(a.getArgumentAt(0, String.class), null));
		actions.callAction(request);
		verify(action).perform(request);
		verify(eventService, never()).fire(any());
		verify(eventService).fireNextPhase(eq(null));
	}

	@Test
	public void callAction_withTargetReference() {
		ActionRequest request = new Request("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:nonExsistantInstance");
		InstanceReference reference = mock(InstanceReference.class);
		Instance instance = new EmfInstance();
		when(reference.toInstance()).thenReturn(instance);
		request.setTargetReference(reference);

		actions.callAction(request);
		verify(action).perform(request);
		verify(eventService).fire(any());
		verify(eventService).fireNextPhase(any());
	}

	/**
	 * Dummy {@link ActionRequest} implementation
	 *
	 * @author BBonev
	 */
	private static class Request extends ActionRequest {
		private static final long serialVersionUID = -7835216083328537817L;
		private final String operation;

		/**
		 * Instantiates a new request.
		 *
		 * @param operation
		 *            the operation
		 */
		Request(String operation) {
			this.operation = operation;
		}

		@Override
		public String getOperation() {
			return operation;
		}
	}

	private static Optional<InstanceReference> getReference(String id, Instance instance) {
		InstanceReference reference = mock(InstanceReference.class);
		when(reference.getIdentifier()).thenReturn(id);
		when(reference.toInstance()).thenReturn(instance);
		return Optional.of(reference);
	}
}
