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
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.semantic.SemanticRepositoryProvisioning;
import com.sirma.itt.seip.tenant.semantic.TenantSemanticContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Tests for {@link TenantCreationSolrConnectorStep}
 *
 * @author BBonev
 */
public class TenantCreationSolrConnectorStepTest {

	@InjectMocks
	private TenantCreationSolrConnectorStep step;

	@Mock
	private SemanticRepositoryProvisioning repositoryProvisioning;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createSolrConnector() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());
		assertTrue(step.execute(data, context));

		verify(repositoryProvisioning).provisionSolrConnector(anyMap(), any(TenantSemanticContext.class),
				any(TenantInfo.class));
	}

	@Test(expected = TenantCreationException.class)
	public void createSolrConnector_withError() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());

		doThrow(TenantCreationException.class).when(repositoryProvisioning).provisionSolrConnector(anyMap(),
				any(TenantSemanticContext.class), any(TenantInfo.class));

		step.execute(data, context);

	}

	@Test
	public void rollbackSolrConnector() throws Exception {
		TenantInitializationContext context = new TenantInitializationContext();
		context.setTenantInfo(new TenantInfo("tenant.com"));
		TenantStepData data = TenantStepData.createEmpty(step.getIdentifier());
		assertTrue(step.rollback(data, context));

		verify(repositoryProvisioning).rollbackSolrConnector(any(TenantSemanticContext.class), any(TenantInfo.class));
	}

}
