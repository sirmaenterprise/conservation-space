package com.sirma.itt.seip.tenant.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

/**
 * Tests for {@link BaseSubsystemTenantAddressProvider} methods.
 *
 * @author BBonev
 */
public class BaseSubsystemTenantAddressProviderTest {

	@Test
	public void testPortPersing_single() throws Exception {
		Map<String, Integer> map = BaseSubsystemTenantAddressProvider.parsePortMapping("all:8080");
		assertNotNull(map);
		assertEquals(Integer.valueOf(8080), map.get("all"));
	}

	@Test
	public void testPortPersing_multiple() throws Exception {
		Map<String, Integer> map = BaseSubsystemTenantAddressProvider.parsePortMapping("all:8080,localhost:8081");
		assertNotNull(map);
		assertEquals(Integer.valueOf(8080), map.get("all"));
		assertEquals(Integer.valueOf(8081), map.get("localhost"));
	}

	@Test
	public void getPortForHost_defaultPort() throws Exception {
		assertEquals(8080, BaseSubsystemTenantAddressProvider.getPortForHost(null, null, 8080));
		assertEquals(8080, BaseSubsystemTenantAddressProvider.getPortForHost("localhost", null, 8080));
		assertEquals(8080, BaseSubsystemTenantAddressProvider.getPortForHost(null, new HashMap<>(), 8080));
		assertEquals(8080, BaseSubsystemTenantAddressProvider.getPortForHost("localhost", new HashMap<>(), 8080));
	}

	@Test
	public void getPortForHost() throws Exception {
		Map<String, Integer> map = BaseSubsystemTenantAddressProvider.parsePortMapping("all:8082,localhost:8081");
		assertEquals(8081, BaseSubsystemTenantAddressProvider.getPortForHost("localhost", map, 8080));
		assertEquals(8082, BaseSubsystemTenantAddressProvider.getPortForHost("notInMapping", map, 8080));
	}

	@Test
	public void createUndeterminedPicker() throws Exception {
		Function<Collection<String>, String> picker = BaseSubsystemTenantAddressProvider.createUndeterminedPicker(null);
		assertNotNull(picker);
		assertEquals("test", picker.apply(Arrays.asList("test")));

		picker = BaseSubsystemTenantAddressProvider.createUndeterminedPicker("test");
		assertNotNull(picker);
		assertEquals("test", picker.apply(Arrays.asList("test")));
		assertEquals("other", picker.apply(Arrays.asList("other")));
	}
}
