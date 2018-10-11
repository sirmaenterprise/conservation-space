package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link TenantMgmtPermissionsFilter}
 *
 * @author nvelkov
 */
public class TenantMgmtPermissionsFilterTest {

	@InjectMocks
	private TenantMgmtPermissionsFilter filter;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest request;

	@Mock
	private FilterChain chain;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Before
	public void beforeMethod() throws ServletException {
		MockitoAnnotations.initMocks(this);
		FilterConfig config = mock(FilterConfig.class);
		ServletContext context = mock(ServletContext.class);
		when(config.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("/emf");
		filter.init(config);
	}

	@Test
	public void should_access_tenantMgmtPage_when_systemTenant() throws Exception {
		mockSystemTenant();
		when(request.getRequestURI()).thenReturn("/emf/tenant-mgmt/");
		filter.doFilter(request, response, chain);
		verify(chain).doFilter(request, response);
	}

	@Test
	public void should_not_access_tenantMgmtPage_when_not_systemTenant() throws Exception {
		ConfigurationProperty<String> ui2UrlConfiguration = mock(ConfigurationProperty.class);
		when(systemConfiguration.getUi2Url()).thenReturn(ui2UrlConfiguration);
		when(request.getRequestURI()).thenReturn("/emf/tenant-mgmt/");
		filter.doFilter(request, response, chain);
		verify(response).sendRedirect("/emf");
	}

	@Test
	public void should_redirectToUi2_ifUi2Set() throws IOException, ServletException {
		ConfigurationProperty<String> ui2UrlConfiguration = mock(ConfigurationProperty.class);
		when(ui2UrlConfiguration.get()).thenReturn("url");
		when(ui2UrlConfiguration.isSet()).thenReturn(true);
		when(systemConfiguration.getUi2Url()).thenReturn(ui2UrlConfiguration);
		filter.doFilter(request, response, chain);
		verify(response).sendRedirect("url");
	}

	private void mockSystemTenant() {
		when(Boolean.valueOf(securityContext.isActive())).thenReturn(Boolean.TRUE);
		when(Boolean.valueOf(securityContext.isSystemTenant())).thenReturn(Boolean.TRUE);
	}
}
