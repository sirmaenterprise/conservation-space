package com.sirma.itt.seip.configuration.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationSource;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link MultiTenantRawConfigurationProvider}
 *
 * @author BBonev
 */
public class MultiTenantRawConfigurationProviderTest {

	private static final String TENANT = "tenant.com";

	@InjectMocks
	private MultiTenantRawConfigurationProvider configurationProvider;

	@Mock
	private SecurityContext securityContext;
	@Mock
	private ConfigurationSource source1;
	@Mock
	private ConfigurationSource source2;
	@Spy
	private List<ConfigurationSource> sources = new ArrayList<>();
	@Mock
	private ConfigurationInstance config1;
	@Mock
	private ConfigurationInstance config2;
	@Mock
	private ConfigurationInstanceProvider configurationInstanceProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		sources.clear();
		sources.add(source1);
		sources.add(source2);

		when(source1.getConfigurationValue("config1")).thenReturn("config_value_1");
		when(source2.getConfigurationValue("config2", TENANT)).thenReturn("config_value_2");

		when(configurationInstanceProvider.getConfiguration("config1")).thenReturn(config1);
		when(configurationInstanceProvider.getConfiguration("config2")).thenReturn(config2);
	}

	@Test
	public void testGetConfiguration_noSuchConfig() throws Exception {
		assertNull(configurationProvider.getRawConfigurationValue("config3"));
		when(configurationInstanceProvider.getConfiguration("config4")).thenReturn(mock(ConfigurationInstance.class));
		assertNull(configurationProvider.getRawConfigurationValue("config4"));
	}

	@Test
	public void testGetConfiguration_systemtenant() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);

		String value = configurationProvider.getRawConfigurationValue("config1");
		assertEquals("config_value_1", value);

		verify(source1, never()).getConfigurationValue(anyString(), any());
		verify(source2, never()).getConfigurationValue(anyString(), any());
	}

	@Test
	public void testGetConfiguration_not_active_context() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.FALSE);

		String value = configurationProvider.getRawConfigurationValue("config1");
		assertEquals("config_value_1", value);

		verify(source1, never()).getConfigurationValue(anyString(), any());
		verify(source2, never()).getConfigurationValue(anyString(), any());
	}

	@Test
	public void testGetConfiguration_tenant_noTennatId() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);

		String value = configurationProvider.getRawConfigurationValue("config1");
		assertEquals("config_value_1", value);

		verify(source1, never()).getConfigurationValue(anyString(), any());
		verify(source2, never()).getConfigurationValue(anyString(), any());
	}

	@Test
	public void testGetConfiguration_tenant() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT);

		String value = configurationProvider.getRawConfigurationValue("config2");
		assertEquals("config_value_2", value);

		verify(source1, never()).getConfigurationValue(anyString());
		verify(source2, never()).getConfigurationValue(anyString());
	}

	@Test
	public void testGetConfiguration_tenant_systemConfig() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT);

		when(config1.isSystemConfiguration()).thenReturn(Boolean.TRUE);

		String value = configurationProvider.getRawConfigurationValue("config1");
		assertEquals("config_value_1", value);

		verify(source1, never()).getConfigurationValue(anyString(), any());
		verify(source2, never()).getConfigurationValue(anyString(), any());
	}

	@Test
	public void testGetConfigurationKeys() throws Exception {

		Properties prop1 = new Properties();
		prop1.put("config1", "");
		when(source1.getConfigurations()).thenReturn(prop1);

		Properties prop2 = new Properties();
		prop2.put("config2", "");
		when(source2.getConfigurations()).thenReturn(prop2);

		Set<String> value = configurationProvider.getAvailableConfigurations();
		assertNotNull(value);
		assertEquals(2, value.size());
		assertTrue(value.contains("config1"));
		assertTrue(value.contains("config2"));
	}
}
