package com.sirma.itt.seip.configuration.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * The Class ConfigurationManagementImplTest.
 *
 * @author BBonev
 */
public class ConfigurationManagementImplTest {
	@InjectMocks
	private ConfigurationManagementImpl management;

	@Mock
	private ConfigurationDao dao;
	@Mock
	private ConfigurationInstanceProvider instanceProvider;
	@Mock
	private ConfigurationProvider configurationProvider;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private DatabaseIdManager databaseIdManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_testAddConfigurations_allNew() {
		Collection<Configuration> configurations = Arrays.asList(new Configuration("key1", "value1", "tenant1"),
				new Configuration("key2", "value2", "tenant2"), new Configuration("key3", "value3", "tenant1"),
				new Configuration("key4", "value4", "tenant2"),
				new Configuration("key5", "value5", SecurityContext.SYSTEM_TENANT));

		when(dao.getTenantConfigurations(anyCollection(), anyString())).thenReturn(Collections.emptyList());

		when(instanceProvider.getRegisteredConfigurations())
				.thenReturn(new HashSet<>(Arrays.asList("key1", "key2", "key3", "key4", "key5")));

		Collection<Configuration> added = management.addConfigurations(configurations);
		assertNotNull(added);
		assertEquals(5, added.size());

		List<String> list = added.stream().map(c -> c.getConfigurationKey()).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("key1", "key2", "key3", "key4", "key5"), list);

		verify(dao, times(5)).persist(any());
	}

	@Test
	public void test_testAddConfigurations_notDefined() {
		Collection<Configuration> configurations = Arrays.asList(new Configuration("key1", "value1", "tenant1"),
				new Configuration("key2", "value2", "tenant2"), new Configuration("key3", "value3", "tenant1"),
				new Configuration("key4", "value4", "tenant2"),
				new Configuration("key5", "value5", SecurityContext.SYSTEM_TENANT));

		when(dao.getTenantConfigurations(anyCollection(), anyString())).thenReturn(Collections.emptyList());

		when(instanceProvider.getRegisteredConfigurations())
				.thenReturn(new HashSet<>(Arrays.asList("key1", "key2", "key3")));

		Collection<Configuration> added = management.addConfigurations(configurations);
		assertNotNull(added);
		assertEquals(5, added.size());

		List<String> list = added.stream().map(c -> c.getConfigurationKey()).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("key1", "key2", "key3", "key4", "key5"), list);

		verify(dao, times(3)).persist(any());
	}

	@Test
	public void test_testAddConfigurations_existing() {
		Collection<Configuration> configurations = Arrays.asList(new Configuration("key1", "value1", "tenant1"),
				new Configuration("key2", "value2", "tenant2"), new Configuration("key3", "value3", "tenant1"),
				new Configuration("key4", "value4", "tenant2"), new Configuration("key5", "value5", null));

		ConfigurationEntity entity1 = new ConfigurationEntity();
		entity1.setId(new ConfigurationId("key2", "tenant2"));
		ConfigurationEntity entity2 = new ConfigurationEntity();
		entity2.setId(new ConfigurationId("key4", "tenant2"));
		entity2.setValue("test");

		when(dao.getTenantConfigurations(anyCollection(), anyString())).thenReturn(Arrays.asList(entity1, entity2));

		when(instanceProvider.getRegisteredConfigurations())
				.thenReturn(new HashSet<>(Arrays.asList("key1", "key2", "key3", "key4", "key5")));

		Collection<Configuration> added = management.addConfigurations(configurations);
		assertNotNull(added);
		assertEquals(4, added.size());

		List<String> list = added.stream().map(c -> c.getConfigurationKey()).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("key1", "key2", "key3", "key5"), list);

		verify(dao, times(4)).persist(any());
	}

	@Test
	public void systemConfigCleanup() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn(SecurityContext.SYSTEM_TENANT);
		when(dao.getSystemConfigurations()).thenReturn(Arrays.asList(create("key1"), create("key2")));
		when(instanceProvider.getRegisteredConfigurations()).thenReturn(new HashSet<>(Arrays.asList("key1", "key4")));

		management.cleanupDeletedSystemConfigurations();
		verify(dao).deleteConfiguration("key2", SecurityContext.SYSTEM_TENANT);
	}

	@Test
	public void configCleanup() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1");
		when(dao.getConfigurationsByTenant("tenant1"))
				.thenReturn(Arrays.asList(create("key1", "tenant1"), create("key2", "tenant1")));
		when(instanceProvider.getRegisteredConfigurations()).thenReturn(new HashSet<>(Arrays.asList("key1", "key4")));

		management.cleanupDeletedConfigurations();
		verify(dao).deleteConfiguration("key2", "tenant1");
	}

	@Test
	public void rawValueAssignment() {
		ConfigurationEntity entity1 = create("key1", "tenant1" ,"value1");
		when(dao.getAllEntities()).thenReturn(Arrays.asList(entity1));
		when(instanceProvider.getRegisteredConfigurations()).thenReturn(new HashSet<>(Arrays.asList("key1")));

		Collection<Configuration> configurations = management.getAllConfigurations();
		assertEquals(1, configurations.size());

		Configuration configuration = configurations.iterator().next();
		assertEquals("value1", configuration.getRawValue());
	}
	
	@Test
	public void should_callConfigurationDeletion() {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1");
		management.removeAllConfigurations();
		verify(dao).deleteTenantConfigurations("tenant1");
	}

	private static ConfigurationEntity create(String key) {
		return create(key, SecurityContext.SYSTEM_TENANT);
	}

	private static ConfigurationEntity create(String key, String tenant) {
		return create(key, tenant, null);
	}

	private static ConfigurationEntity create(String key, String tenant, String value) {
		ConfigurationEntity entity = new ConfigurationEntity();
		entity.setId(new ConfigurationId(key, tenant));
		entity.setValue(value);
		return entity;
	}
}
