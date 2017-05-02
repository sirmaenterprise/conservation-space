package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link DmsSubsystemAddressProvider}
 *
 * @author BBonev
 */
public class DmsSubsystemAddressProviderTest {
	private static final String CONFIG_NAME = "configName";

	@InjectMocks
	private DmsSubsystemAddressProvider addressProvider;

	@Spy
	private ConfigurationPropertyMock<Integer> maxTenantsPerHost = new ConfigurationPropertyMock<>(10);
	@Spy
	private ConfigurationPropertyMock<Set<String>> dmsAddresses = new ConfigurationPropertyMock<>(new HashSet<>());
	@Spy
	private ConfigurationPropertyMock<String> dmsProtocol = new ConfigurationPropertyMock<>();
	@Spy
	private ConfigurationPropertyMock<Map<String, Integer>> dmsPortMapping = new ConfigurationPropertyMock<>(
			new HashMap<>());
	@Mock
	private AdaptersConfiguration adaptersConfiguration;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(adaptersConfiguration.getDmsHostConfiguration()).thenReturn(CONFIG_NAME);

		dmsProtocol.setValue(null);
		// clear before use
		dmsAddresses.get().clear();
		dmsPortMapping.get().clear();
	}

	@Test
	public void resolveWithDefaultAddress() throws Exception {
		URI uri = addressProvider.provideAddressForNewTenant("localhost");
		assertNotNull(uri);
		assertEquals("localhost", uri.getHost());
		assertEquals("Expected default port of 8080", 8080, uri.getPort());
	}

	@Test
	public void resolveAddress() throws Exception {

		dmsAddresses.get().add("localhost");
		dmsPortMapping.get().put("all", 8081);
		when(configurationManagement.getAllConfigurations())
				.thenReturn(Arrays.asList(createConfig("localhost"), createConfig("localhost"), createConfig(null)));

		URI uri = addressProvider.provideAddressForNewTenant();
		assertNotNull(uri);
		assertEquals("localhost", uri.getHost());
		assertEquals(8081, uri.getPort());
	}

	static Configuration createConfig(String value) {
		return new Configuration(CONFIG_NAME, value);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void invalidAddress() throws Exception {
		dmsProtocol.setValue("3232");
		addressProvider.provideAddressForNewTenant("localhost");
	}

}
