package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
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
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.TransactionalPermissionChangesFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link AutoPermissionAssignOnRelationCreated}
 *
 * @author BBonev
 */
public class AutoPermissionAssignOnRelationCreatedTest {

	private static final String COLLABORATOR = SecurityModel.BaseRoles.COLLABORATOR.getIdentifier();
	private static final String CONSUMER = SecurityModel.BaseRoles.CONSUMER.getIdentifier();
	private static final String CONTEXT_ID = "emf:parentInstance";
	private static final String INSTANCE_ID = "emf:sourceInstance";
	private static final String USER_ID = "emf:someUser";
	private static final String ALL_OTHER_USERS_ID = "emf:ALL_OTHER";

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
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Spy
	private PermissionChangeRequestBuffer changeRequestBuffer;
	@Spy
	private TransactionalPermissionChangesFake permissionChanges;
	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Spy
	private InstanceContextServiceMock contextService;

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(roleService.getRoleIdentifier(any()))
				.then(a -> SecurityModel.BaseRoles.getIdentifier(a.getArgumentAt(0, String.class)));

		when(resourceService.getAllOtherUsers()).thenReturn((Resource) createGroup(ALL_OTHER_USERS_ID).toInstance());
		when(resourceService.areEqual(any(), any())).then(a -> {
			Instance arg1 = a.getArgumentAt(0, Instance.class);
			Instance arg2 = a.getArgumentAt(1, Instance.class);
			if (arg1 == null || arg2 == null) {
				return false;
			}
			return nullSafeEquals(arg1.getId(), arg2.getId());
		});
		contextService.bindContext(new EmfInstance(INSTANCE_ID), InstanceReferenceMock.createGeneric(CONTEXT_ID));

		when(schedulerService.buildEmptyConfiguration(any()))
				.then(a -> new DefaultSchedulerConfiguration().setType(a.getArgumentAt(0, SchedulerEntryType.class)));

		when(schedulerService.schedule(anyString(), any(), any())).then(a -> {
			// execute the action immediately
			observer.execute(a.getArgumentAt(2, SchedulerContext.class));
			return null;
		});

		when(semanticDefinitionService.getClassInstance(anyString())).then(a -> {
			ClassInstance type = new ClassInstance();
			type.setId(a.getArgumentAt(0, String.class));
			return type;
		});

		permissionChanges.setPermissionService(permissionService);
	}

	@Test
	public void doNothingOnUndefinedRelation() throws Exception {

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void doNothingOnUndefinedRelationRange() throws Exception {

		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, null);
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void assignPermissionsWhenRelationRangeIsUser() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void assignPermissionsWhenRelationRangeIsGroup() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.GROUP.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void doNothingWhenRelationRangeIsNotGroupOrUser() throws Exception {

		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.CASE.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfConfigured() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsUser() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createUser(INSTANCE_ID), createUser(USER_ID)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsGroup() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createGroup(INSTANCE_ID), createUser(USER_ID)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfCurrentIsClass() throws Exception {

		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(createClass(INSTANCE_ID), createUser(USER_ID)));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignParentPermissionsIfConfigured() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());
		InstanceReference createdInstance = createInstance(INSTANCE_ID);
		Optional<InstanceReference> context = Optional.of(createInstance(CONTEXT_ID));
		when(contextService.getContext(createdInstance)).thenReturn(context);
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, times(2)).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignParentPermissionsAlreadyAssignedAnyAndNotViaAllOthers() throws Exception {

		mockValidInstanceData();
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, COLLABORATOR, EMF.USER.toString());

		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, USER_ID));

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfTargetIsUser() throws Exception {

		mockInstanceData(createUser(USER_ID));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldAssignPermissionsIfTargetIsGroup() throws Exception {

		mockInstanceData(createGroup(USER_ID));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotAssignPermissionsIfTargetIsNotUserOrGroup() throws Exception {

		mockInstanceData(createInstance(USER_ID));
		mockRelation("emf:references", CONSUMER, Boolean.TRUE, null, EMF.USER.toString());
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void shouldOverridePermissionsWhenNotEnabledAndLowerExistingRole() throws Exception {

		mockInstanceData(createGroup(USER_ID));
		mockRelation("emf:references", COLLABORATOR, Boolean.FALSE, null, EMF.USER.toString());
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, USER_ID));
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldOverridePermissionsWhenEnabledAndLowerExistingRole() throws Exception {

		mockInstanceData(createGroup(USER_ID));
		mockRelation("emf:references", COLLABORATOR, Boolean.TRUE, null, EMF.USER.toString());
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, USER_ID));
		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(any(), any());
	}

	@Test
	public void shouldNotOverridePermissionsWhenNotEnabledAndHigherExistingRoleAssginedViaGroup() throws Exception {

		mockInstanceData(createGroup(USER_ID));
		mockRelation("emf:references", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());
		when(resourceService.getContainingResources(USER_ID))
				.thenReturn(Arrays.asList(InstanceReferenceMock.createGeneric("GROUP_AS_CONSUMER_1").toInstance(),
						InstanceReferenceMock.createGeneric("GROUP_AS_CONSUMER_2").toInstance(),
						InstanceReferenceMock.createGeneric("GROUP_AS_COLLABORATOR").toInstance()));

		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put("GROUP_AS_CONSUMER_1", buildRole(CONSUMER, "GROUP_AS_CONSUMER_1"));
		assignments.put("GROUP_AS_CONSUMER_2", buildRole(CONSUMER, "GROUP_AS_CONSUMER_2"));
		assignments.put("GROUP_AS_COLLABORATOR", buildRole(COLLABORATOR, "GROUP_AS_COLLABORATOR"));

		when(permissionService.getPermissionAssignments(any())).thenReturn(assignments);

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test
	public void doNothingIfRelationHasInvalidRole() throws Exception {
		mockInstanceData(createGroup(USER_ID));

		// the second time is requested to return a relation that is no longer configured for automatic permission
		// assignment
		// this is due to the fact that operation will be executed async
		mockRelation("emf:references", "INVALID_ROLE", Boolean.FALSE, null, EMF.USER.toString());

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService, never()).setPermissions(any(), any());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldFailIfCannotLoadAllReferencedInstances() throws Exception {
		mockRelation("emf:references", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:references", USER_ID));
	}

	@Test
	public void onPartOfRelationShouldRemoveParentInheritanceOnInInvalidParent() throws Exception {
		mockValidInstanceData();

		observer.onRelationCreated(createEvent(INSTANCE_ID, "ptop:partOf", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(eq(createInstance(INSTANCE_ID)),
				argThat(CustomMatcher.of((Collection<PermissionsChange> list) -> {
					assertFalse(list.isEmpty());
					Iterator<PermissionsChange> it = list.iterator();
					assertEquals(USER_ID, ((PermissionsChange.ParentChange) it.next()).getValue());
					assertFalse(((PermissionsChange.InheritFromParentChange) it.next()).getValue());
				})));
	}

	@Test
	public void onPartOfRelationShouldChangeParentPermissions() throws Exception {
		mockValidInstanceData();

		when(hierarchyResolver.isAllowedForPermissionSource(any())).thenReturn(Boolean.TRUE);

		observer.onRelationCreated(createEvent(INSTANCE_ID, "ptop:partOf", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		verify(permissionService).setPermissions(eq(createInstance(INSTANCE_ID)),
				argThat(CustomMatcher.of((Collection<PermissionsChange> list) -> !list.isEmpty()
						&& USER_ID.equals(((PermissionsChange.ParentChange) list.iterator().next()).getValue()))));
	}

	@Test
	public void should_SetPermissionsForSourceAndParent_When_UserAssignedViaAllOthers() throws Exception {
		mockInstanceData(createUser(USER_ID));
		mockRelation("emf:hasAssignee", COLLABORATOR, Boolean.FALSE, COLLABORATOR, EMF.USER.toString());

		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, ALL_OTHER_USERS_ID));

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:hasAssignee", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		ArgumentCaptor<Collection<PermissionsChange>> permissionsChangeCaptor = ArgumentCaptor
				.forClass(Collection.class);

		verify(permissionService, times(2)).setPermissions(any(), permissionsChangeCaptor.capture());

		Collection<PermissionsChange> changesForParent = permissionsChangeCaptor.getAllValues().get(0);
		Collection<PermissionsChange> changesForSource = permissionsChangeCaptor.getAllValues().get(1);

		assertEquals(1, changesForParent.size());
		assertEquals(1, changesForSource.size());

		AddRoleAssignmentChange change = (AddRoleAssignmentChange) changesForParent.iterator().next();
		assertEquals(COLLABORATOR, change.getRole());
		assertEquals(USER_ID, change.getAuthority());

		change = (AddRoleAssignmentChange) changesForSource.iterator().next();
		assertEquals(COLLABORATOR, change.getRole());
		assertEquals(USER_ID, change.getAuthority());
	}

	@Test
	public void should_NotChangePermissionsForAllOthersInParent() throws Exception {
		mockInstanceData(createGroup(ALL_OTHER_USERS_ID));
		mockRelation("emf:hasAssignee", COLLABORATOR, Boolean.FALSE, COLLABORATOR, EMF.USER.toString());

		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any()))
				.thenReturn(buildRole(CONSUMER, ALL_OTHER_USERS_ID));

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:hasAssignee", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		ArgumentCaptor<Collection<PermissionsChange>> permissionsChangeCaptor = ArgumentCaptor
				.forClass(Collection.class);
		verify(permissionService).setPermissions(any(), permissionsChangeCaptor.capture());

		AddRoleAssignmentChange change = (AddRoleAssignmentChange) permissionsChangeCaptor.getValue().iterator().next();
		assertEquals(COLLABORATOR, change.getRole());
		assertEquals(ALL_OTHER_USERS_ID, change.getAuthority());
	}

	@Test
	public void should_SetPermissionsForUser_When_UserHasPermissionsViaAllOthers() throws Exception {
		mockInstanceData(createUser(USER_ID));
		mockRelation("emf:hasWatcher", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());

		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put(ALL_OTHER_USERS_ID, buildRole(CONSUMER, ALL_OTHER_USERS_ID));
		when(permissionService.getPermissionAssignments(any(InstanceReference.class))).thenReturn(assignments);

		when(resourceService.getContainingResources(USER_ID))
				.thenReturn(Arrays.asList(InstanceReferenceMock.createGeneric(ALL_OTHER_USERS_ID).toInstance()));

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:hasWatcher", USER_ID));
		// trigger actual changes
		permissionChanges.beforeCompletion();

		ArgumentCaptor<Collection<PermissionsChange>> permissionsChangeCaptor = ArgumentCaptor
				.forClass(Collection.class);
		verify(permissionService).setPermissions(any(), permissionsChangeCaptor.capture());

		AddRoleAssignmentChange change = (AddRoleAssignmentChange) permissionsChangeCaptor.getValue().iterator().next();
		assertEquals(CONSUMER, change.getRole());
		assertEquals(USER_ID, change.getAuthority());
	}

	@Test
	public void should_NotAddPermissionChange_When_RoleIsWithLowerPriorityThanExistingRole() throws Exception {
		InstanceReference instanceReference = createInstance(INSTANCE_ID);
		InstanceReference referencedInstance = createUser(USER_ID);
		mockInstanceData(instanceReference, referencedInstance);
		mockRelation("emf:hasWatcher", CONSUMER, Boolean.FALSE, null, EMF.USER.toString());

		PermissionsChangeBuilder builder = PermissionsChange.builder();
		builder.addRoleAssignmentChange(USER_ID, COLLABORATOR);
		permissionChanges.addBuilder(instanceReference, builder);

		observer.onRelationCreated(createEvent(INSTANCE_ID, "emf:hasWatcher", USER_ID));

		ArgumentCaptor<Collection<PermissionsChange>> permissionsChangeCaptor = ArgumentCaptor
				.forClass(Collection.class);
		verify(permissionService).setPermissions(any(), permissionsChangeCaptor.capture());

		Collection<PermissionsChange> finalPermissionChanges = permissionsChangeCaptor.getValue();
		assertEquals(1, finalPermissionChanges.size());

		AddRoleAssignmentChange change = (AddRoleAssignmentChange) finalPermissionChanges.iterator().next();
		assertEquals(COLLABORATOR, change.getRole());
		assertEquals(USER_ID, change.getAuthority());
	}

	private void mockValidInstanceData() {
		mockInstanceData(createUser(USER_ID));
	}

	private void mockInstanceData(InstanceReference referencedInstance) {
		mockInstanceData(createInstance(INSTANCE_ID), referencedInstance);
	}

	private void mockInstanceData(InstanceReference instanceReference, InstanceReference referencedInstance) {
		when(instanceTypeResolver.resolveReferences(anyCollection()))
				.thenReturn(Arrays.asList(instanceReference, referencedInstance));
	}

	private static InstanceReference createInstance(Serializable id) {
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.DOCUMENT.toString());
		EmfInstance instance = new EmfInstance(id);
		instance.setType(instanceType);
		return InstanceReferenceMock.createGeneric(instance);
	}

	private static InstanceReference createUser(Serializable id) {
		EmfUser instance = new EmfUser();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.USER.toString());
		instance.setType(instanceType);
		return InstanceReferenceMock.createGeneric(instance);
	}

	private static InstanceReference createGroup(Serializable id) {
		EmfGroup instance = new EmfGroup();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.GROUP.toString());
		instance.setType(instanceType);
		return InstanceReferenceMock.createGeneric(instance);
	}

	private static InstanceReference createClass(Serializable id) {
		ObjectInstance instance = new ObjectInstance();
		instance.setId(id);
		ClassInstance instanceType = new ClassInstance();
		instanceType.setId(EMF.CLASS_DESCRIPTION.toString());
		instance.setType(instanceType);
		return InstanceReferenceMock.createGeneric(instance);
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
