package com.sirma.itt.seip.rest.secirity;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * {@link Authenticator} implementation for handling JWT authentications via request parameters. The default supported
 * request parameter is {@value #PARAMETER_NAME}.<br>
 * Additional parameter could be defined via the configuration {@code security.authenticator.jwt.paramName=APIKey}
 *
 * @author BBonev
 */
@Extension(target = Authenticator.NAME, order = 6)
public class JwtParameterAuthenticator extends JwtAuthenticator {

	/**
	 * This is the default parameter named used in the application.
	 */
	public static final String PARAMETER_NAME = "jwt";

	@Inject
	private JwtConfiguration jwtConfiguration;

	@Override
	public User authenticate(AuthenticationContext context) {
		String parameterValue = context.getProperty(PARAMETER_NAME);
		if (StringUtils.isBlank(parameterValue)) {
			parameterValue = context.getProperty(jwtConfiguration.getJwtParameterName());
		}
		if (StringUtils.isBlank(parameterValue)) {
			return null;
		}
		return readUser(parameterValue);
	}

	@Override
	public Object authenticate(User user) {
		// not supported
		return null;
	}

}
