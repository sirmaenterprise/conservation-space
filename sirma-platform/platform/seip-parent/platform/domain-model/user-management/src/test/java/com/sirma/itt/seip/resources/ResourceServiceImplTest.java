package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.resources.ResourceProperties.USER_ID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link ResourceServiceImpl}
 *
 * @author BBonev
 */
public class ResourceServiceImplTest {

	private static final String TENANT_ID = "tenant.com";
	private static final Operation DEACTIVATE = new Operation(ActionTypeConstants.DEACTIVATE);
	private static final Operation ACTIVATE = new Operation(ActionTypeConstants.ACTIVATE);

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
	private DefinitionService definitionService;
	@Mock
	private InstanceLoadDecorator loadDecorator;
	@Mock
	private ServiceRegistry serviceRegistry;
	@Mock
	private EventService eventService;
	@Mock
	private SecurityContextManager securityManager;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private InstanceTypes instanceTypes;
	@Mock
	private UserPreferences userPreferences;
	@Mock
	private StateService stateService;

	@Spy
	private ConfigurationPropertyMock<Pattern> userNameValidationPattern = new ConfigurationPropertyMock<>();

	@Before
	public void setup() throws NoSuchFieldException {
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
			resource.setName(id.substring(id.indexOf(':') + 1));
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
			when(user.getIdentityId()).thenReturn("admin");
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
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(new DefinitionMock());
		when(instanceTypes.from(any(Resource.class))).thenReturn(Optional.of(InstanceType.create("resource")));

		// read default configuration value
		Field field = resourceService.getClass().getDeclaredField("userNameValidationPattern");
		field.setAccessible(true);
		String localUserNameValidationPattern = field.getAnnotation(ConfigurationPropertyDefinition.class).defaultValue();
		userNameValidationPattern.setValue(Pattern.compile(localUserNameValidationPattern));
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

	@Test
	public void findResource_byName_user() throws Exception {
		// test with instance reference as string to instance conversion
		// test with custom type conversion to resource
		when(resourceStore.findByName("someUser@test.com", ResourceType.USER)).thenReturn(new ResourceEntity());
		when(resourceStore.convertToResource(any(ResourceEntity.class)))
				.thenReturn(createResource("emf:someUser-test.com", "someUser@test.com"));

		assertNotNull(resourceService.findResource("someUser@test.com"));
	}

	@Test
	public void findResource_byName_user_defaultTenant() throws Exception {
		// this should check if we can handle correctly user names without tenant suffix
		// but for some reason the mockito does not work when when I try to mock the method findByName
		// it's mocked in the before method and after resourceStore mock reset does not detect anything
		// and the test fails
		when(resourceStore.findByName("someUser", ResourceType.USER)).thenReturn(new ResourceEntity());
		when(resourceStore.convertToResource(any(ResourceEntity.class)))
				.thenReturn(createResource("emf:someUser", "someUser"));

		assertNotNull(resourceService.findResource("someUser"));
	}

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

	private static InstanceReference createInstanceRef(String id) {
		LinkSourceId sourceId = new LinkSourceId();
		sourceId.setId(id);
		ReflectionUtils.setFieldValue(sourceId, "instance", createResource(id, id));
		return sourceId;
	}

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


	/**
	 * Test for {@link ResourceServiceImpl#getAllActiveResources(ResourceType, String)}}
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
		verify(stateService).changeState(eq(toSave), any());
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
		verify(stateService).changeState(eq(toSave), any());
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
		verify(resourceStore).updateResource(any());
		verify(stateService).changeState(resource, DEACTIVATE);
	}

	@Test
	public void testSaveAndDeactivate_shouldNotDeactivateAdminUser() throws Exception {

		when(resourceStore.resourceExists(eq("admin"))).thenReturn(Boolean.TRUE);

		when(resourceStore.getResourceEntity(null, "admin", ResourceType.USER))
				.thenReturn(createEntity("emf:admin", "admin"));

		Resource resource = createResource(null, "admin");
		resource.setActive(false);

		resourceService.saveResource(resource);

		verify(resourceStore).updateResource(argThat(CustomMatcher.ofPredicate(Resource::isActive)));
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
		verify(resourceStore).updateResource(any());
		verify(resourceDao).removeAllMembers("emf:GROUP_test");
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void createUser_ShouldThrowException_When_UserWithThatIdAlreadyExists() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john");

		when(resourceStore.resourceExists(any())).thenReturn(Boolean.TRUE);

		resourceService.createUser(instance);
	}

	@Test
	public void createUser_ShouldCreateUserInLocalStore() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john");
		ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
		ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);

		when(resourceStore.persistNewResource(any(), any())).then(a -> a.getArgumentAt(0, Resource.class));

		resourceService.createUser(instance);

		verify(resourceStore).persistNewResource(resourceCaptor.capture(), operationCaptor.capture());

		Resource resource = resourceCaptor.getValue();
		assertEquals("emf:john-" + TENANT_ID, resource.getId());

		Operation operation = operationCaptor.getValue();
		assertEquals(ActionTypeConstants.CREATE, operation.getOperation());
	}

	@Test
	public void buildUser_ShouldAppendTenantIdToUserId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john");

		mockDefinition(instance);

		com.sirma.itt.seip.resources.User user = resourceService.buildUser(instance);

		assertEquals("john@" + TENANT_ID, user.get(USER_ID));
	}

	private void mockDefinition(Instance instance) {
		DefinitionMock definitionMock = new DefinitionMock();

		PropertyDefinitionMock propertyDefinitionMock = new PropertyDefinitionMock();
		propertyDefinitionMock.setName(USER_ID);

		DataTypeDefinitionMock dataTypeDefinitionMock = new DataTypeDefinitionMock(DataTypeDefinition.TEXT);
		propertyDefinitionMock.setDataType(dataTypeDefinitionMock);

		definitionMock.setFields(Collections.singletonList(propertyDefinitionMock));
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definitionMock);
	}

	@Test
	public void buildUser_ShouldNotModifyUserId_When_UserIdAlreadyHasTenantId() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john@" + TENANT_ID);

		com.sirma.itt.seip.resources.User user = resourceService.buildUser(instance);

		assertEquals("john@" + TENANT_ID, user.get(USER_ID));
	}

	@Test
	public void buildUser_ShouldNotChangeInstanceId_When_DefaultTenant() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john");

		when(securityContext.isDefaultTenant()).thenReturn(Boolean.TRUE);

		com.sirma.itt.seip.resources.User user = resourceService.buildUser(instance);

		assertEquals("john", user.get(USER_ID));
	}

	@Test
	public void buildUser_ShouldRemoveTenantId_When_DefaultTenant() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john@" + TENANT_ID);

		when(securityContext.isDefaultTenant()).thenReturn(Boolean.TRUE);

		com.sirma.itt.seip.resources.User user = resourceService.buildUser(instance);

		assertEquals("john", user.get(USER_ID));
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildUser_ShouldThrowException_When_TenantIdIsDifferentThatTheCurrent() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:john");
		instance.add(USER_ID, "john@other.com");

		resourceService.buildUser(instance);
	}

	@Test
	public void validateUserName_shouldCheckForNull() {
		assertFalse(resourceService.validateUserName(null));
	}

	@Test
	public void validateUserName_shouldAcceptFollowing() {
		assertTrue("Should accept user without domain", resourceService.validateUserName("user"));
		assertTrue("Should accept user name with digits", resourceService.validateUserName("user123"));
		assertTrue("Should accept user with domain", resourceService.validateUserName("user@tenant.com"));
		assertTrue("Should accept dash in the user name part", resourceService.validateUserName("user-name@tenant.com"));
		assertTrue("Should accept dot in the user name part", resourceService.validateUserName("user.name@tenant.com"));
		when(securityContext.getCurrentTenantId()).thenReturn("tt.com");
		assertTrue("Should accept short domain name", resourceService.validateUserName("user@tt.com"));
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.bg");
		assertTrue("Should accept short domain suffix", resourceService.validateUserName("user@tenant.bg"));
		when(securityContext.getCurrentTenantId()).thenReturn("tt-2.bg");
		assertTrue("Should accept dash in the domain name", resourceService.validateUserName("user@tt-2.bg"));
		when(securityContext.getCurrentTenantId()).thenReturn("tt.2.bg");
		assertTrue("Should accept dot in the domain name", resourceService.validateUserName("user@tt.2.bg"));
	}

	@Test
	public void validateUserName_shouldNotAcceptFollowing() {
		assertFalse("Should not accept user with leading dot", resourceService.validateUserName(".user"));
		assertFalse("Should not accept user with leading dash", resourceService.validateUserName("-user"));
		assertFalse("Should not accept user name with space", resourceService.validateUserName("u ser"));
		assertFalse("Should not accept user name with leading space", resourceService.validateUserName("   user"));
		assertFalse("Should not accept user name with trailing space", resourceService.validateUserName("user   "));
		assertFalse("Should not accept user name with multiple repeating non word chars", resourceService.validateUserName("user..name"));
		assertFalse("Should not accept user name with multiple repeating non word chars", resourceService.validateUserName("user--name"));
		assertFalse("Should not accept user name with multiple repeating non word chars", resourceService.validateUserName("user.-name"));
		assertFalse("Should not accept user name with multiple repeating non word chars", resourceService.validateUserName("user-.name"));
		assertFalse("Should not accept user name with multiple repeating non word chars in the domain", resourceService.validateUserName("user@te-.nant.com"));
		assertFalse("Should not accept user name with multiple repeating non word chars in the domain", resourceService.validateUserName("user@te..nant.com"));
		assertFalse("Should not accept user name with multiple repeating non word chars in the domain", resourceService.validateUserName("user@te--nant.com"));
		assertFalse("Should not accept user name with multiple repeating non word chars in the domain", resourceService.validateUserName("user@te.-nant.com"));
		assertFalse("Should not accept user with invalid short domain", resourceService.validateUserName("user@t.com"));
		assertFalse("Should not accept very short domain suffix", resourceService.validateUserName("user@tenant.b"));
		assertFalse("Should not accept different tenant domain", resourceService.validateUserName("user@tenant.bg"));
		when(securityContext.isDefaultTenant()).thenReturn(true);
		assertFalse("Should not accept different tenant domain", resourceService.validateUserName("user@tenant.bg"));
	}

	@Test
	public void updateResource_ShouldReturnNull_When_PassedInstanceIsNull() {
		assertNull(resourceService.updateResource(null, null));
	}

	@Test
	public void updateResource_ShouldReturnNull_When_InstanceNotFound() {
		assertNull(resourceService.updateResource(new EmfInstance(), null));
	}

	@Test
	public void updateResource_ShouldUpdateDisplayName() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:TestGroup");
		instance.add(DefaultProperties.TITLE, "Test Group");
		instance.add(ResourceProperties.GROUP_ID, "TestGroup");

		when(resourceStore.resourceExists(instance.getId())).thenReturn(Boolean.TRUE);

		Resource resource = resourceService.updateResource(instance, null);

		assertEquals("Test Group", resource.getDisplayName());
	}

	@Test
	public void saveResource_ShouldReturnNull_When_PassedInstanceIsNull() {
		assertNull(resourceService.saveResource(null));
	}

	@Test
	public void getResource_nullIdentifier_shouldReturnNull() {
		assertNull(resourceService.getResource(null));
		verifyZeroInteractions(resourceStore);
	}

	@Test
	public void getResource_emptyIdentifier_shouldReturnNull() {
		assertNull(resourceService.getResource(""));
		verifyZeroInteractions(resourceStore);
	}

	@Test
	public void getResource_ShouldFetchFromResourceStore() {
		String resourceId = "user";
		resourceService.getResource(resourceId);

		verify(resourceStore).findResourceById(resourceId);
	}

	@Test
	public void getResource_version_shouldNormalizeIdentifierBeforeCallingInternalService() {
		String resourceId = "user-v1.5";
		resourceService.getResource(resourceId);
		verify(resourceStore).findResourceById("user");
	}

	@Test
	public void getResource_ShouldFindResourceByName_When_ResourceTypeIsKnown() {
		String resourceId = "resourceId";
		resourceService.getResource(resourceId, ResourceType.GROUP);

		verify(resourceStore).findResourceByName(resourceId, ResourceType.GROUP);
	}

	@Test
	public void getResource_ShouldFetchResourceEntity_When_ResourceTypeIsUnknown() {
		String resourceId = "resourceId";
		ResourceEntity resourceEntity = new ResourceEntity();
		resourceEntity.setId(resourceId);
		when(resourceStore.getResourceEntity(any(Serializable.class), anyString(), any(ResourceType.class)))
				.thenReturn(null, resourceEntity);

		Resource resource = resourceService.getResource(resourceId, ResourceType.UNKNOWN);

		assertNotNull(resource);
		assertEquals(resourceId, resource.getId());
	}

	@Test
	public void getAllResources_returnNothingOnMissingType() {
		List<Resource> resources = resourceService.getAllResources(null, null);
		assertNotNull(resources);
		assertTrue(resources.isEmpty());
	}

	@Test
	public void getAllResources_shouldReturnNonSortedResourcesIfNoSortingIsRequested() {

		when(resourceStore.getAllResourcesReadOnly(ResourceType.USER)).thenReturn(
				Arrays.asList(new EmfUser("user1"), new EmfUser("user3"), new EmfUser("user2")));

		List<Resource> resources = resourceService.getAllResources(ResourceType.USER, null);
		assertNotNull(resources);
		assertEquals(3, resources.size());

		Iterator<Resource> it = resources.iterator();
		Resource r1 = it.next();
		assertEquals("user1", r1.getName());
		Resource r2 = it.next();
		assertEquals("user3", r2.getName());
		Resource r3 = it.next();
		assertEquals("user2", r3.getName());
	}

	@Test
	public void getAllResources_shouldSortResourcesByGivenProperty() {

		String[] names = { "Aaren", "Abbie", "Zygfried", "Мирослав", "Иван", "Адрей", "Synclair", "Zya", "Byran" };

		Function<String, User> buildUser = name -> {
			User user = new EmfUser(name.toLowerCase());
			user.add(TITLE, name);
			return user;
		};
		when(resourceStore.getAllResourcesReadOnly(ResourceType.USER)).thenReturn(
				Arrays.stream(names).map(buildUser).collect(Collectors.toList()));

		when(userPreferences.getLanguage()).thenReturn("en", "bg");

		List<Resource> resources = resourceService.getAllResources(ResourceType.USER, TITLE);
		assertNotNull(resources);
		assertEquals(9, resources.size());

		String[] sortedByEn = { "Aaren", "Abbie", "Byran", "Synclair", "Zya", "Zygfried", "Адрей", "Иван", "Мирослав" };
		String[] sortedByBg = { "Aaren", "Abbie", "Byran", "Synclair", "Zya", "Zygfried", "Адрей", "Иван", "Мирослав" };

		String[] sorted = resources.stream().map(user -> user.get(TITLE)).toArray(String[]::new);
		assertArrayEquals(sortedByEn, sorted);

		resources = resourceService.getAllResources(ResourceType.USER, TITLE);
		assertNotNull(resources);
		assertEquals(9, resources.size());

		sorted = resources.stream().map(user -> user.get(TITLE)).toArray(String[]::new);
		assertArrayEquals(sortedByBg, sorted);

	}

	@Test
	public void buildGroup_shouldBuildGroupFromInstanceWithAllProperties() {
		Instance instance = new EmfInstance("emf:some-group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");
		instance.add(TITLE, "Some group");
		ClassInstance type = new ClassInstance();
		type.setCategory("group");
		instance.setType(type);

		Group group = resourceService.buildGroup(instance);
		assertNotNull(group);

		assertEquals("emf:GROUP_some-group", group.getId());
		assertEquals("GROUP_some-group", group.getName());
		assertEquals("Some group", group.get(TITLE));
		assertNotNull(group.type());
	}

	@Test
	public void createGroup_shouldSaveNewGroupIfNotExists() {
		Instance instance = new EmfInstance("emf:some-group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");
		instance.add(TITLE, "Some group");
		ClassInstance type = new ClassInstance();
		type.setCategory("group");
		instance.setType(type);

		resourceService.createGroup(instance);

		ArgumentCaptor<Resource> captor = ArgumentCaptor.forClass(Resource.class);
		verify(resourceStore).persistNewResource(captor.capture(), any());

		Group group = (Group) captor.getValue();
		assertNotNull(group);

		assertEquals("emf:GROUP_some-group", group.getId());
		assertEquals("GROUP_some-group", group.getName());
		assertEquals("Some group", group.get(TITLE));
		assertNotNull(group.type());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void createGroup_shouldFailIfGroupIfExists() {
		Instance instance = new EmfInstance("emf:some-group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");
		instance.add(TITLE, "Some group");
		ClassInstance type = new ClassInstance();
		type.setCategory("group");
		instance.setType(type);

		when(resourceStore.resourceExists("GROUP_some-group")).thenReturn(Boolean.TRUE);

		resourceService.createGroup(instance);
	}

	@Test
	public void getContainingResources_nullIdentifier_emptyCollectionAsResult() {
		List<Instance> containingResources = resourceService.getContainingResources(null);
		assertTrue(containingResources.isEmpty());
	}

	@Test
	public void getContainingResources_emptyIdentifier_emptyCollectionAsResult() {
		List<Instance> containingResources = resourceService.getContainingResources("");
		assertTrue(containingResources.isEmpty());
		verifyZeroInteractions(resourceDao);
	}

	@Test
	public void getContainingResources_ShouldReturnEmptyList_When_NoResourcesFound() {
		List<Instance> containingResources = resourceService.getContainingResources("userId");
		assertTrue(containingResources.isEmpty());
	}

	@Test
	public void getContainedResources_versionResourceId_internalLogicCalledWithNormalizedId() {
		resourceService.getContainedResources("user-v1.2");
		verify(resourceDao).getMemberIdsOf("user");
	}

	@Test
	public void deactivate_Should_DoNothing_When_NoResourceFound() {
		resourceService.deactivate("emf:some-user123", DEACTIVATE);
		verify(stateService, never()).changeState(any(Instance.class), any(Operation.class));

		resourceService.deactivate(null, DEACTIVATE);
		verify(stateService, never()).changeState(any(Instance.class), any(Operation.class));
	}

	@Test
	public void deactivate_Should_DoNothing_When_ResourceAlreadyInactive() {
		EmfGroup resource = new EmfGroup("GROUP_group1", "group1");
		resource.setId("emf:GROUP_group1");
		resource.setType(ResourceType.GROUP);
		resource.setActive(false);

		resourceService.deactivate(resource, DEACTIVATE);

		verify(stateService, never()).changeState(any(Instance.class), any(Operation.class));
	}

	@Test
	public void deactivate_Should_ChangeResourceState() {
		EmfGroup resource = new EmfGroup("GROUP_group1", "group1");
		resource.setId("emf:GROUP_group1");
		resource.setType(ResourceType.GROUP);

		when(resourceStore.findResourceById("emf:GROUP_group1")).thenReturn(resource);

		resourceService.deactivate(resource, DEACTIVATE);

		verify(stateService).changeState(resource, DEACTIVATE);
	}

	@Test
	public void activate_Should_DoNothing_When_NoResourceFound() {
		resourceService.activate("emf:some-user123", ACTIVATE);
		verify(stateService, never()).changeState(any(Instance.class), any(Operation.class));

		resourceService.activate(null, ACTIVATE);
		verify(stateService, never()).changeState(any(Instance.class), any(Operation.class));
	}

	@Test
	public void activate_Should_ChangeResourceState() {
		EmfGroup resource = new EmfGroup("GROUP_group1", "group1");
		resource.setId("emf:GROUP_group1");
		resource.setType(ResourceType.GROUP);
		resource.setActive(false);

		when(resourceStore.findResourceById("emf:GROUP_group1")).thenReturn(resource);

		resourceService.activate(resource, ACTIVATE);

		verify(stateService).changeState(resource, ACTIVATE);
	}

	@Test
	public void loadByDbId_nullIdentifier_nullResult() {
		Serializable id = null;
		assertNull(resourceService.loadByDbId(id));
		verifyZeroInteractions(resourceStore);
	}

	@Test
	public void loadByDbId_versionIdentifier_internalServiceCalledWithNormailzedIdentifier() {
		resourceService.loadByDbId("user-v1.6");
		verify(resourceStore).getResourceEntity("user", null, null);
	}

	@Test
	public void getContainedResourceIdentifiers_version_shouldCallInternalServiceWithNormalizedId() {
		EmfUser resource = new EmfUser();
		resource.setId("user-v1.1");
		resourceService.getContainedResourceIdentifiers(resource);
		verify(resourceDao).getMemberIdsOf("user");
	}
}
