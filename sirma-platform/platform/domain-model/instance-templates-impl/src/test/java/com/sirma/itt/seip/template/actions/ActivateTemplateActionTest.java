package com.sirma.itt.seip.template.actions;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.template.TemplateService;

/**
 * Tests the functionality of {@link ActivateTemplateAction}.
 * 
 * @author Vilizar Tsonev
 */
public class ActivateTemplateActionTest {

	@InjectMocks
	private ActivateTemplateAction activateTemplateAction;

	@Mock
	private TemplateService templateService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Call_Template_Service_With_Correct_Arguments() throws Exception {
		ActivateTemplateActionRequest request = new ActivateTemplateActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(ActivateTemplateActionRequest.OPERATION_NAME);

		activateTemplateAction.perform(request);
		verify(templateService).activate(eq("emf:instance"));
	}
}
