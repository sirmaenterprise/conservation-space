package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Test for {@link DomainInstanceServiceImpl}.
 *
 * @author BBonev
 * @author A. Kunchev
 */
public class DomainInstanceServiceImplTest {

	@InjectMocks
	private DomainInstanceServiceImpl service;

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private InstanceSaveManager instanceSaveManager;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Spy
	private InstanceContextServiceMock contextService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(instanceService.save(any(), any())).then(a -> a.getArgumentAt(0, Instance.class));
		doAnswer(a -> a.getArgumentAt(0, Instance.class))
				.when(instanceVersionService)
					.saveVersion(any(VersionContext.class));
		when(namespaceRegistryService.getShortUri(any(String.class))).then(a -> a.getArgumentAt(0, String.class));
	}

	private void mockInstanceTypeResolver(Serializable id, Instance result) {
		mockInstanceTypeResolver(id, Collections.singletonList(result));
	}

	private void mockInstanceTypeResolver(Serializable id, Collection<Instance> result) {
		when(instanceTypeResolver.resolveInstances(Collections.singletonList(id))).thenReturn(result);
	}

	@Test
	public void touchInstanceTest() {
		Instance instance = mock(Instance.class);

		service.touchInstance(instance);

		verify(instanceService).touchInstance(instance);
	}

	@Test(expected = IllegalArgumentException.class)
	public void delete_nullId() {
		service.delete(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void delete_emptyId() {
		service.delete("");
	}

	@Test
	public void save_withContext_internalServiceCalled() {
		InstanceSaveContext context = InstanceSaveContext.create(new EmfInstance(), new Operation());
		service.save(context);
		verify(instanceSaveManager).saveInstance(context);
	}

	@Test
	public void testClone() {
		final String identifier = "emf:id";
		Operation operation = new Operation();
		Instance instance = mock(Instance.class);

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		when(instanceService.clone(instance, operation)).thenReturn(instance);
		mockInstanceTypeResolver(identifier, instance);
		service.clone(identifier, operation);

		verify(instanceService).clone(any(Instance.class), any(Operation.class));
		verify(instanceAccessEvaluator).getAccessPermission(any(Instance.class));
	}

	@Test
	public void delete_internalDeleteServiceCalled() {
		EmfInstance instance = new EmfInstance();
		when(instanceService.loadByDbId("instance-id")).thenReturn(instance);
		when(instanceAccessEvaluator.getAccessPermission(instance)).thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("instance-id", instance);
		service.delete("instance-id");

		verify(instanceService).delete(eq(instance), eq(new Operation(ActionTypeConstants.DELETE, true)), eq(false));
	}

	@Test
	public void createInstance_internalServicesCalled() {
		Instance instance = new EmfInstance();
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);
		service.createInstance(mock(DefinitionModel.class), new EmfInstance());
		verify(instanceVersionService).populateVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_emptyParentId() {
		Instance instance = new EmfInstance();
		when(definitionService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);

		service.createInstance("definition-id", "");
		verify(instanceAccessEvaluator, never()).getAccessPermission(any());
		verify(instanceTypeResolver, never()).resolveInstances(anyCollection());
		verify(instanceVersionService).populateVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_nullParentId() {
		Instance instance = new EmfInstance();
		when(definitionService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);

		service.createInstance("definition-id", null);
		verify(instanceAccessEvaluator, never()).getAccessPermission(any());
		verify(instanceTypeResolver, never()).resolveInstances(anyCollection());
		verify(instanceVersionService).populateVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_withParentId() {
		Instance instance = new EmfInstance();
		when(definitionService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);
		when(instanceAccessEvaluator.getAccessPermission(instance)).thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("parent-instance-id", instance);

		service.createInstance("definition-id", "parent-instance-id");
		verify(instanceVersionService).populateVersion(instance);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_noReadPermissions() {
		when(instanceTypeResolver.resolveInstances(anyCollection()))
				.then(a -> Collections.singletonList(new EmfInstance()));
		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);
		service.loadInstance("instance-id");
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_instanceNotFound() {
		when(instanceAccessEvaluator.getAccessPermission(any())).thenReturn(InstanceAccessPermissions.CAN_READ);
		service.loadInstance("instance-id");
	}

	@Test
	public void loadInstance_withReadPermissions_internalServiceCalled() {
		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("instance-id", new EmfInstance());
		service.loadInstance("instance-id");
		verify(instanceTypeResolver).resolveInstances(Collections.singletonList("instance-id"));
	}

	@Test
	public void loadInstances_nullInputCollection() {
		assertEquals(Collections.emptyList(), service.loadInstances(null));
	}

	@Test
	public void loadInstances_emptyInputCollection() {
		assertEquals(Collections.emptyList(), service.loadInstances(new ArrayList<>()));
	}

	@Test
	public void loadInstances_withoutPermissions() {
		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(emptyMap());

		Collection<Instance> instances = service
				.loadInstances(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		assertEquals(0, instances.size());
	}

	@Test(expected = ResourceException.class)
	public void loadInstances_executorErrors() {
		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenThrow(new NullPointerException());
		service.loadInstances(Arrays.asList("instance-id"));
	}

	@Test
	public void loadInstances_withPermissions() {
		Map<Serializable, InstanceAccessPermissions> permissionMap = new HashMap<>(3);
		permissionMap.put("instance-id-1", InstanceAccessPermissions.CAN_WRITE);
		permissionMap.put("instance-id-2", InstanceAccessPermissions.NO_ACCESS);
		permissionMap.put("instance-id-3", InstanceAccessPermissions.CAN_READ);

		List<String> identifiers = Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3");
		Collection<Instance> instances = buildInstances(identifiers);
		when(instanceTypeResolver.resolveInstances(identifiers)).thenReturn(instances);
		when(instanceAccessEvaluator.getAccessPermissions(instances)).thenReturn(permissionMap);

		Collection<Instance> resultInstances = service.loadInstances(identifiers);
		assertEquals(2, resultInstances.size());

		// instance-id-1
		Iterator<Instance> iterator = resultInstances.iterator();
		Instance instanceWithFullAccess = iterator.next();
		assertEquals("instance-id-1", instanceWithFullAccess.getId());
		assertTrue(instanceWithFullAccess.isReadAllowed());
		assertTrue(instanceWithFullAccess.isWriteAllowed());

		// instance-id-3
		Instance instanceWithReadPermissions = iterator.next();
		assertEquals("instance-id-3", instanceWithReadPermissions.getId());
		assertTrue(instanceWithReadPermissions.isReadAllowed());
		assertFalse(instanceWithReadPermissions.isWriteAllowed());
	}

	@Test
	public void loadInstances_withPermissions_withDeleted() {
		Map<Serializable, InstanceAccessPermissions> permissionMap = new HashMap<>(3);
		permissionMap.put("instance-id-1", InstanceAccessPermissions.CAN_WRITE);
		permissionMap.put("instance-id-2", InstanceAccessPermissions.NO_ACCESS);
		permissionMap.put("instance-id-3", InstanceAccessPermissions.CAN_READ);
		permissionMap.put("deleted-instance", InstanceAccessPermissions.CAN_WRITE);

		List<String> identifiers = new ArrayList<>(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		Collection<Instance> instances = buildInstances(identifiers);
		when(instanceTypeResolver.resolveInstances(identifiers)).thenReturn(instances);
		when(instanceAccessEvaluator.getAccessPermissions(any())).thenReturn(permissionMap);

		identifiers.add(2, "deleted-instance");
		when(instanceService.loadDeleted("deleted-instance")).then(a -> {
			EmfInstance instance = new EmfInstance("deleted-instance");
			instance.markAsDeleted();
			return Optional.of(instance);
		});

		Collection<Instance> resultInstances = service.loadInstances(identifiers, true);
		assertEquals(3, resultInstances.size());

		// instance-id-1
		Iterator<Instance> iterator = resultInstances.iterator();
		Instance instanceWithFullAccess = iterator.next();
		assertEquals("instance-id-1", instanceWithFullAccess.getId());
		assertTrue(instanceWithFullAccess.isReadAllowed());
		assertTrue(instanceWithFullAccess.isWriteAllowed());

		// deleted-instance
		Instance deletedInstance = iterator.next();
		assertEquals("deleted-instance", deletedInstance.getId());
		assertTrue(deletedInstance.isReadAllowed());
		assertTrue(deletedInstance.isWriteAllowed());
		assertTrue(deletedInstance.isDeleted());

		// instance-id-3
		Instance instanceWithReadPermissions = iterator.next();
		assertEquals("instance-id-3", instanceWithReadPermissions.getId());
		assertTrue(instanceWithReadPermissions.isReadAllowed());
		assertFalse(instanceWithReadPermissions.isWriteAllowed());
	}

	private static Collection<Instance> buildInstances(List<String> identifiers) {
		return identifiers.stream().map(EmfInstance::new).collect(Collectors.toList());
	}

	@Test
	public void loadInstances_withPermissions_resolverCalled() {
		Map<Serializable, InstanceAccessPermissions> permissionMap = new HashMap<>(2);
		permissionMap.put("instance-id-1", InstanceAccessPermissions.CAN_WRITE);
		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(permissionMap);

		service.loadInstances(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		verify(instanceTypeResolver).resolveInstances(anyCollection());
	}

	@Test
	public void loadInstance_version() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.of(new EmfInstance()));
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		Instance instance = service.loadInstance("emf:instance-v1.0", true);
		assertNotNull(instance);
		verify(instanceVersionService).loadVersion(any());
		verify(instanceService, never()).loadDeleted(any());
		verify(instanceService, never()).loadByDbId(any(Serializable.class));
	}

	@Test
	public void loadInstance_Deleted() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadDeleted(anyString())).then(a -> {
			EmfInstance instance = new EmfInstance();
			instance.markAsDeleted();
			return Optional.of(instance);
		});
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		Instance instance = service.loadInstance("emf:instance", true);
		assertNotNull(instance);
		verify(instanceVersionService, never()).loadVersion(any());
		verify(instanceService).loadDeleted(any());
		verify(instanceService, never()).loadByDbId(any(Serializable.class));
	}

	@Test
	public void loadInstance_normal() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.of(new EmfInstance()));
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		Instance instance = service.loadInstance("emf:instance", false);
		assertNotNull(instance);
		verify(instanceVersionService, never()).loadVersion(any());
		verify(instanceService, never()).loadDeleted(any());
		verify(instanceService).loadByDbId(any(Serializable.class));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_normal_notFound() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(null);
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(null);

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_deleted_notFound() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(null);
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(null);

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_version_notFound() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(null);
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(null);

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_normal_noPermissions() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_version_noPermissions() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);

		service.loadInstance("emf:instance-v1.0", false);
	}

	@Test
	public void loadInstance_deleted_noPermissions() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadDeleted(anyString())).then(a -> {
			EmfInstance instance = new EmfInstance();
			instance.markAsDeleted();
			return Optional.of(instance);
		});
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Instance.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);

		Instance instance = service.loadInstance("emf:instance", true);
		assertNotNull(instance);
		verify(instanceVersionService, never()).loadVersion(any());
		verify(instanceService).loadDeleted(any());
		verify(instanceService, never()).loadByDbId(any(Serializable.class));
		verify(instanceAccessEvaluator).getAccessPermission(any(Instance.class));
	}

	@Test
	public void getInstanceContext_ShouldNotFilterParentsWithoutPermissions() throws Exception {
		Instance childInstance = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		mockInstanceTypeResolver("emf:instance", childInstance);
		when(instanceAccessEvaluator.getAccessPermission(childInstance)).thenReturn(InstanceAccessPermissions.CAN_READ);

		Instance parent1 = InstanceReferenceMock.createGeneric("emf:parentWithPermissions").toInstance();
		Instance parent2 = InstanceReferenceMock.createGeneric("emf:parentWithNoPermissions").toInstance();
		Instance parent3 = InstanceReferenceMock.createGeneric("emf:otherparentWithPermissions").toInstance();
		contextService.bindContext(childInstance, parent1);
		contextService.bindContext(parent1, parent2);
		contextService.bindContext(parent2, parent3);
		Map<Serializable, InstanceAccessPermissions> permissions = new HashMap<>();
		permissions.put("emf:parentWithPermissions", InstanceAccessPermissions.CAN_WRITE);
		permissions.put("emf:parentWithNoPermissions", InstanceAccessPermissions.NO_ACCESS);
		permissions.put("emf:otherparentWithPermissions", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(permissions);

		List<Instance> context = service.getInstanceContext("emf:instance");

		assertNotNull(context);
		assertEquals(4, context.size());
		assertEquals("The root instance should be first", "emf:otherparentWithPermissions", context.get(0).getId());
		assertEquals("emf:parentWithNoPermissions", context.get(1).getId());
		assertEquals("emf:parentWithPermissions", context.get(2).getId());
		assertTrue("The parent should have its read permissions set", context.get(2).isReadAllowed());
		assertTrue("The parent should have its write permissions set", context.get(2).isWriteAllowed());
		assertEquals("emf:instance", context.get(3).getId());
		assertTrue("The instance should have read permissions set", context.get(3).isReadAllowed());
		assertFalse("The instance should have write permissions set", context.get(3).isWriteAllowed());

		verify(instanceLoadDecorator).decorateResult(anyCollectionOf(Instance.class));
	}

	@Test
	public void getInstanceContext_ShouldReturnOneItemOnNoParents() throws Exception {
		Instance child = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		mockInstanceTypeResolver("emf:instance", child);
		when(instanceAccessEvaluator.getAccessPermission(child)).thenReturn(InstanceAccessPermissions.CAN_READ);

		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(Collections.emptyMap());

		List<Instance> context = service.getInstanceContext("emf:instance");

		assertNotNull(context);
		assertEquals(1, context.size());
		assertEquals("emf:instance", context.get(0).getId());

		verify(instanceLoadDecorator).decorateResult(anyCollectionOf(Instance.class));
	}

	@Test(expected = NoPermissionsException.class)
	public void getInstanceContext_ShouldFailOnNoPermissions() throws Exception {
		Instance child = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		when(instanceService.loadByDbId("emf:instance")).thenReturn(child);
		when(instanceAccessEvaluator.getAccessPermission(child)).thenReturn(InstanceAccessPermissions.NO_ACCESS);
		mockInstanceTypeResolver("emf:instance", child);

		service.getInstanceContext("emf:instance");
	}
}
