package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.resources.ResourceType.ALL;
import static com.sirma.itt.seip.resources.ResourceType.GROUP;
import static com.sirma.itt.seip.resources.ResourceType.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.resources.event.ResourceUpdatedEvent;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.DatabaseIdManagerMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link ResourceStore}
 *
 * @author BBonev
 */
public class ResourceStoreTest {

	@InjectMocks
	private ResourceStore resourceStore;

	@Mock
	private ResourceEntityDao entityDao;
	@Mock
	private ObjectMapper mapper;
	@Mock
	private DbDao dbDao;
	@Mock
	private PropertiesService propertiesService;
	@Mock
	private EventService eventService;
	@Spy
	private DatabaseIdManager idManager = new DatabaseIdManagerMock();
	@Spy
	private EntityLookupCacheContextFake cacheContext = EntityLookupCacheContextFake.createInMemory();
	@Spy
	private ContextualConcurrentMap<ResourceType, List<Resource>> allResources = ContextualConcurrentMap.create();
	@Spy
	private SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();
	@Mock
	private SecurityContext securityContext;
	@Mock
	private RemoteUserStoreAdapter userStoreAdapter;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private ExpressionsManager expressionsManager;
	@Mock
	private InstanceTypes instanceTypes;
	@Mock
	private SynchronizationRunner synchronizationRunner;
	@Mock
	private SystemConfiguration systemConfigs;
	@Mock
	private StateService stateService;
	@Mock
	private TransactionSupport transactionSupport;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		// reset caches
		cacheContext.reset();
		resourceStore.initialize();

		when(dbDao.saveOrUpdate(any())).then(a -> a.getArgumentAt(0, Entity.class));

		User currentUser = mock(User.class);
		when(currentUser.getIdentityId()).thenReturn("admin");
		when(securityContext.getAuthenticated()).thenReturn(currentUser);
		securityContextManager.setCurrentContext(securityContext);

		when(mapper.map(any(), eq(EmfUser.class))).then(a -> {
			ResourceEntity entity = a.getArgumentAt(0, ResourceEntity.class);
			EmfUser user = new EmfUser();
			user.setId(entity.getId());
			user.setName(entity.getIdentifier());
			user.setDisplayName(entity.getDisplayName());
			user.setSource(entity.getSource());
			user.setIdentifier(entity.getDefinitionId());
			user.setType(ResourceType.getById(entity.getType()));
			return user;
		});
		when(mapper.map(any(), eq(EmfGroup.class))).then(a -> {
			ResourceEntity entity = a.getArgumentAt(0, ResourceEntity.class);
			EmfGroup group = new EmfGroup();
			group.setId(entity.getId());
			group.setName(entity.getIdentifier());
			group.setDisplayName(entity.getDisplayName());
			group.setIdentifier(entity.getDefinitionId());
			group.setSource(entity.getSource());
			group.setType(ResourceType.getById(entity.getType()));
			return group;
		});

		when(mapper.map(any(), eq(ResourceEntity.class))).then(a -> {
			Resource resource = a.getArgumentAt(0, Resource.class);
			ResourceEntity entity = new ResourceEntity();
			entity.setDisplayName(resource.getDisplayName());
			entity.setId((String) resource.getId());
			entity.setIdentifier(resource.getName());
			entity.setDefinitionId(resource.getIdentifier());
			entity.setType(resource.getType().getType());
			entity.setSource(resource.getSource());
			return entity;
		});

		when(definitionService.getDefaultDefinitionId(any(Resource.class))).thenReturn("userDefinition");
		when(instanceTypes.from(anyString())).then(a -> Optional.empty());

		when(transactionSupport.invokeInNewTx(any(Callable.class))).then(answer -> {
			Callable<ResourceEntity> callable = answer.getArgumentAt(0, Callable.class);
			return callable.call();
		});
	}

	@Test
	public void getAllUsers() throws Exception {
		when(entityDao.getAllResourcesByType(USER))
				.thenReturn(Arrays.asList(createEntity("user1", USER), createEntity("user2", USER)));
		List<Resource> users = resourceStore.getAllResourcesReadOnly(USER);
		assertNotNull(users);
		assertFalse(users.isEmpty());
		assertEquals(2, users.size());
	}

	@Test
	public void getAllGroups() throws Exception {
		when(entityDao.getAllResourcesByType(GROUP))
				.thenReturn(Arrays.asList(createEntity("group1", GROUP), createEntity("group2", GROUP)));
		List<Resource> users = resourceStore.getAllResourcesReadOnly(GROUP);
		assertNotNull(users);
		assertFalse(users.isEmpty());
		assertEquals(2, users.size());
	}

	@Test
	public void getAllResources() throws Exception {
		when(entityDao.getAllResources()).thenReturn(Arrays.asList(createEntity("user1", USER),
				createEntity("user2", USER), createEntity("group1", GROUP), createEntity("group2", GROUP)));

		List<Resource> users = resourceStore.getAllResourcesReadOnly(ALL);
		assertNotNull(users);
		assertFalse(users.isEmpty());
		assertEquals(4, users.size());
	}

	@Test
	public void findFromCache() throws Exception {
		when(entityDao.getAllResources()).thenReturn(Arrays.asList(createEntity("user1", USER),
				createEntity("user2", USER), createEntity("group1", GROUP), createEntity("group2", GROUP)));

		cacheContext.reset();
		resourceStore.initialize();

		assertNotNull(resourceStore.findById("emf:user2"));
		assertNotNull(resourceStore.findByName("group1", GROUP));
		assertNotNull(resourceStore.findResourceById("emf:user1"));
		assertNotNull(resourceStore.findResourceByName("group2", GROUP));
	}

	@Test
	public void notfindFromCache() throws Exception {
		assertNull(resourceStore.findById("emf:user2"));
		assertNull(resourceStore.findById(null));
		assertNull(resourceStore.findResourceById("emf:user1"));
		assertNull(resourceStore.findResourceById(null));
		assertNull(resourceStore.findByName("group1", GROUP));
		assertNull(resourceStore.findResourceByName("group2", GROUP));
		assertNull(resourceStore.findByName("group1", null));
		assertNull(resourceStore.findResourceByName("group2", null));
		assertNull(resourceStore.findByName(null, GROUP));
		assertNull(resourceStore.findResourceByName(null, GROUP));
		assertNull(resourceStore.findByName(null, null));
	}

	@Test
	public void importResource_Should_WorkWithUser() throws Exception {
		ResourceEntity entity = createEntity("test", USER);
		entity.setId(null);
		Resource resource = resourceStore.importResource(mapper.map(entity, EmfUser.class));

		assertNotNull(resource);
		assertNotNull(resource.getId());
		assertNotNull(resource.type());
		assertEquals(EMF.USER.toString(), resource.type().getId());

		verify(propertiesService).saveProperties(any(), anyBoolean());
		verify(eventService, times(2)).fire(any(EmfEvent.class));
		verify(stateService).changeState(eq(resource), any());
	}

	@Test
	public void importResource_Should_WorkWithGroup() {
		ResourceEntity entity = createEntity("test", GROUP);
		entity.setId(null);
		Resource resource = resourceStore.importResource(mapper.map(entity, EmfGroup.class));

		assertNotNull(resource);
		assertNotNull(resource.getId());
		assertNotNull(resource.type());
		assertEquals(EMF.GROUP.toString(), resource.type().getId());

		verify(propertiesService).saveProperties(any(), anyBoolean());
		verify(eventService, times(2)).fire(any(EmfEvent.class));
		verify(stateService).changeState(eq(resource), any());
	}

	@Test
	public void persistNew() throws Exception {
		ResourceEntity entity = createEntity("test", USER);
		entity.setId(null);
		Resource resource = resourceStore.persistNewResource(mapper.map(entity, EmfUser.class),
				new Operation(ActionTypeConstants.CREATE));
		assertNotNull(resource);
		assertNotNull(resource.getId());
		verify(propertiesService).saveProperties(any(), anyBoolean());
	}

	@Test
	public void persistNew_withId() throws Exception {
		ResourceEntity entity = createEntity("test", GROUP);
		Resource resource = resourceStore.persistNewResource(mapper.map(entity, EmfGroup.class),
				new Operation(ActionTypeConstants.CREATE));
		assertNotNull(resource);
		assertNotNull(resource.getId());
		verify(propertiesService).saveProperties(any(), anyBoolean());
	}

	@Test(expected = DatabaseException.class)
	public void updateResource_notFound() throws Exception {
		ResourceEntity entity = createEntity("test", USER);
		resourceStore.updateResource(mapper.map(entity, EmfUser.class));
	}

	@Test
	public void updateResource() throws Exception {
		ResourceEntity entity = createEntity("test", GROUP);
		Resource resource = resourceStore.persistNewResource(mapper.map(entity, EmfGroup.class),
				new Operation(ActionTypeConstants.CREATE));
		assertNotNull(resource);
		assertNotNull(resource.getId());
		verify(propertiesService).saveProperties(any(), anyBoolean());
		verify(eventService, times(2)).fire(any(ResourceUpdatedEvent.class));

		Resource updatedResource = resourceStore.updateResource(resource);
		assertNotNull(updatedResource);
		verify(eventService, times(3)).fire(any(ResourceUpdatedEvent.class));
	}

	@Test
	public void findUserFromExtension() throws Exception {
		com.sirma.itt.seip.resources.User value = mapper.map(createEntity("user1", USER), EmfUser.class);
		value.setId(null);
		when(userStoreAdapter.getUserData("user1")).thenReturn(Optional.of(value));

		ResourceEntity findByName = resourceStore.findByName("user1", USER);
		assertNotNull(findByName);
		assertNotNull(findByName.getId());
		verify(dbDao).saveOrUpdate(findByName);
		verify(synchronizationRunner).runAll();
	}

	@Test
	public void findUserFromExtension_Should_NotImportUser_IfNotFoundInRemoteStore() throws Exception {
		com.sirma.itt.seip.resources.User value = mapper.map(createEntity("user1", USER), EmfUser.class);
		value.setId(null);
		when(userStoreAdapter.getUserData("user1")).thenReturn(Optional.empty());

		ResourceEntity findByName = resourceStore.findByName("user1", USER);
		assertNull(findByName);
		verify(dbDao, never()).saveOrUpdate(any());
		verify(synchronizationRunner, never()).runAll();
	}

	@Test(expected = EmfRuntimeException.class)
	public void findUserFromExtension_Should_ThrowDedicatedException_When_FetchingUserFromRemoteStoreFails()
			throws Exception {
		com.sirma.itt.seip.resources.User value = mapper.map(createEntity("user1", USER), EmfUser.class);
		value.setId(null);
		when(userStoreAdapter.getUserData("user1")).thenThrow(new RemoteStoreException("Failed"));

		resourceStore.findByName("user1", USER);
	}

	@Test
	public void findGroupFromExtension() throws Exception {
		when(userStoreAdapter.isExistingGroup("group1")).thenReturn(Boolean.TRUE);
		ResourceEntity findByName = resourceStore.findByName("group1", GROUP);
		assertNotNull(findByName);
		assertNotNull(findByName.getId());
		verify(dbDao).saveOrUpdate(findByName);
		verify(synchronizationRunner, never()).runAll();
	}

	@Test
	public void getResourceEntity() throws Exception {
		ResourceEntity entity = createEntity("id", USER);
		entity.setId(null);
		resourceStore.persistNewResource(mapper.map(entity, EmfUser.class), new Operation(ActionTypeConstants.CREATE));

		assertNotNull(resourceStore.getResourceEntity("emf:id", null, null));
		assertNotNull(resourceStore.getResourceEntity(null, "id", USER));

		assertNull(resourceStore.getResourceEntity(null, "id", null));
		assertNull(resourceStore.getResourceEntity(null, null, null));
	}

	@Test
	public void deleteResource_invalid() throws Exception {
		resourceStore.deleteResource(null, true);
		resourceStore.deleteResource(new ResourceEntity(), true);
		verify(dbDao, never()).find(any(), any());

		// already deleted
		resourceStore.deleteResource(createEntity("test", USER), true);
	}

	@Test
	public void delete_permanent() throws Exception {
		ResourceEntity entity = createEntity("test", USER);
		when(dbDao.find(ResourceEntity.class, "emf:test")).thenReturn(entity);

		resourceStore.deleteResource(entity, true);

		verify(dbDao).delete(any(), any());
		verify(entityDao).removeParticipation(any());
		verify(propertiesService).removeProperties(any(), any());
	}

	@Test
	public void delete_soft() throws Exception {
		ResourceEntity entity = createEntity("test", USER);
		when(dbDao.find(ResourceEntity.class, "emf:test")).thenReturn(entity);

		resourceStore.deleteResource(entity, false);

		verify(dbDao, never()).delete(any(), any());
		verify(dbDao).saveOrUpdate(any());
		verify(entityDao).removeParticipation(any());
		verify(propertiesService, never()).removeProperties(any(), any());
	}

	@Test
	public void delete_group_permanent() throws Exception {
		ResourceEntity entity = createEntity("test", ResourceType.GROUP);
		when(dbDao.find(ResourceEntity.class, "emf:test")).thenReturn(entity);

		resourceStore.deleteResource(entity, true);

		verify(dbDao).delete(any(), any());
		verify(entityDao).removeParticipation(any());
		verify(entityDao).removeAllMembers(anyString());
		verify(propertiesService).removeProperties(any(), any());
	}

	@Test
	public void resourceExist() throws Exception {
		assertFalse(resourceStore.resourceExists(null));
		assertFalse(resourceStore.resourceExists(""));
		assertFalse(resourceStore.resourceExists("test"));
		verify(entityDao).resourceExists("test");
	}

	private static ResourceEntity createEntity(String name, ResourceType resourceType) {
		ResourceEntity entity = new ResourceEntity();
		entity.setIdentifier(name);
		entity.setId("emf:" + name);
		entity.setDisplayName(name);
		entity.setType(resourceType.getType());
		return entity;
	}
}
