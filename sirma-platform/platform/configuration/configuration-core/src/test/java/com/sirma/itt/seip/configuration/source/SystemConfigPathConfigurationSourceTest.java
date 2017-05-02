package com.sirma.itt.seip.configuration.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link SystemConfigPathConfigurationSource}
 *
 * @author BBonev
 */
public class SystemConfigPathConfigurationSourceTest {

	private File file;

	@Before
	public void beforeMethod() throws IOException {
		file = new File("./" + UUID.randomUUID() + ".properties");
		Properties properties = new Properties();
		properties.put("key1", "value1");
		properties.put("tenant.com-key2", "value2");
		properties.put("tenant.com-key3", "value3");
		properties.put("tenant.bg-key3", "value4");
		try (FileWriter writer = new FileWriter(file)) {
			properties.store(writer, "");
		}
		System.setProperty(SystemConfigPathConfigurationSource.CONFIG_PATH, file.getAbsolutePath());
	}

	@Test
	public void testReadProperty() throws Exception {
		SystemConfigPathConfigurationSource source = new SystemConfigPathConfigurationSource();
		assertEquals("value1", source.getConfigurationValue("key1"));
	}

	@Test
	public void testReadTenantProperty() throws Exception {
		SystemConfigPathConfigurationSource source = new SystemConfigPathConfigurationSource();
		assertEquals("value2", source.getConfigurationValue("key2", "tenant.com"));

		assertEquals("value1", source.getConfigurationValue("key1", SecurityContext.SYSTEM_TENANT));
		assertEquals("value1", source.getConfigurationValue("key1", SecurityContext.DEFAULT_TENANT));
		assertNull(source.getConfigurationValue("key1", "tenant.com"));
	}

	@Test
	public void testGetConfigurations() throws Exception {
		SystemConfigPathConfigurationSource source = new SystemConfigPathConfigurationSource();
		Properties properties = source.getConfigurations();
		assertNotNull(properties);
		assertEquals(4, properties.size());
	}

	@Test
	public void testGetConfigurationsForTenant() throws Exception {
		SystemConfigPathConfigurationSource source = new SystemConfigPathConfigurationSource();
		Properties properties = source.getConfigurations("tenant.com");
		assertNotNull(properties);
		assertEquals(3, properties.size());
	}

	@After
	public void afterMethod() {
		file.delete();
	}
}
