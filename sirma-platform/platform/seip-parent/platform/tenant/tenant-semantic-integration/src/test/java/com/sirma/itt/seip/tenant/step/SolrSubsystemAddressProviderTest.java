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

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link SolrSubsystemAddressProvider}
 *
 * @author BBonev
 */
public class SolrSubsystemAddressProviderTest {
	private static final String CONFIG_NAME = "configName";

	@InjectMocks
	private SolrSubsystemAddressProvider addressProvider;

	@Spy
	private ConfigurationPropertyMock<Integer> maxSolrCoresPerHost = new ConfigurationPropertyMock<>(10);
	@Spy
	private ConfigurationPropertyMock<Set<String>> solrAddresses = new ConfigurationPropertyMock<>(new HashSet<>());
	@Spy
	private ConfigurationPropertyMock<String> solrAddressProtocol = new ConfigurationPropertyMock<>();
	@Spy
	private ConfigurationPropertyMock<Map<String, Integer>> solrPortMapping = new ConfigurationPropertyMock<>(
			new HashMap<>());
	@Mock
	private SolrConfiguration solrConfiguration;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(solrConfiguration.getSolrHostConfiguration()).thenReturn(CONFIG_NAME);

		solrAddressProtocol.setValue(null);
		// clear before use
		solrAddresses.get().clear();
		solrPortMapping.get().clear();
	}

	@Test
	public void resolveWithDefaultAddress() throws Exception {
		URI uri = addressProvider.provideAddressForNewTenant("localhost");
		assertNotNull(uri);
		assertEquals("localhost", uri.getHost());
		assertEquals("Expected default port of 8983", 8983, uri.getPort());
	}

	@Test
	public void resolveAddress() throws Exception {

		solrAddresses.get().add("localhost");
		solrPortMapping.get().put("all", 8081);
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
		solrAddressProtocol.setValue("3232");
		addressProvider.provideAddressForNewTenant("localhost");
	}

}
