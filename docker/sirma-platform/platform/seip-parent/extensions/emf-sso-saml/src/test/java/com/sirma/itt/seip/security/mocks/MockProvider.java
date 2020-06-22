package com.sirma.itt.seip.security.mocks;

import java.net.URI;

import org.mockito.Mockito;

import com.sirma.itt.emf.authentication.sso.saml.SAMLMessageProcessor;
import com.sirma.itt.emf.authentication.sso.saml.authenticator.SystemUserAuthenticator;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class MockProvider.
 *
 * @author bbanchev
 */
public class MockProvider {

	private MockProvider() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Provide system authenticator.
	 *
	 * @return the system user authenticator
	 */
	public static SystemUserAuthenticator provideSystemAuthenticator() {
		ConfigurationProperty<String> configurationPropertyMock = new ConfigurationPropertyMock<>("test:8080");
		ConfigurationProperty<URI> systemAccessUrl = new ConfigurationPropertyMock<>(URI.create("http://test:8080"));
		SystemUserAuthenticator authSystem = new SystemUserAuthenticator();
		SystemConfiguration systemConfiguration = Mockito.mock(SystemConfiguration.class);
		Mockito.when(systemConfiguration.getSystemAccessUrl()).thenReturn(systemAccessUrl);

		ReflectionUtils.setFieldValue(authSystem, "systemConfiguration", systemConfiguration);

		IDPConfiguration idpConfiguration = Mockito.mock(IDPConfiguration.class);
		Mockito.when(idpConfiguration.getIdpServerURL()).thenReturn(configurationPropertyMock);
		ReflectionUtils.setFieldValue(authSystem, "idpConfiguration", idpConfiguration);

		SAMLMessageProcessor samlMessageProcessor = Mockito.mock(SAMLMessageProcessor.class);
		Mockito.when(samlMessageProcessor.getIssuerId()).thenReturn(configurationPropertyMock);
		ReflectionUtils.setFieldValue(authSystem, "samlMessageProcessor", samlMessageProcessor);
		return authSystem;
	}
}
