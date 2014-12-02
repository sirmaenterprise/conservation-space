package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;
import java.net.URLEncoder;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.SystemConfiguration;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;
import com.sirma.itt.emf.util.CDI;

/**
 * Redirects the user to a SSO login page if not already authenticated.
 */
@WebFilter(filterName = "Authentication Filter", servletNames = { "Faces Servlet" })
public class SAMLSSOSecurityFilter implements Filter {
	/** The log. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLSSOSecurityFilter.class);
	private boolean traceEnabled = LOGGER.isTraceEnabled();
	/**
	 * Web context parameter describing paths that should be skipped from SSO validation.
	 */
	static final String EXCLUDE_PATHS_PARAM = "com.sirma.itt.cmf.security.sso.excludePaths";

	@Inject
	private BeanManager beanManager;

	@Inject
	private SAMLMessageProcessor messageProcessor;

	/** The sso enabled. */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_ENABLED, defaultValue = "false")
	private Boolean ssoEnabled;

	/** The default idp address. */
	@Inject
	@Config(name = SSOConfiguration.SECURITY_SSO_IDP_URL)
	private String defaultIdPUrl;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Inject
	private EmfContext emfContext;

	private String[] pathsToExclude;

	@Override
	public void init(FilterConfig config) throws ServletException {
		// Inits the context of the application i.e. /emf/.
		emfContext.init(config);
		if (ssoEnabled) {
			String excludePathsParam = config.getServletContext().getInitParameter(
					EXCLUDE_PATHS_PARAM);
			// harcode exclusion of the ServiceLogin servlet
			if (excludePathsParam != null) {
				excludePathsParam = excludePathsParam + "," + SAMLServiceLogin.SERVICE_LOGIN + ","
						+ SAMLServiceLogout.SERVICE_LOGOUT;
			} else {
				excludePathsParam = SAMLServiceLogin.SERVICE_LOGIN + ","
						+ SAMLServiceLogout.SERVICE_LOGOUT;
			}

			pathsToExclude = excludePathsParam.split(",");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (ssoEnabled) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			String requestURI = httpRequest.getRequestURI();
			String contextPath = httpRequest.getContextPath();

			// requested path = requested URI without the application context
			// path
			String requestedPath = requestURI.substring(contextPath.length());

			// iterate all paths marked for exclusion and checks if the current
			// request matches one of the paths. If matches, the sso check if
			// skipped.
			if (pathsToExclude != null) {
				for (String current : pathsToExclude) {
					if (requestedPath.startsWith(current)) {
						chain.doFilter(request, response);
						return;
					}
				}
			}

			AuthenticationService authenticationService = CDI.instantiateDefaultBean(
					AuthenticationService.class, beanManager);

			if (!authenticationService.isAuthenticated()) {
				String fullPath = requestURI;
				// append query string (request parameters) if available
				String queryString = httpRequest.getQueryString();
				if (StringUtils.isNotNullOrEmpty(queryString)) {
					fullPath = fullPath + "?" + queryString;
				}

				// set the original return url on invoking servlet
				String issuerId = request.getServerName() + "_" + request.getServerPort();
				String encodedRequestMessage = messageProcessor.buildAuthenticationRequest(
						issuerId, constructAssertionUrl(httpRequest));
				String idpUrl = constructIdpUrl(httpRequest);

				StringBuilder requestURIBuilder = new StringBuilder();
				requestURIBuilder.append(idpUrl);
				requestURIBuilder.append("?SAMLRequest=");
				requestURIBuilder.append(encodedRequestMessage);
				requestURIBuilder.append("&RelayState=");
				requestURIBuilder.append(URLEncoder.encode(fullPath, "UTF-8"));
				// RelayState is used to store return URL that will be provided
				// back when the user
				// authenticates successfully
				httpResponse.sendRedirect(requestURIBuilder.toString());
				return;
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * Constructs the IdP login url using a configuration parameter.
	 *
	 * @param request
	 *            http request.
	 * @return constructed idp url.
	 */
	private String constructIdpUrl(HttpServletRequest request) {
		String idpUrlKey = SSOConfiguration.SECURITY_SSO_IDP_URL + "." + request.getLocalAddr();
		String idpUrl = systemConfiguration.getConfiguration(idpUrlKey);
		if (traceEnabled) {
			LOGGER.trace("UDP url build for: " + idpUrlKey + ":" + idpUrl);
		}
		if (idpUrl == null) {
			idpUrl = defaultIdPUrl;
		}
		return idpUrl;
	}

	/**
	 * Builds an assertion URL that should be used by the IdP for return when the user is
	 * authenticated.
	 *
	 * @param request
	 *            the request
	 * @return constructed assertion url.
	 */
	private String constructAssertionUrl(HttpServletRequest request) {
		String serverURL = request.getScheme() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		return serverURL + request.getContextPath() + SAMLServiceLogin.SERVICE_LOGIN;
	}

	@Override
	public void destroy() {
	}

}