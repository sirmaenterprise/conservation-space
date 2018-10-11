package com.sirma.itt.seip.tenant.step;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.semantic.SemanticRepositoryProvisioning;
import com.sirma.itt.seip.tenant.semantic.TenantSemanticContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Test the tenant semantic step.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantSemanticStepTest {
	@Mock
	private SemanticRepositoryProvisioning repositoryProvisioning;

	@Mock
	private SemanticConfiguration semanticConfiguration;

	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private TenantSemanticStep step;

	@Test
	public void should_callRepositoryProvisioning() throws JSONException {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		step.execute(data, new TenantInitializationContext());
		verify(repositoryProvisioning).provision(anyMap(), anyList(), any(TenantInfo.class), any(Consumer.class));
	}

	@Test(expected = TenantCreationException.class)
	public void should_throwException_onError() {
		step.execute(new TenantStepData("id", new JSONObject()), new TenantInitializationContext());
	}

	@Test
	public void should_deleteRepository_when_deletingTenant() throws JSONException {
		TenantStepData data = new TenantStepData("id", new JSONObject("{properties:[]}"));
		mockConfigurations();

		step.delete(data, new TenantInfo("tenantId"), false);
		verify(securityContextManager).initializeTenantContext("tenantId");
		verify(securityContextManager).endContextExecution();
		verify(repositoryProvisioning).rollback(any(TenantSemanticContext.class), any(TenantInfo.class));
	}

	private void mockConfigurations() {
		ConfigurationProperty<String> repositoryNameConfig = mockConfigurationProperty("repoName");
		when(semanticConfiguration.getRepositoryName()).thenReturn(repositoryNameConfig);

		ConfigurationProperty<String> serverURLConfig = mockConfigurationProperty("serverUrl");
		when(semanticConfiguration.getServerURL()).thenReturn(serverURLConfig);
	}

	private static <T> ConfigurationProperty<T> mockConfigurationProperty(T value) {
		ConfigurationProperty<T> property = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(property.get()).thenReturn(value);
		return property;
	}
}
