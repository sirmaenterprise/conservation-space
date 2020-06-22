package com.sirma.itt.seip.instance.lock.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockService;

/**
 * Test for {@link UnlockAction}.
 *
 * @author A. Kunchev
 */
public class UnlockActionTest {

	@InjectMocks
	private UnlockAction action;

	@Mock
	private LockService lockService;

	@Before
	public void setup() {
		action = new UnlockAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("unlock", action.getName());
	}

	@Test
	public void perform_lockServiceCalled() {
		UnlockRequest request = new UnlockRequest();
		request.setTargetId("instanceId");
		InstanceReference reference = mock(InstanceReference.class);
		request.setTargetReference(reference);
		action.perform(request);
		verify(lockService).unlock(reference);
	}
}
