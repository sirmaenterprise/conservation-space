/**
 * Copyright (c) 2013 11.07.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;

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
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.security.SecurityTokenService;

/**
 * Called when a security token is needed. Connects to the WSO2 Identity Server using HTTP client
 * (simulating web browser) to request a SAML token. WARNING this implementation is tightly coupled
 * to the WSO2 Identity Server and cannot be reused for other servers.
 * 
 * @author Adrian Mitev
 */
@ApplicationScoped
public class WSO2SAMLSecurityTokenService implements SecurityTokenService {
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(WSO2SAMLSecurityTokenService.class);

	/** The message processor. */
	@Inject
	private SAMLMessageProcessor messageProcessor;

	/** The issuer id. */
	@Inject
	@Config(name = SSOConfiguration.ISSUER_ID)
	private String issuerId;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_DEFAULT_HOST_PROTOCOL)
	private String protocol;

	/** The default idp address. */
	@Inject
	@Config(name = SSOConfiguration.SECURITY_SSO_IDP_URL)
	private String idpUrl;

	/** The sso authentication url. */
	@Inject
	@Config(name = SSOConfiguration.SECURITY_SSO_IDP_AUTH_CUSTOM_URL)
	private String ssoAuthenticationUrl;

	/** The emf context. */
	@Inject
	private EmfContext emfContext;

	private String formattedIssuerId;
	private boolean enabled = true;

	/**
	 * Check configuration.
	 */
	@PostConstruct
	public void checkConfiguration() {
		enabled = !((issuerId == null) || (protocol == null) || (idpUrl == null));

		if (enabled) {
			formattedIssuerId = issuerId.replace("_", ":");
		} else {
			LOGGER.warn("Missing configuration. Will not provide security tockens until configured!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String requestToken(String username, String password) throws Exception {
		if (!enabled) {
			LOGGER.warn("Disabled token service due to missing configuration!");
			return null;
		}
		// get context path
		String contextPath = emfContext.getServletContext().getContextPath();
		if (StringUtils.isNullOrEmpty(contextPath)) {
			LOGGER.warn("Was unable to extract context path, using default which is : \\emf");
			contextPath = "/emf";
		}
		StringBuilder requstURI = new StringBuilder();
		requstURI.append(protocol);
		requstURI.append("://");
		requstURI.append(formattedIssuerId);
		requstURI.append(contextPath);
		requstURI.append(SAMLServiceLogin.SERVICE_LOGIN);
		final String consumerURL = requstURI.toString();
		String buildAuthenticationRequest = messageProcessor.buildAuthenticationRequest(issuerId,
				consumerURL);
		LOGGER.debug(
				"Issuer id is : {}, ConsumerURL is: {}, idp URL is: {}, provided username is: {}, provided pass is: {}",
				issuerId, consumerURL, idpUrl, username, password == null ? "NULL" : "NOT NULL");
		HttpClient client = new HttpClient();
		try {
			requstURI = new StringBuilder();
			requstURI.append(idpUrl);
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
			PostMethod post = buildAuthRequest(get.getResponseBodyAsString(), username, password,
					consumerURL);
			// post.setFollowRedirects(true);
			client.executeMethod(post);
			if (post.getStatusCode() == 302) {
				Header redirectLocation = post.getResponseHeader("Location");
				if (redirectLocation != null) {
					// NameValuePair[] parameters = post.getParameters();
					post = new PostMethod(redirectLocation.getValue());
					// post.addParameters(parameters);
					LOGGER.debug("Sending redirect as part of initial request to: {}",
							post.getURI());
					client.executeMethod(post);
				}
			}
			String samlResponse = getValueOfElement(post.getResponseBodyAsString(), "SAMLResponse");
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
	private PostMethod buildAuthRequest(String httpResponse, String username, String password,
			String consumerURL) {
		PostMethod post = null;
		String assertionString = getValueOfElement(httpResponse, "assertionString");
		if (ssoAuthenticationUrl == null) {
			post = new PostMethod(idpUrl);
			post.addParameter("assertionString", assertionString);
			post.addParameter("assertnConsumerURL", consumerURL);
			post.addParameter("issuer", issuerId);
			post.addParameter("id", "0");
			post.addParameter("subject", "null");
			post.addParameter("relyingPartySessionId", "null");
			post.addParameter("RelayState", "null");
		} else {
			String sessionDataKey = getValueOfElement(httpResponse, "sessionDataKey");
			String uri = ssoAuthenticationUrl + "?sessionDataKey=" + sessionDataKey;
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
	private String getValueOfElement(String html, String name) {
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
	 * Finds the closing character for the value string associated with the 'name' tag - might be '
	 * or "
	 * 
	 * @param html
	 *            is the html reponse to read from
	 * @param name
	 *            is the tag to find closing value string
	 * @param startIndexOfElement
	 *            is where to start looing for
	 * @return the closing character on throws {@link EmfRuntimeException} if not found
	 */
	private String getEndSuffix(String html, String name, int startIndexOfElement) {
		String endSuffix = null;
		int valueStartIndex = html.indexOf("value=\"", startIndexOfElement);
		int endIndexOfElement = html.indexOf(">", startIndexOfElement);
		if ((valueStartIndex != -1) && (valueStartIndex < (endIndexOfElement))) {
			endSuffix = "\"";
		} else {
			valueStartIndex = html.indexOf("value='", startIndexOfElement);
			if ((valueStartIndex != -1) && (valueStartIndex < (endIndexOfElement))) {
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
