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

import com.sirma.itt.commons.utils.string.StringUtils;
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

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(WSO2SAMLSecurityTokenService.class);

	@Inject
	private SecurityConfiguration securityConfiguration;

	/** The message processor. */
	@Inject
	private SAMLMessageProcessor messageProcessor;

	/** The default idp address. */
	@Inject
	private SSOConfiguration ssoConfiguration;

	@Inject
	private SystemConfiguration systemConfiguration;

	/** The emf context. */
	@Inject
	private EmfContext emfContext;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String requestToken(String username, String password) throws Exception {
		if (!enabled) {
			throw new IllegalStateException(disabledReason);
		}
		// get context path
		String contextPath = emfContext.getServletContext().getContextPath();
		if (StringUtils.isNullOrEmpty(contextPath)) {
			LOGGER.warn("Was unable to extract context path, using default which is : /emf");
			contextPath = "/emf";
		}
		StringBuilder requstURI = new StringBuilder();
		requstURI.append(systemConfiguration.getSystemAccessUrl().get().getScheme());
		requstURI.append("://");
		requstURI.append(messageProcessor.getFormattedIssuerId().get());
		requstURI.append(contextPath);
		requstURI.append(SAMLServiceLogin.SERVICE_LOGIN);
		final String consumerURL = requstURI.toString();
		String buildAuthenticationRequest = messageProcessor
				.buildAuthenticationRequest(messageProcessor.getIssuerId().get(), consumerURL);
		LOGGER.debug(
				"Issuer id is : {}, ConsumerURL is: {}, idp URL is: {}, provided username is: {}, provided pass is: {}",
				messageProcessor.getFormattedIssuerId().get(), consumerURL, ssoConfiguration.getIdpUrl().get(),
				username, password == null ? "NULL" : "NOT NULL");
		HttpClient client = new HttpClient();
		try {
			requstURI = new StringBuilder();
			requstURI.append(ssoConfiguration.getIdpUrl().get());
			requstURI.append("?SAMLRequest=");
			requstURI.append(buildAuthenticationRequest);
			requstURI.append("&RelayState=");
			requstURI.append(contextPath);
			final String getUrl = requstURI.toString();
			LOGGER.debug("Sending get request to: {}", getUrl);
			// get credential submit document
			GetMethod get = new GetMethod(getUrl);
			client.executeMethod(get);
			// post credentials and fetch the saml assertion response
			PostMethod post = buildAuthRequest(get.getResponseBodyAsString(), username, password, consumerURL);
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
