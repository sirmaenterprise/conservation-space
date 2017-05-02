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

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Tests for {@link GraphDbSubsystemAddressProvider}
 *
 * @author BBonev
 */
public class GraphDbSubsystemAddressProviderTest {
	private static final String CONFIG_NAME = "configName";

	@InjectMocks
	private GraphDbSubsystemAddressProvider addressProvider;

	@Spy
	private ConfigurationPropertyMock<Integer> maxReposPerHost = new ConfigurationPropertyMock<>(10);
	@Spy
	private ConfigurationPropertyMock<Set<String>> gdbAddresses = new ConfigurationPropertyMock<>(new HashSet<>());
	@Spy
	private ConfigurationPropertyMock<String> gdbProtocol = new ConfigurationPropertyMock<>();
	@Spy
	private ConfigurationPropertyMock<Map<String, Integer>> gdbPortMapping = new ConfigurationPropertyMock<>(
			new HashMap<>());
	@Spy
	private ConfigurationPropertyMock<String> gdbApplicationName = new ConfigurationPropertyMock<>("");

	@Mock
	private SemanticConfiguration semanticConfiguration;
	@Mock
	private ConfigurationManagement configurationManagement;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(semanticConfiguration.getServerURLConfiguration()).thenReturn(CONFIG_NAME);
		// reset configurations before use
		gdbAddresses.get().clear();
		gdbPortMapping.get().clear();
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

		gdbAddresses.get().add("localhost");
		gdbPortMapping.get().put("all", 8081);
		when(configurationManagement.getAllConfigurations())
				.thenReturn(Arrays.asList(createConfig("http://localhost:8080/test"),
						createConfig("http://localhost:8080/test"), createConfig(null)));

		URI uri = addressProvider.provideAddressForNewTenant();
		assertNotNull(uri);
		assertEquals("localhost", uri.getHost());
		assertEquals(8081, uri.getPort());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void invalidAddress() throws Exception {
		gdbProtocol.setValue("3232");
		addressProvider.provideAddressForNewTenant("localhost");
	}

	static Configuration createConfig(String value) {
		return new Configuration(CONFIG_NAME, value);
	}
}
