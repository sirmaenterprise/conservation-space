package com.sirma.itt.seip.template.actions;

import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.template.rules.TemplateRuleTranslator;

/**
 * Tests the functionality of {@link EditTemplateRuleAction}.
 *
 * @author Vilizar Tsonev
 */
public class EditTemplateRuleActionTest {

	@InjectMocks
	private EditTemplateRuleAction editTemplateRuleAction;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private TemplateRuleTranslator ruleTranslator;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Save_Edited_Rule() {
		EditTemplateRuleActionRequest request = new EditTemplateRuleActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(EditTemplateRuleActionRequest.OPERATION_NAME);
		request.setRule("testRule");

		mockExistingInstance();

		editTemplateRuleAction.perform(request);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		assertEquals("testRule", captor.getValue().getInstance().get(TEMPLATE_RULE));
		assertEquals("emf:loadedInstance", captor.getValue().getInstance().getId());
	}

	@Test
	public void should_Save_Rule_And_Description_As_Null_When_Empty_Rule_Passed() {
		EditTemplateRuleActionRequest request = new EditTemplateRuleActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(EditTemplateRuleActionRequest.OPERATION_NAME);
		request.setRule("");

		mockExistingInstance();

		editTemplateRuleAction.perform(request);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		assertNull(captor.getValue().getInstance().get(TEMPLATE_RULE));
		assertNull(captor.getValue().getInstance().get(TEMPLATE_RULE_DESCRIPTION));
		assertEquals("emf:loadedInstance", captor.getValue().getInstance().getId());
	}

	@Test
	public void should_Build_And_Save_Rule_Description_When_Rule_Is_Provided() {
		EditTemplateRuleActionRequest request = new EditTemplateRuleActionRequest();
		request.setTargetId("emf:instance");
		request.setUserOperation(EditTemplateRuleActionRequest.OPERATION_NAME);
		request.setRule("testRule");

		mockExistingInstance();
		when(ruleTranslator.translate(eq("testRule"), eq("sampleForType"))).thenReturn("testRuleDescription");

		editTemplateRuleAction.perform(request);

		ArgumentCaptor<InstanceSaveContext> captor = ArgumentCaptor.forClass(InstanceSaveContext.class);
		verify(domainInstanceService).save(captor.capture());

		assertEquals("testRule", captor.getValue().getInstance().get(TEMPLATE_RULE));
		assertEquals("testRuleDescription", captor.getValue().getInstance().get(TEMPLATE_RULE_DESCRIPTION));
		assertEquals("emf:loadedInstance", captor.getValue().getInstance().getId());
	}

	private void mockExistingInstance() {
		EmfInstance templateInstance = new EmfInstance();
		templateInstance.setId("emf:loadedInstance");
		templateInstance.add(FOR_OBJECT_TYPE, "sampleForType");
		when(domainInstanceService.loadInstance(eq("emf:instance"))).thenReturn(templateInstance);
		when(domainInstanceService.save(any())).thenReturn(templateInstance);
	}
}
