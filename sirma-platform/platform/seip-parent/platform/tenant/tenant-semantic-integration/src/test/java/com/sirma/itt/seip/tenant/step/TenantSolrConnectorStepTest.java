package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.semantic.SolrConnectorProvisioning;
import com.sirma.itt.seip.tenant.semantic.TenantSemanticContext;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Tests for {@link TenantSolrConnectorStep}
 *
 * @author BBonev
 */
public class TenantSolrConnectorStepTest {

	@InjectMocks
	private TenantSolrConnectorStep step;

	@Mock
	private SemanticConfiguration configurations;
	
	@Mock
	private SolrConnectorProvisioning connectorProvisioning;

	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		mockConfigurations();
	}

	@Test
	public void createSolrConnector() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());
		assertTrue(step.execute(data, context));

		verify(connectorProvisioning).provisionSolrConnector(anyMap(), any(TenantSemanticContext.class),
				any(TenantInfo.class));
	}

	@Test(expected = TenantCreationException.class)
	public void createSolrConnector_withError() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());

		doThrow(TenantCreationException.class).when(connectorProvisioning).provisionSolrConnector(anyMap(),
				any(TenantSemanticContext.class), any(TenantInfo.class));

		step.execute(data, context);

	}

	@Test
	public void rollbackSolrConnector() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("tenant.com");
		context.setTenantInfo(info);
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());
		assertTrue(step.delete(data, new TenantDeletionContext(info, true)));

		verify(connectorProvisioning).rollbackSolrConnector(any(TenantInfo.class));
	}

	private void mockConfigurations() {
		Mockito.when(configurations.getServerURL()).thenReturn(new ConfigurationPropertyMock<>("url"));
		Mockito.when(configurations.getFtsIndexName()).thenReturn(new ConfigurationPropertyMock<>("fts"));
		Mockito.when(configurations.getRepositoryName()).thenReturn(new ConfigurationPropertyMock<>("repo"));
	}
}