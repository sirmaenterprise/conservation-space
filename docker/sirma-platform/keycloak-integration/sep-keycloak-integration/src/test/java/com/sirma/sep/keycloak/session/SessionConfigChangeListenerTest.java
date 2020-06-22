package com.sirma.sep.keycloak.session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;

/**
 * Tests for {@link SessionConfigChangeListener}.
 *
 * @author smustafov
 */
public class SessionConfigChangeListenerTest {

	@InjectMocks
	private SessionConfigChangeListener listener;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private KeycloakClientProducer clientProducer;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_DoNothing_When_IdpNotKeycloak() {
		mockIdpConfiguration("wso");

		listener.registerListener(securityConfiguration, userPreferences, clientProducer);

		verify(userPreferences, never()).getSessionTimeoutPeriodConfig();
	}

	@Test
	public void should_UpdateConfig_When_ConfigurationChanges() {
		mockIdpConfiguration(SessionConfigChangeListener.KEYCLOAK_IDP);
		ConfigurationPropertyMock<Integer> config = mockSessionConfig(30);
		RealmResource realmResource = mockRealmResource();

		listener.registerListener(securityConfiguration, userPreferences, clientProducer);

		verify(userPreferences).getSessionTimeoutPeriodConfig();

		config.valueUpdated();
		verifyConfigUpdated(realmResource, 30 * 60);
	}

	private void mockIdpConfiguration(String idp) {
		when(securityConfiguration.getIdpProviderName()).thenReturn(new ConfigurationPropertyMock<>(idp));
	}

	private ConfigurationPropertyMock<Integer> mockSessionConfig(int minutes) {
		ConfigurationPropertyMock<Integer> config = new ConfigurationPropertyMock<>(minutes);
		when(userPreferences.getSessionTimeoutPeriodConfig()).thenReturn(config);
		return config;
	}

	private RealmResource mockRealmResource() {
		RealmResource realmResource = mock(RealmResource.class);
		when(realmResource.toRepresentation()).thenReturn(new RealmRepresentation());
		when(clientProducer.produceRealmResource()).thenReturn(realmResource);
		return realmResource;
	}

	private void verifyConfigUpdated(RealmResource realmResource, int expected) {
		ArgumentCaptor<RealmRepresentation> argumentCaptor = ArgumentCaptor.forClass(RealmRepresentation.class);
		verify(realmResource).update(argumentCaptor.capture());
		assertEquals(expected, argumentCaptor.getValue().getSsoSessionIdleTimeout().intValue());
	}

}
