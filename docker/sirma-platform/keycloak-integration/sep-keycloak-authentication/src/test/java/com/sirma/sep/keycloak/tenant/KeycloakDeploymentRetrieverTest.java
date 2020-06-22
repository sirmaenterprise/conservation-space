package com.sirma.sep.keycloak.tenant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;

/**
 * Tests for {@link KeycloakDeploymentRetriever}.
 *
 * @author smustafov
 */
public class KeycloakDeploymentRetrieverTest {

	@InjectMocks
	private KeycloakDeploymentRetriever retriever;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(keycloakConfiguration.getKeycloakAddress()).thenReturn(new ConfigurationPropertyMock<>("idp"));
	}

	@Test
	public void should_RetrieveDeployment() {
		KeycloakDeployment keycloakDeployment = retriever.getDeployment("sep.test");

		assertNotNull(keycloakDeployment);
		assertEquals("idp", keycloakDeployment.getAuthServerBaseUrl());
		assertEquals("sep.test", keycloakDeployment.getRealm());
		assertEquals(ClientProperties.SEP_BACKEND_CLIENT_ID, keycloakDeployment.getResourceName());
		assertTrue(keycloakDeployment.isBearerOnly());
		assertTrue(keycloakDeployment.isCors());
	}

}
