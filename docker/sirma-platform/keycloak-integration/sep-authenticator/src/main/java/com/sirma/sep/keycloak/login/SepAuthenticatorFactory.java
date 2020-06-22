package com.sirma.sep.keycloak.login;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Factory that provides {@link SepAuthenticator}. Produces singleton instance.
 *
 * @author smustafov
 */
public class SepAuthenticatorFactory implements AuthenticatorFactory {

	static final String AUTHENTICATOR_ID = "sep-authenticator";
	static final String DISPLAY_TYPE = "SEP username and password authenticator";
	static final String HELP_TEXT = "SEP Authenticator based on realm extracted from username";
	static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED };

	private static final SepAuthenticator AUTHENTICATOR = new SepAuthenticator();

	@Override
	public Authenticator create(KeycloakSession keycloakSession) {
		return AUTHENTICATOR;
	}

	@Override
	public String getId() {
		return AUTHENTICATOR_ID;
	}

	@Override
	public String getDisplayType() {
		return DISPLAY_TYPE;
	}

	@Override
	public String getHelpText() {
		return HELP_TEXT;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public String getReferenceCategory() {
		return UserCredentialModel.PASSWORD;
	}

	@Override
	public boolean isConfigurable() {
		return false;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return Collections.emptyList();
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
