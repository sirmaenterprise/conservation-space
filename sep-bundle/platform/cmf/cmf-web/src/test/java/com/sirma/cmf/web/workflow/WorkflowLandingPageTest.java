package com.sirma.cmf.web.workflow;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Test for WorkflowLandingPage.
 * 
 * @author svelikov
 */
@Test
public class WorkflowLandingPageTest extends CMFTest {

	private final WorkflowLandingPage action;
	private WorkflowService workflowService;

	/**
	 * Instantiates a new workflow landing page test.
	 */
	public WorkflowLandingPageTest() {
		action = new WorkflowLandingPage() {

			private DocumentContext docContext = new DocumentContext();

			@Override
			public DocumentContext getDocumentContext() {
				return docContext;
			}

			@Override
			public void setDocumentContext(DocumentContext documentContext) {
				docContext = documentContext;
			}
		};

		workflowService = Mockito.mock(WorkflowService.class);
		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "workflowService", workflowService);
	}

	/**
	 * Test for getInstanceDefinitionClass method.
	 */
	public void getInstanceDefinitionClassTest() {
		Class<WorkflowDefinition> actual = action.getInstanceDefinitionClass();
		Assert.assertEquals(actual, WorkflowDefinition.class);
	}

	/**
	 * Test for getNewInstance method.
	 */
	public void getNewInstanceTest() {
		WorkflowDefinition workflowDefinition = createWorkflowDefinition("dmsid");
		CaseInstance context = createCaseInstance(Long.valueOf(1L));
		WorkflowInstanceContext workflowInstance = createWorkflowInstance(Long.valueOf(1L));
		Mockito.when(workflowService.createInstance(workflowDefinition, context)).thenReturn(
				workflowInstance);
		WorkflowInstanceContext newInstance = action.getNewInstance(workflowDefinition, context);
		Assert.assertEquals(newInstance, workflowInstance);
	}
}
