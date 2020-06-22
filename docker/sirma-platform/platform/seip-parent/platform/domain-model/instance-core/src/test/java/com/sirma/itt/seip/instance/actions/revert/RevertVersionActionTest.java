package com.sirma.itt.seip.instance.actions.revert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.revert.RevertContext;

/**
 * Test for {@link RevertVersionAction}.
 *
 * @author A. Kunchev
 */
public class RevertVersionActionTest {

	@InjectMocks
	private RevertVersionAction action;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Before
	public void setup() {
		action = new RevertVersionAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("revertVersion", action.getName());
	}

	@Test
	public void perform_internalServicesCalled() {
		RevertVersionRequest request = new RevertVersionRequest();
		request.setTargetId("instance-id-v1.5");
		action.perform(request);

		verify(instanceVersionService).revertVersion(any(RevertContext.class));
	}

}
