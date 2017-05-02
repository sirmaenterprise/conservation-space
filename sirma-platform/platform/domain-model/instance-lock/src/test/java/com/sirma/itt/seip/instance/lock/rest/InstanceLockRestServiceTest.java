package com.sirma.itt.seip.instance.lock.rest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.lock.action.LockRequest;

/**
 * Test for {@link InstanceLockRestService}.
 *
 * @author A. Kunchev
 */
public class InstanceLockRestServiceTest {

	@InjectMocks
	private InstanceLockRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new InstanceLockRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void lock_actionsCalled() {
		LockRequest request = new LockRequest();
		request.setTargetId("instanceId");
		request.setLockType("");
		service.lock(request);
		Mockito.verify(actions).callAction(request);
	}

	@Test
	public void unlock_actionsCalled() {
		service.unlock("instanceId");
		Mockito.verify(actions).callAction(Matchers.any(LockRequest.class));
	}

}
