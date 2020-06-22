package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link TenantMgmtAccessServletFilter}
 *
 * @author BBonev
 */
public class TenantMgmtAccessServletFilterTest {

	@InjectMocks
	private TenantMgmtAccessServletFilter filter;
	
	@Mock
	private SecurityContext securityContext;

	@Mock
	private HttpServletResponse response;
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private FilterChain chain;
	
	@Mock
	private SecurityExclusion exclusions;

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);
		FilterConfig config = mock(FilterConfig.class);
		ServletContext context = mock(ServletContext.class);
		when(config.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("/emf");
		when(exclusions.isForExclusion(anyString())).thenReturn(false);
		filter.init(config);
	}

	@Test
	public void testRestCall() throws Exception {
		mockSystemTenant();

		when(request.getRequestURI()).thenReturn("/emf/api/someService");

		filter.doFilter(request, response, chain);
		verify(chain).doFilter(request, response);
	}

	@Test
	public void testRandomUrl() throws Exception {
		mockSystemTenant();

		when(request.getRequestURI()).thenReturn("/emf/someOtherAddress");

		filter.doFilter(request, response, chain);

		verify(response).sendRedirect(anyString());
	}

	@Test
	public void testMgMtAddress() throws Exception {
		mockSystemTenant();

		when(request.getRequestURI()).thenReturn("/emf/tenant-mgmt");

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@Test
	public void testServiceTenant() throws Exception {
		mockSystemTenant();

		when(request.getRequestURI()).thenReturn("/emf/service/tenant");

		filter.doFilter(request, response, chain);

		verify(response).sendRedirect(anyString());
	}

	@Test
	public void testNotAuthenticated() throws Exception {

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
	}

	@SuppressWarnings("boxing")
	private void mockSystemTenant() {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);
	}
}
