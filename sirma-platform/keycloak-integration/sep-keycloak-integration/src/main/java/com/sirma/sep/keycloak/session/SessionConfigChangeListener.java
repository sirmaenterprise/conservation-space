package com.sirma.sep.keycloak.session;

import java.lang.invoke.MethodHandles;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;

/**
 * Listens for session configuration change and updates it in Keycloak.
 *
 * @author smustafov
 */
public class SessionConfigChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String KEYCLOAK_IDP = "keycloak";

	/**
	 * Registers listener for the session configuration change on server startup per tenant.
	 *
	 * @param securityConfiguration the tenant security configuration
	 * @param userPreferences       the tenant user preferences
	 * @param clientProducer        keycloak client producer
	 */
	@RunAsAllTenantAdmins
	@Startup(phase = StartupPhase.AFTER_APP_START)
	public void registerListener(SecurityConfiguration securityConfiguration, UserPreferences userPreferences,
			KeycloakClientProducer clientProducer) {
		if (KEYCLOAK_IDP.equals(securityConfiguration.getIdpProviderName().get())) {
			userPreferences.getSessionTimeoutPeriodConfig().addConfigurationChangeListener(
					config -> updateSessionConfig(config.get(), clientProducer.produceRealmResource()));
		}
	}

	public static void updateSessionConfig(Integer sessionTimeout, RealmResource realmResource) {
		RealmRepresentation realmRepresentation = realmResource.toRepresentation();
		realmRepresentation.setSsoSessionIdleTimeout(convertToSeconds(sessionTimeout));
		realmResource.update(realmRepresentation);
		LOGGER.info("Successfully updated session idle timeout to {} seconds",
				realmRepresentation.getSsoSessionIdleTimeout());
	}

	private static int convertToSeconds(int minutes) {
		return minutes * 60;
	}

}
