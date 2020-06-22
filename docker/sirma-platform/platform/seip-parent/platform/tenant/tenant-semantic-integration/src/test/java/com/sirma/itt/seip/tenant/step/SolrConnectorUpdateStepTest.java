package com.sirma.itt.seip.tenant.step;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.seip.semantic.management.ConnectorConfiguration;
import com.sirma.seip.semantic.management.ConnectorService;


/**
 * Test {@link TenantUpdateSolrConnectorStep}
 * @author kirq4e
 *
 */
public class SolrConnectorUpdateStepTest {

	private static final String CONNECTOR_NAME = "fts_test_com";

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private ConnectorService connectorService;
	
	@Mock
	private SolrConfiguration solrConfiguration;

	@InjectMocks
	private TenantUpdateSolrConnectorStep updateStep;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		when(solrConfiguration.getSolrHostConfiguration()).thenReturn("http://localhost:8983/solr");
		when(connectorService.createConnectorName(Matchers.anyString())).thenAnswer(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void testExecuteRecreateConnector() {
		List<ConnectorConfiguration> connectors = new ArrayList<>(1);
		ConnectorConfiguration configuration = new ConnectorConfiguration(CONNECTOR_NAME);
		configuration.setRecreate(true);
		connectors.add(configuration);

		when(connectorService.listConnectors()).thenReturn(connectors);

		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(CONNECTOR_NAME));

		updateStep.execute(new TenantStepData(CONNECTOR_NAME, new JSONObject()), context);

		verify(connectorService).resetConnector(Matchers.eq(CONNECTOR_NAME));
	}
	
	@Test
	public void testExecuteNoConnectorForRecreation() {
		List<ConnectorConfiguration> connectors = new ArrayList<>(1);
		ConnectorConfiguration configuration = new ConnectorConfiguration(CONNECTOR_NAME);
		configuration.setRecreate(false);
		connectors.add(configuration);

		when(connectorService.listConnectors()).thenReturn(connectors);

		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(CONNECTOR_NAME));

		updateStep.execute(new TenantStepData(CONNECTOR_NAME, new JSONObject()), context);

		verify(connectorService, never()).resetConnector(anyString());
	}
	
	@Test
	public void testExecuteNoExsitingConnector() {
		ConnectorConfiguration configuration = new ConnectorConfiguration(CONNECTOR_NAME);
		configuration.setRecreate(false);
		
		when(connectorService.createDefaultConnectorConfiguration(Matchers.eq(CONNECTOR_NAME))).thenReturn(configuration);

		when(connectorService.listConnectors()).thenReturn(CollectionUtils.emptyList());

		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo(CONNECTOR_NAME));

		updateStep.execute(new TenantStepData(CONNECTOR_NAME, new JSONObject()), context);

		verify(connectorService).saveConnectorConfiguration(Matchers.eq(configuration));
		verify(connectorService).resetConnector(Matchers.eq(CONNECTOR_NAME));
	}
}
