package com.sirma.itt.seip.instance.lock.action;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockService;

/**
 * Test for {@link LockAction}.
 *
 * @author A. Kunchev
 */
public class LockActionTest {

	@InjectMocks
	private LockAction action;

	@Mock
	private LockService lockService;

	@Before
	public void setup() {
		action = new LockAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("lock", action.getName());
	}

	@Test
	public void perform_lockServiceCalled() {
		LockRequest request = new LockRequest();
		request.setTargetId("instanceId");
		request.setLockType("for edit");
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		request.setTargetReference(reference);
		action.perform(request);
		Mockito.verify(lockService).lock(reference, "for edit");
	}
}
