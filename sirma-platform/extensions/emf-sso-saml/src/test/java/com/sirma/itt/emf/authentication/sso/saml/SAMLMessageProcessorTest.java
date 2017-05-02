package com.sirma.itt.emf.authentication.sso.saml;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;

import com.sirma.itt.seip.configuration.ConfigurationProperty;


public class SAMLMessageProcessorTest {

	@InjectMocks
	private SAMLMessageProcessor processor;
	
	@Mock
	private SSOConfiguration ssoConfiguration;
	
	@Mock
	private ConfigurationProperty<String> issuerId;
	
	@Mock
	private HttpServletRequest request;
	
	@Before
	public void init() throws ConfigurationException {
		DefaultBootstrap.bootstrap();
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(ssoConfiguration.getIssuerId()).thenReturn(issuerId);
		Mockito.when(issuerId.isNotSet()).thenReturn(false);
		Mockito.when(issuerId.get()).thenReturn("issuer-id");
		
		Mockito.when(ssoConfiguration.getIdpUrlForInterface(Mockito.any())).thenReturn("1.2.3.4");
		
		Mockito.when(request.getContextPath()).thenReturn("/test");
	}
	
	@Test
	public void testDefaultRelayState() {
		String message = processor.buildLogoutMessage("1", null, request, "1-2-3-4-5");
		Assert.assertTrue(Pattern.compile("1.2.3.4\\?SAMLRequest=[^&]{1,}&RelayState=%2Ftest").matcher(message).matches());
	}
	
	@Test
	public void testEmptyRelayState() {
		String message = processor.buildLogoutMessage("1", "", request, "1-2-3-4-5");
		Assert.assertTrue(Pattern.compile("1.2.3.4\\?SAMLRequest=[^&]{1,}&RelayState=%2Ftest").matcher(message).matches());
	}
	
	@Test
	public void testProvidedRelayState() {
		String message = processor.buildLogoutMessage("1", "/relay", request, "1-2-3-4-5");
		Assert.assertTrue(Pattern.compile("1.2.3.4\\?SAMLRequest=[^&]{1,}&RelayState=%2Frelay").matcher(message).matches());
	}
	
}
