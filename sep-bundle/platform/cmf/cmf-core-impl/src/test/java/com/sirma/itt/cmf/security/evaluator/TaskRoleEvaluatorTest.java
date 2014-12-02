package com.sirma.itt.cmf.security.evaluator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorManagerService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfPermission;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * This class implements unit tests for task role evaluation
 * 
 * @author dvladov
 */
@Test
public class TaskRoleEvaluatorTest extends TaskRoleEvaluator {

	/** The state service. */
	private StateService stateService;

	/** The transition manager. */
	private StateTransitionManager transitionManager;

	/** The resource service. */
	private ResourceService resourceService;

	/** The instance service. */
	@SuppressWarnings("rawtypes")
	private InstanceService instanceService;

	/** The authority service. */
	private AuthorityService authorityService;

	/** The role evaluator manager service. */
	private RoleEvaluatorManagerService roleEvaluatorManagerService;

	/** The role registry. */
	private RoleRegistry registry;

	/** BaseRoleEvaluator mock object */
	private BaseRoleEvaluator taskRoleEvaluatorMock;

	private TaskService taskService;

	/**
	 * This method is preparation for the unit tests
	 */
	@Override
	@BeforeMethod
	public void init() {
		stateService = Mockito.mock(StateService.class);
		Mockito.when(stateService.getPrimaryState(Mockito.any(Instance.class)))
				.then(new Answer<String>() {

					@Override
					public String answer(InvocationOnMock invocation)
							throws Throwable {
						Instance instance = (Instance) invocation
								.getArguments()[0];
						return (String) instance.getProperties().get(
								DefaultProperties.STATUS);
					}
				});
		transitionManager = Mockito.mock(StateTransitionManager.class);
		resourceService = Mockito.mock(ResourceService.class);
		instanceService = Mockito.mock(InstanceService.class);
		authorityService = Mockito.mock(AuthorityService.class);
		roleEvaluatorManagerService = Mockito
				.mock(RoleEvaluatorManagerService.class);
		registry = Mockito.mock(RoleRegistry.class);
		taskRoleEvaluatorMock = Mockito.mock(BaseRoleEvaluator.class);
		taskService = Mockito.mock(TaskService.class);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: self owned document
	 */
	public void testEvaluateInternal_stateSelfOwned() {

		RoleEvaluator taskEvaluator = createEvaluator();

		Resource resource = createResource();

		TaskInstance taskInstance = createInstance();
		taskInstance.getProperties().put(TaskProperties.TASK_OWNER,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(SecurityModel.ActivitiRoles.ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.ActivitiRoles.ASSIGNEE))
				.thenReturn(role);
		Mockito.when(
				resourceService.areEqual(resource, taskInstance.getProperties()
						.get(TaskProperties.TASK_OWNER))).thenReturn(true);
		Pair<Role, RoleEvaluator<TaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, createResource(), null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: creator or assignee involved
	 */
	public void testEvaluateInternal_stateCreatorOrAssigneeInvolved() {

		RoleEvaluator taskEvaluator = createEvaluator();

		Resource resource = createResource();

		TaskInstance taskInstance = createInstance();
		taskInstance.getProperties().put(WorkflowProperties.CREATED_BY,
				resource);

		RoleImpl role = new RoleImpl(
				SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE))
				.thenReturn(role);
		Mockito.when(
				resourceService.areEqual(resource, taskInstance.getProperties()
						.get(WorkflowProperties.CREATED_BY))).thenReturn(true);
		Mockito.when(
				taskService.isClaimable(taskInstance, resource.getIdentifier()))
				.thenReturn(true);
		Pair<Role, RoleEvaluator<TaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, createResource(), null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: is releasable
	 */
	@SuppressWarnings("unchecked")
	public void testEvaluateInternal_stateIsReleasable() {

		RoleEvaluator taskEvaluator = createEvaluator();

		Resource resource = createResource();

		TaskInstance taskInstance = createInstance();
		taskInstance.getProperties().put(WorkflowProperties.CREATED_BY,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(SecurityModel.ActivitiRoles.ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.ActivitiRoles.ASSIGNEE))
				.thenReturn(role);
		Mockito.when(
				taskService.isReleasable(taskInstance, resource.getIdentifier()))
				.thenReturn(Boolean.TRUE);
		Pair<Role, RoleEvaluator<TaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, createResource(), null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: is admin or system user
	 */
	@SuppressWarnings("unchecked")
	public void testEvaluateInternal_stateIsAdminOrSystemUser() {

		RoleEvaluator taskEvaluator = createEvaluator();

		Resource resource = createResource();

		TaskInstance taskInstance = createInstance();
		taskInstance.getProperties().put(WorkflowProperties.CREATED_BY,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.ADMINISTRATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.BaseRoles.ADMINISTRATOR))
				.thenReturn(role);
		Mockito.when(authorityService.isAdminOrSystemUser(resource))
				.thenReturn(Boolean.TRUE);
		Pair<Role, RoleEvaluator<TaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, createResource(), null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: is customer
	 */
	@SuppressWarnings("unchecked")
	public void testEvaluateInternal_stateCustomer() {

		RoleEvaluator taskEvaluator = createEvaluator();

		Resource resource = createResource();

		TaskInstance taskInstance = createInstance();
		taskInstance.getProperties().put(WorkflowProperties.CREATED_BY,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.BaseRoles.CONSUMER))
				.thenReturn(role);
		Pair<Role, RoleEvaluator<TaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, createResource(), null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality filterInternal
	 * case: retain all actions
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_retainAll() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		// resource.getProperties().put(TaskProperties.TASK_OWNER,
		// resource.getIdentifier());
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		RoleImpl role = new RoleImpl(
				SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE))
				.thenReturn(role);

		setActions = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertTrue(setActions.contains(TaskRoleEvaluator.CLAIM));

	}


		/**
	 * Testing functionality filterInternal
	 * case: retain all actions for type owner
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_retainAllCaseOwner() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		resource.setIdentifier("owner");
		resource.getProperties().put(TaskProperties.TASK_OWNER, resource);
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(TaskInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		RoleImpl role = new RoleImpl(BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.NO_PERMISSION)).thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance,
				resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.CANCEL));

	}


	/**
	 * Testing functionality filterInternal
	 * case: retain all actions if not releasable
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_retainAllNotReleasable() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		resource.setIdentifier("owner");
		resource.getProperties().put(TaskProperties.TASK_OWNER, resource);
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(TaskInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);
		Mockito.when(
				taskService.isReleasable(taskInstance, resource.getIdentifier()))
				.thenReturn(Boolean.FALSE);

		RoleImpl role = new RoleImpl(BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.NO_PERMISSION)).thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance,
				resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.RELEASE));

	}


	/**
	 * Testing functionality filterInternal
	 * case: retain all actions / without start release
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_retainAllWithoutStartRelease() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(TaskInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		RoleImpl role = new RoleImpl(BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.NO_PERMISSION)).thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance,
				resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.RELEASE));
		Assert.assertFalse(returnedSet
				.contains(TaskRoleEvaluator.START_PROGRESS));

	}


	/**
	 * Testing functionality filterInternal
	 * case: retain all actions / create sub task
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_createSubTask() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(TaskInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		RoleImpl role = new RoleImpl(BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.NO_PERMISSION)).thenReturn(role);

		Mockito.when(
				instanceService.isChildAllowed(taskInstance,
						ObjectTypesCmf.STANDALONE_TASK)).thenReturn(false);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance,
				resource, role);

		Assert.assertFalse(returnedSet
				.contains(TaskRoleEvaluator.SUBTASK_CREATE));

	}

	/**
	 * Testing functionality filterInternal
	 * case: retain all actions / without claim
	 */
	@SuppressWarnings("unchecked")
	public void testFilterInternal_retainAllWithoutClaim() {

		RoleEvaluator taskEvaluator = createEvaluator();
		Resource resource = createResource();
		TaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		taskInstance.getProperties().put(TaskProperties.TASK_OWNER,
				resource.getId());
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(TaskInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		RoleImpl role = new RoleImpl(BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.NO_PERMISSION)).thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance,
				resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.CLAIM));

	}

	/**
	 * Creates object of type CaseInstance
	 * 
	 * @return caseInstance of type CaseInstance
	 */
	private TaskInstance createInstance() {
		TaskInstance taskInstance = new TaskInstance();

		taskInstance.setId("task");
		taskInstance.setProperties(new HashMap<String, Serializable>());

		return taskInstance;
	}

	/**
	 * Creates the resource.
	 *
	 * @return the resource
	 */
	private Resource createResource() {
		Resource resource = new EmfUser();
		resource.setId("emf:user");
		resource.setIdentifier("user");
		return resource;
	}

	/**
	 * Creates the role.
	 *
	 * @param roleId
	 *            the role id
	 * @param permission
	 *            the permission
	 * @param target
	 *            the target
	 * @param actions
	 *            the actions
	 * @return the role
	 */
	private Role createRole(RoleIdentifier roleId, String permission,
			Class<?> target, Action... actions) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		EmfPermission key = new EmfPermission(permission);
		for (Action action : actions) {
			CollectionUtils.addValueToMap(permissions, key,
					new Pair<Class<?>, Action>(target, action));
		}
		Role role = new RoleImpl(roleId, permissions);
		return role;
	}

	/**
	 * Creates the role.
	 *
	 * @param permission
	 *            the permission
	 * @param target
	 *            the target
	 * @param actions
	 *            the actions
	 * @return the role
	 */
	private Role createAdminRole(String permission, Class<?> target,
			Action... actions) {
		return createRole(BaseRoles.ADMINISTRATOR, permission, target, actions);
	}

	/**
	 * Creates the evaluator.
	 *
	 * @return the document role evaluator
	 */

	private RoleEvaluator createEvaluator() {
		TaskRoleEvaluator evaluator = new TaskRoleEvaluator();
		ReflectionUtils.setField(evaluator, "stateService", stateService);
		ReflectionUtils.setField(evaluator, "transitionManager",
				transitionManager);
		ReflectionUtils.setField(evaluator, "resourceService", resourceService);
		ReflectionUtils.setField(evaluator, "instanceService", instanceService);
		ReflectionUtils.setField(evaluator, "authorityService",
				authorityService);
		ReflectionUtils.setField(evaluator, "registry", registry);
		ReflectionUtils.setField(evaluator, "roleEvaluatorManagerService",
				new InstanceProxyMock<RoleEvaluatorManagerService>(
						roleEvaluatorManagerService));
		ReflectionUtils.setField(evaluator, "taskService", taskService);
		return evaluator;
	}

	/**
	 * This method gets all of the Task actions
	 * 
	 * @return List<Actions> List of type actions
	 */
	private List<Action> getActions() {
		List<Action> listOfActions = Arrays.asList(TaskRoleEvaluator.REASSIGN,
				TaskRoleEvaluator.START_PROGRESS,
				TaskRoleEvaluator.SUBTASK_CREATE, TaskRoleEvaluator.HOLD,
				TaskRoleEvaluator.CLAIM, TaskRoleEvaluator.RELEASE,
				TaskRoleEvaluator.EDIT, TaskRoleEvaluator.CANCEL);
		return listOfActions;
	}

}
