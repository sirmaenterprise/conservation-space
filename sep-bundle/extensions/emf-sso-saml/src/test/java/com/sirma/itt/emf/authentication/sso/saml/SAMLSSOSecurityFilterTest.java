/**
 * Copyright (c) 2013 25.06.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Tests for {@link SAMLSSOSecurityFilter}.
 *
 * @author Adrian Mitev
 */
public class SAMLSSOSecurityFilterTest {

	private SAMLSSOSecurityFilter cut = new SAMLSSOSecurityFilter();

	/**
	 * Tests init() method for proper construction of the "pathsToExclude" property when the
	 * EXCLUDE_PATHS_PARAM is provided.
	 *
	 * @throws ServletException
	 *             not expected
	 */
	@Test
	public void testInitWithExistingInitParameter() throws ServletException {
		ReflectionUtils.setField(cut, "emfContext", new EmfContext());
		ReflectionUtils.setField(cut, "ssoEnabled", Boolean.TRUE);
		ServletContext servletContext = mock(ServletContext.class);
		FilterConfig filterConfig = mock(FilterConfig.class);
		when(filterConfig.getServletContext()).thenReturn(servletContext);
		// test for non-existing init parameter
		cut.init(filterConfig);

		withPathsToExcudeInitParam("/a1,/a2");
		// assert that there are 3 elements and their values
		expectPathsToExclude("/a1", "/a2", SAMLServiceLogin.SERVICE_LOGIN, SAMLServiceLogout.SERVICE_LOGOUT);
	}

	/**
	 * Tests init() method for proper construction of the "pathsToExclude" property when the
	 * EXCLUDE_PATHS_PARAM is NOT provided.
	 *
	 * @throws ServletException
	 */
	@Test
	public void testInitWithMissingInitParameter() throws ServletException {
		withPathsToExcudeInitParam(null);
		// assert that there are is only one elements - the login servlet that should be skipped
		expectPathsToExclude(SAMLServiceLogin.SERVICE_LOGIN, SAMLServiceLogout.SERVICE_LOGOUT);
	}

	/**
	 * Provides a mock FilterConfig that provides the paths to exclude parameter with specific
	 * value.
	 *
	 * @param pathsCSV
	 *            value of the paths to exclude parameter (CSV list of paths).
	 * @throws ServletException
	 *             should never be thrown.
	 */
	private void withPathsToExcudeInitParam(String pathsCSV) throws ServletException {
		ReflectionUtils.setField(cut, "emfContext", new EmfContext());
		ReflectionUtils.setField(cut, "ssoEnabled", Boolean.TRUE);

		// test with existing init parameter
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getInitParameter(SAMLSSOSecurityFilter.EXCLUDE_PATHS_PARAM))
				.thenReturn(pathsCSV);

		FilterConfig filterConfig = mock(FilterConfig.class);
		when(filterConfig.getServletContext()).thenReturn(servletContext);

		// test for non-existing init parameter
		cut.init(filterConfig);
	}

	/**
	 * Expects a specific array of values on the "pathsToExclude" attribute.
	 *
	 * @param expectedValue
	 *            expected values.
	 */
	private void expectPathsToExclude(String... expectedValue) {
		String[] pathsToExclude = (String[]) ReflectionUtils.getField(cut, "pathsToExclude");
		Assert.assertArrayEquals(expectedValue, pathsToExclude);
	}
}
