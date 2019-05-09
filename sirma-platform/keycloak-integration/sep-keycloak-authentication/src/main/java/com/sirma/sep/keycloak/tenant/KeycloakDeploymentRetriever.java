package com.sirma.sep.keycloak.tenant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;

/**
 * Builds {@link KeycloakDeployment}s for tenants, which contain authentication client info for the backend. The
 * deployments are cached.
 *
 * @author smustafov
 */
@ApplicationScoped
public class KeycloakDeploymentRetriever {

	private Map<String, KeycloakDeployment> deploymentsCache = new ConcurrentHashMap<>();

	@Inject
	private KeycloakConfiguration keycloakConfiguration;

	/**
	 * Builds and returns keycloak deployment for the given tenant. The deployments are cached.
	 *
	 * @param tenant the tenant for which to get keycloak deployment
	 * @return the keycloak deployment needed for adapter authentication
	 */
	public KeycloakDeployment getDeployment(String tenant) {
		return deploymentsCache.computeIfAbsent(tenant, t -> {
			AdapterConfig adapterConfig = new AdapterConfig();
			adapterConfig.setAuthServerUrl(keycloakConfiguration.getKeycloakAddress().get());
			adapterConfig.setRealm(tenant);
			adapterConfig.setResource(ClientProperties.SEP_BACKEND_CLIENT_ID);
			adapterConfig.setBearerOnly(true);
			adapterConfig.setCors(true);
			return KeycloakDeploymentBuilder.build(adapterConfig);
		});
	}

}
