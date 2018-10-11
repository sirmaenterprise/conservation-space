package com.sirma.itt.seip.instance.lock.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
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

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Before
	public void setup() {
		action = new UnlockAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("unlock", action.getName());
	}

	public void perform_nullReference() {
		Mockito.when(instanceTypeResolver.resolveReference(any())).thenReturn(Optional.empty());
		Mockito.verifyZeroInteractions(lockService.unlock(Matchers.any(InstanceReference.class)));
	}

	@Test
	public void perform_lockServiceCalled() {
		UnlockRequest request = new UnlockRequest();
		request.setTargetId("instanceId");
		InstanceReference reference = Mockito.mock(InstanceReference.class);
		Mockito.when(instanceTypeResolver.resolveReference("instanceId")).thenReturn(Optional.of(reference));
		action.perform(request);
		Mockito.verify(lockService).unlock(reference);
	}


}
