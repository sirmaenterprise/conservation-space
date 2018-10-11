package com.sirma.itt.seip.instance.actions.move;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test the instance move rest service.
 *
 * @author nvelkov
 */
public class InstanceMoveRestServiceTest {

	@InjectMocks
	private InstanceMoveRestService service;

	@Mock
	private Actions actions;

	/**
	 * Setup the mocks.
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test if the move action is executed when the rest service is called.
	 */
	@Test
	public void testExecuteMoveAction() {
		MoveActionRequest request = new MoveActionRequest();
		service.executeMoveAction(request);
		verify(actions).callAction(request);
	}

}
