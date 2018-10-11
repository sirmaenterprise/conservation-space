package com.sirma.itt.seip.template.actions;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.template.TemplateService;

public class DeactivateTemplateActionTest {

	@InjectMocks
	private DeactivateTemplateAction deactivateTemplateAction;

	@Mock
	private TemplateService templateService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Call_Template_Service_With_Correct_Arguments() throws Exception {
		DeactivateTemplateActionRequest request = new DeactivateTemplateActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(DeactivateTemplateActionRequest.OPERATION_NAME);

		deactivateTemplateAction.perform(request);
		verify(templateService).deactivate(eq("emf:instance"));
	}
}
