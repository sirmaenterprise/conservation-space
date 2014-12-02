package com.sirma.itt.cmf.instance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class DocumentAllowedChildrenProviderTest.
 * 
 * @author BBonev
 */
@Test
public class DocumentAllowedChildrenProviderTest extends CmfTest {

	/** The workflow service. */
	private WorkflowService workflowService;

	/** The allowed children type provider. */
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The provider. */
	private DocumentAllowedChildrenProvider provider;

	/** The task service. */
	private TaskService taskService;

	/**
	 * Initializes the test.
	 */
	@BeforeMethod
	public void initTest() {
		workflowService = Mockito.mock(WorkflowService.class);
		taskService = Mockito.mock(TaskService.class);
		Instance<WorkflowService> workflowServiceProxy = new InstanceProxyMock<WorkflowService>(
				workflowService);
		allowedChildrenTypeProvider = Mockito.mock(AllowedChildrenTypeProvider.class);
		dictionaryService = Mockito.mock(DictionaryService.class);
		provider = new DocumentAllowedChildrenProvider(dictionaryService,
				allowedChildrenTypeProvider, workflowServiceProxy);
		ReflectionUtils.setField(provider, "taskService", new InstanceProxyMock<TaskService>(
				taskService));

		createTypeConverter();
	}

	/**
	 * Calculate active test.
	 */
	public void calculateActiveTest() {
		DocumentInstance instance = new DocumentInstance();
		instance.setId("emf:document");

		// test without context
		boolean active = provider.calculateActive(instance, ObjectTypesCmf.WORKFLOW);
		Assert.assertFalse(active);
		Mockito.verify(taskService, Mockito.never());

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("emf:case");
		SectionInstance sectionInstance = new SectionInstance();
		sectionInstance.setId("emf:section");
		caseInstance.getSections().add(sectionInstance);
		sectionInstance.getContent().add(instance);
		caseInstance.initBidirection();

		// test without active tasks
		active = provider.calculateActive(instance, ObjectTypesCmf.WORKFLOW);
		Assert.assertFalse(active);

		// test with active tasks
		Mockito.when(
				taskService.getOwnedTaskInstances(Mockito.eq(caseInstance),
						Mockito.eq(TaskState.IN_PROGRESS), Mockito.eq(TaskType.WORKFLOW_TASK)))
				.thenReturn(Arrays.asList("testTaskId"));
		active = provider.calculateActive(instance, ObjectTypesCmf.WORKFLOW);
		Assert.assertTrue(active);
	}

	/**
	 * Gets the active test.
	 */
	public void getActiveTest() {
		DocumentInstance instance = new DocumentInstance();
		instance.setId("emf:document");

		// test without context
		List<com.sirma.itt.emf.instance.model.Instance> active = provider.getActive(instance,
				ObjectTypesCmf.WORKFLOW);
		Assert.assertNotNull(active);
		Assert.assertTrue(active.isEmpty());
		Mockito.verify(workflowService, Mockito.never());

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("emf:case");
		SectionInstance sectionInstance = new SectionInstance();
		sectionInstance.setId("emf:section");
		caseInstance.getSections().add(sectionInstance);
		sectionInstance.getContent().add(instance);
		caseInstance.initBidirection();
		//
		Mockito.when(workflowService.getCurrentWorkflow(caseInstance)).then(
				new Answer<List<WorkflowInstanceContext>>() {

					@Override
					public List<WorkflowInstanceContext> answer(InvocationOnMock invocation)
							throws Throwable {
						return Collections.emptyList();
					}
				});
		// test without active tasks
		active = provider.getActive(instance, ObjectTypesCmf.WORKFLOW);
		Assert.assertNotNull(active);
		Assert.assertTrue(active.isEmpty());

		WorkflowInstanceContext context = new WorkflowInstanceContext();
		context.setId("emf:workflow");
		// test with active tasks
		Mockito.when(workflowService.getCurrentWorkflow(Mockito.eq(caseInstance))).thenReturn(
				Arrays.asList(context));
		active = provider.getActive(instance, ObjectTypesCmf.WORKFLOW);
		Assert.assertNotNull(active);
		Assert.assertFalse(active.isEmpty());
		Assert.assertEquals(active.size(), 1);

	}

}
