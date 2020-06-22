package com.sirma.itt.seip.rest.secirity;

import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang.StringUtils;
import org.jose4j.jwt.NumericDate;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * {@link Authenticator} implementation for handling JWT authentications.
 *
 * @author yasko
 */
@Extension(target = Authenticator.NAME, order = 5)
public class JwtAuthenticator implements Authenticator {

	public static final String AUTHORIZATION_METHOD = "Jwt";
	private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private UserStore userStore;

	@Inject
	private SecurityTokensManager securityTokensManager;

	@Override
	public User authenticate(AuthenticationContext context) {
		String header = context.getProperty(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isBlank(header)) {
			return null;
		}

		String[] split = SPACE_PATTERN.split(header);
		if (split.length != 2 || !AUTHORIZATION_METHOD.equals(split[0]) || StringUtils.isBlank(split[1])) {
			return null;
		}
		return readUser(split[1]);
	}

	protected User readUser(String token) {
		Pair<String, NumericDate> userNameAndDate = securityTokensManager.readUserNameAndDate(token);
		if (userNameAndDate == null || userNameAndDate.getFirst() == null) {
			return null;
		}

		StringPair userAndTenant = SecurityUtil.getUserAndTenant(userNameAndDate.getFirst());
		String tenant = userAndTenant.getSecond();
		User user = securityContextManager.executeAsTenant(tenant).biFunction(this::loadUser, userNameAndDate, tenant);

		if (user == null) {
			throw new AuthenticationException(userNameAndDate.getFirst(), "User not found in the system");
		}

		userStore.setUserTicket(user, token);
		return user;
	}

	private User loadUser(Pair<String, NumericDate> userNameAndDate, String tenant) {
		if (securityTokensManager.isRevoked(userNameAndDate.getSecond())) {
			throw new AuthenticationException(userNameAndDate.getFirst(), "Token revoked");
		}
		return userStore.loadByIdentityId(userNameAndDate.getFirst(), tenant);
	}

	@Override
	public Object authenticate(User user) {
		// not supported
		return null;
	}

}
