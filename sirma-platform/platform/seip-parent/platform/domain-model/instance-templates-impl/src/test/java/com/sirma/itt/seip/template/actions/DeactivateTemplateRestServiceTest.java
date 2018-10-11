package com.sirma.itt.seip.template.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

public class DeactivateTemplateRestServiceTest {

	@InjectMocks
	private DeactivateTemplateRestService deactivateTemplateRestService;

	@Mock
	private Actions actions;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Call_Deactivate_Action() throws Exception {
		deactivateTemplateRestService.deactivate(new DeactivateTemplateActionRequest());
		verify(actions).callAction(any(DeactivateTemplateActionRequest.class));
	}
}
