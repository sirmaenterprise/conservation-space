package com.sirma.itt.seip.permissions.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.PrimaryStateFactory;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link ResourceRoleEvaluator}
 *
 * @author BBonev
 */
public class ResourceRoleEvaluatorTest {

	@InjectMocks
	private ResourceRoleEvaluator evaluator;

	@Mock
	private RoleRegistry registry;
	@Mock
	private StateService stateService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private AuthorityService authorityService;
	@Mock
	private StateTransitionManager transitionManager;
	@Mock
	private ActionRegistry actionRegistry;
	@Mock
	private RoleEvaluatorManagerService evaluatorManagerService;
	@Spy
	private InstanceProxyMock<RoleEvaluatorManagerService> roleEvaluatorManagerService;
	@Spy
	private PrimaryStateFactory stateFactory;
	@Mock
	private ResourceService resourceService;
	@Mock
	private PermissionService permissionService;
	@Mock
	private RoleActionFilterService actionEvaluatorService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		roleEvaluatorManagerService.set(evaluatorManagerService);
		evaluator.init();

		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		when(registry.find(any())).then(a -> new Role(a.getArgumentAt(0, RoleIdentifier.class)));
	}

	@Test
	public void testEvaluate() throws Exception {
		Resource resource = buildUser();
		Resource target = buildUser();

		ResourceRole resourceRole = new ResourceRole();
		resourceRole.setRole(new RoleId("MANAGER", 0));
		when(permissionService.getPermissionAssignment(target.toReference(), resource.getId()))
				.thenReturn(resourceRole);

		Pair<Role, RoleEvaluator<Resource>> evaluated = evaluator.evaluate(target, resource, null);
		assertNotNull(evaluated);
		assertNotNull(evaluated.getFirst());
		assertNotNull(evaluated.getFirst().getRoleId());
		assertEquals("MANAGER", evaluated.getFirst().getRoleId().getIdentifier());
	}

	@Test
	public void testEvaluate_defaultRole() throws Exception {
		Resource resource = buildUser();
		Resource target = buildUser();

		Pair<Role, RoleEvaluator<Resource>> evaluated = evaluator.evaluate(target, resource, null);
		assertNotNull(evaluated);
		assertNotNull(evaluated.getFirst());
		assertNotNull(evaluated.getFirst().getRoleId());
		assertEquals("VIEWER", evaluated.getFirst().getRoleId().getIdentifier());
	}

	private static Resource buildUser() {
		EmfUser resource = new EmfUser("userId");
		resource.setId("emf:userId");
		InstanceReference reference = new InstanceReferenceMock("emf:userdId", mock(DataTypeDefinition.class),
				resource);
		resource.setReference(reference);
		return resource;
	}

}
