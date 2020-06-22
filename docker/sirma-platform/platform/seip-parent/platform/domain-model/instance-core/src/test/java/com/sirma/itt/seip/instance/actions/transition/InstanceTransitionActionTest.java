package com.sirma.itt.seip.instance.actions.transition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link InstanceTransitionAction}.
 *
 * @author A. Kunchev
 */
public class InstanceTransitionActionTest {

	@InjectMocks
	private InstanceTransitionAction action;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void setup() {
		action = new InstanceTransitionAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("transition", action.getName());
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullTargetId() {
		action.perform(new TransitionActionRequest());
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_emptyTargetId() {
		TransitionActionRequest request = new TransitionActionRequest();
		request.setTargetId("");
		action.perform(request);
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullUserOperation() {
		TransitionActionRequest request = new TransitionActionRequest();
		request.setTargetId("targetId");
		action.perform(request);
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_emptyUserOperation() {
		TransitionActionRequest request = new TransitionActionRequest();
		request.setTargetId("targetId");
		request.setUserOperation("");
		action.perform(request);
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_nullInstance() {
		TransitionActionRequest request = new TransitionActionRequest();
		request.setTargetId("targetIdNull");
		request.setUserOperation("userOperation");
		request.setTargetInstance(null);
		action.perform(request);
	}

	@Test(expected = EmfRuntimeException.class)
	public void perform_exceptionWhileSavingRelations() {
		TransitionActionRequest request = new TransitionActionRequest();
		request.setTargetId("targetIdNull");
		request.setUserOperation("userOperation");
		Instance instance = new EmfInstance();
		request.setTargetInstance(instance);

		when(domainInstanceService.save(any(InstanceSaveContext.class))).thenThrow(new RuntimeException());
		action.perform(request);
	}

	@Test
	public void perform_saveCalled() {
		TransitionActionRequest request = new TransitionActionRequest();
		Instance instance = new EmfInstance();
		String id = "instanceId";
		instance.setId(id);
		request.setTargetId(id);
		request.setUserOperation("userOperation");
		request.setTargetInstance(instance);
		action.perform(request);
		verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(instance, context.getInstance());
			assertEquals("userOperation", context.getOperation().getUserOperationId());
			assertEquals("transition", context.getOperation().getOperation());
			assertTrue(context.getOperation().isUserOperation());
		})));
	}

}
