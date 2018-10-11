package com.sirma.itt.seip.shared.security;

import java.lang.invoke.MethodHandles;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.shared.ShareCodeUtils;
import com.sirma.itt.seip.shared.exception.ShareCodeValidationException;

/**
 * Authenticator for requests that use a share code. The share code contains the user and tenant.
 * The share code is verified to ensure that the the user is not trying to access a different
 * resource.
 *
 * @author nvelkov
 */
@Extension(target = Authenticator.NAME, order = 101)
public class ShareCodeSecurityAuthenticator implements Authenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String SHARE_CODE = "shareCode";
	public static final String SECRET_KEY = "secretKey";
	public static final String RESOURCE_ID = "resourceId";

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private UserStore userStore;

	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		String shareCode = authenticationContext.getProperty(SHARE_CODE);
		String secretKey = authenticationContext.getProperty(SECRET_KEY);
		String resourceId = authenticationContext.getProperty(RESOURCE_ID);

		if (StringUtils.isNotEmpty(resourceId)) {
			try {
				if (ShareCodeUtils.verify(resourceId, shareCode, secretKey)) {
					String user = ShareCodeUtils.deconstruct(resourceId, shareCode, secretKey).getUser();
					StringPair username = SecurityUtil.getUserAndTenant(user);
					return securityContextManager.executeAsTenant(username.getSecond())
							.function(userStore::loadByIdentityId, user);
				}
			} catch (ShareCodeValidationException e) {
				LOGGER.warn("Error during share code authentication {}", e.getMessage());
				LOGGER.trace("Error during share code authentication.", e);
			}
		}
		return null;
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		return null;
	}
}
