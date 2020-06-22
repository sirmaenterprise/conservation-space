package com.sirma.itt.seip.rest.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Test assertion of active security context for non public resources.
 *
 * @author yasko
 */
@Test
public class AuthorizationFilterTest {

	@InjectMocks
	private AuthorizationFilter filter;

	@Mock
	private User admin;
	@Mock
	private SecurityContext context;
	@Mock
	private SecurityContextManager securityContextManager;

	/**
	 * Initialize tests.
	 */
	@BeforeTest
	protected void init() {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.getAdminUser()).thenReturn(admin);
		when(securityContextManager.getCurrentContext()).thenReturn(context);
	}

	/**
	 * Test exception is thrown when there's no active context.
	 * @throws IOException thrown for class being tested.
	 */
	@Test(expectedExceptions = AuthenticationException.class)
	public void testNoActiveContext() throws IOException {
		when(context.isActive()).thenReturn(Boolean.FALSE);

		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		when(requestContext.getUriInfo()).thenReturn(mock(UriInfo.class));
		when(requestContext.getCookies()).thenReturn(Collections.emptyMap());

		filter.filter(requestContext);
	}

	/**
	 * Test when there's an active security context.
	 * @throws IOException thrown for class being tested.
	 */
	public void testWithActiveContext() throws IOException {
		when(context.isActive()).thenReturn(Boolean.TRUE);

		filter.filter(null);
	}
}
