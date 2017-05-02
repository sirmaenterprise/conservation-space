package com.sirma.itt.seip.rest.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Test for {@link TenantInitializationForPublicAccessFilter}
 *
 * @author BBonev
 */
public class TenantInitializationForPublicAccessFilterTest {

	private TenantInitializationForPublicAccessFilter filter;

	@Mock
	private User admin;
	@Mock
	private SecurityContext context;
	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(securityContextManager.getAdminUser()).thenReturn(admin);
		when(securityContextManager.getCurrentContext()).thenReturn(context);

		filter = new TenantInitializationForPublicAccessFilter(securityContextManager, "tenant");
	}

	@Test
	public void shouldResolveTenantFromQueryParam() throws Exception {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		UriInfo info = mock(UriInfo.class);
		when(requestContext.getUriInfo()).thenReturn(info);
		MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
		when(info.getQueryParameters()).thenReturn(params);
		params.add("tenant", "tenant.com");

		filter.filter(requestContext);

		verify(securityContextManager).initializeTenantContext("tenant.com");
	}

	@Test
	public void shouldResolveTenantFromPathParamIfNotPassedAsQueryParam() throws Exception {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		UriInfo info = mock(UriInfo.class);
		when(requestContext.getUriInfo()).thenReturn(info);
		when(info.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
		MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
		params.add("tenant", "tenant.com");
		when(info.getPathParameters()).thenReturn(params);

		filter.filter(requestContext);

		verify(securityContextManager).initializeTenantContext("tenant.com");
	}

	@Test(expected = AuthenticationException.class)
	public void shouldFailIfNoConfiguredTenantIdIsFoundAndContextNotActive() throws Exception {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		UriInfo info = mock(UriInfo.class);
		when(requestContext.getUriInfo()).thenReturn(info);
		when(info.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
		when(info.getPathParameters()).thenReturn(new MultivaluedHashMap<>());
		when(requestContext.getCookies()).thenReturn(new HashMap<>());

		filter.filter(requestContext);
	}

	@Test
	public void shouldEndContextOnRequesResponse() throws Exception {
		filter.filter(null, null);
		verify(securityContextManager).endContextExecution();
	}
}
