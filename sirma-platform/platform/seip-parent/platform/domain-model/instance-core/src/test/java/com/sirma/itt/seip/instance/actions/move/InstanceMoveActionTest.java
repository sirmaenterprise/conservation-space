package com.sirma.itt.seip.instance.actions.move;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Tests for {@link InstanceMoveAction}.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceMoveActionTest {

	private static final String INSTANCE_ID = "emf:instanceId";
	private static final String PARENT_INSTANCE_ID = "emf:parentInstanceId";
	private static final String USER_OPERATION_NAME = "user-move-operation-name";

	private Instance target = new EmfInstance();

	@Mock
	private InstanceReference targetReference;

	@Mock
	private Instance parent;

	@Mock
	private InstanceReference parentReference;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private InstanceOperations operationInvoker;

	@InjectMocks
	private InstanceMoveAction instanceMoveAction;

	@Before
	public void init() {
		when(domainInstanceService.loadInstance(PARENT_INSTANCE_ID)).thenReturn(parent);
	}

	@Test
	public void getName_move() {
		assertEquals("move", instanceMoveAction.getName());
	}

	@Test
	public void should_InvokeOperationWithNewParent_When_NewParentIdIsPassed() {
		MoveActionRequest request = createMoveActionRequest(PARENT_INSTANCE_ID);
		Instance resultedTarget = instanceMoveAction.perform(request);
		assertEquals(target, resultedTarget);

		verify(operationInvoker).invokeMove(argThat(matchesInstance(parent)),
											argThat(matchesOperation(new Operation(USER_OPERATION_NAME, true))),
											argThat(matchesInstance(target)));
	}

	@Test
	public void should_InvokeOperationWithoutNewParent_When_NewParentIdIsNotPassed() {
		MoveActionRequest request = createMoveActionRequest(null);
		Instance resultedTarget = instanceMoveAction.perform(request);
		assertEquals(target, resultedTarget);

		verify(operationInvoker).invokeMove(argThat(matchesInstance(null)),
											argThat(matchesOperation(new Operation(USER_OPERATION_NAME, true))),
											argThat(matchesInstance(target)));
	}

	private static CustomMatcher<Instance> matchesInstance(Instance expectedInstance) {
		return CustomMatcher.of((Instance instance) -> assertEquals(expectedInstance, instance));
	}

	private static CustomMatcher<Operation> matchesOperation(Operation expectedOperation) {
		return CustomMatcher.of((Operation operation) -> assertEquals(expectedOperation, operation));
	}

	private MoveActionRequest createMoveActionRequest(Serializable newParentId) {
		MoveActionRequest request = new MoveActionRequest();
		request.setTargetId(INSTANCE_ID);
		request.setDestinationId(newParentId);
		request.setUserOperation(USER_OPERATION_NAME);
		when(targetReference.toInstance()).thenReturn(target);
		request.setTargetReference(targetReference);
		return request;
	}
}