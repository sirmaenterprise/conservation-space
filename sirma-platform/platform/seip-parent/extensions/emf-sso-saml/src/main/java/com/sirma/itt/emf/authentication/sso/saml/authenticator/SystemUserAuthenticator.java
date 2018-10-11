/**
 *
 */
package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.sirma.itt.emf.authentication.sso.saml.SAMLMessageProcessor;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Th SystemUserAuthenticator is responsible to login system users only using a generated token.
 *
 * @author bbanchev
 */
@Extension(target = Authenticator.NAME, order = 20)
public class SystemUserAuthenticator extends BaseSamlAuthenticator {
	@Inject
	private Instance<SecurityConfiguration> securityConfiguration;
	@Inject
	private IDPConfiguration idpConfiguration;
	@Inject
	private SAMLMessageProcessor samlMessageProcessor;
	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public User authenticate(AuthenticationContext authenticationContext) {
		return null;
	}

	@Override
	public Object authenticate(User toAuthenticate) {
		return authenticateById(toAuthenticate, toAuthenticate.getIdentityId());
	}

	private Object authenticateById(User toAuthenticate, final String username) {
		if (StringUtils.isBlank(username)) {
			return null;
		}
		String userSimpleName = SecurityUtil.getUserWithoutTenant(username);
		if (isSystemUser(userSimpleName) || isSystemAdmin(userSimpleName)) {
			return authenticateWithTokenAndGetTicket(toAuthenticate,
					createToken(username, securityConfiguration.get().getCryptoKey().get()));
		}
		return null;
	}

	@SuppressWarnings("static-method")
	protected boolean isSystemAdmin(String userSimpleName) {
		return EqualsHelper.nullSafeEquals(SecurityContext.getSystemAdminName(), userSimpleName, true);
	}

	protected boolean isSystemUser(String userSimpleName) {
		return EqualsHelper.nullSafeEquals(userSimpleName,
				SecurityUtil.getUserWithoutTenant(SecurityContext.SYSTEM_USER_NAME), true);
	}

	@Override
	protected void completeAuthentication(User authenticated, SAMLTokenInfo info, Map<String, String> processedToken) {
		authenticated.getProperties().putAll(processedToken);
	}

	/**
	 * Creates a token for given user.
	 *
	 * @param user
	 *            the user to create for
	 * @param secretKey
	 *            is the encrypt key for saml token
	 * @return the saml token
	 */
	protected byte[] createToken(String user, SecretKey secretKey) {
		return Base64.encodeBase64(SecurityUtil.encrypt(
				createResponse(systemConfiguration.getSystemAccessUrl().getOrFail().toString(),
						samlMessageProcessor.getIssuerId().get(), idpConfiguration.getIdpServerURL().get(), user),
				secretKey));
	}

	/**
	 * Creates the response for authentication in DMS. The time should be synchronized
	 *
	 * @param assertionUrl
	 *            the alfresco url
	 * @param audianceUrl
	 *            the audiance url
	 * @param samlURL
	 *            the saml url
	 * @param user
	 *            the user to authenticate
	 * @return the resulted saml2 message
	 */
	@SuppressWarnings("static-method")
	protected byte[] createResponse(String assertionUrl, String audianceUrl, String samlURL, String user) {

		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTime barrier = now.plusMinutes(10);
		StringBuilder saml = new StringBuilder(2048);
		saml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("<saml2p:Response ID=\"inppcpljfhhckioclinjenlcneknojmngnmgklab\" IssueInstant=\"").append(now)
				.append("\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\">")
				.append("<saml2p:Status>")
				.append("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>")
				.append("</saml2p:Status>")
				.append("<saml2:Assertion ID=\"ehmifefpmmlichdcpeiogbgcmcbafafckfgnjfnk\" IssueInstant=\"").append(now)
				.append("\" Version=\"2.0\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">")
				.append("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">").append(samlURL)
				.append("</saml2:Issuer>").append("<saml2:Subject>").append("<saml2:NameID>").append(user)
				.append("</saml2:NameID>")
				.append("<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">")
				.append("<saml2:SubjectConfirmationData InResponseTo=\"0\" NotOnOrAfter=\"").append(barrier)
				.append("\" Recipient=\"").append(assertionUrl).append("\"/>").append("</saml2:SubjectConfirmation>")
				.append("</saml2:Subject>").append("<saml2:Conditions NotBefore=\"").append(now)
				.append("\" NotOnOrAfter=\"").append(barrier).append("\">").append("<saml2:AudienceRestriction>")
				.append("<saml2:Audience>").append(audianceUrl).append("</saml2:Audience>")
				.append("</saml2:AudienceRestriction>").append("</saml2:Conditions>")
				.append("<saml2:AuthnStatement AuthnInstant=\"").append(now).append("\">")
				.append("<saml2:AuthnContext>")
				.append("<saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml2:AuthnContextClassRef>")
				.append("</saml2:AuthnContext>").append("</saml2:AuthnStatement>").append("</saml2:Assertion>")
				.append("</saml2p:Response>");
		return saml.toString().getBytes(StandardCharsets.UTF_8);
	}
}
