package com.sirma.itt.seip.instance.actions.transition;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link InstanceTransitionRestService}.
 *
 * @author A. Kunchev
 */
public class InstanceTransitionRestServicеTest {

	@InjectMocks
	private InstanceTransitionRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void executeChangeStateAction() {
		TransitionActionRequest request = new TransitionActionRequest();
		service.executeChangeStateAction(request);
		verify(actions).callAction(request);
	}

}
