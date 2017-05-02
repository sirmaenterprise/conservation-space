package com.sirma.itt.seip.runtime;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Tests the access restriction logic in {@link ApplicationAccessFilter}
 * 
 * @author Mihail Radkov
 */
public class ApplicationAccessFilterTest {

	private ApplicationAccessFilter accessFilter;

	private ServletRequest request;
	private HttpServletResponse response;
	private FilterChain chain;

	@Before
	public void setUp() {
		accessFilter = new ApplicationAccessFilter();

		request = Mockito.mock(ServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		chain = Mockito.mock(FilterChain.class);
	}

	@Test
	public void testRestrictedAccess() throws IOException, ServletException {
		RuntimeInfo.instance().setPhase(StartupPhase.BEFORE_APP_START);

		accessFilter.doFilter(request, response, chain);

		Mockito.verify(chain, Mockito.never()).doFilter(Matchers.eq(request), Matchers.eq(response));
		Mockito.verify(response, Mockito.times(1)).sendError(Matchers.eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE));
	}

	@Test
	public void testAllowedAccess() throws IOException, ServletException {
		RuntimeInfo.instance().setPhase(StartupPhase.STARTUP_COMPLETE);

		accessFilter.doFilter(request, response, chain);

		Mockito.verify(chain, Mockito.times(1)).doFilter(Matchers.eq(request), Matchers.eq(response));
		Mockito.verify(response, Mockito.never()).sendError(Matchers.eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE));
	}

	@AfterClass
	public static void cleanUp() {
		RuntimeInfo.instance().setPhase(null);
	}

}
