package com.sirma.itt.seip.adapters.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link AlfrescoRESTClient}.
 *
 * @author smustafov
 */
public class AlfrescoRESTClientTest {

	private static final String TENANT_ID = "tenant.com";
	private static final String SYSTEM_ADMIN_USERNAME = "systemadmin";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "pa$$word";
	private static final String VALID_TOKEN = "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";

	@InjectMocks
	private AlfrescoRESTClient restClient;

	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private AdaptersConfiguration configuration;
	@Mock
	private ConfigurationProperty<Integer> timeout;
	@Mock
	private HttpClient httpClient;
	@Mock
	private SecurityTokenService tokenService;

	private static final SecretKey secretKey = SecurityUtil.createSecretKey("AlfrescoT1st");

	@Before
	public void before() {
		initMocks(this);

		restClient.setClient(() -> httpClient);

		when(timeout.get()).thenReturn(Integer.valueOf(0));

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);

		when(securityConfiguration.getAdminUserName()).thenReturn(new ConfigurationPropertyMock<>(USERNAME));
		when(securityConfiguration.getAdminUserPassword()).thenReturn(new ConfigurationPropertyMock<>(PASSWORD));
		when(securityConfiguration.getSystemAdminUsername()).thenReturn(SYSTEM_ADMIN_USERNAME);

		when(securityConfiguration.getCryptoKey()).thenReturn(new ConfigurationPropertyMock<>(secretKey));
	}

	@Test
	public void rawRequest_ShouldRequestToken() throws Exception {
		Pair<HttpMethod, URI> mockHttpMethod = mockHttpMethod(200);
		HttpMethod method = mockHttpMethod.getFirst();
		when(tokenService.requestToken(USERNAME, PASSWORD)).thenReturn(VALID_TOKEN);

		restClient.rawRequest(method, mockHttpMethod.getSecond());

		verifyAuthHeader(method, encrypt(VALID_TOKEN));
	}

	private static Pair<HttpMethod, URI> mockHttpMethod(int statusCode) throws URIException {
		URI uri = new URI("/alfresco", true);
		HttpMethod method = mock(HttpMethod.class);
		when(method.getURI()).thenReturn(uri);
		when(method.getStatusCode()).thenReturn(statusCode);
		return new Pair<>(method, uri);
	}

	private static void verifyAuthHeader(HttpMethod method, String token) {
		ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
		verify(method).addRequestHeader(eq(AlfrescoRESTClient.SAML_TOKEN), argCaptor.capture());
		assertEquals(token, argCaptor.getValue());
	}

	private static String encrypt(String token) {
		byte[] encryptedToken = SecurityUtil.encrypt(Base64.decodeBase64(token), secretKey);
		return new String(Base64.encodeBase64(encryptedToken), StandardCharsets.UTF_8);
	}

}
