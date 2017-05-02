package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyStore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link WSO2SAMLSecurityTokenService}
 *
 * @author BBonev
 */
public class WSO2SAMLSecurityTokenServiceTest {

	@InjectMocks
	private WSO2SAMLSecurityTokenService tokenService;

	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private SAMLMessageProcessor messageProcessor;
	@Mock
	private SSOConfiguration ssoConfiguration;
	@Mock
	private SystemConfiguration systemConfiguration;
	@Mock
	private EmfContext emfContext;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = IllegalStateException.class)
	public void testInvalidConfigurations() throws Exception {
		ConfigurationPropertyMock<KeyStore> storeConfig = new ConfigurationPropertyMock<>();
		storeConfig.setName("store");
		when(securityConfiguration.getTrustStore()).thenReturn(storeConfig);
		ConfigurationPropertyMock<String> issuer = new ConfigurationPropertyMock<>();
		issuer.setName("issuer");
		when(messageProcessor.getIssuerId()).thenReturn(issuer);
		ConfigurationPropertyMock<String> idpUrl = new ConfigurationPropertyMock<>();
		idpUrl.setName("idpUrl");
		when(ssoConfiguration.getIdpUrl()).thenReturn(idpUrl);

		tokenService.checkConfiguration();
		tokenService.requestToken("admin", "admin");
	}

	@Test
	public void testValidConfigurations() throws Exception {
		ConfigurationPropertyMock<KeyStore> storeConfig = new ConfigurationPropertyMock<>();
		storeConfig.setName("store");
		storeConfig.setValue(mock(KeyStore.class));
		when(securityConfiguration.getTrustStore()).thenReturn(storeConfig);
		ConfigurationPropertyMock<String> issuer = new ConfigurationPropertyMock<>();
		issuer.setName("issuer");
		issuer.setValue("issuer");
		when(messageProcessor.getIssuerId()).thenReturn(issuer);
		ConfigurationPropertyMock<String> idpUrl = new ConfigurationPropertyMock<>();
		idpUrl.setName("idpUrl");
		idpUrl.setValue("idpUrl");
		when(ssoConfiguration.getIdpUrl()).thenReturn(idpUrl);

		tokenService.checkConfiguration();
	}
}
