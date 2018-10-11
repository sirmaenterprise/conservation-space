package com.sirma.itt.seip.security.context;

import java.lang.invoke.MethodHandles;

import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;

/**
 * Security context that is backed out by the {@link SecurityContextHolder}. Instance of the class is used as universal
 * injection.
 *
 * @author BBonev
 */
@NoOperation
class ThreadLocalSecurityContext implements SecurityContext {
	private static final long serialVersionUID = 7599821299180796574L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final SecurityContext INSTANCE = new ThreadLocalSecurityContext();

	/**
	 * Gets the single instance of ThreadLocalSecurityContext.
	 *
	 * @return single instance of ThreadLocalSecurityContext
	 */
	@Produces
	@SuppressWarnings("static-method")
	public SecurityContext produceInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the single instance of ThreadLocalSecurityContext.
	 *
	 * @return single instance of ThreadLocalSecurityContext
	 */
	static SecurityContext getInstance() {
		return INSTANCE;
	}

	@Override
	public User getAuthenticated() {
		if (!isActive()) {
			throw new ContextNotActiveException();
		}
		return SecurityContextHolder.getContext().getAuthenticated();
	}

	@Override
	public User getEffectiveAuthentication() {
		if (!isActive()) {
			throw new ContextNotActiveException();
		}
		return SecurityContextHolder.getContext().getEffectiveAuthentication();
	}

	@Override
	public boolean isAuthenticated() {
		return isActive() && SecurityContextHolder.getContext().isAuthenticated();
	}

	@Override
	public boolean isActive() {
		return SecurityContextHolder.isSet();
	}

	@Override
	public String getCurrentTenantId() {
		if (isActive()) {
			return SecurityContextHolder.getContext().getCurrentTenantId();
		}
		LOGGER.warn("Security context is not active - returning system tenant id!");
		return SYSTEM_TENANT;
	}

	@Override
	public String getRequestId() {
		if (isActive()) {
			return SecurityContextHolder.getContext().getRequestId();
		}
		// if the context is not active, yet. we will return null so that new request id is generated
		return null;
	}

	@Override
	public String toString() {
		return new StringBuilder(128)
				.append("ThreadLocalSecurityContext [isActive=")
					.append(isActive())
					.append(", tenantId=")
					.append(getCurrentTenantId())
					.append(", requestId=")
					.append(getRequestId())
					.append("]")
					.toString();
	}
}