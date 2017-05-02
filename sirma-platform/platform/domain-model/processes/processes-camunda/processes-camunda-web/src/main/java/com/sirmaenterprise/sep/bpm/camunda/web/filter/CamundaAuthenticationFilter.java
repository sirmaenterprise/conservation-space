package com.sirmaenterprise.sep.bpm.camunda.web.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.webapp.impl.security.SecurityActions;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.security.SSOAuthenticator;

/**
 * {@link CamundaAuthenticationFilter} registers a web filter with the default Camunda implementation.
 * 
 * @author bbanchev
 */
public class CamundaAuthenticationFilter extends org.camunda.bpm.webapp.impl.security.auth.AuthenticationFilter {
	@Inject
	private SSOAuthenticator ssoAuthenticator;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		// prevent login with another tenant
		String requestedEngineName = extractEngine(httpRequest);
		Authentications authentications = new Authentications();
		Authentication login = ssoAuthenticator.doLogin(requestedEngineName);
		if (login != null) {
			authentications.addAuthentication(login);
		}
		if (login == null && requestedEngineName != null) {
			httpResponse.sendError(401);
			Authentications.clearCurrent();
			return;
		}
		Authentications.setCurrent(authentications);
		try {
			SecurityActions.runWithAuthentications(() -> {
				try {
					chain.doFilter(request, response);
				} catch (Exception e) {
					throw new CamundaIntegrationRuntimeException(e);
				}
				return null;
			}, authentications);
		} finally {
			Authentications.clearCurrent();
		}

	}

	private static String extractEngine(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String engineName;
		if ((engineName = findEngineName(requestURI, "/engine/engine/")) != null) {
			return engineName;
		} else if ((engineName = findEngineName(requestURI, "/auth/user/")) != null) {
			return engineName;
		} else if ((engineName = findEngineName(requestURI, "/plugin/base/")) != null) {
			return engineName;
		}
		// for resources which are not part of engine request
		return SSOAuthenticator.ANY_ENGINE;
	}

	private static String findEngineName(String requestURI, String preffix) {
		int startIndex;
		if ((startIndex = requestURI.lastIndexOf(preffix)) > 0) {
			int beginIndex = startIndex + preffix.length();
			int endIndex = requestURI.indexOf('/', beginIndex);
			if (endIndex > beginIndex) {
				return requestURI.substring(beginIndex, endIndex);
			}
			return requestURI.substring(beginIndex);
		}
		return null;
	}

}
