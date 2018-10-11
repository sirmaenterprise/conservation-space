package com.sirma.itt.emf.authentication.sso.saml;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * Builder class for SAML requests
 */
@Singleton
public class SAMLRequestBuilder {

	@Inject
	private SAMLMessageProcessor messageProcessor;
	@Inject
	private SSOConfiguration ssoConfiguration;

	/**
	 * Builds the a request for the given redirect
	 *
	 * @param request
	 *            the request
	 * @param redirect
	 *            the redirect
	 * @return the string
	 */
	public String build(HttpServletRequest request, String redirect) {
		StringBuilder requestURIBuilder = new StringBuilder();
		requestURIBuilder.append(ssoConfiguration.getIdpUrlForInterface(request.getLocalAddr()));
		requestURIBuilder.append("?SAMLRequest=");
		requestURIBuilder.append(messageProcessor.buildAuthenticationRequest(request));
		requestURIBuilder.append("&RelayState=");
		requestURIBuilder.append(Base64.getEncoder().encodeToString(redirect.getBytes(StandardCharsets.UTF_8)));
		return requestURIBuilder.toString();
	}

}
