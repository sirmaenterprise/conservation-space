package com.sirma.itt.emf.semantic.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link SeipToSemanticResourceSynchronizationConfig}.
 *
 * @author BBonev
 */
public class SeipToSemanticResourceSynchronizationConfigTest {

	@InjectMocks
	private SeipToSemanticResourceSynchronizationConfig config;

	@Mock
	private ResourceService resourceService;
	@Mock
	private DbDao dbDao;
	@Mock
	private SearchService searchService;
	@Mock
	private NamespaceRegistryService registryService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private PermissionService permissionService;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private InstanceService instanceService;

	private ValueFactory factory = SimpleValueFactory.getInstance();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(registryService.buildFullUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(registryService.buildUri(anyString())).then(a -> factory.createIRI(a.getArgumentAt(0, String.class)));
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("manager"));
		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(true, false, false));

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));

		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock testField = new PropertyDefinitionMock();
		testField.setName("test");
		testField.setDmsType("cm:test");
		testField.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		definition.getFields().add(testField);
		PropertyDefinitionMock uriField = new PropertyDefinitionMock();
		uriField.setName("testUri");
		uriField.setDmsType("cm:testUri");
		uriField.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		definition.getFields().add(uriField);
		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);

		when(resourceService.getAllOtherUsers()).thenReturn(buildGroup("allOtherUsers"));
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void testMigration() throws Exception {
		Resource system1 = buildUser("System");
		system1.add("test", "value1");
		when(resourceService.getAllResources(ResourceType.ALL, null)).thenReturn(
				Arrays.asList(buildUser("admin"), system1, buildUser("regular-user"), buildGroup("GROUP_ADMIN")));

		when(searchService.stream(any(SearchArguments.class), any(ResultItemTransformer.SingleValueTransformer.class)))
				.then(a -> Stream.of("admin", "System", "user"));

		Resource system2 = buildUser("System");
		system2.add("test", "value2");
		when(dbDao.fetchWithNamed(eq(NamedQueries.SELECT_BY_IDS), anyList()))
				.thenReturn(Arrays.asList(buildUser("admin"), system2, buildUser("user")));

		SynchronizationRunner.synchronize(config);
		verify(domainInstanceService, times(3)).save(any(InstanceSaveContext.class));

		verify(dbDao).delete(any(), eq(new HashSet<>(Arrays.asList("emf:user"))));
		verify(instanceService, times(1)).touchInstance(any());

		doAnswer(invocation -> {
			BiConsumer<Object, Object> consumer = invocation.getArgumentAt(0, BiConsumer.class);
			consumer.accept(invocation.getArguments()[1], invocation.getArguments()[2]);
			return null;
		}).when(transactionSupport).invokeBiConsumerInNewTx(any(BiConsumer.class), any(SynchronizationResult.class),
				any(SyncRuntimeConfiguration.class));
	}

	@Test
	public void testSave_shouldDoNothingIfNoModel() {
		Map<Serializable, Instance> toAdd = new HashMap<>();
		toAdd.put("emf:user", buildUser("emf:user"));
		SynchronizationResult<Serializable, Instance> synchronizationResult = new SynchronizationResult<>(toAdd,
				new HashMap<>(), new HashMap<>());
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();

		when(definitionService.getInstanceDefinition(any())).thenReturn(null);

		config.save(synchronizationResult, runtimeConfiguration);
		verify(instanceService, times(0)).touchInstance(any(Instance.class));
	}

	@Test
	public void testSave_shouldSaveIfThereIsModel() {
		Map<Serializable, Instance> toAdd = new HashMap<>();
		toAdd.put("emf:user", buildUser("user"));
		SynchronizationResult<Serializable, Instance> synchronizationResult = new SynchronizationResult<>(toAdd,
				new HashMap<>(), new HashMap<>());
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();

		config.save(synchronizationResult, runtimeConfiguration);
		verify(domainInstanceService).save(argThat(buildInstanceSaveContextMatcher("emf:user")));
	}

	private static CustomMatcher<InstanceSaveContext> buildInstanceSaveContextMatcher(String userId) {
		return CustomMatcher.of((InstanceSaveContext context) -> {
			assertNotNull(context.getInstance());
			assertEquals(userId, context.getInstanceId());
			assertNotNull(context.getOperation());
			assertNotNull(context.getVersionCreationDate());
			assertNotNull(context.getDisableValidationReason());
			assertFalse(context.isValidationEnabled());
			assertNotNull(context.getVersionContext());
			assertFalse(context.getVersionContext().isObjectPropertiesVersioningEnabled());
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_AssignManagerPermissionsToUser_When_UserIsNewlyCreated() {
		Map<Serializable, Instance> toAdd = new HashMap<>();
		toAdd.put("emf:user", buildUser("user"));

		SynchronizationResult<Serializable, Instance> synchronizationResult = buildSyncResult(toAdd, new HashMap<>(),
				new HashMap<>());
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();
		runtimeConfiguration.enableForceSynchronization();

		ArgumentCaptor<List<PermissionsChange>> permissionArgCaptor = ArgumentCaptor.forClass(List.class);

		config.save(synchronizationResult, runtimeConfiguration);

		verify(permissionService).setPermissions(any(), permissionArgCaptor.capture());
		List<PermissionsChange> permissions = permissionArgCaptor.getValue();

		assertTrue(hasPermissions(permissions, "emf:user", SecurityModel.BaseRoles.MANAGER.getIdentifier()));
		assertTrue(hasPermissions(permissions, "emf:allOtherUsers", SecurityModel.BaseRoles.CONSUMER.getIdentifier()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_AssignConsumerPermissionsToAllOther_When_UserIsModified() {
		Map<Serializable, Instance> modified = new HashMap<>();
		modified.put("emf:user", new ObjectInstance());

		SynchronizationResult<Serializable, Instance> synchronizationResult = buildSyncResult(new HashMap<>(),
				new HashMap<>(), modified);
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();
		runtimeConfiguration.enableForceSynchronization();

		ArgumentCaptor<List<PermissionsChange>> permissionArgCaptor = ArgumentCaptor.forClass(List.class);

		config.save(synchronizationResult, runtimeConfiguration);

		verify(permissionService).setPermissions(any(), permissionArgCaptor.capture());
		List<PermissionsChange> permissions = permissionArgCaptor.getValue();

		assertFalse(hasPermissions(permissions, "emf:user", SecurityModel.BaseRoles.MANAGER.getIdentifier()));
		assertTrue(hasPermissions(permissions, "emf:allOtherUsers", SecurityModel.BaseRoles.CONSUMER.getIdentifier()));
	}

	@Test
	public void merge_Should_UpdateProperty_When_NewPropertyIsDefinedAndDifferent() {
		Resource oldValue = buildUser("emf:user");
		oldValue.add(STATUS, "INIT");
		Resource newValue = buildUser("emf:user");
		newValue.add(STATUS, "ACTIVE");

		Instance merged = config.merge(oldValue, newValue);
		assertEquals("ACTIVE", merged.get(STATUS));
	}

	@Test
	public void merge_Should_NotModifyProperty_When_NewPropertyIsNull() {
		Resource oldValue = buildUser("emf:user");
		oldValue.add(STATUS, "ACTIVE");
		Resource newValue = buildUser("emf:user");

		Instance merged = config.merge(oldValue, newValue);
		assertEquals("ACTIVE", merged.get(STATUS));
	}

	@Test
	public void merge_Should_SetProperty_When_OldValueNull_And_NewValueNotNull() {
		Resource oldValue = buildUser("emf:user");
		Resource newValue = buildUser("emf:user");
		newValue.add(STATUS, "ACTIVE");

		Instance merged = config.merge(oldValue, newValue);
		assertEquals("ACTIVE", merged.get(STATUS));
	}

	@Test
	public void merge_Should_NotModifyProperties_When_Old_And_NewValuesAreSame() {
		Resource oldValue = buildUser("emf:user");
		oldValue.add(STATUS, "ACTIVE");
		Resource newValue = buildUser("emf:user");
		newValue.add(STATUS, "ACTIVE");

		Instance merged = config.merge(oldValue, newValue);
		assertEquals("ACTIVE", merged.get(STATUS));
	}

	private static boolean hasPermissions(List<PermissionsChange> permissions, String authority, String role) {
		List<AddRoleAssignmentChange> changes = permissions
				.stream()
					.map(change -> (AddRoleAssignmentChange) change)
					.collect(Collectors.toList());

		for (AddRoleAssignmentChange change : changes) {
			if (change.getAuthority().equals(authority) && change.getRole().equals(role)) {
				return true;
			}
		}
		return false;
	}

	private static SynchronizationResult<Serializable, Instance> buildSyncResult(Map<Serializable, Instance> toAdd,
			Map<Serializable, Instance> toRemove, Map<Serializable, Instance> modified) {
		return new SynchronizationResult<>(toAdd, toRemove, modified);
	}

	private static Resource buildUser(String name) {
		EmfUser user = new EmfUser(name);
		user.setId(EMF.PREFIX + ":" + name);
		user.add(SEMANTIC_TYPE, EMF.USER);
		InstanceReferenceMock.createGeneric(user);
		return user;
	}

	private static Resource buildGroup(String name) {
		EmfGroup group = new EmfGroup(name, name);
		group.setId(EMF.PREFIX + ":" + name);
		group.add(SEMANTIC_TYPE, EMF.GROUP);
		InstanceReferenceMock.createGeneric(group);
		return group;
	}
}
