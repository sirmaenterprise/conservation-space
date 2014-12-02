package com.sirma.itt.emf.authentication.idp;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreException;
import com.sirma.itt.emf.security.UserCredentialService;

/**
 * The IDPUserCredentialService is the idp specific backend for {@link UserCredentialService}. Make
 * use of {@link IdentityServerConnector}
 */
@ApplicationScoped
public class IDPUserCredentialServiceImpl implements UserCredentialService {

	private static final Logger LOGGER = Logger.getLogger(IDPUserCredentialServiceImpl.class);
	/** The user store manager. */
	@Inject
	private Instance<UserStoreManager> userStoreManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeUserPassword(String username, String oldPassword, String newPassword)
			throws Exception {
		try {
			userStoreManager.get().updateCredential(username, newPassword, oldPassword);
			return true;
		} catch (UserStoreException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Failed during password change process." + e.getMessage(), e);
			throw e;
		}
	}

}
