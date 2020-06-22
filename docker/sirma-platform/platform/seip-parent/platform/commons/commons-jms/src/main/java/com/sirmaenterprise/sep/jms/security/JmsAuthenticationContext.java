package com.sirmaenterprise.sep.jms.security;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;

/**
 * Special {@link AuthenticationContext} used by the {@link JmsSecurityAuthenticator} to provude authentication for
 * receiving messages and overriding the request info
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/05/2017
 */
public class JmsAuthenticationContext implements AuthenticationContext {

	private Map<String, String> properties = new HashMap<>(8);

	@Override
	public Set<String> getKeys() {
		return properties.keySet();
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Set the custom request id
	 *
	 * @param requestId the requested id to set
	 * @return current instance
	 */
	public JmsAuthenticationContext setRequestId(String requestId) {
		addNonNullValue(properties, CORRELATION_REQUEST_ID, requestId);
		return this;
	}

	/**
	 * Set the user that need to be authenticated
	 *
	 * @param authenticatedUser the user system id
	 * @return current instance
	 */
	public JmsAuthenticationContext setAuthenticatedUser(String authenticatedUser) {
		addNonNullValue(properties, CommunicationConstants.AUTHENTICATED_USER_KEY, authenticatedUser);
		return this;
	}

	/**
	 * Get the authenticated user id
	 *
	 * @return the user id
	 */
	public String getAuthenticatedUser() {
		return getProperty(CommunicationConstants.AUTHENTICATED_USER_KEY);
	}

	/**
	 * Set the tenant id that should be initialized
	 *
	 * @param tenantId the tenant id to set
	 * @return current instance
	 */
	public JmsAuthenticationContext setTenantId(String tenantId) {
		addNonNullValue(properties, CommunicationConstants.TENANT_ID_KEY, tenantId);
		return this;
	}

	/**
	 * Get the requested tenant
	 *
	 * @return the tenant id
	 */
	public String getTenantId() {
		return getProperty(CommunicationConstants.TENANT_ID_KEY);
	}
}
