/**
 * Copyright (c) 2013 11.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.authentication.sso.saml;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Called when a security token is needed. Connects to the WSO2 Identity Server using HTTP client (simulating web
 * browser) to request a SAML token. WARNING this implementation is tightly coupled to the WSO2 Identity Server and
 * cannot be reused for other servers.
 *
 * @author Adrian Mitev
 */
@ApplicationScoped
public class WSO2SAMLSecurityTokenService implements SecurityTokenService {
	private static final String MISSING_CONFIGURATION = "Missing configuration %s. Will not provide security tokens until configured!";
	private static final Logger LOGGER = LoggerFactory.getLogger(WSO2SAMLSecurityTokenService.class);

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private SAMLMessageProcessor messageProcessor;

	@Inject
	private SSOConfiguration ssoConfiguration;

	@Inject
	private SystemConfiguration systemConfiguration;

	private boolean enabled = true;
	private String disabledReason;

	/**
	 * Check configuration.
	 */
	@PostConstruct
	public void checkConfiguration() {
		List<String> missingConfigs = new ArrayList<>(3);
		if (securityConfiguration.getTrustStore().isNotSet()) {
			missingConfigs.add(securityConfiguration.getTrustStore().getName());
		}
		if (messageProcessor.getIssuerId().isNotSet()) {
			missingConfigs.add(messageProcessor.getIssuerId().getName());
		}
		if (ssoConfiguration.getIdpUrl().isNotSet()) {
			missingConfigs.add(ssoConfiguration.getIdpUrl().getName());
		}
		if (!isEmpty(missingConfigs)) {
			enabled = false;
			disabledReason = String.format(MISSING_CONFIGURATION, missingConfigs);
		}
	}

	@Override
	public String requestToken(String username, String password) throws Exception {
		if (!enabled) {
			throw new IllegalStateException(disabledReason);
		}

		String assertionUrl = ssoConfiguration.getAssertionURL().get();
		String issuerId = messageProcessor.getIssuerId().get();

		StringBuilder requstURI = new StringBuilder(assertionUrl);
		String buildAuthenticationRequest = messageProcessor.buildAuthenticationRequest(issuerId, assertionUrl);

		HttpClient client = new HttpClient();
		try {
			requstURI = new StringBuilder();
			requstURI.append(ssoConfiguration.getIdpUrl().get());
			requstURI.append("?SAMLRequest=");
			requstURI.append(buildAuthenticationRequest);
			requstURI.append("&RelayState=");
			requstURI.append(systemConfiguration.getDefaultContextPath().get());

			// get login form
			GetMethod get = new GetMethod(requstURI.toString());
			client.executeMethod(get);
			PostMethod post = buildAuthRequest(get.getResponseBodyAsString(), username, password, assertionUrl);
			client.executeMethod(post);
			if (post.getStatusCode() == 302) {
				Header redirectLocation = post.getResponseHeader("Location");
				if (redirectLocation != null) {
					post = new PostMethod(redirectLocation.getValue());
					LOGGER.debug("Sending redirect as part of initial request to: {}", post.getURI());
					client.executeMethod(post);
				}
			}
			String responseBodyAsString = post.getResponseBodyAsString();
			String samlResponse = getValueOfElement(responseBodyAsString, "SAMLResponse");
			return StringEscapeUtils.unescapeXml(samlResponse);
		} catch (IOException e) {
			throw new EmfRuntimeException("Failed to comunicate with SSO provider", e);
		}
	}

	/**
	 * Builds the auth request depending on the provided config parameters.
	 *
	 * @param httpResponse
	 *            the response body from login page
	 * @param username
	 *            the username to authenticate with
	 * @param password
	 *            the password to authenticate with
	 * @param consumerURL
	 *            the consumer url for assertion. If custom page is provided param is not needed
	 * @return the post method for actual authentication
	 */
	private PostMethod buildAuthRequest(String httpResponse, String username, String password, String consumerURL) {
		PostMethod post = null;
		String assertionString = getValueOfElement(httpResponse, "assertionString");
		if (ssoConfiguration.getSsoAuthenticationUrl().isNotSet()) {
			post = new PostMethod(ssoConfiguration.getIdpUrl().get());
			post.addParameter("assertionString", assertionString);
			post.addParameter("assertnConsumerURL", consumerURL);
			post.addParameter("issuer", messageProcessor.getIssuerId().get());
			post.addParameter("id", "0");
			post.addParameter("subject", "null");
			post.addParameter("relyingPartySessionId", "null");
			post.addParameter("RelayState", "null");
		} else {
			String sessionDataKey = getValueOfElement(httpResponse, "sessionDataKey");
			String uri = ssoConfiguration.getSsoAuthenticationUrl().get() + "?sessionDataKey=" + sessionDataKey;
			post = new PostMethod(uri);
			post.addParameter("sessionDataKey", sessionDataKey);
		}
		post.addParameter("username", username);
		post.addParameter("password", password);
		return post;
	}

	/**
	 * Extracts the value from a html input tag by its name attribute.
	 *
	 * @param html
	 *            html content.
	 * @param name
	 *            value of the name of the input tag.
	 * @return the fetched value or null if the element is not found.
	 */
	private static String getValueOfElement(String html, String name) {
		// find the name attribute matching the provided name parameter
		int indexOfElement = html.indexOf("name=\"" + name);
		String endSuffix = null;
		if (indexOfElement != -1) {
			endSuffix = getEndSuffix(html, name, indexOfElement);
			// find the value attribute for the element with the provided name
			int indexOfValue = html.indexOf("value=", indexOfElement) + 7;

			// get the content of the value attribute
			return html.substring(indexOfValue, html.indexOf(endSuffix, indexOfValue));
		}
		indexOfElement = html.indexOf("name='" + name);
		if (indexOfElement != -1) {
			// find the value attribute for the element with the provided name
			int indexOfValue = html.indexOf("value=", indexOfElement) + 7;
			// get the content of the value attribute
			endSuffix = getEndSuffix(html, name, indexOfElement);
			return html.substring(indexOfValue, html.indexOf(endSuffix, indexOfValue));
		}
		return null;
	}

	/**
	 * Finds the closing character for the value string associated with the 'name' tag - might be ' or "
	 *
	 * @param html
	 *            is the html reponse to read from
	 * @param name
	 *            is the tag to find closing value string
	 * @param startIndexOfElement
	 *            is where to start looing for
	 * @return the closing character on throws {@link EmfRuntimeException} if not found
	 */
	private static String getEndSuffix(String html, String name, int startIndexOfElement) {
		String endSuffix = null;
		int valueStartIndex = html.indexOf("value=\"", startIndexOfElement);
		int endIndexOfElement = html.indexOf('>', startIndexOfElement);
		if (valueStartIndex != -1 && valueStartIndex < endIndexOfElement) {
			endSuffix = "\"";
		} else {
			valueStartIndex = html.indexOf("value='", startIndexOfElement);
			if (valueStartIndex != -1 && valueStartIndex < endIndexOfElement) {
				endSuffix = "'";
			} else {
				throw new EmfRuntimeException("Invalid closing value for " + name);
			}
		}
		return endSuffix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateToken(String token) {
		return messageProcessor.validateToken(token);
	}

}
