package com.sirma.itt.seip.template.actions;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.template.TemplateService;

public class SetTemplateAsPrimaryActionTest {

	@InjectMocks
	private SetTemplateAsPrimaryAction setTemplateAsPrimaryAction;

	@Mock
	private TemplateService templateService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Call_Template_Service_With_Correct_Arguments() throws Exception {
		SetTemplateAsPrimaryActionRequest request = new SetTemplateAsPrimaryActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(SetTemplateAsPrimaryActionRequest.OPERATION_NAME);

		setTemplateAsPrimaryAction.perform(request);
		verify(templateService).setAsPrimaryTemplate(eq("emf:instance"));
	}

}
