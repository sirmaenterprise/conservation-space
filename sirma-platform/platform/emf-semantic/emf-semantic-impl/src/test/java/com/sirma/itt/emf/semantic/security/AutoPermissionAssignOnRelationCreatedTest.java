package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link AutoPermissionAssignOnRelationCreated}
 *
 * @author BBonev
 */
public class AutoPermissionAssignOnRelationCreatedTest {

	private static final String COLLABORATOR = SecurityModel.BaseRoles.COLLABORATOR.getIdentifier();
	private static final String NO_PERMISSION = SecurityModel.BaseRoles.NO_PERMISSION.getIdentifier();
	private static final String CONSUMER = SecurityModel.BaseRoles.CONSUMER.getIdentifier();
	private static final String PARENT = "emf:parentInstance";
	private static final String SOURCE = "emf:sourceInstance";
	private static final String VALID_DESTINATION = "emf:someUser";

	@InjectMocks
	private AutoPermissionAssignOnRelationCreated observer;

	@Mock
	private PermissionService permissionService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private RoleService roleService;
	@Mock
	private ResourceService resourceService;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private InstanceContextInitializer contextInitializer;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Spy
	private PermissionChangeRequestBuffer changeRequestBuffer;
	@Spy
	private TransactionalPermissionChanges permissionChanges = new TransactionalPermissionChanges(null, new InstanceProxyMock<>(transactionSupport));
	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(roleService.getRoleIdentifier(any()))
				.then(a -> SecurityModel.BaseRoles.getIdentifier(a.getArgumentAt(0, String.class)));

		when(resourceService.getAllOtherUsers()).thenReturn((Resource) createUser("emf:ALL_OTHER").toInstance());
		when(resourceService.areEqual(any(), any())).then(a -> {
			Instance arg1 = a.getArgumentAt(0, Instance.class);
			Instance arg2 = a.getArgumentAt(1, Instance.class);
			if (arg1 == null || arg2 == null) {
				return false;
			}
			return nullSafeEquals(arg1.getId(), arg2.getId());
		});

		when(schedulerService.buildEmptyConfiguration(any()))
				.then(a -> new DefaultSchedulerConfiguration().setType(a.getArgumentAt(0, SchedulerEntryType.class)));

		when(schedulerService.schedule(anyString(), any(), any())).then(a -> {
			// execute the action immediately
			observer.execute(a.getArgumentAt(2, SchedulerContext.class));
			return null;
		});
		doAnswer(a -> {
			a.getArgumentAt(0, InstanceReference.class).setParent(createInstance(PARENT));
			return null;
		}).when(contextInitializer).restoreHierarchy(any(InstanceReference.class));

		when(semanticDefinitionService.getClassInstance(anyString())).then(a -> {
			ClassInstance type = new ClassInstance();
			type.setId(a.getArgumentAt(0, String.class));
			return type;
		});
	}

	@Test
	public void doNothingOnUndefinedRelation() throws Exception {

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void doNothingOnUndefinedRelationRange() throws Exception {

		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, null);
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void assignPermissionsWhenRelationRangeIsUser() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void assignPermissionsWhenRelationRangeIsGroup() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.GROUP.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void doNothingWhenRelationRangeIsNotGroupOrUser() throws Exception {

		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.CASE.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfConfigured() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfLatestRevision() throws Exception {

		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE + "-rlatest", "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsUser() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createUser(SOURCE), createUser(VALID_DESTINATION)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsGroup() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createGroup(SOURCE), createUser(VALID_DESTINATION)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsClass() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createClass(SOURCE), createUser(VALID_DESTINATION)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignParentPermissionsIfConfigured() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, times(2)).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignParentPermissionsAlreadyAssignedAnyAndNotViaAllOthers() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());

		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, VALID_DESTINATION));

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignParentPermissionsAlreadyAssignedViaAllOthers() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());

		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, "emf:ALL_OTHER"));

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, times(2)).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignParentPermissionsAlreadyAssignedViaAllOthersButNotExplicitlyNoPermissions()
			throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());

		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put(VALID_DESTINATION, buildRole(NO_PERMISSION, VALID_DESTINATION));
		when(permissionService.getPermissionAssignments(any())).thenReturn(assignments);

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, times(1)).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfTargetIsUser() throws Exception {

		mockInstanceData(createUser(VALID_DESTINATION));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfTargetIsGroup() throws Exception {

		mockInstanceData(createGroup(VALID_DESTINATION));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfTargetIsNotUserOrGroup() throws Exception {

		mockInstanceData(createInstance(VALID_DESTINATION));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotOverridePermissionsWhenEnabledAndExplicitNoPermissionOfExistingRole() throws Exception {

		mockInstanceData(createGroup(VALID_DESTINATION));
		mockRelation("emf:references", COLLABORATOR, Boolean.TRUE, null, EMF.USER.toString());

		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put(VALID_DESTINATION, buildRole(NO_PERMISSION, VALID_DESTINATION));
		when(permissionService.getPermissionAssignments(any())).thenReturn(assignments);

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldOverridePermissionsWhenNotEnabledAndLowerExistingRole() throws Exception {

		mockInstanceData(createGroup(VALID_DESTINATION));
		mockRelation("emf:references", COLLABORATOR, Boolean.FALSE, null, EMF.USER.toString());
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, VALID_DESTINATION));
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldOverridePermissionsWhenEnabledAndLowerExistingRole() throws Exception {

		mockInstanceData(createGroup(VALID_DESTINATION));
		mockRelation("emf:references", COLLABORATOR, Boolean.TRUE, null, EMF.USER.toString());
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, VALID_DESTINATION));
		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotOverridePermissionsWhenNotEnabledAndHigherExistingRole() throws Exception {

		mockInstanceData(createGroup(VALID_DESTINATION));
		mockRelation("emf:references", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());

		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put(VALID_DESTINATION, buildRole(NO_PERMISSION, VALID_DESTINATION));
		when(permissionService.getPermissionAssignments(any())).thenReturn(assignments);

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void doNothingIfRelationHasInvalidRole() throws Exception {
		mockInstanceData(createGroup(VALID_DESTINATION));

		// the second time is requested to return a relation that is no longer configured for automatic permission
		// assignment
		// this is due to the fact that operation will be executed async
		mockRelation("emf:references", "INVALID_ROLE", Boolean.FALSE, null, EMF.USER.toString());

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldFailIfCannotLoadAllReferencedInstances() throws Exception {
		mockRelation("emf:references", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());

		observer.onRelationCreated(createEvent(SOURCE, "emf:references", VALID_DESTINATION));
	}

	private void mockValidInstanceData() {
		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createInstance(SOURCE), createUser(VALID_DESTINATION)));
	}

	private void mockInstanceData(InstanceReference referencedInstance) {
		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createInstance(SOURCE), referencedInstance));
	}

	private static InstanceReference createInstance(Serializable id) {
		return InstanceReferenceMock.createGeneric(id.toString());
	}

	private static InstanceReference createUser(Serializable id) {
		EmfUser instance = new EmfUser();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.USER.toString());
		InstanceReferenceMock instanceReference = new InstanceReferenceMock(id.toString(),
				mock(DataTypeDefinition.class), instance, instanceType);
		instance.setReference(instanceReference);
		return instanceReference;
	}

	private static InstanceReference createGroup(Serializable id) {
		EmfGroup instance = new EmfGroup();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.GROUP.toString());
		InstanceReferenceMock instanceReference = new InstanceReferenceMock(id.toString(),
				mock(DataTypeDefinition.class), instance, instanceType);
		instance.setReference(instanceReference);
		return instanceReference;
	}

	private static InstanceReference createClass(Serializable id) {
		ObjectInstance instance = new ObjectInstance();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.CLASS_DESCRIPTION.toString());
		InstanceReferenceMock instanceReference = new InstanceReferenceMock(id.toString(),
				mock(DataTypeDefinition.class), instance, instanceType);
		instance.setReference(instanceReference);
		return instanceReference;
	}

	private void mockRelation(String id, String role, Boolean allowOverride, String parentRole, String range) {
		when(semanticDefinitionService.getRelation(id))
				.then(a -> createRelation(id, role, allowOverride, parentRole, range));
	}

	private static ResourceRole buildRole(String role, String resource) {
		ResourceRole resourceRole = new ResourceRole();
		if (resource != null) {
			resourceRole.setAuthorityId(resource);
		}
		resourceRole.setRole(SecurityModel.BaseRoles.getIdentifier(role));
		return resourceRole;
	}

	private static PropertyInstance createRelation(String id, String role, Boolean allowOverride, String parentRole,
			String range) {
		PropertyInstance property = new PropertyInstance();
		property.setId(id);
		property.addIfNotNull(AutoPermissionAssignOnRelationCreated.ALLOW_PERMISSION_OVERRIDE, allowOverride);
		property.addIfNotNull(AutoPermissionAssignOnRelationCreated.MINIMAL_PERMISSION_ROLE, role);
		property.addIfNotNull(AutoPermissionAssignOnRelationCreated.MINIMAL_PARENT_PERMISSION_ROLE, parentRole);
		property.setRangeClass(range);
		return property;
	}

	private static ObjectPropertyAddEvent createEvent(Serializable source, String relationName, Serializable target) {
		ObjectPropertyAddEvent event = mock(ObjectPropertyAddEvent.class);
		when(event.getSourceId()).thenReturn(source);
		when(event.getObjectPropertyName()).thenReturn(relationName);
		when(event.getTargetId()).thenReturn(target);
		return event;
	}
}
