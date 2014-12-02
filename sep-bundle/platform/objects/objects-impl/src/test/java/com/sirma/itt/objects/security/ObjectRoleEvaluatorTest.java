package com.sirma.itt.objects.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.model.ViewInstance;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorManagerService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleImpl;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.state.PrimaryStateFactory;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * This is test class that implements unit tests for ObjectRoleEvaluator
 * 
 * @author dvladov
 */
@Test
public class ObjectRoleEvaluatorTest extends CmfTest {

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
	@SuppressWarnings("unused")
	private RoleEvaluatorManagerService roleEvaluatorManagerService;

	/** The role registry. */
	private RoleRegistry registry;

	/** BaseRoleEvaluator mock object */
	@SuppressWarnings({ "rawtypes", "unused" })
	private BaseRoleEvaluator objectRoleEvaluatorMock;

	/** Primary state factory mock */
	private PrimaryStateFactory stateFactory;

	/** Type converter mock*/
	private TypeConverter typeConverter;

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
		objectRoleEvaluatorMock = Mockito.mock(ObjectRoleEvaluator.class);
		typeConverter = Mockito.mock(TypeConverter.class);
		stateFactory = Mockito.mock(PrimaryStateFactory.class);

		Mockito.when(stateFactory.create(PrimaryStateType.DELETED)).thenReturn(
				PrimaryStates.DELETED);
		Mockito.when(stateFactory.create(PrimaryStateType.COMPLETED))
				.thenReturn(PrimaryStates.COMPLETED);
		Mockito.when(stateFactory.create(PrimaryStateType.CANCELED))
				.thenReturn(PrimaryStates.CANCELED);

	}

	/**
	 * Testing functionality evaluateInternal case: if state is deleted
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testEvaluateInternal_stateDeleted() {

		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		objectInstance.getProperties().put(DefaultProperties.STATUS,
				PrimaryStateType.DELETED);

		Mockito.when(
				stateService.isInStates(objectInstance, PrimaryStates.DELETED))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.VIEWER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.VIEWER)).thenReturn(role);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator
				.evaluate(objectInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal case: if state is creator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testEvaluateInternal_stateCreator() {

		RoleEvaluator objectEvaluator = createEvaluator();

		ObjectInstance objectInstance = createInstance();

		objectInstance.getProperties().put(DefaultProperties.CREATED_BY,
				"emf:user");

		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:user")).thenReturn(
				true);

		RoleImpl role = new RoleImpl(BaseRoles.CREATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(registry.find(BaseRoles.CREATOR)).thenReturn(role);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator
				.evaluate(objectInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal case: if state is deleted
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testEvaluateInternal_stateDeletedContext() {

		RoleEvaluator objectEvaluator = createEvaluator();

		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();
		objectInstance.getProperties().put(DefaultProperties.STATUS,
				PrimaryStateType.DELETED);

		Mockito.when(
				stateService.isInStates(objectInstance, PrimaryStates.DELETED))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.VIEWER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(BaseRoles.VIEWER)).thenReturn(role);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator
				.evaluate(objectInstance, resource, null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal case: if state is in states
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testEvaluateInternal_stateIsInStates() {

		RoleEvaluator objectEvaluator = createEvaluator();

		ObjectInstance objectInstance = createInstance();

		objectInstance.getProperties().put(DefaultProperties.STATUS,
				PrimaryStateType.DELETED);
		PrimaryStates[] states = new PrimaryStates[] { PrimaryStates.DELETED,
				PrimaryStates.COMPLETED, PrimaryStates.CANCELED };

		Mockito.when(stateService.isInStates(objectInstance, states))
				.thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(BaseRoles.CONSUMER)).thenReturn(role);

		Pair<Role, RoleEvaluator<ObjectInstance>> actual = objectEvaluator
				.evaluate(objectInstance, createResource(), null);

		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Unit test remove all after conditions
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilterInternal_removeAllAfterConditions() {

		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		PrimaryStates[] states = new PrimaryStates[] { PrimaryStates.DELETED,
				PrimaryStates.COMPLETED, PrimaryStates.CANCELED };
		CaseInstance ci = new CaseInstance();
		ci.setId("emf:user");
		objectInstance.setOwningInstance(ci);

		ViewInstance vi = new ViewInstance();
		vi.setLockedBy("emf:user");

		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(ObjectInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		Mockito.when(typeConverter.convert(ViewInstance.class, objectInstance))
				.thenReturn(vi);
		RoleImpl role = new RoleImpl(BaseRoles.ADMINISTRATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(BaseRoles.ADMINISTRATOR)).thenReturn(role);

		Mockito.when(stateService.isInStates(ci, states)).thenReturn(true);

		Mockito.when(resourceService.areEqual(vi.getLockedBy(), resource))
				.thenReturn(false);

		Set<Action> returnedActions = objectEvaluator.filterActions(
				objectInstance, resource, role);

		Assert.assertFalse(returnedActions.isEmpty());
		Assert.assertEquals(returnedActions.size(), 3);

	}

	/**
	 * Unit test remove unlock and move_to.....
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilterInternal_removeUnlockAndMoveToOtherSectionAction() {

		RoleEvaluator objectEvaluator = createEvaluator();
		ObjectInstance objectInstance = createInstance();
		Resource resource = createResource();

		PrimaryStates[] states = new PrimaryStates[] { PrimaryStates.DELETED,
				PrimaryStates.COMPLETED, PrimaryStates.CANCELED };
		CaseInstance ci = new CaseInstance();
		ci.setId("emf:user");
		objectInstance.setOwningInstance(ci);

		ViewInstance vi = new ViewInstance();

		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(ObjectInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		Mockito.when(typeConverter.convert(ViewInstance.class, objectInstance))
				.thenReturn(vi);
		RoleImpl role = new RoleImpl(BaseRoles.ADMINISTRATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(BaseRoles.ADMINISTRATOR)).thenReturn(role);

		Mockito.when(stateService.isInStates(ci, states)).thenReturn(true);

		Mockito.when(resourceService.areEqual(vi.getLockedBy(), resource))
				.thenReturn(false);

		Set<Action> returnedActions = objectEvaluator.filterActions(
				objectInstance, resource, role);

		Assert.assertFalse(returnedActions.isEmpty());
		Assert.assertEquals(returnedActions.size(), 3);

	}

	/**
	 * Unit test for case remove move_to_other_section
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFilterInternal_withoutMoveToOtherSection() {

		RoleEvaluator objectEvaluator = createEvaluator();
		Resource resource = createResource();
		ObjectInstance objectInstance = createInstance();

		List<Action> actions = getActions();
		Set<Action> setActions = new HashSet<Action>(actions);

		RoleImpl role = new RoleImpl(
				SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(
				transitionManager.getAllowedActions(
						Mockito.any(ObjectInstance.class), Mockito.anyString(),
						Mockito.any(Set.class))).thenReturn(setActions);

		Mockito.when(
				registry.find(SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE))
				.thenReturn(role);

		Set<Action> returnedActions = objectEvaluator.filterActions(
				objectInstance, resource, role);

		Assert.assertFalse(returnedActions
				.contains(ObjectRoleEvaluator.MOVE_TO_OTHER_SECTION));

	}

	/**
	 * Creates object of type ObjectInstance
	 * 
	 * @return objectInstance of type ObjectInstance
	 */
	private ObjectInstance createInstance() {
		ObjectInstance objectInstance = new ObjectInstance();

		objectInstance.setId("object");
		objectInstance.setProperties(new HashMap<String, Serializable>());

		return objectInstance;
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
	 * Creates the evaluator.
	 *
	 * @return the document role evaluator
	 */
	@SuppressWarnings("rawtypes")
	private RoleEvaluator createEvaluator() {
		ObjectRoleEvaluator evaluator = new ObjectRoleEvaluator();
		ReflectionUtils.setField(evaluator, "stateService", stateService);
		ReflectionUtils.setField(evaluator, "transitionManager",
				transitionManager);
		ReflectionUtils.setField(evaluator, "resourceService", resourceService);
		ReflectionUtils.setField(evaluator, "instanceService", instanceService);
		ReflectionUtils.setField(evaluator, "authorityService",
				authorityService);
		ReflectionUtils.setField(evaluator, "registry", registry);
		ReflectionUtils.setField(evaluator, "typeConverter", typeConverter);
		ReflectionUtils.setField(evaluator, "stateFactory", stateFactory);
		evaluator.init();

		return evaluator;
	}

	/**
	 * This method gets all of the Task actions
	 * 
	 * @return List<Actions> List of type actions
	 */
	private List<Action> getActions() {

		List<Action> listOfActions = Arrays.asList(
				ObjectRoleEvaluator.EDIT_DETAILS, ObjectRoleEvaluator.DELETE,
				ObjectRoleEvaluator.MOVE_TO_OTHER_SECTION,
				ObjectRoleEvaluator.LOCK, ObjectRoleEvaluator.UNLOCK,
				ObjectRoleEvaluator.CLONE, ObjectRoleEvaluator.PRINT,
				ObjectRoleEvaluator.EXPORT);
		return listOfActions;
	}

}
