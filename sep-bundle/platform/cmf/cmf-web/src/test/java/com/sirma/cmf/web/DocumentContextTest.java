package com.sirma.cmf.web;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;

/**
 * Test for DocumentContext class.
 * 
 * @author svelikov
 */
@Test
public class DocumentContextTest extends CMFTest {

	private final DocumentContext context;

	/**
	 * Constructor initializes the class under test.
	 */
	public DocumentContextTest() {
		context = new DocumentContext();
	}

	/**
	 * Reset the context before tests.
	 */
	@BeforeMethod
	public void reset() {
		context.clear();
	}

	/**
	 * Clear workflow data test.
	 */
	public void clearWorkflowDataTest() {
		WorkflowInstanceContext workflowInstance = createWorkflowInstance(Long.valueOf(1L));
		context.addInstance(workflowInstance);
		TaskInstance taskInstance = createWorkflowTaskInstance(Long.valueOf(1L));
		context.addInstance(taskInstance);
		WorkflowDefinition workflowDefinition = createWorkflowDefinition("wfdefinition");
		context.addDefinition(WorkflowDefinition.class, workflowDefinition);
		TaskDefinitionRef taskDefinition = createTaskDefinition("taskdefinition");
		context.addDefinition(TaskDefinitionRef.class, taskDefinition);

		context.clearWorkflowData();
		assertNull(context.getInstance(WorkflowInstanceContext.class));
		assertNull(context.getInstance(TaskInstance.class));
		assertNull(context.getDefinition(WorkflowDefinition.class));
		assertNull(context.getDefinition(TaskDefinitionRef.class));
	}

	/**
	 * Test if the method returns appropriate result.
	 */
	public void getCurrentInstanceTest() {
		Instance currentInstance = context.getCurrentInstance();
		assertNull(currentInstance);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.setCurrentInstance(caseInstance);
		Instance actual = context.getCurrentInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test if method sets current instance.
	 */
	public void setCurrentInstanceTest() {
		context.setCurrentInstance(null);

		Instance actual = context.getCurrentInstance();
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.setCurrentInstance(caseInstance);
		actual = context.getCurrentInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test setRootInstance method.
	 */
	public void setRootInstanceTest() {
		context.setRootInstance(null);

		Instance actual = context.getRootInstance();
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.setRootInstance(caseInstance);
		actual = context.getRootInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test getRootInstance method.
	 */
	public void getRootInstanceTest() {
		Instance actual = context.getRootInstance();
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.setRootInstance(caseInstance);
		actual = context.getRootInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test populateContext method.
	 */
	public void populateContextTest() {
		// context should be populated if both definition and instance are provided
		context.populateContext(null, CaseDefinition.class, null);

		Instance actualInstance = context.getCurrentInstance();
		assertNull(actualInstance);
		CaseDefinition actualDefinition = context.getDefinition(CaseDefinition.class);
		assertNull(actualDefinition);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		CaseDefinition caseDefinition = createCaseDefinition("dmsid");

		context.populateContext(caseInstance, CaseDefinition.class, null);
		actualInstance = context.getCurrentInstance();
		assertNull(actualInstance);
		actualDefinition = context.getDefinition(CaseDefinition.class);
		assertNull(actualDefinition);

		context.populateContext(null, CaseDefinition.class, caseDefinition);
		actualInstance = context.getCurrentInstance();
		assertNull(actualInstance);
		actualDefinition = context.getDefinition(CaseDefinition.class);
		assertNull(actualDefinition);

		context.populateContext(caseInstance, CaseDefinition.class, caseDefinition);
		actualInstance = context.getCurrentInstance();
		assertEquals(actualInstance, caseInstance);
		actualDefinition = context.getDefinition(CaseDefinition.class);
		assertEquals(actualDefinition, caseDefinition);
	}

	/**
	 * Test for setFormMode method.
	 */
	public void setFormModeTest() {
		context.setFormMode(null);

		FormViewMode actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PREVIEW);

		context.setFormMode(FormViewMode.PREVIEW);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PREVIEW);

		context.setFormMode(FormViewMode.EDIT);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.EDIT);

		context.setFormMode(FormViewMode.PRINT);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PRINT);
	}

	/**
	 * Test for getFormMode method.
	 */
	public void getFormModeTest() {
		FormViewMode actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PREVIEW);

		context.setFormMode(FormViewMode.PREVIEW);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PREVIEW);

		context.setFormMode(FormViewMode.EDIT);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.EDIT);

		context.setFormMode(FormViewMode.PRINT);
		actualFormMode = context.getFormMode();
		assertEquals(actualFormMode, FormViewMode.PRINT);
	}

	/**
	 * Test for getCurrentOperation method.
	 */
	public void getCurrentOperation() {
		String actual = context.getCurrentOperation(null);
		assertNull(actual);

		// if no operation exists for provided instance, we expect null
		actual = context.getCurrentOperation(CaseInstance.class.getSimpleName());
		assertNull(actual);

		String operationId = "createCase";
		context.setCurrentOperation(CaseInstance.class.getSimpleName(), operationId);
		actual = context.getCurrentOperation(CaseInstance.class.getSimpleName());
		assertEquals(actual, operationId);
	}

	/**
	 * Test for resetCurrentOperation method.
	 */
	public void resetCurrentOperationTest() {
		String operationId = "createCase";
		context.setCurrentOperation(CaseInstance.class.getSimpleName(), operationId);

		context.resetCurrentOperation(null);
		String actual = context.getCurrentOperation(CaseInstance.class.getSimpleName());
		assertNotNull(actual);

		context.resetCurrentOperation(TaskInstance.class.getSimpleName());
		actual = context.getCurrentOperation(CaseInstance.class.getSimpleName());
		assertNotNull(actual);

		context.resetCurrentOperation(CaseInstance.class.getSimpleName());
		actual = context.getCurrentOperation(CaseInstance.class.getSimpleName());
		assertNull(actual);
	}

	/**
	 * Test for addContextInstance method.
	 */
	public void addContextInstanceTest() {
		context.addContextInstance(null);
		Instance actual = context.getContextInstance();
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.addContextInstance(caseInstance);
		actual = context.getContextInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test for getContextInstance method.
	 */
	public void getContextInstanceTest() {
		Instance actual = context.getContextInstance();
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.addContextInstance(caseInstance);
		actual = context.getContextInstance();
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test setter. :)
	 */
	public void testSetGetCaseInstance() {
		assertNull(context.getInstance(CaseInstance.class));

		CaseInstance caseInstance = new CaseInstance();
		context.addInstance(caseInstance);
		assertNotNull(context.getInstance(CaseInstance.class));
	}

	/**
	 * Test for addInstance method.
	 */
	public void addInstanceTest() {
		context.addInstance(null);
		CaseInstance actual = context.getInstance(CaseInstance.class);
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.addInstance(caseInstance);
		actual = context.getInstance(CaseInstance.class);
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test for getInstance method.
	 */
	public void getInstanceTest() {
		CaseInstance actual = context.getInstance(CaseInstance.class);
		assertNull(actual);

		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.addInstance(caseInstance);
		actual = context.getInstance(CaseInstance.class);
		assertEquals(actual, caseInstance);
	}

	/**
	 * Test for removeInstance method.
	 */
	public void removeInstanceTest() {
		CaseInstance caseInstance = createCaseInstance(Long.valueOf(1L));
		context.addInstance(caseInstance);
		context.removeInstance(caseInstance);
		CaseInstance actual = context.getInstance(CaseInstance.class);
		assertNull(actual);
	}

	/**
	 * Test for addDefintion method.
	 */
	public void addDefintionTest() {
		context.addDefinition(CaseDefinition.class, null);
		CaseDefinition actual = context.getDefinition(CaseDefinition.class);
		assertNull(actual);

		CaseDefinition caseDefinition = createCaseDefinition("dmsid");
		context.addDefinition(CaseDefinition.class, caseDefinition);
		actual = context.getDefinition(CaseDefinition.class);
		assertEquals(actual, caseDefinition);
	}

	/**
	 * Test for getDefinition method.
	 */
	public void getDefinitionTest() {
		CaseDefinition actual = context.getDefinition(CaseDefinition.class);
		assertNull(actual);

		CaseDefinition caseDefinition = createCaseDefinition("dmsId");
		context.addDefinition(CaseDefinition.class, caseDefinition);
		actual = context.getDefinition(CaseDefinition.class);
		assertEquals(actual, caseDefinition);
	}

	/**
	 * Test for removeDefinition method.
	 */
	public void removeDefinitionTest() {
		CaseDefinition caseDefinition = createCaseDefinition("dmsId");
		context.addDefinition(CaseDefinition.class, caseDefinition);
		context.removeDefinition(DocumentDefinitionRef.class);
		CaseDefinition actual = context.getDefinition(CaseDefinition.class);
		assertNotNull(actual);

		context.removeDefinition(CaseDefinition.class);
		actual = context.getDefinition(CaseDefinition.class);
		assertNull(actual);
	}

	/**
	 * Test for setSelectedAction method.
	 */
	public void setSelectedActionTest() {
		context.setSelectedAction(null);
		Action actual = context.getSelectedAction();
		assertNull(actual);

		Action action = new EmfAction("edit");
		context.setSelectedAction(action);
		actual = context.getSelectedAction();
		assertEquals(actual, action);
	}

	/**
	 * Test for getSelectedAction method.
	 */
	public void getSelectedActionTest() {
		Action actual = context.getSelectedAction();
		assertNull(actual);

		context.setSelectedAction(null);
		actual = context.getSelectedAction();
		assertNull(actual);

		Action action = new EmfAction("edit");
		context.setSelectedAction(action);
		actual = context.getSelectedAction();
		assertEquals(actual, action);
	}

	/**
	 * Test for clearSelectedAction method.
	 */
	public void clearSelectedActionTest() {
		Action action = new EmfAction("edit");
		context.setSelectedAction(action);
		context.clearSelectedAction();
		Action actual = context.getSelectedAction();
		assertNull(actual);
	}

	/**
	 * Test for setSelectedTab method.
	 */
	public void setSelectedTabTest() {
		context.setSelectedTab(null);
		String actual = context.getSelectedTab();
		assertNull(actual);

		String selectedTab = "case-details";
		context.setSelectedTab(selectedTab);
		actual = context.getSelectedTab();
		assertEquals(actual, selectedTab);
	}

	/**
	 * Test for getSelectedTab method.
	 */
	public void getSelectedTabTest() {
		String actual = context.getSelectedTab();
		assertNull(actual);

		String selectedTab = "case-details";
		context.setSelectedTab(selectedTab);
		actual = context.getSelectedTab();
		assertEquals(actual, selectedTab);
	}

}
