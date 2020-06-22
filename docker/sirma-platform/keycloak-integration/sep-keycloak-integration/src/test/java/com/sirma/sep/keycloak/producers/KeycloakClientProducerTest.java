package com.sirma.sep.keycloak.producers;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;

/**
 * Tests for {@link KeycloakClientProducer}.
 *
 * @author smustafov
 */
public class KeycloakClientProducerTest {

	@InjectMocks
	private KeycloakClientProducer producer;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		mockConfigurations();
	}

	@Test
	public void produceClient_Should_ProduceClientForNonSystemTenant() {
		withSecurityContext("sep.test");

		Keycloak keycloak = producer.produceClient();

		assertNotNull(keycloak);
		verify(securityConfiguration, never()).getSystemAdminUsername();
		verify(keycloakConfiguration, never()).getKeycloakSystemTenantId();
	}

	@Test
	public void produceClient_Should_ProduceClientForSystemTenant() {
		withSecurityContext("system.tenant");

		Keycloak keycloak = producer.produceClient();

		assertNotNull(keycloak);
		verify(securityConfiguration).getSystemAdminUsername();
		verify(keycloakConfiguration).getKeycloakSystemTenantId();
	}

	private void withSecurityContext(String tenantId) {
		when(securityContext.getCurrentTenantId()).thenReturn(tenantId);
	}

	private void mockConfigurations() {
		when(keycloakConfiguration.getKeycloakAddress())
				.thenReturn(new ConfigurationPropertyMock<>("http://idp:8090/auth"));
		when(keycloakConfiguration.getKeycloakSystemTenantId()).thenReturn(new ConfigurationPropertyMock<>("master"));
		when(keycloakConfiguration.getKeycloakAdminClientId()).thenReturn(new ConfigurationPropertyMock<>("admin-cli"));

		when(securityConfiguration.getAdminUserName()).thenReturn(new ConfigurationPropertyMock<>("admin"));
		when(securityConfiguration.getAdminUserPassword()).thenReturn(new ConfigurationPropertyMock<>("123456"));
		when(securityConfiguration.getSystemAdminUsername()).thenReturn("systemadmin");
	}

}
