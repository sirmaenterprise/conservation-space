package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.OperationExecutedEvent;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * The Class ResourceServiceTest.
 *
 * @author BBonev
 */
public class ResourceServiceImplTest {

	@InjectMocks
	private ResourceServiceImpl resourceService;
	@Mock
	private TypeConverter converter;
	@Mock
	private ResourceEntityDao resourceDao;
	@Mock
	private ResourceStore resourceStore;
	@Mock
	private PropertiesService propertiesService;
	@Mock
	private DictionaryService dictionaryService;
	@Mock
	private InstanceLoadDecorator loadDecorator;
	@Mock
	private ServiceRegistry serviceRegistry;
	@Mock
	private EventService eventService;
	@Mock
	private SecurityContextManager securityManager;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(serviceRegistry.getEventProvider(any()))
				.thenReturn(InstanceEventProvider.NoOpInstanceEventProvider.instance());

		when(resourceStore.getResourceEntity(any(), any(), any())).thenCallRealMethod();

		when(resourceStore.convertToResource(any(ResourceEntity.class))).then(a -> {
			ResourceEntity entity = a.getArgumentAt(0, ResourceEntity.class);
			if (entity == null) {
				return null;
			}
			EmfResource resource = new EmfResource();
			resource.setId(entity.getId());
			resource.setName(entity.getIdentifier());
			return resource;
		});
		when(converter.convert(eq(ShortUri.class), anyString()))
				.then(a -> new ShortUri(a.getArgumentAt(1, String.class)));
		when(resourceStore.findById(anyString())).then(a -> {
			String id = a.getArgumentAt(0, String.class);
			if (StringUtils.isBlank(id) || !id.contains(":")) {
				return null;
			}
			ResourceEntity entity = new ResourceEntity();
			entity.setId(id);
			entity.setIdentifier(id.substring(id.indexOf(':')));
			return entity;
		});
		when(resourceStore.findResourceById(anyString())).then(a -> {
			String id = a.getArgumentAt(0, String.class);
			if (StringUtils.isBlank(id) || !id.contains(":")) {
				return null;
			}
			EmfResource resource = new EmfResource();
			resource.setId(id);
			resource.setName(id.substring(id.indexOf(':')));
			return resource;
		});
		when(resourceStore.findByName(anyString(), any(ResourceType.class))).then(a -> {
			String id = a.getArgumentAt(0, String.class);
			if (StringUtils.isBlank(id)) {
				return null;
			}
			ResourceEntity entity = new ResourceEntity();
			entity.setId("emf:" + id);
			entity.setIdentifier(id);
			return entity;
		});
		when(resourceStore.findResourceByName(anyString(), any(ResourceType.class))).then(a -> {
			String id = a.getArgumentAt(0, String.class);
			if (StringUtils.isBlank(id)) {
				return null;
			}
			EmfResource resource = new EmfResource();
			resource.setId("emf:" + id);
			resource.setName(id);
			return resource;
		});
		when(resourceStore.updateResource(any(Resource.class))).then(a -> a.getArgumentAt(0, Resource.class));
		when(securityManager.getAdminUser()).then(a -> {
			User user = mock(User.class);
			when(user.getSystemId()).thenReturn("emf:admin");
			return user;
		});
		when(securityManager.getSystemUser()).then(a -> {
			User user = mock(User.class);
			when(user.getSystemId()).thenReturn("emf:system");
			return user;
		});
		when(securityManager.getSuperAdminUser()).then(a -> {
			User user = mock(User.class);
			when(user.getSystemId()).thenReturn("emf:systemadmin");
			return user;
		});
	}

	@Test
	public void findResource_invalidArg() throws Exception {
		assertNull(resourceService.findResource(null));
		assertNull(resourceService.findResource(""));
	}

	@Test
	public void findResource_referenceAsString() throws Exception {
		// test with instance reference as string to instance conversion
		String jsonUser = "{instanceType:\"user\", instanceId=\"emf:test\"}";
		when(converter.convert(eq(InstanceReference.class), eq(jsonUser))).thenReturn(createInstanceRef("emf:test"));

		assertNotNull(resourceService.findResource(jsonUser));
	}

	@Test
	public void findResource_customToResource() throws Exception {
		// test with instance reference as string to instance conversion
		// test with custom type conversion to resource
		Resource testResource = createResource("emf:test", "test");
		Pair<Class<?>, String> pair = new Pair<>(EmfUser.class, "emf:test");
		when(converter.convert(eq(Resource.class), eq(pair))).thenReturn(testResource);

		assertNotNull(resourceService.findResource(pair));
	}

	@Test
	public void findResource_byId() throws Exception {
		// test with instance reference as string to instance conversion
		// test with custom type conversion to resource
		when(converter.convert(eq(ShortUri.class), eq("emf:test"))).thenReturn(new ShortUri("emf:test"));
		when(resourceStore.findById("emf:test")).thenReturn(new ResourceEntity());

		assertNotNull(resourceService.findResource("emf:test"));
	}

	@Test
	public void findResource_byName_group() throws Exception {
		// test with instance reference as string to instance conversion
		// test with custom type conversion to resource
		when(resourceStore.findByName("GROUP_test", ResourceType.GROUP)).thenReturn(new ResourceEntity());
		when(resourceStore.convertToResource(any(ResourceEntity.class)))
				.thenReturn(createResource("emf:GROUP_test", "GROUP_test"));

		assertNotNull(resourceService.findResource("GROUP_test"));
	}

	/**
	 * Test equals.
	 */
	@Test
	public void testResourceEquals() {
		// null checks
		assertFalse(resourceService.areEqual(null, null));
		assertFalse(resourceService.areEqual("", null));
		assertFalse(resourceService.areEqual(null, ""));
		assertFalse(resourceService.areEqual("", ""));

		// test before initialization a.k.a missing users
		// assertFalse(resourceService.areEqual("test", "test"));

		assertTrue(resourceService.areEqual("test", "test"));

		// test with different users
		when(resourceService.getResource(eq("test2"), eq(ResourceType.USER)))
				.thenReturn(createResource("emf:test2", "test2"));
		assertFalse(resourceService.areEqual("test", "test2"));


		// test mixed id checks
		assertTrue(resourceService.areEqual("emf:test", "test"));
		assertTrue(resourceService.areEqual("emf:test", "emf:test"));
		assertTrue(resourceService.areEqual("test", "emf:test"));

		// test with instance reference as string to instance conversion
		String jsonUser = "{instanceType:\"user\", instanceId=\"emf:test\"}";
		when(converter.convert(eq(InstanceReference.class), eq(jsonUser))).thenReturn(createInstanceRef("emf:test"));
		assertTrue(resourceService.areEqual("test", jsonUser));

		Resource testResource = createResource("emf:test", "test");
		// test with resource instance
		assertTrue(resourceService.areEqual("test", testResource));

		// test with custom type conversion to resource
		Pair<Class<?>, String> pair = new Pair<>(EmfUser.class, "emf:test");
		when(converter.convert(eq(Resource.class), eq(pair))).thenReturn(testResource);
		assertTrue(resourceService.areEqual("test", pair));
	}

	/**
	 * Creates the instance ref.
	 *
	 * @param id
	 *            the id
	 * @return the instance reference
	 */
	private InstanceReference createInstanceRef(String id) {
		LinkSourceId sourceId = new LinkSourceId();
		sourceId.setIdentifier(id);
		ReflectionUtils.setField(sourceId, "instance", createResource(id, id));
		return sourceId;
	}

	/**
	 * Creates the resource.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @return the resource
	 */
	private static Resource createResource(String id, String name) {
		EmfUser user = new EmfUser(name);
		user.setId(id);
		return user;
	}

	private static ResourceEntity createEntity(String id, String name) {
		ResourceEntity user = new ResourceEntity();
		user.setIdentifier(name);
		user.setId(id);
		user.setType(ResourceType.USER.getType());
		return user;
	}

	// ---------------------------- getAllActiveResources ----------------------------------

	/**
	 * Test for {@link ResourceServiceImpl#getAllActiveResources()}
	 */
	@Test
	public void getAllActiveResources_filteredResources() {
		ResourceServiceImpl service = mock(ResourceServiceImpl.class);
		when(service.getAllActiveResources(any(ResourceType.class), anyString())).thenCallRealMethod();
		when(service.getAllResources(any(), any())).thenReturn(buildResourceList());
		List<Resource> result = service.getAllActiveResources(ResourceType.USER, "");
		assertEquals(2, result.size());
	}

	private static List<Resource> buildResourceList() {
		List<Resource> resources = new LinkedList<>();
		resources.add(new EmfUser("user1"));
		resources.add(null);
		EmfUser activeUser1 = new EmfUser("activeUser1");
		activeUser1.setActive(true);
		resources.add(activeUser1);
		EmfUser inactiveUser1 = new EmfUser("inactiveUser1");
		inactiveUser1.setActive(false);
		resources.add(inactiveUser1);
		return resources;
	}

	@Test
	public void test_resourceExists() {
		resourceService.resourceExists("found");
		verify(resourceStore).resourceExists("found");
	}

	@Test
	public void delete_soft() throws Exception {

		ResourceEntity entity = new ResourceEntity();
		entity.setId("emf:test");
		when(resourceStore.getResourceEntity("emf:test", "test", ResourceType.USER)).thenReturn(entity);

		resourceService.delete(createResource("emf:test", "test"), null, false);

		verify(resourceStore).deleteResource(entity, false);

		verify(eventService).fire(any());
		verify(eventService).fireNextPhase(any());
	}

	@Test
	public void delete_permanent() throws Exception {

		resourceService.delete(createResource("emf:test", "test"), null, true);

		verify(resourceStore).deleteResource(any(), eq(true));

		verify(eventService).fire(any());
		verify(eventService).fireNextPhase(any());
	}

	@Test
	public void delete_notFound() throws Exception {

		resourceService.delete(null, null, true);

		verify(resourceStore, never()).deleteResource(any(), anyBoolean());
		verify(eventService, never()).fire(any());
		verify(eventService, never()).fireNextPhase(any());

		reset(resourceStore);

		when(resourceStore.getResourceEntity(anyString(), anyString(), any())).thenReturn(null);

		resourceService.delete(createResource("emf:test", "test"), null, true);

		verify(resourceStore, never()).deleteResource(any(), anyBoolean());
		verify(eventService, never()).fire(any());
		verify(eventService, never()).fireNextPhase(any());
	}

	@Test
	public void delete_notDeleteInternalResources() throws Exception {

		resourceService.delete(createResource("emf:admin", "test"), null, true);
		resourceService.delete(createResource("emf:system", "test"), null, true);
		resourceService.delete(createResource("emf:systemadmin", "test"), null, true);

		verify(resourceStore, never()).deleteResource(any(), anyBoolean());
		verify(eventService, never()).fire(any());
		verify(eventService, never()).fireNextPhase(any());
	}

	@Test
	public void tesUpdateKnownResource() throws Exception {
		when(resourceStore.resourceExists(eq("emf:test"))).thenReturn(Boolean.TRUE);

		Resource toSave = createResource("emf:test", "test");

		resourceService.saveResource(toSave);

		verify(resourceStore).updateResource(toSave);
		verify(eventService).fire(any(OperationExecutedEvent.class));
	}

	@Test
	public void testUpdateUnknownResource() throws Exception {

		when(resourceStore.resourceExists(eq((Serializable) null))).thenReturn(Boolean.FALSE);
		when(resourceStore.resourceExists(eq("test"))).thenReturn(Boolean.TRUE);

		when(resourceStore.getResourceEntity(null, "test", ResourceType.USER))
				.thenReturn(createEntity("emf:test", "test"));
		Resource toSave = createResource(null, "test");

		Resource saved = resourceService.saveResource(toSave);
		assertEquals("emf:test", saved.getId());

		verify(resourceStore).updateResource(toSave);
		verify(eventService).fire(any(OperationExecutedEvent.class));
	}

	@Test
	public void testSaveNewResource() throws Exception {
		when(resourceStore.getResourceEntity(null, "test", ResourceType.USER)).thenReturn(null);
		Resource toSave = createResource(null, "test");
		resourceService.saveResource(toSave);

		verify(resourceStore).importResource(toSave);
	}

	@Test
	public void testSaveAndActivate() throws Exception {

		when(resourceStore.resourceExists(eq((Serializable) null))).thenReturn(Boolean.FALSE);
		when(resourceStore.resourceExists(eq("test"))).thenReturn(Boolean.TRUE);

		when(resourceStore.getResourceEntity(null, "test", ResourceType.USER)).then(a -> {
			ResourceEntity entity = createEntity("emf:test", "test");
			entity.setActive(Boolean.FALSE);
			return entity;
		});

		resourceService.saveResource(createResource(null, "test"));

		verify(loadDecorator, atLeastOnce()).decorateInstance(any());
		verify(eventService, atLeastOnce()).fire(any());
		verify(resourceStore).updateResource(any());
	}

	@Test
	public void testSaveAndDeactivate() throws Exception {

		when(resourceStore.resourceExists(eq("test"))).thenReturn(Boolean.TRUE);

		when(resourceStore.getResourceEntity(null, "test", ResourceType.USER))
				.thenReturn(createEntity("emf:test", "test"));

		Resource resource = createResource(null, "test");
		resource.setActive(false);

		resourceService.saveResource(resource);

		verify(loadDecorator, atLeastOnce()).decorateInstance(any());
		verify(eventService, atLeastOnce()).fire(any());
		verify(resourceStore).updateResource(any());
	}

	@Test
	public void testSaveAndDeactivateGroup() throws Exception {

		when(resourceStore.resourceExists(eq("GROUP_test"))).thenReturn(Boolean.TRUE);

		when(resourceStore.getResourceEntity(null, "test", ResourceType.GROUP))
				.thenReturn(createEntity("emf:GROUP_test", "GROUP_test"));

		EmfGroup resource = new EmfGroup();
		resource.setName("GROUP_test");
		resource.setActive(false);

		resourceService.saveResource(resource);

		verify(loadDecorator, atLeastOnce()).decorateInstance(any());
		verify(eventService, atLeastOnce()).fire(any());
		verify(resourceStore).updateResource(any());
		verify(resourceDao).removeAllMembers("emf:GROUP_test");
	}

	@Test
	public void testLoadSystemAllOther() throws Exception {
		Resource otherUsers = resourceService.getAllOtherUsers();
		assertNotNull(otherUsers);

		reset(resourceStore);

		assertEquals(otherUsers, resourceService.loadByDbId(otherUsers.getId()));
		assertEquals(otherUsers, resourceService.load(otherUsers.getName()));
	}
}
