package com.sirma.itt.cmf.security.evaluator;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
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
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleImpl;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * This is test class created for unit testing of WorkflowRoleEvaluator
 * 
 * @author dvladov
 *
 */
public class WorkflowRoleEvaluatorTest {

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
	private BaseRoleEvaluator workflowRoleEvaluatorMock;

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
		workflowRoleEvaluatorMock = Mockito.mock(WorkflowRoleEvaluator.class);
	}

	/**
	 * Testing functionality evaluate case: is administrator
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void evaluateIsAdmin() {

		RoleEvaluator workflowEvaluator = createEvaluator();
		Resource resource = createResource();
		WorkflowInstanceContext workflowInstance = createInstance();

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.ADMINISTRATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(SecurityModel.BaseRoles.ADMINISTRATOR))
				.thenReturn(role);

		Mockito.when(authorityService.isAdminOrSystemUser(resource))
				.thenReturn(true);

		Pair<Role, RoleEvaluator<WorkflowInstanceContext>> actual = workflowEvaluator
				.evaluate(workflowInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal case: is creator
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void evaluateInternalIsRoleIrellevant() {

		RoleEvaluator workflowEvaluator = createEvaluator();
		Resource resource = createResource();
		WorkflowInstanceContext workflowInstance = createInstance();

		workflowInstance.getProperties().put(TaskProperties.CREATED_BY,
				resource.getIdentifier());

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.CREATOR,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(
				resourceService.areEqual(resource, workflowInstance
						.getProperties().get(TaskProperties.CREATED_BY)))
				.thenReturn(true);

		Mockito.when(registry.find(SecurityModel.BaseRoles.CREATOR))
				.thenReturn(role);

		Pair<Role, RoleEvaluator<WorkflowInstanceContext>> actual = workflowEvaluator
				.evaluate(workflowInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Testing functionality evaluateInternal case: is consumer
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void evaluateIsConsumer() {

		RoleEvaluator workflowEvaluator = createEvaluator();
		Resource resource = createResource();
		WorkflowInstanceContext workflowInstance = createInstance();

		RoleImpl role = new RoleImpl(SecurityModel.BaseRoles.CONSUMER,
				Collections
						.<Permission, List<Pair<Class<?>, Action>>> emptyMap());

		Mockito.when(registry.find(SecurityModel.BaseRoles.CONSUMER))
				.thenReturn(role);

		Pair<Role, RoleEvaluator<WorkflowInstanceContext>> actual = workflowEvaluator
				.evaluate(workflowInstance, resource, null);

		Assert.assertNotNull(actual);
		Assert.assertEquals(actual.getFirst(), role);

	}

	/**
	 * Creates object of type WorkflowInstance
	 * 
	 * @return caseInstance of type WorkflowInstance
	 */
	private WorkflowInstanceContext createInstance() {
		WorkflowInstanceContext workflowInstance = new WorkflowInstanceContext();

		workflowInstance.setId("task");
		workflowInstance.setProperties(new HashMap<String, Serializable>());

		return workflowInstance;
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
	 * @return the workflow role evaluator
	 */

	private RoleEvaluator createEvaluator() {
		WorkflowRoleEvaluator evaluator = new WorkflowRoleEvaluator();
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
		return evaluator;
	}

}
