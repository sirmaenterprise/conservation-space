package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.observers.TaskOperationsObserver;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * The TaskServiceCITest is CI test for task services methods.
 */
public class TaskServiceCITest extends BaseArquillianCITest {

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The case service. */
	@Inject
	private CaseService caseService;

	/** The standalone task service. */
	@Inject
	private StandaloneTaskService standaloneTaskService;

	/** The owning instance. */
	private static CaseInstance owningInstance;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + TaskServiceCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).addClasess(
				TaskOperationsObserver.class).packageWar();
	}

	/**
	 * Creates the test case for testing workflow.
	 *
	 * @return single instance
	 */
	private CaseInstance getTestableCase() {
		if (owningInstance == null) {
			owningInstance = createTestableCase();
		}
		return owningInstance;
	}

	/**
	 * Test get owned task instances.
	 */
	@Test(enabled = true)
	// , dependsOnGroups = "initialization")
	public void testGetOwnedTaskInstances() {
		int taskStActive = 0;
		int taskStCompleted = 0;

		int taskWfActive = 0;
		int taskWfCompleted = 0;

		// prepare owning instance
		CaseDefinition definition = getDefinition(CaseDefinition.class, DEFAULT_DEFINITION_ID_CASE);
		CaseInstance owning = caseService.createInstance(definition, null);
		Map<String, Serializable> workflowProperties = new HashMap<>(2);
		workflowProperties.put(TaskProperties.TASK_ASSIGNEE,
				authenticationService.getCurrentUserId());
		caseService.save(owning, new Operation(ActionTypeConstants.CREATE_CASE));
		WorkflowInstanceContext startedWorkflow = startWorkflow(owning,
				DEFAULT_DEFINITION_ID_WORKFLOW, workflowProperties);
		taskWfActive++;
		// start task is completed
		taskWfCompleted++;

		// one more wf
		WorkflowInstanceContext workflowSecond = startWorkflow(owning,
				DEFAULT_DEFINITION_ID_WORKFLOW, workflowProperties);
		taskWfActive++;
		taskWfCompleted++;

		assertState(owning, taskWfActive, taskWfCompleted, TaskType.WORKFLOW_TASK);
		assertState(owning, taskStActive, taskStCompleted, TaskType.STANDALONE_TASK);
		Map<String, Serializable> taskRprops = new HashMap<>();
		taskRprops.put(TaskProperties.TASK_ASSIGNEE, authenticationService.getCurrentUserId());
		StandaloneTaskInstance standaloneTask = startStandaloneTask(owning,
				DEFAULT_DEFINITION_ID_TASK_STANDALONE, taskRprops);
		assertActiveState(standaloneTask, Boolean.TRUE);
		taskStActive++;
		assertState(owning, taskWfActive, taskWfCompleted, TaskType.WORKFLOW_TASK);
		assertState(owning, taskStActive, taskStCompleted, TaskType.STANDALONE_TASK);

		standaloneTask = startStandaloneTask(owning, DEFAULT_DEFINITION_ID_TASK_STANDALONE,
				taskRprops);
		assertActiveState(standaloneTask, Boolean.TRUE);
		taskStActive++;
		assertState(owning, taskWfActive, taskWfCompleted, TaskType.WORKFLOW_TASK);
		assertState(owning, taskStActive, taskStCompleted, TaskType.STANDALONE_TASK);

		// now complete task
		standaloneTaskService.complete(standaloneTask, new Operation(ActionTypeConstants.STOP));
		assertActiveState(standaloneTask, Boolean.FALSE);
		taskStActive--;
		taskStCompleted++;
		assertState(owning, taskStActive, taskStCompleted, TaskType.STANDALONE_TASK);
		List<TaskInstance> workflowTasks = workflowService.getWorkflowTasks(startedWorkflow,
				TaskState.IN_PROGRESS);
		assertTrue(workflowTasks.size() > 0, "Should have active tasks!");
		for (TaskInstance taskInstance : workflowTasks) {
			assertActiveState(taskInstance, Boolean.TRUE);
		}

		// stop the wf
		workflowService.cancel(startedWorkflow);
		workflowTasks = workflowService.getWorkflowTasks(startedWorkflow, TaskState.COMPLETED);
		assertTrue(workflowTasks.size() > 0, "Should have completed tasks!");
		for (TaskInstance taskInstance : workflowTasks) {
			assertActiveState(taskInstance, Boolean.FALSE);
		}
		// with 1 active task
		taskWfActive--;
		taskWfCompleted++;
		assertState(owning, taskWfActive, taskWfCompleted, TaskType.WORKFLOW_TASK);
		assertState(owning, taskStActive, taskStCompleted, TaskType.STANDALONE_TASK);
		workflowService.delete(workflowSecond, new Operation(ActionTypeConstants.DELETE), true);
		workflowTasks = workflowService.getWorkflowTasks(workflowSecond, TaskState.COMPLETED);
		assertTrue(workflowTasks.size() > 0, "Should have completed tasks!");
		for (TaskInstance taskInstance : workflowTasks) {
			assertActiveState(taskInstance, Boolean.FALSE);
		}
		// with 1 active task
		taskWfActive--;
		taskWfCompleted++;
		assertState(owning, taskWfActive, taskWfCompleted, TaskType.WORKFLOW_TASK);

		assertState(owning, taskStActive + taskWfActive, taskStCompleted + taskWfCompleted, null);
	}

	/**
	 * Tests the multiinstance task creation and transitions. Depends on definition test to
	 * initialize newest definitions
	 */

	@Test(enabled = true)
	// , dependsOnGroups = "initialization")
	public void testMultiInstanceTasksWorkflow() {
		WorkflowDefinition wfDefinition = getDefinition(WorkflowDefinition.class,
				"activiti$WFTYPE9M1");
		CaseInstance testableCase = getTestableCase();
		final WorkflowInstanceContext wfInstance = workflowService.createInstance(wfDefinition,
				testableCase);
		// start wf
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(wfDefinition);
		TaskInstance startupTask = (TaskInstance) taskService.createInstance(startTaskDefinition,
				wfInstance);
		Set<String> multiInstances = new HashSet<String>();
		String currentUserId = authenticationService.getCurrentUserId();
		multiInstances.add("bbanchev");
		multiInstances.add("admin");
		multiInstances.add("TEST2");
		multiInstances.add(currentUserId);
		startupTask.getProperties().put(TaskProperties.TASK_ASSIGNEES,
				new ArrayList<String>(multiInstances));
		List<TaskInstance> startedWorkflow = workflowService.startWorkflow(wfInstance, startupTask);

		// first task checks
		assertEquals(startedWorkflow.size(), multiInstances.size(), multiInstances.size()
				+ " active task");
		TaskInstance lastTask = startedWorkflow.get(0);
		assertEquals(lastTask.getIdentifier(), "TSTYPE9M12",
				"First task after start should be: TSTYPE9M12");
		// reject
		final List<TaskInstance> updateWorkflow = workflowService.updateWorkflow(wfInstance,
				startedWorkflow.get(0), "RT0098");
		assertEquals(updateWorkflow.size(), 1, "1 active task");
		lastTask = updateWorkflow.get(0);
		Assert.assertNotEquals(lastTask.getIdentifier(), "TSTYPE9M12",
				"Next task should not be: TSTYPE9M12");
		for (TaskInstance taskInstance : startedWorkflow) {
			AbstractTaskInstance persisted = taskService.load(taskInstance.getTaskInstanceId());
			taskService.refresh(persisted);
			assertEquals(persisted.getState(), TaskState.COMPLETED, "Task must be completed");
			// assertEquals(persisted.getProperties().get(TaskProperties.STATUS),
			// PrimaryStates.COMPLETED.getType(), "Task must be completed");
		}
	}

	/**
	 * Assert state for owned tasks.
	 *
	 * @param owning
	 *            the owning
	 * @param activeCount
	 *            the active count
	 * @param inactiveCount
	 *            the inactive count
	 * @param type
	 *            the type
	 */
	private void assertState(Instance owning, int activeCount, int inactiveCount, TaskType type) {
		List<String> ownedTaskInstances;
		ownedTaskInstances = taskService.getOwnedTaskInstances(owning, TaskState.IN_PROGRESS, type);
		StringBuilder stringBuilder = new StringBuilder();

		assertEquals(ownedTaskInstances.size(), activeCount, stringBuilder.append("Should have ")
				.append(activeCount).append(" ").append(type).append(" started").toString());
		stringBuilder = new StringBuilder();
		// start on the instance a task
		ownedTaskInstances = taskService.getOwnedTaskInstances(owning, TaskState.COMPLETED, type);
		assertEquals(ownedTaskInstances.size(), inactiveCount, stringBuilder.append("Should have ")
				.append(inactiveCount).append(" ").append(type).append(" completed").toString());
	}

	/**
	 * Assert active state contained as property in each task - it is mandatory field
	 *
	 * @param task
	 *            the task to check
	 * @param isActive
	 *            the state to assert
	 */
	private void assertActiveState(AbstractTaskInstance task, Boolean isActive) {
		assertEquals(task.getProperties().get(TaskProperties.TASK_ACTIVE_STATE), isActive,
				"Should be: " + isActive);
	}

	/**
	 * Test pooled workflows processing.
	 */
	@Test(enabled = true)
	// , dependsOnGroups = "initialization")
	public void testPooledWorkflows() {
		CaseInstance createTestableCase = createTestableCase();
		String currentUserId = authenticationService.getCurrentUserId();
		startWorkflow995(createTestableCase);
		boolean hasUserPooledTasks = getTaskService().hasUserPooledTasks(createTestableCase,
				currentUserId, TaskState.COMPLETED);
		assertTrue(hasUserPooledTasks, "Should have completed pool tasks!");

		hasUserPooledTasks = getTaskService().hasUserPooledTasks(createTestableCase, currentUserId,
				TaskState.IN_PROGRESS);
		assertTrue(!hasUserPooledTasks, "Should not have in progress pool tasks!");
		List<String> ownedTaskInstances = getTaskService().getOwnedTaskInstances(
				createTestableCase, TaskState.IN_PROGRESS, TaskType.WORKFLOW_TASK);
		ownedTaskInstances = getTaskService().getOwnedTaskInstances(createTestableCase,
				TaskState.COMPLETED, TaskType.WORKFLOW_TASK);
		assertEquals(ownedTaskInstances.size(), 5, "5 Complted tasks");

	}

	/**
	 * Creates the testable case to start task/wf on.
	 *
	 * @return the case instance
	 */
	private CaseInstance createTestableCase() {
		Map<String, Serializable> caseProperties = new HashMap<>();
		caseProperties.put(DefaultProperties.TITLE, "task holder");
		caseProperties.put(DefaultProperties.DESCRIPTION, "created for holder of tasks");
		CaseInstance createCase = createCase(null, DEFAULT_DEFINITION_ID_CASE, caseProperties);
		return createCase;
	}

	/**
	 * Start and iterate over known workflow (995).
	 *
	 * @param owningInstance
	 *            is the instance wf is attached to
	 * @return the workflow created/completed
	 */
	private WorkflowInstanceContext startWorkflow995(Instance owningInstance) {
		WorkflowDefinition wfDefinition = dictionaryService.getDefinition(WorkflowDefinition.class,
				"activiti$WFTYPE995");
		WorkflowInstanceContext wfInstance = workflowService.createInstance(wfDefinition,
				owningInstance);
		// start wf
		TaskDefinitionRef startTaskDefinition = WorkflowHelper.getStartTask(wfDefinition);
		TaskInstance startupTask = (TaskInstance) taskService.createInstance(startTaskDefinition,
				wfInstance);
		Set<String> poolAssignees = new HashSet<String>();
		String currentUserId = authenticationService.getCurrentUserId();
		poolAssignees.add("bbanchev");
		poolAssignees.add("admin");
		poolAssignees.add("TEST2");
		poolAssignees.add(currentUserId);
		poolAssignees.add("GROUP_Consumers");
		startupTask.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));
		// ###################### TRANSITION ##############
		List<TaskInstance> startedWorkflow = workflowService.startWorkflow(wfInstance, startupTask);

		// first task checks
		assertEquals(startedWorkflow.size(), 1, "1 active task");
		TaskInstance lastTask = startedWorkflow.get(0);
		assertEquals(lastTask.getIdentifier(), "TSPOOL01",
				"First task after start should be: TSPOOL01");

		Pair<Set<String>, Set<String>> poolResources = getTaskService().getPoolResources(lastTask);
		Assert.assertNotNull(poolResources, "Should contain pool resources");
		assertEquals(poolResources.getFirst().size(), 1, "Should contain group assignment");
		assertTrue(poolResources.getSecond().size() >= 3, "Should contain user assignment");
		// claim and continue
		claimTask(lastTask, currentUserId);

		// update pool with next assignees
		poolAssignees = new HashSet<String>();
		poolAssignees.add(currentUserId);
		lastTask.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));
		// ###################### TRANSITION ##############
		lastTask = transitionStandardPoolTask(wfInstance, lastTask, "RT0097", "TSPOOL02");

		// claim and continue
		claimTask(lastTask, currentUserId);

		lastTask.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));

		// ###################### TRANSITION ##############
		lastTask = transitionStandardPoolTask(wfInstance, lastTask, "RT0095", "TSPOOL01");

		claimTask(lastTask, currentUserId);
		// set next assignees
		lastTask.getProperties().put(TaskProperties.TASK_MULTI_ASSIGNEES,
				new ArrayList<String>(poolAssignees));
		lastTask = transitionStandardPoolTask(wfInstance, lastTask, "RT0097", "TSPOOL02");

		claimTask(lastTask, currentUserId);

		// ###################### TRANSITION END ##############
		lastTask = transitionStandardPoolTask(wfInstance, lastTask, "RT0096", null);

		// DO some final checks

		return wfInstance;
	}

	// public void onTaskCreated(
	// @Observes(during = TransactionPhase.AFTER_SUCCESS) AfterTaskPersistEvent event) {
	// TaskInstance instance = event.getInstance();
	// System.out.println("TaskServiceCITest.onTaskCreated() " + instance.getIdentifier());
	// }

	/**
	 * transition standard pool task to the next tasks.
	 *
	 * @param wfInstance
	 *            the wf instance is workflow
	 * @param lastTask
	 *            the last task is the current task
	 * @param transition
	 *            the transition is the transition to take in activiti
	 * @param expectedNext
	 *            the expected next is the next task type. if null wf is expected to be completed
	 * @return the task instance that is created after transition or null if wf is completed
	 */
	private TaskInstance transitionStandardPoolTask(WorkflowInstanceContext wfInstance,
			TaskInstance lastTask, String transition, String expectedNext) {
		List<TaskInstance> updateWorkflow = workflowService.updateWorkflow(wfInstance, lastTask,
				transition);

		if (expectedNext != null) {
			Pair<Set<String>, Set<String>> poolResources;
			// second task checks
			assertEquals(updateWorkflow.size(), 1, "1 active task");
			lastTask = updateWorkflow.get(0);
			assertEquals(lastTask.getIdentifier(), expectedNext, "Task after tranistion '"
					+ transition + "' should be: " + expectedNext);

			poolResources = getTaskService().getPoolResources(lastTask);
			// TODO check pool resources more sophisticated
			Assert.assertNotNull(poolResources, "Should contain pool resources");
			// Assert.assertNotNull(poolResources, "Should contain pool resources");
			// assertEquals(poolResources.getFirst().size(), 0,
			// "Should not contain group assignment");
			// assertEquals(poolResources.getSecond().size(), 1, "Should contain user assignment");
			return lastTask;
		} else {
			// completed
			assertEquals(updateWorkflow.size(), 0, "0 active task after complete!");
		}
		return null;
	}

	/**
	 * Mock claiming task.
	 *
	 * @param taskInstance
	 *            the task instance
	 * @param userId
	 *            the user id to claim task
	 * @return the task instance
	 */
	private TaskInstance claimTask(TaskInstance taskInstance, String userId) {
		Collection<String> poolResources = getTaskService().getPoolUsers(taskInstance);
		if (poolResources.contains(userId)) {
			taskInstance.getProperties().put(TaskProperties.TASK_OWNER, userId);
			taskInstance.getProperties().put(TaskProperties.TASK_ASSIGNEE, userId);
			getWorkflowService().updateTaskInstance(taskInstance);
			return taskInstance;
		}
		fail("User is not part of pool users");
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected WorkflowService getWorkflowService() {
		return workflowService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskService getTaskService() {
		return taskService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StandaloneTaskService getStandaloneTaskService() {
		return standaloneTaskService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CaseService getCaseService() {
		return caseService;
	}
}
