package com.sirma.itt.emf.semantic.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Test for {@link SeipToSemanticResourceSynchronizationConfig}
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
	private DictionaryService dictionaryService;
	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Mock
	private InstanceService instanceService;

	private ValueFactory factory = ValueFactoryImpl.getInstance();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(registryService.buildFullUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(registryService.buildUri(anyString())).then(a -> factory.createURI(a.getArgumentAt(0, String.class)));
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("manager"));
		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(true, false, false));

		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.then(a -> new InstanceReferenceMock(a.getArgumentAt(1, Instance.class)));

		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock testField = new PropertyDefinitionMock();
		testField.setName("test");
		testField.setDmsType("cm:test");
		definition.getFields().add(testField);
		when(dictionaryService.getInstanceDefinition(any())).thenReturn(definition);
	}

	@Test
	@SuppressWarnings({ "unchecked" })
	public void testMigration() throws Exception {
		Resource system1 = buildUser("System");
		system1.add("test", "value1");
		when(resourceService.getAllResources(ResourceType.ALL, null)).thenReturn(
				Arrays.asList(buildUser("admin"), system1, buildUser("regular-user"), buildGroup("GROUP_ADMIN")));

		doAnswer(a -> {
			SearchArguments<Instance> arguments = a.getArgumentAt(1, SearchArguments.class);
			arguments.setResult(Arrays.asList(buildUser("admin"), buildUser("System"), buildUser("user")));
			return null;
		}).when(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));

		Resource system2 = buildUser("System");
		system2.add("test", "value2");
		when(dbDao.fetchWithNamed(eq(NamedQueries.SELECT_BY_IDS), anyList()))
				.thenReturn(Arrays.asList(buildUser("admin"), system2, buildUser("user")));

		SynchronizationRunner.synchronize(config);
		verify(dbDao).saveOrUpdate(any(), any());
		verify(instanceVersionService, times(2)).setInitialVersion(any(Instance.class));
		verify(dbDao, times(2)).saveOrUpdate(any());
		verify(instanceVersionService, times(2)).createVersion(any(VersionContext.class));
		verify(dbDao).delete(any(), eq(new HashSet<>(Arrays.asList("emf:user"))));

		verify(instanceService, times(4)).touchInstance(any());
	}

	@Test
	public void testSave_shouldDoNothingIfNoModel() {
		Map<Serializable, Instance> toAdd = new HashMap<>();
		toAdd.put("emf:user", buildUser("emf:user"));
		SynchronizationResult<Serializable, Instance> synchronizationResult = new SynchronizationResult<>(toAdd,
				new HashMap<>(), new HashMap<>());
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();

		when(dictionaryService.getInstanceDefinition(any())).thenReturn(null);
		doAnswer(invocation -> {
			BiConsumer consumer = invocation.getArgumentAt(0, BiConsumer.class);
			consumer.accept(invocation.getArguments()[1], invocation.getArguments()[2]);
			return null;
		}).when(transactionSupport).invokeBiConsumerInNewTx(any(BiConsumer.class), any(SynchronizationResult.class),
				any(SyncRuntimeConfiguration.class));

		config.save(synchronizationResult, runtimeConfiguration);
		verify(instanceService, times(0)).touchInstance(any(Instance.class));
	}

	@Test
	public void testSave_shouldSaveIfThereIsModel() {
		Map<Serializable, Instance> toAdd = new HashMap<>();
		toAdd.put("emf:user", buildUser("emf:user"));
		SynchronizationResult<Serializable, Instance> synchronizationResult = new SynchronizationResult<>(toAdd,
				new HashMap<>(), new HashMap<>());
		SyncRuntimeConfiguration runtimeConfiguration = new SyncRuntimeConfiguration();

		doAnswer(invocation -> {
			BiConsumer consumer = invocation.getArgumentAt(0, BiConsumer.class);
			consumer.accept(invocation.getArguments()[1], invocation.getArguments()[2]);
			return null;
		}).when(transactionSupport).invokeBiConsumerInNewTx(any(BiConsumer.class), any(SynchronizationResult.class),
				any(SyncRuntimeConfiguration.class));

		config.save(synchronizationResult, runtimeConfiguration);
		verify(instanceService).touchInstance(any(Instance.class));
	}

	private static Resource buildUser(String name) {
		EmfUser user = new EmfUser(name);
		user.setId(EMF.PREFIX + ":" + name);
		user.add(SEMANTIC_TYPE, EMF.USER);
		user.setReference(new InstanceReferenceMock(user));
		return user;
	}

	private static Resource buildGroup(String name) {
		EmfGroup group = new EmfGroup(name, name);
		group.setId(EMF.PREFIX + ":" + name);
		group.add(SEMANTIC_TYPE, EMF.GROUP);
		group.setReference(new InstanceReferenceMock(group));
		return group;
	}
}
