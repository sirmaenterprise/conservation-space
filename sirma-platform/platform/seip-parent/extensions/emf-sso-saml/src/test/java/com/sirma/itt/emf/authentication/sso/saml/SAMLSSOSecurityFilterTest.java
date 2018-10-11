/**
 * Copyright (c) 2013 25.06.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Tests for {@link SAMLSSOSecurityFilter}.
 *
 * @author Adrian Mitev
 */
public class SAMLSSOSecurityFilterTest {

	@Mock
	private SecurityContextManager securityContextManager;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private FilterChain chain;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private SAMLRequestBuilder requestBuilder;

	@InjectMocks
	private SAMLSSOSecurityFilter ssoFilter;

	private final String CONTEXT_PATH = "/emf";

	@Before
	public void beforeMethod() throws ServletException {
		ssoFilter = new SAMLSSOSecurityFilter();
		MockitoAnnotations.initMocks(this);

		// they do
		ssoFilter.init(null);
		ssoFilter.destroy();
	}

	/**
	 * Verifies that the filter chain proceeds if the user is authenticated.
	 *
	 * @throws IOException
	 *             thrown by the method under test.
	 * @throws ServletException
	 *             thrown by the method under test.
	 */
	@Test
	public void verifyRequestProceedsIfTheUserIsAuthenticated() throws IOException, ServletException {
		withSecurityContext(true);

		forRequest("/api", null);

		ssoFilter.doFilter(request, null, chain);

		verifyThatRequestProceeds(chain, request);
	}

	/**
	 * Verifies that the filter chain proceeds if the user is NOT authenticated but the request path is excluded.
	 *
	 * @throws IOException
	 *             thrown by the method under test.
	 * @throws ServletException
	 *             thrown by the method under test.
	 */
	@Test
	public void verifyRequestProceedsIfTheUserIsNotAuthenticatedButThePathIsExcluded()
			throws IOException, ServletException {
		withSecurityContext(false);

		final String RESOURCE_PATH = "/api";

		forRequest(RESOURCE_PATH, null);

		forSecurityExclusions(RESOURCE_PATH);

		ssoFilter.doFilter(request, null, chain);

		verifyThatRequestProceeds(chain, request);
	}

	private void forRequest(final String requestPath, final String queryString) {
		when(request.getContextPath()).thenReturn(CONTEXT_PATH);
		when(request.getQueryString()).thenReturn(queryString);
		when(request.getRequestURI()).thenReturn(CONTEXT_PATH + requestPath);
	}

	/**
	 * Verifies that the request is redirected to the login chain proceeds if the user is NOT authenticated and the
	 * request path is NOT excluded.
	 *
	 * @throws IOException
	 *             thrown by the method under test.
	 * @throws ServletException
	 *             thrown by the method under test.
	 */
	@Test
	public void verifyRedirectToLoginIfTheUserIsNotAuthenticatedAndThePathIsNotExcluded()
			throws IOException, ServletException {
		withSecurityContext(false);

		forRequest("/something", "test=123");

		forSecurityExclusions("/other_thing");

		// verify redirect happened and the request parameters are retained
		when(requestBuilder.build(Mockito.any(HttpServletRequest.class), Mockito.anyString()))
				.thenAnswer(AdditionalAnswers.returnsLastArg());

		ssoFilter.doFilter(request, response, chain);

		verify(response).sendRedirect("/emf/something?test=123");

		// verify redirect happened even with missing request parameters
		forRequest("/something", "");

		ssoFilter.doFilter(request, response, chain);

		verify(response).sendRedirect("/emf/something");
	}

	private void forSecurityExclusions(final String... exclusionPaths) {
		SecurityExclusion exclusions = path -> {
			for (String exclusionPath : exclusionPaths) {
				if (path.startsWith(exclusionPath)) {
					return true;
				}
			}
			return false;
		};
		ReflectionUtils.setFieldValue(ssoFilter, "exclusions", exclusions);
	}

	private void withSecurityContext(boolean active) {
		when(securityContext.isActive()).thenReturn(active);
		when(securityContextManager.getCurrentContext()).thenReturn(securityContext);
	}

	private void verifyThatRequestProceeds(FilterChain chain, HttpServletRequest request)
			throws IOException, ServletException {
		verify(chain, Mockito.times(1)).doFilter(request, null);
	}

}
