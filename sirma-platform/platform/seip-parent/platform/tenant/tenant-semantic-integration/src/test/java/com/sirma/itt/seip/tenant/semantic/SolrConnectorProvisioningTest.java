package com.sirma.itt.seip.tenant.semantic;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils;
import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.seip.semantic.management.ConnectorConfiguration;
import com.sirma.seip.semantic.management.ConnectorService;

/**
 * @author kirq4e
 */
public class SolrConnectorProvisioningTest {

	@InjectMocks
	private SolrConnectorProvisioning connectorProvisioning;

	@Mock
	private SolrConfiguration solrConfiguration;

	@Mock
	private ConnectorService connectorService;

	@Mock
	private SubsystemTenantAddressProvider connectorProviderMock;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private SemanticConfiguration semanticConfiguration;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Spy
	private ConfigurationPropertyMock<SubsystemTenantAddressProvider> connectorProvider = new ConfigurationPropertyMock<>();

	private TenantInfo tenantInfo = new TenantInfo("tenant.com");
	private TenantInfo defaultTenant = new TenantInfo(SecurityContext.DEFAULT_TENANT);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		connectorProvider.setValue(connectorProviderMock);

		when(configurationManagement.addConfigurations(anyCollection()))
				.then(a -> a.getArgumentAt(0, Collection.class));

		when(connectorProviderMock.provideAddressForNewTenant(any(String.class))).thenReturn(URI.create("localhost"));
		when(connectorService.createConnectorName(Matchers.anyString())).thenAnswer(a -> "fts_" + RepositoryUtils.escapeRepositoryName(a.getArgumentAt(0, String.class)));
	}

	@Test
	public void createSolrConnector() throws Exception {

		when(semanticConfiguration.getFtsIndexName()).thenReturn(new ConfigurationPropertyMock<>());

		TenantSemanticContext semanticContext = new TenantSemanticContext();
		semanticContext.setRepoName("test_repo");
		semanticContext.setSemanticAddress(URI.create("localhost"));

		when(connectorService.listConnectors()).thenReturn(new ArrayList<>());
		when(connectorService.isConnectorPresent(anyString())).thenReturn(Boolean.FALSE);
		when(connectorService.createDefaultConnectorConfiguration(Matchers.anyString()))
				.thenAnswer(a -> new ConnectorConfiguration(a.getArgumentAt(0, String.class)));

		connectorProvisioning.provisionSolrConnector(new HashMap<>(), semanticContext, tenantInfo);

		assertEquals(semanticContext.getSolrCoreName(), "fts_tenant_com");
		verify(connectorService).createConnector(any(ConnectorConfiguration.class));
	}

	@Test
	public void createSolrConnector_alreadyExists() throws Exception {

		when(semanticConfiguration.getFtsIndexName()).thenReturn(new ConfigurationPropertyMock<>());
		when(connectorService.isConnectorPresent(anyString())).thenReturn(Boolean.TRUE);

		TenantSemanticContext semanticContext = new TenantSemanticContext();
		semanticContext.setRepoName("test_repo");
		semanticContext.setSemanticAddress(URI.create("localhost"));
		
		when(connectorService.listConnectors()).thenReturn(new ArrayList<>());
		when(connectorService.isConnectorPresent(anyString())).thenReturn(Boolean.TRUE);
		when(connectorService.createDefaultConnectorConfiguration(Matchers.anyString()))
				.thenAnswer(a -> new ConnectorConfiguration(a.getArgumentAt(0, String.class)));
		
		connectorProvisioning.provisionSolrConnector(new HashMap<>(), semanticContext, tenantInfo);
		
		assertEquals(semanticContext.getSolrCoreName(), "fts_tenant_com");
		verify(connectorService).createConnector(any(ConnectorConfiguration.class));
		verify(connectorService, Mockito.atLeastOnce()).deleteConnector(Matchers.matches("fts_tenant_com"));
	}

	@Test()
	public void createSolrConnector_defaultTenant() throws Exception {

		when(semanticConfiguration.getFtsIndexName()).thenReturn(new ConfigurationPropertyMock<>());

		TenantSemanticContext semanticContext = new TenantSemanticContext();
		semanticContext.setRepoName("test_repo");
		semanticContext.setSemanticAddress(URI.create("localhost"));
		connectorProvisioning.provisionSolrConnector(new HashMap<>(), semanticContext, defaultTenant);

		verify(connectorService, never()).createConnector(any(ConnectorConfiguration.class));
	}

	@Test
	public void rollbackSolrConnector() throws Exception {

		when(semanticConfiguration.getFtsIndexName())
				.thenReturn(new ConfigurationPropertyMock<>("solr:fts_tenant_com"));

		connectorProvisioning.rollbackSolrConnector(tenantInfo);

		verify(connectorService).deleteConnector(eq("fts_tenant_com"));
	}

}
