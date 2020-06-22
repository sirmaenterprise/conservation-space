package com.sirma.itt.seip.id.wso2;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.KeyStore;

import org.apache.axis2.context.ConfigurationContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.context.ContextualReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.idp.wso2.WSO2IdPStubManagement;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Tests for {@link WSO2IdPStubManagement}.
 *
 * @author smustafov
 */
public class WSO2IdPStubManagementTest {

	@InjectMocks
	private WSO2IdPStubManagement idpStubManagement;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private IDPConfiguration idpConfiguration;

	@Mock
	private SecurityContextManager securityContextManager;

	@Spy
	private Contextual<ConfigurationContext> configurationContext = ContextualReference.create();

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInit_verifyIdpUrlsSet() {
		ConfigurationProperty<KeyStore> trustStoreConfigMock = mock(ConfigurationProperty.class);
		ConfigurationProperty<String> idpUrlConfigMock = mock(ConfigurationProperty.class);
		ConfigurationProperty<String> idpServicesPathConfigMock = mock(ConfigurationProperty.class);

		when(securityConfiguration.getTrustStore()).thenReturn(trustStoreConfigMock);
		when(idpConfiguration.getIdpServerURL()).thenReturn(idpUrlConfigMock);
		when(idpUrlConfigMock.get()).thenReturn("url/samlsso");
		when(idpConfiguration.getIdpServicesPath()).thenReturn(idpServicesPathConfigMock);
		when(idpServicesPathConfigMock.get()).thenReturn("/services/");

		idpStubManagement.init();

		verify(idpConfiguration, atLeastOnce()).getIdpServerURL();
		verify(idpConfiguration).getIdpServicesPath();
	}

	@Test(expected = EmfRuntimeException.class)
	public void testInit_verifyCorrectExceptionThrown() {
		when(securityConfiguration.getTrustStore()).thenThrow(new RuntimeException());
		idpStubManagement.init();
	}

}
