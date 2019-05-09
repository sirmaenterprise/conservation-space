package com.sirmaenterprise.sep.jms.security;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;
import com.sirmaenterprise.sep.jms.api.MessageConsumerListener;

/**
 * Initialize the security context when processing messages
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/06/2017
 */
@Extension(target = MessageConsumerListener.EXTENSION_NAME, order = -1)
public class SecurityMessageListener implements MessageConsumerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private UserStore userStore;

	/**
	 * For tracking if the context is initialized twice so it can be destroyed twice.
	 */
	private boolean doubleContextExecution = false;

	@Override
	public void beforeMessage(Message message) {
		boolean authenticated = tryAuthenticate(message);
		// if the authentication fails try initializing the tenant and system user
		if (!authenticated) {
			securityContextManager.initializeTenantContext(readCurrentTenant(message), readRequestId(message));
		}
	}

	@Override
	public void onSuccess() {
		securityContextManager.endContextExecution();
		if (doubleContextExecution) {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	public void onError(Exception e) {
		securityContextManager.endContextExecution();
		if (doubleContextExecution) {
			securityContextManager.endContextExecution();
		}
	}

	private boolean tryAuthenticate(Message message) {
		AuthenticationContext authContext = buildAuthenticationContext(message);
		if (authContext != null && securityContextManager.initializeExecution(authContext)) {
			// if we have some special user effective authentication try setting it
			User effectiveAuthentication = getEffectiveAuthentication(message);
			if (effectiveAuthentication != null) {
				securityContextManager.beginContextExecution(effectiveAuthentication);
				doubleContextExecution = true;
			}
			return true;
		}
		return false;
	}

	private static AuthenticationContext buildAuthenticationContext(Message message) {
		try {
			String requestId = readRequestId(message);
			String authenticated = message.getStringProperty(CommunicationConstants.AUTHENTICATED_USER_KEY);
			String tenantId = message.getStringProperty(CommunicationConstants.TENANT_ID_KEY);

			if (authenticated != null && tenantId != null) {
				return new JmsAuthenticationContext()
						.setRequestId(requestId)
						.setTenantId(tenantId)
						.setAuthenticatedUser(authenticated);
			}
		} catch (JMSException e) {
			LOGGER.warn("Could not read security properties. Falling back to tenant initialization", e);
		}
		return null;
	}

	private User getEffectiveAuthentication(Message message) {
		try {
			String effectiveUserId = message.getStringProperty(CommunicationConstants.EFFECTIVE_USER_KEY);
			String currentEffective = Objects.toString(securityContextManager.getCurrentContext()
					.getEffectiveAuthentication().getSystemId(), null);

			if (nullSafeEquals(effectiveUserId, currentEffective, true)) {
				// if the current effective authentication is the same with the requested
				// we do not need to do anything special. it's already initialized
				return null;
			}
			return userStore.loadBySystemId(effectiveUserId);
		} catch (JMSException e) {
			LOGGER.warn("Could not read effective authentication", e);
			return null;
		}
	}

	private static String readCurrentTenant(Message message) {
		try {
			String tenantId = message.getStringProperty(CommunicationConstants.TENANT_ID_KEY);
			return EqualsHelper.getOrDefault(trimToNull(tenantId), SecurityContext.SYSTEM_TENANT);
		} catch (JMSException e) {
			LOGGER.trace("Could not read tenant", e);
			return SecurityContext.SYSTEM_TENANT;
		}
	}

	private static String readRequestId(Message message) {
		try {
			return message.getStringProperty(CommunicationConstants.REQUEST_ID_KEY);
		} catch (JMSException e) {
			LOGGER.trace("Could not read request id", e);
			return null;
		}
	}
}
