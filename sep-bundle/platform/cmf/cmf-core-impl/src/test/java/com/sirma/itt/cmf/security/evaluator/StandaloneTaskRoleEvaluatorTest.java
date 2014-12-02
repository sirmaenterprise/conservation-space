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
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
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
 * This is test class created for unit testing of StandAloneTaskEvaluator
 * @author dvladov
 *
 */
public class StandaloneTaskRoleEvaluatorTest {

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

	private TaskService taskService;

	/**
	 * This method prepares sets mocks for testing
	 */
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
		taskService = Mockito.mock(TaskService.class);
	}


		/**
	 * Testing functionality evaluateInternal
	 * case: is claimable
	 */
	@Test
	public void evaluateInternalIsClaimable() {

		RoleEvaluator<StandaloneTaskInstance> standAloneTask = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();

		taskInstance.getProperties().put(TaskProperties.CREATED_BY,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(
				SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(
				taskService.isClaimable(taskInstance, resource.getIdentifier()))
				.thenReturn(true);

		Mockito.when(
				registry.find(SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE))
				.thenReturn(role);


		Pair<Role, RoleEvaluator<StandaloneTaskInstance>> actual = standAloneTask
				.evaluate(taskInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal
	 * case: role task owner
	 */
	@Test
	public void evaluateInternalCaseTaskOwner() {
		checkRoleForUserProperty(SecurityModel.ActivitiRoles.ASSIGNEE, TaskProperties.CREATED_BY,
				TaskProperties.TASK_OWNER);
	}

	/**
	 * Check role for user property.
	 * 
	 * @param roleToCheck
	 *            the role to check
	 * @param properties
	 *            the property
	 */
	private void checkRoleForUserProperty(RoleIdentifier roleToCheck, String... properties) {
		RoleEvaluator<StandaloneTaskInstance> standAloneTask = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();

		for (String string : properties) {
			taskInstance.getProperties().put(string, resource.getIdentifier());
		}

		RoleImpl role = new RoleImpl(roleToCheck,
				Collections.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(roleToCheck)).thenReturn(role);
		Mockito.when(resourceService.areEqual(resource.getIdentifier(), resource)).thenReturn(true);

		Pair<Role, RoleEvaluator<StandaloneTaskInstance>> actual = standAloneTask.evaluate(
				taskInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: task creator
	 */
	@Test
	public void evaluateInternalCaseTaskCreator() {
		checkRoleForUserProperty(SecurityModel.BaseRoles.CREATOR, TaskProperties.CREATED_BY);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: task creator
	 */
	@Test
	public void evaluateInternalCaseTaskStartedBy() {
		checkRoleForUserProperty(SecurityModel.BaseRoles.CREATOR, TaskProperties.START_BY);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: task owner with no creator
	 */
	@Test
	public void evaluateInternalCaseTaskTaskOwnerWithNoCreator() {
		checkRoleForUserProperty(SecurityModel.ActivitiRoles.ASSIGNEE, TaskProperties.TASK_OWNER);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: is claimable without created by
	 */
	@Test
	public void evaluateInternalIsClaimableWithoutCreatedBy() {

		RoleEvaluator<StandaloneTaskInstance> standAloneTask = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();

		RoleImpl role = new RoleImpl(
				SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(
				taskService.isClaimable(taskInstance, resource.getIdentifier()))
				.thenReturn(true);

		Mockito.when(
				registry.find(SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE))
				.thenReturn(role);


		Pair<Role, RoleEvaluator<StandaloneTaskInstance>> actual = standAloneTask
				.evaluate(taskInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal
	 * case: checks is administrator or system user
	 */
	@Test
	public void evaluateInternalIsAdminOrSystemUser() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();

		Resource resource = createResource();

		StandaloneTaskInstance taskInstance = createInstance();

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.ADMINISTRATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.BaseRoles.ADMINISTRATOR))
				.thenReturn(role);
		Mockito.when(authorityService.isAdminOrSystemUser(resource))
				.thenReturn(Boolean.TRUE);
		Pair<Role, RoleEvaluator<StandaloneTaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal
	 * case: only consumer
	 */
	@Test
	public void evaluateInternalcaseConsumer() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();

		Resource resource = createResource();

		StandaloneTaskInstance taskInstance = createInstance();

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(SecurityModel.BaseRoles.CONSUMER))
				.thenReturn(role);

		Pair<Role, RoleEvaluator<StandaloneTaskInstance>> actual = taskEvaluator
				.evaluate(taskInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality filterInternal
	 * case: retain all actions
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_retainAll() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

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

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertTrue(returnedSet.contains(StandaloneTaskRoleEvaluator.CLAIM));

	}

	/**
	 * Testing functionality filterInternal
	 * case: retain all actions / case owner
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_retainAllCaseOwner() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);
		taskInstance.getProperties().put(TaskProperties.TASK_OWNER, resource.getIdentifier());

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		RoleImpl role = new RoleImpl(
				SecurityModel.BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.BaseRoles.NO_PERMISSION))
				.thenReturn(role);

	   Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertFalse(returnedSet.contains(StandaloneTaskRoleEvaluator.CLAIM));

	}

	/**
	 * Testing functionality filterInternal
	 * case: retain actions if resource owner equals owner of the task
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_CaseResourceIdentifierEqualsOwner() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		resource.getProperties().put(TaskProperties.TASK_OWNER, resource.getId());
		resource.setIdentifier("owner");
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		RoleImpl role = new RoleImpl(
				SecurityModel.BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.BaseRoles.NO_PERMISSION))
				.thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertFalse(returnedSet.contains(StandaloneTaskRoleEvaluator.CANCEL));

	}

	/**
	 * Testing functionality filterInternal
	 * case: retain actions if resource owner equals owner of the task and is releasable
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_CaseResourceIdentifierEqualsOwnerIsReleasable() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		resource.setIdentifier("owner");
		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		Mockito.when(
				taskService.isReleasable(taskInstance, resource.getIdentifier()))
				.thenReturn(Boolean.FALSE);

		RoleImpl role = new RoleImpl(
				SecurityModel.BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.BaseRoles.NO_PERMISSION))
				.thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertFalse(returnedSet.contains(StandaloneTaskRoleEvaluator.RELEASE));

	}

	/**
	 * Testing functionality filterInternal
	 * case: without resource identifier
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_NotCaseResourceIdentifier() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		RoleImpl role = new RoleImpl(
				SecurityModel.BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.BaseRoles.NO_PERMISSION))
				.thenReturn(role);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.RELEASE));
		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.START_PROGRESS));

	}

	/**
	 * Testing functionality filterInternal
	 * case: if there is no sub task
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilterInternal_CaseWithoutSubTask() {

		RoleEvaluator<StandaloneTaskInstance> taskEvaluator = createEvaluator();
		Resource resource = createResource();
		StandaloneTaskInstance taskInstance = createInstance();
		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class)))
				.thenReturn(setActions);

		RoleImpl role = new RoleImpl(
				SecurityModel.BaseRoles.NO_PERMISSION,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(
				registry.find(SecurityModel.BaseRoles.NO_PERMISSION))
				.thenReturn(role);

		Mockito.when(
				instanceService.isChildAllowed(taskInstance,
						ObjectTypesCmf.STANDALONE_TASK)).thenReturn(false);

		Set<Action> returnedSet = taskEvaluator.filterActions(taskInstance, resource, role);

		Assert.assertFalse(returnedSet.contains(TaskRoleEvaluator.SUBTASK_CREATE));

	}

	/**
	 * Creates object of type CaseInstance
	 * 
	 * @return caseInstance of type CaseInstance
	 */
	private StandaloneTaskInstance createInstance() {
		StandaloneTaskInstance taskInstance = new StandaloneTaskInstance();

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

	private RoleEvaluator<StandaloneTaskInstance> createEvaluator() {
		StandaloneTaskRoleEvaluator evaluator = new StandaloneTaskRoleEvaluator();
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
		List<Action> listOfActions = Arrays.asList(
				StandaloneTaskRoleEvaluator.REASSIGN,
				StandaloneTaskRoleEvaluator.START_PROGRESS,
				StandaloneTaskRoleEvaluator.SUBTASK_CREATE,
				StandaloneTaskRoleEvaluator.HOLD,
				StandaloneTaskRoleEvaluator.CLAIM,
				StandaloneTaskRoleEvaluator.RELEASE,
				StandaloneTaskRoleEvaluator.EDIT,
				StandaloneTaskRoleEvaluator.CANCEL);
		return listOfActions;
	}

}
