package com.sirma.itt.seip.instance.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.PrimaryStateFactory;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * This is test class that implements unit tests for ObjectRoleEvaluator
 *
 * @author dvladov
 */
@SuppressWarnings({ "unchecked", "rawtypes", "boxing" })
public class ObjectRoleEvaluatorTest extends EmfTest {

	@InjectMocks
	private ObjectRoleEvaluator evaluator;

	@Mock
	private StateService stateService;
	@Mock
	private StateTransitionManager transitionManager;
	@Mock
	private ResourceService resourceService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private AuthorityService authorityService;
	@Mock
	private RoleRegistry registry;
	@Mock
	private PrimaryStateFactory stateFactory;
	@Mock
	private PermissionService permissionService;
	@Mock
	private RoleActionFilterService actionEvaluatorService;
	@Mock
	private InstanceVersionService instanceVersionService;

	/**
	 * This method prepares & sets mocks for testing
	 */
	@Before
	public void init() {
		super.beforeMethod();
		Mockito.when(stateService.getPrimaryState(Matchers.any(Instance.class))).then(invocation -> {
			Instance instance = (Instance) invocation.getArguments()[0];
			return (String) instance.getProperties().get(DefaultProperties.STATUS);
		});

		Mockito.when(stateFactory.create(PrimaryStates.DELETED_KEY)).thenReturn(PrimaryStates.DELETED);
		Mockito.when(stateFactory.create(PrimaryStates.COMPLETED_KEY)).thenReturn(PrimaryStates.COMPLETED);
		Mockito.when(stateFactory.create(PrimaryStates.CANCELED_KEY)).thenReturn(PrimaryStates.STOPPED);
	}

	/**
	 * Testing functionality evaluateInternal case: if state is deleted
	 */
	@Test
	public void testEvaluateInternal_stateDeleted() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		objectInstance.getProperties().put(DefaultProperties.IS_DELETED, true);

		Role role = new Role(BaseRoles.VIEWER);
		Mockito.when(registry.find(BaseRoles.VIEWER)).thenReturn(role);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator.evaluate(objectInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);
	}

	/**
	 * Testing functionality evaluateInternal case: if state is creator
	 */
	@Test
	public void testEvaluateInternal_stateCreator() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		objectInstance.add(DefaultProperties.CREATED_BY, "emf:user");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:user")).thenReturn(true);

		Role role = new Role(BaseRoles.CREATOR);
		Mockito.when(registry.find(BaseRoles.CREATOR)).thenReturn(role);

		ResourceRole mock = new ResourceRole();
		mock.setRole(role.getRoleId());
		Mockito.when(permissionService.getPermissionAssignment(objectInstance.toReference(), "emf:user")).thenReturn(
				mock);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator.evaluate(objectInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);
	}
	
	/**
	 * Unit test remove all after conditions
	 */
	@Test
	public void testFilterInternal_removeAllAfterConditions() {

		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		PrimaryStates[] states = new PrimaryStates[] { PrimaryStates.DELETED, PrimaryStates.COMPLETED,
				PrimaryStates.STOPPED };
		Instance ci = InstanceReferenceMock.createGeneric("emf:user").toInstance();
		contextService.bindContext(objectInstance, ci);
		objectInstance.setLockedBy("emf:user");

		Set<Action> actions = getActions();
		Set<Action> setActions = new HashSet<>(actions);

		when(transitionManager.getAllowedActions(any(ObjectInstance.class), isNull(String.class), any(Set.class)))
				.thenReturn(setActions);

		Role role = new Role(BaseRoles.ADMINISTRATOR);

		Mockito.when(registry.find(BaseRoles.ADMINISTRATOR)).thenReturn(role, role);

		Mockito.when(stateService.isInStates(ci, states)).thenReturn(true);

		Mockito.when(resourceService.areEqual(objectInstance.getLockedBy(), resource)).thenReturn(false);

		Set<Action> returnedActions = objectEvaluator.filterActions(objectInstance, resource, role);

		Assert.assertFalse(returnedActions.isEmpty());
		Assert.assertEquals(4, returnedActions.size());
	}

	/**
	 * Unit test remove all after conditions
	 */
	@Test
	public void testFilterInternal_InstanceIsLocked() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		InstanceReferenceMock ci = InstanceReferenceMock.createGeneric("emf:user");
		contextService.bindContext(objectInstance, ci);

		objectInstance.setLockedBy("emf:user");

		Set<Action> actions = getActions();
		Set<Action> setActions = new HashSet<>(actions);

		when(transitionManager.getAllowedActions(any(ObjectInstance.class), isNull(String.class), any(Set.class)))
				.thenReturn(setActions);

		Role role = new Role(BaseRoles.CONSUMER);

		Mockito.when(registry.find(BaseRoles.CONSUMER)).thenReturn(role, role);

		Mockito.when(resourceService.areEqual(objectInstance.getLockedBy(), resource)).thenReturn(false);

		Set<Action> returnedActions = objectEvaluator.filterActions(objectInstance, resource, role);

		Action VIEW_DETAILS = new EmfAction(ActionTypeConstants.VIEW_DETAILS);

		Assert.assertFalse(returnedActions.isEmpty());
		Assert.assertTrue(returnedActions.contains(VIEW_DETAILS));
	}

	@Test
	public void should_AddRevertActionForVersion_When_ActionIsAllowedForUserRole() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance version = createInstance();
		version.setId("object-v1.0");
		Resource resource = createResource();
		Role role = new Role(BaseRoles.CONTRIBUTOR);
		Action revertAction = new EmfAction(InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
		Set<Action> actionsSet = new HashSet<>(Arrays.asList(revertAction));

		when(instanceVersionService.isRevertOperationAllowed(version)).thenReturn(Boolean.TRUE);
		when(actionEvaluatorService.filter(anySet(), any(RoleActionEvaluatorContext.class)))
				.thenReturn(actionsSet);
		when(transitionManager.getActions(any(Serializable.class), anySet())).thenReturn(actionsSet);

		Set<Action> actions = objectEvaluator.filterActions(version, resource, role);
		assertTrue(actions.size() == 1);
		assertEquals(revertAction, actions.iterator().next());
	}

	@Test
	public void should_NotAddRevertActionForVersion_When_ActionIsNotAllowedForUserRole() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance version = createInstance();
		version.setId("object-v1.0");
		Resource resource = createResource();
		Role role = new Role(BaseRoles.CONTRIBUTOR);
		Action revertAction = new EmfAction(InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
		Set<Action> actionsSet = new HashSet<>(Arrays.asList(revertAction));

		when(instanceVersionService.isRevertOperationAllowed(version)).thenReturn(Boolean.TRUE);
		when(transitionManager.getActions(any(Serializable.class), anySet())).thenReturn(actionsSet);

		assertTrue(objectEvaluator.filterActions(version, resource, role).isEmpty());
	}

	@Test
	public void should_RemoveRevertAction_When_RevertOperationIsNotAllowed() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance version = createInstance();
		version.setId("object-v1.0");
		Resource resource = createResource();
		Role role = new Role(BaseRoles.CONTRIBUTOR);
		Action revertAction = new EmfAction(InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
		Set<Action> actionsSet = new HashSet<>(Arrays.asList(revertAction));

		when(transitionManager.getAllowedActions(any(ObjectInstance.class), isNull(String.class), any(Set.class)))
				.thenReturn(actionsSet);

		assertTrue(objectEvaluator.filterActions(version, resource, role).isEmpty());
	}

	@Test
	public void should_NotDuplicateRevertAction_When_ActionAlreadyInSet() {
		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance version = createInstance();
		version.setId("object-v1.0");
		Resource resource = createResource();
		Role role = new Role(BaseRoles.CONTRIBUTOR);
		Action revertAction = new EmfAction(InstanceVersionService.REVERT_VERSION_SERVER_OPERATION);
		Set<Action> actionsSet = new HashSet<>(Arrays.asList(revertAction));

		when(instanceVersionService.isRevertOperationAllowed(version)).thenReturn(Boolean.TRUE);
		when(transitionManager.getAllowedActions(any(ObjectInstance.class), isNull(String.class), any(Set.class)))
				.thenReturn(actionsSet);

		Set<Action> actions = objectEvaluator.filterActions(version, resource, role);
		assertTrue(actions.size() == 1);
		assertEquals(revertAction, actions.iterator().next());
	}

	/**
	 * Creates object of type ObjectInstance
	 *
	 * @return objectInstance of type ObjectInstance
	 */
	private static ObjectInstance createInstance() {
		ObjectInstance objectInstance = new ObjectInstance();

		objectInstance.setId("object");
		objectInstance.setProperties(new HashMap<String, Serializable>());
		ReflectionUtils.setFieldValue(objectInstance, "reference",
				new InstanceReferenceMock("object", mock(DataTypeDefinition.class), objectInstance));

		return objectInstance;
	}

	/**
	 * Creates the resource.
	 *
	 * @return the resource
	 */
	private static Resource createResource() {
		Resource resource = new EmfUser();
		resource.setId("emf:user");
		resource.setName("user");
		return resource;
	}

	/**
	 * Creates the evaluator.
	 *
	 * @return the document role evaluator
	 */
	private RoleEvaluator createEvaluator() {
		evaluator.init();
		return evaluator;
	}

	/**
	 * This method gets all of the Task actions
	 *
	 * @return List<Actions> List of type actions
	 */
	private static Set<Action> getActions() {
		Set<Action> listOfActions = new LinkedHashSet<>(
				Arrays.asList(ObjectRoleEvaluator.EDIT_DETAILS, ObjectRoleEvaluator.DELETE,
						ObjectRoleEvaluator.CLONE, ObjectRoleEvaluator.PRINT, ObjectRoleEvaluator.EXPORT,
						new EmfAction(ActionTypeConstants.VIEW_DETAILS)));
		return listOfActions;
	}

}
