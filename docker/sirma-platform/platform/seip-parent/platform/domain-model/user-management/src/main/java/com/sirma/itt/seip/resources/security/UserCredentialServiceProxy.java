package com.sirma.itt.seip.resources.security;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Proxy implementation of {@link UserCredentialService} that decides what actual implementation to use based on
 * {@link SecurityConfiguration#getIdpProviderName()} for the tenant.
 *
 * @author smustafov
 */
@Singleton
public class UserCredentialServiceProxy implements UserCredentialService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private EventService eventService;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private Contextual<UserCredentialService> delegate;

	@Inject
	@ExtensionPoint(UserCredentialService.NAME)
	private Plugins<UserCredentialService> credentialServices;

	@PostConstruct
	private void initialize() {
		delegate.initializeWith(this::resolveService);
	}

	private UserCredentialService resolveService() {
		String idpName = securityConfiguration.getIdpProviderName().get();
		return credentialServices.get(idpName).orElseThrow(() -> new EmfRuntimeException(
				"No " + UserCredentialService.class + " implementation with name " + idpName + " could be found"));
	}

	@Transactional
	@Override
	public boolean changeUserPassword(String username, String oldPassword, String newPassword) {
		boolean changed = getDelegate().changeUserPassword(username, oldPassword, newPassword);

		if (changed) {
			// revert the password in idp on failed transaction
			transactionSupport.invokeOnFailedTransaction(() -> {
				LOGGER.error("Changing password of {} failed in SEP. Trying to revert the password in idp.", username);
				getDelegate().changeUserPassword(username, newPassword, oldPassword);
			});

			eventService.fire(new UserPasswordChangeEvent(username, newPassword));
		}

		return changed;
	}



	@Override
	public String getName() {
		return getDelegate().getName();
	}

	private UserCredentialService getDelegate() {
		return delegate.getContextValue();
	}

}
