package com.sirma.cmf.web.caseinstance.tab;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Tests for CaseWorkflowListAction class.
 */
@Test
public class CaseWorkflowListActionTest extends CMFTest {

	/** The action. */
	private final CaseWorkflowListAction action;

	/** The workflow service. */
	private final WorkflowService workflowService;

	/**
	 * Instantiates a new case workflow list action test.
	 */
	public CaseWorkflowListActionTest() {
		action = new CaseWorkflowListAction();

		workflowService = Mockito.mock(WorkflowService.class);
		ReflectionUtils.setField(action, "log", LOG);
		ReflectionUtils.setField(action, "workflowService", workflowService);
	}

	/**
	 * Workflow list tab selected test.
	 */
	public void workflowListTabSelectedTest() {
		// if no instance is found in event, we expect the backend to not be invoked and the list to
		// not be null but empty
		CaseInstance caseInstanceNoWorkflows = null;
		action.retrieveWorkflows(caseInstanceNoWorkflows);
		Mockito.verify(workflowService, Mockito.never()).getWorkflowsHistory(
				Mockito.any(Instance.class));
		Assert.assertNotNull(action.getWorkflowInstanceContexts());
		Assert.assertTrue(action.getWorkflowInstanceContexts().size() == 0);
		// if instance is found in event
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1));
		List<WorkflowInstanceContext> workflowInstances = new ArrayList<WorkflowInstanceContext>();
		workflowInstances.add(createWorkflowInstance(Long.valueOf(1)));
		Mockito.when(workflowService.getWorkflowsHistory(caseInstance)).thenReturn(
				workflowInstances);
		action.retrieveWorkflows(caseInstance);
		Assert.assertNotNull(action.getWorkflowInstanceContexts());
		Assert.assertTrue(action.getWorkflowInstanceContexts().size() == 1);
	}
}
