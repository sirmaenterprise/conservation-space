package com.sirma.itt.cmf.services.adapters;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * The needed rest client impl. It does nothing, all dms adapters should be mocked
 */
public class RESTClientMock implements RESTClient {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6756998137509780179L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String request(String uri, HttpMethod method) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream request(HttpMethod method, String uri) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod rawRequest(HttpMethod method, String uri) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, Part[] parts, boolean authentication) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, String content, boolean authentication)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI buildFullURL(final String relativeURL) throws URIException {
		return null;
	}

	/**
	 * Simulates checks auth for dms rest invokes. If no authentication is available exception is
	 * thrown.
	 */
	public static void checkAuthenticationInfo() {
		User authentication = SecurityContextManager.getRunAsAuthentication();
		if (authentication instanceof UserWithCredentials) {
			UserWithCredentials credentials = (UserWithCredentials) authentication;
			if (StringUtils.isNotNullOrEmpty(credentials.getName())) {
				String[] result = new String[3];
				result[0] = credentials.getName();
				if (StringUtils.isNotNullOrEmpty((String) credentials.getCredentials())) {
					result[1] = (String) credentials.getCredentials();
				}
				if (StringUtils.isNotNullOrEmpty(credentials.getTicket())) {
					SecurityContextManager.updateUserToken(credentials);
					result[2] = credentials.getTicket();
				}
				return;
			}
		} else {
			authentication = SecurityContextManager.getFullAuthentication();
			if (authentication instanceof UserWithCredentials) {
				UserWithCredentials credentials = (UserWithCredentials) authentication;
				if (StringUtils.isNotNullOrEmpty(credentials.getName())) {
					String[] result = new String[3];
					result[0] = credentials.getName();
					if (StringUtils.isNotNullOrEmpty((String) credentials.getCredentials())) {
						result[1] = (String) credentials.getCredentials();
					}
					if (StringUtils.isNotNullOrEmpty(credentials.getTicket())) {
						SecurityContextManager.updateUserToken(credentials);
						result[2] = credentials.getTicket();
					}
					return;
				}
			}
		}
		throw new IllegalStateException(
				"Must provide username and password for authenticating in the DMS");
	}
}
