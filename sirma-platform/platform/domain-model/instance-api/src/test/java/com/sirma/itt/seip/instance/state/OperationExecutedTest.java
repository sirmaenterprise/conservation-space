package com.sirma.itt.seip.instance.state;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test for {@link OperationExecutedEvent}.
 *
 * @author Ivo Rusev
 */
public class OperationExecutedTest {

	private static final String OPERATION_ID = "myOperation";

	private static final String EVENT_INFO = "OperationExecutedEvent [operation=Operation [operation=myOperation, userOperationId=null, isUserOperation=false,"
			+ " nextPrimaryState=null, nextSecondaryState=null], target=null]";

	@Mock
	private Instance instance;

	private Operation operation = new Operation();

	@Before
	public void init() {
		operation.setOperation(OPERATION_ID);
	}

	@Test
	public void testOperationExecutedEvent() {
		OperationExecutedEvent event = new OperationExecutedEvent(operation, instance);
		assertEquals(OPERATION_ID, event.getOperationId());
		assertEquals(OPERATION_ID, event.getOperation().getOperation());
		assertEquals(instance, event.getInstance());
		assertEquals(EVENT_INFO, event.toString());
	}

}
