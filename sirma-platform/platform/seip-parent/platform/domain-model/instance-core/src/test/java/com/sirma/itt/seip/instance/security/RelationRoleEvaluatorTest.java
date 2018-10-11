package com.sirma.itt.seip.instance.security;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * @author BBonev
 */
public class RelationRoleEvaluatorTest {

	@InjectMocks
	private RelationRoleEvaluator roleEvaluator;

	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private DatabaseIdManager idManager;
	@Mock
	protected ResourceService resourceService;
	@Mock
	private StateService stateService;
	@Mock
	private StateTransitionManager transitionManager;
	@Mock
	AuthorityService authorityService;
	@Mock
	protected PermissionService permissionService;
	@Mock
	private RoleRegistry roleRegistry;
	private Role roleViewer;
	private Role roleCreator;
	@Mock
	private RoleEvaluatorManagerService evaluatorManagerServiceMock;
	@Spy
	private InstanceProxyMock<RoleEvaluatorManagerService> roleEvaluatorManagerService = new InstanceProxyMock<>(null);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		// add the default roles
		roleViewer = new Role(BaseRoles.VIEWER);
		roleCreator = new Role(BaseRoles.CREATOR);
		when(roleRegistry.find(BaseRoles.VIEWER)).thenReturn(roleViewer);
		when(roleRegistry.find(BaseRoles.CREATOR)).thenReturn(roleCreator);

		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		roleEvaluatorManagerService.set(evaluatorManagerServiceMock);
	}

	@Test
	public void test_SimpleLink() {
		assertEquals(roleEvaluator.evaluate(new LinkInstance(), null, null).getFirst(), roleViewer);
	}

	@Test
	public void test_deleted() {
		when(stateService.isInStates(any(), eq(PrimaryStates.DELETED))).thenReturn(Boolean.TRUE);
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		assertEquals(roleEvaluator.evaluate(linkInstance, mockUser(), null).getFirst(), roleViewer);
	}

	@Test
	public void test_createdBy_system() {
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		User mock = mock(User.class);
		when(securityContextManager.getSystemUser()).thenReturn(mock);
		when(resourceService.areEqual(eq("emf:creator"), eq(mock))).thenReturn(Boolean.TRUE);

		assertEquals(roleEvaluator.evaluate(linkInstance, mockUser(), null).getFirst(), roleViewer);
	}

	private Resource mockUser() {
		Resource mock = mock(Resource.class);
		when(mock.getId()).thenReturn("emf:user");
		return mock;
	}

	@Test
	public void test_createdBy_given() {
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		User user = mock(User.class);
		when(resourceService.areEqual(eq("emf:creator"), eq(user))).thenReturn(Boolean.TRUE);
		when(user.getId()).thenReturn("emf:user");

		assertEquals(roleEvaluator.evaluate(linkInstance, user, null).getFirst(), roleCreator);
	}

	@Test
	public void test_notPersistedSource() {
		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		EmfInstance instance = new EmfInstance("emf:source");
		linkInstance.setFrom(instance);

		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		User user = mock(User.class);
		when(user.getId()).thenReturn("emf:user");

		assertEquals(roleEvaluator.evaluate(linkInstance, user, null).getFirst(), roleCreator);
	}

	@Test
	public void test_notPersistedSource_fromReference() {
		LinkReference linkInstance = new LinkReference();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		EmfInstance instance = new EmfInstance("emf:source");
		linkInstance.setFrom(InstanceReferenceMock.createGeneric(instance));

		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		User user = mock(User.class);
		when(user.getId()).thenReturn("emf:user");

		assertEquals(roleEvaluator.evaluate(linkInstance, user, null).getFirst(), roleCreator);
	}

	@Test
	public void test_sourceInstanceRole() {

		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		InstanceReferenceMock instance = InstanceReferenceMock.createGeneric("emf:source");

		linkInstance.setFrom(instance.toInstance());

		User user = mock(User.class);
		when(user.getId()).thenReturn("emf:user");

		when(idManager.isPersisted(instance.toInstance())).thenReturn(Boolean.TRUE);

		ResourceRole role = mock(ResourceRole.class);

		when(role.getRole()).thenReturn(BaseRoles.MANAGER);

		when(permissionService.getPermissionAssignment(instance, user.getId())).thenReturn(role);
		Role expectedDefaultRole = new Role(BaseRoles.MANAGER);

		RoleEvaluator<Instance> parentEvaluator = mock(RoleEvaluator.class);
		when(parentEvaluator.evaluate(instance.toInstance(), user, null))
				.thenReturn(new Pair<>(expectedDefaultRole, null));

		when(roleRegistry.find(BaseRoles.MANAGER)).thenReturn(expectedDefaultRole);

		assertEquals(roleEvaluator.evaluate(linkInstance, user, null).getFirst(), expectedDefaultRole);
	}

	@Test
	public void test_sourceInstanceRole_notDefined() {

		LinkInstance linkInstance = new LinkInstance();
		linkInstance.setId("emf:link");
		linkInstance.add(DefaultProperties.CREATED_BY, "emf:creator");

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:source");
		InstanceReferenceMock reference = new InstanceReferenceMock("emf:source", mock(DataTypeDefinition.class),
				instance);
		ReflectionUtils.setFieldValue(instance, "reference", reference);
		linkInstance.setFrom(instance);

		User user = mock(User.class);
		when(user.getId()).thenReturn("emf:user");

		when(idManager.isPersisted(instance)).thenReturn(Boolean.TRUE);

		assertEquals(roleEvaluator.evaluate(linkInstance, user, null).getFirst(), roleViewer);
	}
}
