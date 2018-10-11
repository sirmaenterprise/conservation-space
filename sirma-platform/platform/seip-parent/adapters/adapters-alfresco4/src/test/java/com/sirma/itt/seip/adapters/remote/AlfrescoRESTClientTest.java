package com.sirma.itt.seip.adapters.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.commons.httpclient.Header;
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
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Tests for {@link AlfrescoRESTClient}.
 *
 * @author smustafov
 */
public class AlfrescoRESTClientTest {

	private static final String TENANT_ID = "tenant.com";
	private static final String TICKET = "ticket";

	@InjectMocks
	private AlfrescoRESTClient restClient;

	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private AdaptersConfiguration configuration;
	@Mock
	private ConfigurationProperty<Integer> timeout;
	@Mock
	private HttpClient httpClient;

	@Before
	public void before() {
		initMocks(this);

		restClient.setClient(() -> httpClient);

		EmfUser user = new EmfUser("admin", "password");
		user.setTicket(TICKET);
		when(securityContextManager.getAdminUser()).thenReturn(user);
		when(timeout.get()).thenReturn(Integer.valueOf(0));

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
	}

	@Test
	public void rawRequest_ShouldUseUserTicket_When_TicketAvailable() throws Exception {
		Pair<HttpMethod, URI> mockHttpMethod = mockHttpMethod(200);
		HttpMethod method = mockHttpMethod.getFirst();

		restClient.rawRequest(method, mockHttpMethod.getSecond());

		verify(method).addRequestHeader(AlfrescoRESTClient.SAML_TOKEN, TICKET);
	}

	@Test
	public void rawRequest_ShouldUseTicket_When_AlreadyAvailableInHttpHeader() throws Exception {
		Pair<HttpMethod, URI> mockHttpMethod = mockHttpMethod(200);
		HttpMethod method = mockHttpMethod.getFirst();
		when(method.getRequestHeader(AlfrescoRESTClient.SAML_TOKEN))
				.thenReturn(new Header(AlfrescoRESTClient.SAML_TOKEN, TICKET));

		restClient.rawRequest(method, mockHttpMethod.getSecond());

		verify(method).addRequestHeader(AlfrescoRESTClient.SAML_TOKEN, TICKET);
	}

	@Test
	public void rawRequest_ShouldReplaceNewLineAndCarriageReturnSymbolsWithTab_When_TicketContainsThem()
			throws Exception {
		Pair<HttpMethod, URI> mockHttpMethod = mockHttpMethod(200);
		HttpMethod method = mockHttpMethod.getFirst();
		when(method.getRequestHeader(AlfrescoRESTClient.SAML_TOKEN))
				.thenReturn(new Header(AlfrescoRESTClient.SAML_TOKEN, "tic\r\nket"));
		ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);

		restClient.rawRequest(method, mockHttpMethod.getSecond());

		verify(method).addRequestHeader(eq(AlfrescoRESTClient.SAML_TOKEN), argCaptor.capture());

		String ticket = argCaptor.getValue();
		assertFalse(ticket.contains("\r\n"));
		assertEquals("tic\tket", ticket);
	}

	private static Pair<HttpMethod, URI> mockHttpMethod(int statusCode) throws URIException {
		URI uri = new URI("/alfresco", true);
		HttpMethod method = mock(HttpMethod.class);
		when(method.getURI()).thenReturn(uri);
		when(method.getStatusCode()).thenReturn(statusCode);
		return new Pair<>(method, uri);
	}

}
