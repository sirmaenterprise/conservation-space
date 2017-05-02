package com.sirma.itt.cmf.security.sso;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.repository.User;
import org.apache.log4j.Logger;

/**
 * The Class AlfrescoOpenSSOFilter.
 */
public class AlfrescoSSOFilter implements Filter {
	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(AlfrescoSSOFilter.class);
	private static final String ALFRESCO_SERVICE = "/alfresco/service/";
	private static final String ALFRESCO_SERVICE_SHORT = "/alfresco/s/";
	/** The alfresco facade. */
	private AlfrescoFacade alfrescoFacade;

	/** The servlet context. */
	private ServletContext servletContext;

	/** The client. */
	private WSO2SAMLClient client;

	/** is sso enabled?. */
	private boolean ssoEnabled = true;

	@Override
	public void destroy() {
		// nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String requestURI = httpRequest.getRequestURI();
		if (requestURI.startsWith(ALFRESCO_SERVICE) || requestURI.startsWith(ALFRESCO_SERVICE_SHORT)) {
			if (AuthenticationUtil.getRunAsUser() == null) {
				chain.doFilter(httpRequest, httpResponse);
				return;
			} else {
				AuthenticationUtil.clearCurrentSecurityContext();
				chain.doFilter(httpRequest, httpResponse);
				return;
			}
		}
		if (ssoEnabled) {
			HttpSession httpSession = httpRequest.getSession();
			try {
				if (httpSession.getAttribute("_alfAuthTicket") == null) {
					client = new WSO2SAMLClient(httpRequest);
					httpSession.invalidate();
					client.doPost(httpRequest, httpResponse, alfrescoFacade);
					return;
				} else {
					if (isLogoutRequest(httpRequest)) {
						client = new WSO2SAMLClient(httpRequest);
						client.doPost(httpRequest, httpResponse, alfrescoFacade);
						httpSession.invalidate();
						return;
					} else {
						Object attribute = httpSession.getAttribute("_alfAuthTicket");
						if (attribute instanceof User) {
							String userName = ((User) attribute).getUserName();
							getAlfrescoFacade().setAuthenticatedUser(httpRequest, httpResponse, httpSession, userName,
									false);
						}
					}

				}
			} catch (Exception e) {
				LOGGER.error("Failed to check authenticated user!", e);
			}
			chain.doFilter(httpRequest, httpResponse);
		} else {
			// allow the login page to proceed
			if (!httpRequest.getRequestURI().endsWith("/alfresco/faces/jsp/login.jsp")) {
				AuthenticationStatus status = AuthenticationHelper.authenticate(servletContext, httpRequest,
						httpResponse, false);

				if (status == AuthenticationStatus.Success || status == AuthenticationStatus.Guest) {
					// continue filter chaining
					chain.doFilter(httpRequest, httpResponse);
				} else {
					// authentication failed - so end servlet execution and
					// redirect to login page
					// also save the requested URL so the login page knows where
					// to redirect too later
					BaseServlet.redirectToLoginPage(httpRequest, httpResponse, servletContext);
				}
			} else {
				// continue filter chaining
				chain.doFilter(httpRequest, httpResponse);
			}
		}

	}

	/**
	 * Checks if is logout request.
	 * 
	 * @param request
	 *            the request
	 * @return true, if is logout request
	 */
	private boolean isLogoutRequest(HttpServletRequest request) {
		@SuppressWarnings("rawtypes")
		Enumeration parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameter = (String) parameterNames.nextElement();
			String[] string = request.getParameterValues(parameter);
			for (int i = 0; i < string.length; i++) {
				if ((string[i] != null) && (string[i].contains(":logout"))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.servletContext = config.getServletContext();
		ssoEnabled = SAMLSSOConfigurations.isSSOEnabled();
	}

	/**
	 * Gets the open sso client.
	 * 
	 * /** Gets the alfresco facade.
	 * 
	 * @return the alfresco facade
	 */
	public AlfrescoFacade getAlfrescoFacade() {
		if (this.alfrescoFacade == null) {
			this.alfrescoFacade = new AlfrescoFacade(this.servletContext);
		}
		return this.alfrescoFacade;
	}

	/**
	 * Sets the alfresco facade.
	 * 
	 * @param alfrescoFacade
	 *            the new alfresco facade
	 */
	public void setAlfrescoFacade(AlfrescoFacade alfrescoFacade) {
		this.alfrescoFacade = alfrescoFacade;
	}

}