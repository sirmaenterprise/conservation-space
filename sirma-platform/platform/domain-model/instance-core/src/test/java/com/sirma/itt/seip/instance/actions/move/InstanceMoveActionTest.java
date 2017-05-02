package com.sirma.itt.seip.instance.actions.move;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;

/**
 * Test the instance move action.
 *
 * @author nvelkov
 */
public class InstanceMoveActionTest {

	@Mock
	private InstanceOperations operationInvoker;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private InstanceContextInitializer contextInitializer;

	@InjectMocks
	private InstanceMoveAction instanceMoveAction = new InstanceMoveAction();

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the move action with a null {@link MoveActionRequest}.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void testMoveRequestNull() {
		instanceMoveAction.perform(null);
	}

	/**
	 * Test the move action with a missing operation.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void testMoveMissingOperation() {
		MoveActionRequest request = new MoveActionRequest();
		request.setTargetId("id");
		instanceMoveAction.perform(request);
	}

	/**
	 * Test the move action with a missing target instance.
	 */
	@Test(expected = EmfRuntimeException.class)
	public void testMoveMissingTargetInstance() {
		MoveActionRequest request = new MoveActionRequest();
		request.setUserOperation("move");
		instanceMoveAction.perform(request);
	}

	/**
	 * Test the move action with a valid data.
	 */
	@Test
	public void testMove() {
		MoveActionRequest request = new MoveActionRequest();
		request.setUserOperation("move");
		request.setTargetId("targetId");
		request.setDestinationId("destinationId");
		mockInstanceHelper(request.getTargetId().toString());
		mockInstanceHelper(request.getDestinationId().toString());
		Instance result = instanceMoveAction.perform(request);
		Assert.assertEquals(request.getTargetId(), result.getId());
		verify(contextInitializer, times(2)).restoreHierarchy(any(Instance.class));
	}

	private void mockInstanceHelper(String instanceId) {
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Instance instance = new EmfInstance();
		instance.setId(instanceId);
		Mockito.when(reference.toInstance()).thenReturn(instance);
		Mockito.when(instanceTypeResolver.resolveReference(Matchers.eq(instanceId))).thenReturn(Optional.of(reference));
	}
}
