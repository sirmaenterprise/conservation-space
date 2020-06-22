package com.sirma.sep.keycloak;

import org.keycloak.Config;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * JPA Entity provider factory which creates {@link SepJpaEntityProvider}.
 *
 * @author smustafov
 */
public class SepJpaEntityProviderFactory implements JpaEntityProviderFactory {

	public static final String ID = "sep-entity-provider";

	@Override
	public JpaEntityProvider create(KeycloakSession keycloakSession) {
		return new SepJpaEntityProvider();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void init(Config.Scope scope) {
		// not needed
	}

	@Override
	public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
		// not needed
	}

	@Override
	public void close() {
		// not needed
	}

}
