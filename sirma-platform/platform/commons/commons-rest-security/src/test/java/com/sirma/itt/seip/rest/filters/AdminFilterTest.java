package com.sirma.itt.seip.rest.filters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.NoPermissionsException;

/**
 * Tests for {@link AdminFilter}.
 *
 * @author smustafov
 */
public class AdminFilterTest {

	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private AdminFilter filter;

	@Before
	public void before() {
		filter = new AdminFilter();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = NoPermissionsException.class)
	public void testFilter_withNonAdminUser() throws IOException {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		when(requestContext.getUriInfo()).thenReturn(mock(UriInfo.class));
		when(requestContext.getCookies()).thenReturn(Collections.emptyMap());
		when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(Boolean.FALSE);

		filter.filter(requestContext);
	}

	@Test
	public void testFilter_withAdminUser() throws IOException {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		when(securityContextManager.isAuthenticatedAsAdmin()).thenReturn(Boolean.TRUE);

		filter.filter(requestContext);
	}

}
