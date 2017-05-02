package com.sirma.itt.seip.instance.actions.revert;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link RevertVersionRestService}.
 *
 * @author A. Kunchev
 */
public class RevertVersionRestServiceTest {

	@InjectMocks
	private RevertVersionRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new RevertVersionRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void revertVersion_internalServiceCalled() {
		RevertVersionRequest request = new RevertVersionRequest();
		service.revertVersion(request);
		verify(actions).callAction(request);
	}

}
