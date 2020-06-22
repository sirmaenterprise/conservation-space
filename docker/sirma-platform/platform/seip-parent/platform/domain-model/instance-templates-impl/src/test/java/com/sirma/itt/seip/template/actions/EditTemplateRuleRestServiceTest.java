package com.sirma.itt.seip.template.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Tests the functionality of {@link EditTemplateRuleAction}
 * 
 * @author Vilizar Tsonev
 */
public class EditTemplateRuleRestServiceTest {

	@InjectMocks
	private EditTemplateRuleRestService editTemplateRuleRestService;

	@Mock
	private Actions actions;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Call_EditTemplateRule_Action() {
		editTemplateRuleRestService.editRule(new EditTemplateRuleActionRequest());
		verify(actions).callAction(any(EditTemplateRuleActionRequest.class));
	}

}
