package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyListOf;
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
import java.util.Date;
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

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.itt.seip.testutil.fakes.TaskExecutorFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link DomainInstanceServiceImpl}.
 *
 * @author BBonev
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
	private DictionaryService dictionaryService;

	@Mock
	private InstanceSaveManager instanceSaveManager;

	@Mock
	private InstanceContextInitializer contextInitializer;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Spy
	private TaskExecutor taskExecutor = new TaskExecutorFake();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(instanceService.save(any(), any())).then(a -> a.getArgumentAt(0, Instance.class));
		doAnswer(a -> {
			return a.getArgumentAt(0, Instance.class);
		}).when(instanceVersionService).createVersion(any(VersionContext.class));
	}

	private void mockInstanceTypeResolver(Serializable id, Optional<InstanceReference> result) {
		when(instanceTypeResolver.resolveReference(id)).thenReturn(result);
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
	public void save_withArguments_internalServiceCalled() {
		service.save(new EmfInstance(), new Operation(), new Date());
		verify(instanceSaveManager).saveInstance(any(InstanceSaveContext.class));
	}

	@Test
	public void testClone() {
		final String identifier = "emf:id";
		Operation operation = new Operation();
		Instance instance = mock(Instance.class);

		InstanceReferenceMock reference = new InstanceReferenceMock(instance);
		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		when(instanceService.clone(instance, operation)).thenReturn(instance);
		mockInstanceTypeResolver(identifier, Optional.of(reference));
		service.clone(identifier, operation);

		verify(instanceService).clone(any(Instance.class), any(Operation.class));
		verify(instanceAccessEvaluator).getAccessPermission(any(Serializable.class));
		verify(instanceTypeResolver).resolveReference(identifier);
	}

	@Test
	public void delete_internalDeleteServiceCalled() {
		InstanceReference instanceReference = mock(InstanceReference.class);
		EmfInstance instance = new EmfInstance();
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(instanceReference));
		when(instanceAccessEvaluator.getAccessPermission(instanceReference))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		service.delete("instance-id");

		verify(instanceService).delete(eq(instance), eq(new Operation(ActionTypeConstants.DELETE, true)), eq(false));
	}

	@Test
	public void createInstance_internalServicesCalled() {
		Instance instance = new EmfInstance();
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);
		service.createInstance(mock(DefinitionModel.class), new EmfInstance());
		verify(instanceVersionService).setInitialVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_emptyParentId() {
		Instance instance = new EmfInstance();
		when(dictionaryService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);

		service.createInstance("definition-id", "");
		verify(instanceAccessEvaluator, never()).getAccessPermission(anyString());
		verify(instanceTypeResolver, never()).resolveReference(anyString());
		verify(instanceVersionService).setInitialVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_nullParentId() {
		Instance instance = new EmfInstance();
		when(dictionaryService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);

		service.createInstance("definition-id", null);
		verify(instanceAccessEvaluator, never()).getAccessPermission(anyString());
		verify(instanceTypeResolver, never()).resolveReference(anyString());
		verify(instanceVersionService).setInitialVersion(instance);
	}

	@Test
	public void createInstance_StringParameters_withParentId() {
		Instance instance = new EmfInstance();
		when(dictionaryService.find("definition-id")).thenReturn(mock(DefinitionModel.class));
		when(instanceService.createInstance(any(DefinitionModel.class), any(Instance.class))).thenReturn(instance);
		InstanceReferenceMock reference = InstanceReferenceMock.createGeneric("parent-instance-id");
		when(instanceAccessEvaluator.getAccessPermission(reference)).thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("parent-instance-id", Optional.of(reference));

		service.createInstance("definition-id", "parent-instance-id");
		verify(instanceVersionService).setInitialVersion(instance);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_noReadPermissions() {
		when(instanceTypeResolver.resolveReference(any()))
				.then(a -> Optional.of(InstanceReferenceMock.createGeneric(a.getArgumentAt(0, String.class))));
		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);
		service.loadInstance("instance-id");
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_noInstanceButNoInstanceToLoad() {
		when(instanceTypeResolver.resolveReference(any())).then(a -> Optional.of(mock(InstanceReference.class)));
		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		service.loadInstance("instance-id");
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_instanceNotFound() {
		when(instanceAccessEvaluator.getAccessPermission(anyString())).thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("instance-id", Optional.empty());
		service.loadInstance("instance-id");
	}

	@Test
	public void loadInstance_withReadPermissions_internalServiceCalled() {
		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);
		mockInstanceTypeResolver("instance-id", Optional.of(new InstanceReferenceMock(new EmfInstance())));
		service.loadInstance("instance-id");
		verify(instanceTypeResolver).resolveReference("instance-id");
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
		when(instanceAccessEvaluator.getAccessPermissions(identifiers)).thenReturn(permissionMap);
		when(instanceTypeResolver.resolveInstances(identifiers)).thenReturn(buildInstances(identifiers));

		Collection<Instance> instances = service.loadInstances(identifiers);
		assertEquals(2, instances.size());

		// instance-id-1
		Iterator<Instance> iterator = instances.iterator();
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

	private static Collection<Instance> buildInstances(List<String> identifiers) {
		return identifiers.stream().map(id -> {
			Instance instance = new EmfInstance();
			instance.setId(id);
			return instance;
		}).collect(Collectors.toList());
	}

	@Test
	public void loadInstances_withPermissions_resolverCalled() {
		when(instanceAccessEvaluator.getAccessPermission(anyString())).thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstances(Arrays.asList("instance-id-1", "instance-id-2", "instance-id-3"));
		verify(instanceTypeResolver).resolveInstances(anyListOf(String.class));
	}

	@Test
	public void loadInstance_version() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.of(new EmfInstance()));
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
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

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
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

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
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

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_deleted_notFound() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(null);
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(null);

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = InstanceNotFoundException.class)
	public void loadInstance_version_notFound() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(null);
		when(instanceService.loadDeleted(anyString())).thenReturn(Optional.empty());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(null);

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.CAN_READ);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_normal_noPermissions() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);

		service.loadInstance("emf:instance", false);
	}

	@Test(expected = NoPermissionsException.class)
	public void loadInstance_version_noPermissions() throws Exception {
		when(instanceVersionService.loadVersion(anyString())).thenReturn(new EmfInstance());
		when(instanceService.loadByDbId(any(Serializable.class))).thenReturn(new EmfInstance());

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
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

		when(instanceAccessEvaluator.getAccessPermission(any(Serializable.class)))
				.thenReturn(InstanceAccessPermissions.NO_ACCESS);

		Instance instance = service.loadInstance("emf:instance", true);
		assertNotNull(instance);
		verify(instanceVersionService, never()).loadVersion(any());
		verify(instanceService).loadDeleted(any());
		verify(instanceService, never()).loadByDbId(any(Serializable.class));
		verify(instanceAccessEvaluator).getAccessPermission(any(Serializable.class));
	}

	@Test
	public void getInstanceContext_ShouldFilterParentsWithoutPermissions() throws Exception {
		InstanceReferenceMock child = InstanceReferenceMock.createGeneric("emf:instance");
		when(instanceTypeResolver.resolveReference("emf:instance")).thenReturn(Optional.of(child));
		when(instanceAccessEvaluator.getAccessPermission(child)).thenReturn(InstanceAccessPermissions.CAN_READ);

		Instance childInstance = child.toInstance();
		Instance parent1 = InstanceReferenceMock.createGeneric("emf:parentWithPermissions").toInstance();
		Instance parent2 = InstanceReferenceMock.createGeneric("emf:parentWithNoPermissions").toInstance();
		Instance parent3 = InstanceReferenceMock.createGeneric("emf:otherparentWithPermissions").toInstance();
		childInstance.setOwningInstance(parent1);
		parent1.setOwningInstance(parent2);
		parent2.setOwningInstance(parent3);
		Map<Serializable, InstanceAccessPermissions> permissions = new HashMap<>();
		permissions.put("emf:parentWithPermissions", InstanceAccessPermissions.CAN_WRITE);
		permissions.put("emf:parentWithNoPermissions", InstanceAccessPermissions.NO_ACCESS);
		permissions.put("emf:otherparentWithPermissions", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(permissions);

		List<Instance> context = service.getInstanceContext("emf:instance");

		assertNotNull(context);
		assertEquals(2, context.size());
		assertEquals("The parent instance should be first", "emf:parentWithPermissions", context.get(0).getId());
		assertTrue("The parent should have its read permissions set", context.get(0).isReadAllowed());
		assertTrue("The parent should have its write permissions set", context.get(0).isWriteAllowed());
		assertEquals("emf:instance", context.get(1).getId());
		assertTrue("The instance should have read permissions set", context.get(1).isReadAllowed());
		assertFalse("The instance should have write permissions set", context.get(1).isWriteAllowed());

		verify(contextInitializer).restoreHierarchy(childInstance);
		verify(instanceLoadDecorator).decorateResult(anyCollection());
	}

	@Test
	public void getInstanceContext_ShouldReturnOneItemOnNoParents() throws Exception {
		InstanceReferenceMock child = InstanceReferenceMock.createGeneric("emf:instance");
		when(instanceTypeResolver.resolveReference("emf:instance")).thenReturn(Optional.of(child));
		when(instanceAccessEvaluator.getAccessPermission(child)).thenReturn(InstanceAccessPermissions.CAN_READ);

		when(instanceAccessEvaluator.getAccessPermissions(anyCollection())).thenReturn(Collections.emptyMap());

		List<Instance> context = service.getInstanceContext("emf:instance");

		assertNotNull(context);
		assertEquals(1, context.size());
		assertEquals("emf:instance", context.get(0).getId());

		verify(contextInitializer).restoreHierarchy(child.toInstance());
		verify(instanceLoadDecorator).decorateResult(anyCollection());
	}

	@Test(expected = NoPermissionsException.class)
	public void getInstanceContext_ShouldFailOnNoPermissions() throws Exception {
		InstanceReferenceMock child = InstanceReferenceMock.createGeneric("emf:instance");
		when(instanceTypeResolver.resolveReference("emf:instance")).thenReturn(Optional.of(child));
		when(instanceAccessEvaluator.getAccessPermission(child)).thenReturn(InstanceAccessPermissions.NO_ACCESS);

		service.getInstanceContext("emf:instance");
	}
}
