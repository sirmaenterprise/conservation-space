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

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.states.PrimaryStates;
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
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * This class implements unit testing of CaseRoleEvaluator 
 * @author dvladov
 *
 */
public class CaseRoleEvaluatorTest extends CaseRoleEvaluator {

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
	@SuppressWarnings("rawtypes")
	private BaseRoleEvaluator caseRoleEvaluatorMock;

	private TaskService taskService;

	
	/**
	 * This method prepares & sets mocks for testing
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
		caseRoleEvaluatorMock = Mockito.mock(CaseRoleEvaluator.class);
		taskService = Mockito.mock(TaskService.class);
	}

	
	/**
	 * Testing functionality evaluateInternal
	 * case: if state is deleted 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateInternal_stateDeleted() {

		RoleEvaluator caseEvaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		caseInstance.getProperties().put(DefaultProperties.STATUS,
				PrimaryStateType.DELETED);

		Mockito.when(
				stateService.isInStates(caseInstance, PrimaryStates.DELETED))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.VIEWER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.VIEWER)).thenReturn(role);

		Pair<Role, RoleEvaluator<CaseInstance>> actual = caseEvaluator
				.evaluate(caseInstance, createResource(), null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	
	/**
	 * Testing functionality evaluateInternal
	 * case: user is of type creator 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateInternal_stateCreator() {

		RoleEvaluator caseEvaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		caseInstance.getProperties().put(DefaultProperties.CREATED_BY,
				"emf:user");

		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:user")).thenReturn(
				true);

		RoleImpl role = new RoleImpl(BaseRoles.CREATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.CREATOR)).thenReturn(role);

		Pair<Role, RoleEvaluator<CaseInstance>> actual = caseEvaluator
				.evaluate(caseInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	
	/**
	 * Testing functionality evaluateInternal
	 * case: if case is assigned to collaborator 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateInternal_assignedToColloborator() {

		RoleEvaluator caseEvaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		Resource resource = createResource();
		caseInstance.getProperties()
				.put(DefaultProperties.CREATED_BY, resource);

		Mockito.when(
				taskService.hasUserTasks(caseInstance,
						resource.getIdentifier(), TaskState.IN_PROGRESS))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.COLLABORATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.COLLABORATOR)).thenReturn(role);

		Pair<Role, RoleEvaluator<CaseInstance>> actual = caseEvaluator
				.evaluate(caseInstance, createResource(), null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	
	/**
	 * Testing functionality evaluateInternal
	 * case: if case is assigned to consumer  
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateInternal_assignedToConsumer() {

		RoleEvaluator caseEvaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		Resource resource = createResource();
		caseInstance.getProperties()
				.put(DefaultProperties.CREATED_BY, resource);

		Mockito.when(
				taskService.hasUserTasks(caseInstance,
						resource.getIdentifier(), TaskState.COMPLETED))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.CONSUMER)).thenReturn(role);

		Pair<Role, RoleEvaluator<CaseInstance>> actual = caseEvaluator
				.evaluate(caseInstance, createResource(), null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal
	 * case: if case is assigned to consumer & pooled tasks 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluateInternal_assignedToConsumerPooledTask() {

		RoleEvaluator caseEvaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		Resource resource = createResource();
		caseInstance.getProperties()
				.put(DefaultProperties.CREATED_BY, resource);

		Mockito.when(
				taskService.hasUserPooledTasks(caseInstance,
						resource.getIdentifier(), TaskState.IN_PROGRESS))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.CONSUMER)).thenReturn(role);

		Pair<Role, RoleEvaluator<CaseInstance>> actual = caseEvaluator
				.evaluate(caseInstance, createResource(), null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality filterInternal
	 * case: contains work flow action
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilter_workflowAction() {
		RoleEvaluator evaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());

		Resource resource = createResource();
		Role role = createAdminRole("create", CaseInstance.class,
				CaseRoleEvaluator.START_WORKFLOW);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(CaseInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays
						.asList(CaseRoleEvaluator.START_WORKFLOW)));

		Set<Action> actions = evaluator.filterActions(caseInstance, resource,
				role);
		Assert.assertFalse(actions.contains(CaseRoleEvaluator.START_WORKFLOW));

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(CaseInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays
						.asList(CaseRoleEvaluator.START_WORKFLOW)));

		Mockito.when(
				instanceService.isChildAllowed(Mockito.any(CaseInstance.class),
						Mockito.eq(ObjectTypesCmf.WORKFLOW))).thenReturn(true);

		caseInstance.getProperties().put(DefaultProperties.STATUS, "status");

		actions = evaluator.filterActions(caseInstance, resource, role);

		Assert.assertTrue(actions
				.contains(CaseRoleEvaluator.START_WORKFLOW));
	}

	/**
	 * Testing functionality filterInternal
	 * case: contains create task action
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFilter_createTaskAction() {
		RoleEvaluator evaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());

		Resource resource = createResource();
		Role role = createAdminRole("create", CaseInstance.class,
				CaseRoleEvaluator.CREATE_TASK);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(CaseInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays
						.asList(CaseRoleEvaluator.CREATE_TASK)));

		Set<Action> actions = evaluator.filterActions(caseInstance, resource,
				role);
		Assert.assertFalse(actions.contains(CaseRoleEvaluator.CREATE_TASK));

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(CaseInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays
						.asList(CaseRoleEvaluator.CREATE_TASK)));

		Mockito.when(
				instanceService.isChildAllowed(Mockito.any(CaseInstance.class),
						Mockito.eq(ObjectTypesCmf.STANDALONE_TASK)))
				.thenReturn(true);

		caseInstance.getProperties().put(DefaultProperties.STATUS, "status");

		actions = evaluator.filterActions(caseInstance, resource, role);

		Assert.assertTrue(actions.contains(CaseRoleEvaluator.CREATE_TASK));
	}

	/**
	 * Creates object of type CaseInstance
	 * 
	 * @return caseInstance of type CaseInstance
	 */
	private CaseInstance createInstance() {
		CaseInstance caseInstance = new CaseInstance();

		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		caseInstance.getProperties().put(DefaultProperties.STATUS,
				PrimaryStates.DELETED);

		return caseInstance;
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
		CaseRoleEvaluator evaluator = new CaseRoleEvaluator();
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
	 * This method gets all of the Case actions
	 * 
	 * @return List<Actions> List of type actions
	 */
	private List<Action> getActions() {
		List<Action> listOfActions = Arrays.asList(
				CaseRoleEvaluator.CASE_CREATE, CaseRoleEvaluator.CREATE_TASK,
				CaseRoleEvaluator.START_WORKFLOW);
		return listOfActions;
	}

}
