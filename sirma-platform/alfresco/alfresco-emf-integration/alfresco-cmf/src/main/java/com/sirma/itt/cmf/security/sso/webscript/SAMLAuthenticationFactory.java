package com.sirma.itt.cmf.security.sso.webscript;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;
import com.sirma.itt.cmf.security.sso.SAMLSSOConfigurations;
import com.sirma.itt.cmf.security.sso.WSO2SAMLClient;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * A factory for creating OpenSSOAuthentication objects.
 */
@SuppressWarnings("restriction")
public class SAMLAuthenticationFactory implements ServletAuthenticatorFactory {

	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(SAMLAuthenticationFactory.class);
	/** The debug enabled. */
	private boolean debugEnabled = LOGGER.isDebugEnabled();

	/** The sso enabled. */
	private boolean ssoEnabled = false;
	/** The authentication service. */
	private AuthenticationService authenticationService;
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;
	/** The client. */
	private WSO2SAMLClient client;
	/** The client. */
	private SysAdminParams sysAdminParams;

	/**
	 * The Class OpenSSOAuthenticator.
	 */
	public class SSOAuthenticator implements Authenticator {

		/** The Constant KEY_SUBJECT. */
		private static final String KEY_SUBJECT = "Subject";

		/** The Constant KEY_SAML_TOKEN. */
		private static final String KEY_SAML_TOKEN = "SAMLToken";

		/** The Constant KEY_ALF_AUTH_TICKET. */
		private static final String KEY_ALF_AUTH_TICKET = "_alfAuthTicket";

		/** The servlet req. */
		private WebScriptServletRequest servletReq;

		/** The servlet res. */
		private WebScriptServletResponse servletRes;

		/** The client. */
		private WSO2SAMLClient client;

		/** The authorization. */
		private String authorization;

		/** The ticket. */
		private String ticket;

		/**
		 * Instantiates a new open sso authenticator.
		 *
		 * @param req
		 *            the req
		 * @param res
		 *            the res
		 * @param client
		 *            the client
		 */
		public SSOAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res, WSO2SAMLClient client) {
			this.servletReq = req;
			this.servletRes = res;
			this.client = client;
			HttpServletRequest httpReq = servletReq.getHttpServletRequest();

			this.authorization = httpReq.getHeader("Authorization");
			Object alfTicket = httpReq.getAttribute("alf_ticket");
			if (alfTicket instanceof String) {
				this.ticket = alfTicket.toString();
			} else {
				this.ticket = httpReq.getParameter("alf_ticket");
			}
		}

		/**
		 * Gets the this url.
		 *
		 * @param request
		 *            the request
		 * @return the this url
		 */
		public String getThisURL(HttpServletRequest request) {
			return request.getServletPath() + request.getPathInfo();
		}

		@Override
		public boolean authenticate(Description.RequiredAuthentication required, boolean isGuest) {

			HttpServletRequest request = this.servletReq.getHttpServletRequest();
			HttpServletResponse response = this.servletRes.getHttpServletResponse();
			HttpSession httpSession = request.getSession();
			boolean authorized = false;
			//
			// validate credentials
			//
			if (isDebugEnabled()) {
				LOGGER.debug(new StringBuilder("HTTP Authorization provided: ")
						.append((authorization != null && authorization.length() > 0)).append(". URL ticket provided: ")
						.append((ticket != null && ticket.length() > 0)).toString());
			}
			// authenticate as guest, if service allows
			if (isGuest && RequiredAuthentication.guest == required) {
				if (isDebugEnabled())
					LOGGER.debug("Authenticating as Guest");

				authenticationService.authenticateAsGuest();
				authorized = true;
			} else

			// authenticate as guest, if service allows
				if (ticket != null && ticket.length() > 0) {
				try {
					if (isDebugEnabled()) {
						LOGGER.debug(
								new StringBuilder("Authenticating (URL argument) ticket ").append(ticket).toString());
					}
					// assume a ticket has been passed
					authenticationService.validate(ticket);
					authorized = true;
				} catch (AuthenticationException e) {
					// failed authentication
				}
			}

			// authenticate as specified by HTTP Basic Authentication
			else if (authorization != null && authorization.length() > 0) {
				try {
					String[] authorizationParts = authorization.split(" ");
					if (!authorizationParts[0].equalsIgnoreCase("basic")) {
						throw new WebScriptException("Authorization '" + authorizationParts[0] + "' not supported.");
					}

					String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
					Authorization auth = new Authorization(decodedAuthorisation);
					if (auth.isTicket()) {
						if (isDebugEnabled()) {
							LOGGER.debug(new StringBuilder("Authenticating (BASIC HTTP) ticket ")
									.append(auth.getTicket()).toString());
						}
						// assume a ticket has been passed
						authenticationService.validate(auth.getTicket());
						authorized = true;
					} else {
						if (isDebugEnabled()) {
							LOGGER.debug(new StringBuilder("Authenticating (BASIC HTTP) user ")
									.append(auth.getUserName()).toString());
						}
						// No longer need a special call to authenticate as
						// guest
						// Leave guest name resolution up to the services
						authenticationService.authenticate(auth.getUserName(), auth.getPassword().toCharArray());
						authorized = true;
					}
				} catch (AuthenticationException e) {
					// failed authentication
					LOGGER.warn("Failed Authentication: " + e.getMessage());
				}
			} else {
				//
				if (httpSession.getAttribute(KEY_ALF_AUTH_TICKET) == null) {
					HttpServletResponse httpServletResponse = this.servletRes.getHttpServletResponse();
					// httpSession.invalidate();
					String header = request.getHeader(KEY_SAML_TOKEN);

					if (header != null && SAMLSSOConfigurations.isAllowedAudience(request.getRemoteAddr())) {
						// as header terminated by a carriage return (CR) and
						// line feed (LF) character sequence

						Map<String, Object> processResponseMessage = client
								.processStandaloneResponseMessage(decrypt(header.replace("\t", "\r\n")));
						if (processResponseMessage.containsKey(KEY_SUBJECT)) {
							if (SAMLSSOConfigurations.isUsingTimeConstraints()) {
								DateTime now = new DateTime(DateTimeZone.UTC);
								if (now.compareTo((DateTime)processResponseMessage.get("NotBefore")) >= 0
										&& now.compareTo((DateTime)processResponseMessage.get("NotOnOrAfter")) < 0) {
									String userName = (String) processResponseMessage.get(KEY_SUBJECT);
									authorized = login(userName);
								} else {
									LOGGER.error(
											" Current SAMLToken is invalid! " + processResponseMessage + " for " + now);
								}
							} else {
								final String userName = (String) processResponseMessage.get(KEY_SUBJECT);
								authorized = login(userName);

							}
						}
					} else {
						String ticket = request.getHeader("alf_ticket");

						if (ticket != null) {
							try {
								if (isDebugEnabled()) {
									LOGGER.debug(new StringBuilder("Authenticating (URL argument) ticket ")
											.append(ticket).toString());
								}
								// assume a ticket has been passed
								authenticationService.validate(ticket);
								authorized = true;
							} catch (AuthenticationException e) {
								LOGGER.warn("Failed Authentication: " + e.getMessage());
							}
						} else {
							if (isDebugEnabled()) {
								LOGGER.debug(new StringBuilder("Header 'SAMLToken' invalid or ip: ")
										.append(request.getRemoteAddr())
										.append(" not allowed! Redirecting to login page...").toString());
							}
							String requestMessage = client.buildRequestMessage(request);
							try {
								httpServletResponse.sendRedirect(requestMessage);
							} catch (Exception e) {
								throw new WebScriptException("Redirection failure!", e);
							}
							return false;
						}
					}
				} else {
					org.alfresco.web.bean.repository.User attribute = (org.alfresco.web.bean.repository.User) httpSession
							.getAttribute(KEY_ALF_AUTH_TICKET);
					SAMLAuthenticationFactory.this.authenticationComponent.setCurrentUser(attribute.getUserName());
					request.setAttribute("alf_ticket", attribute.getTicket());
					return true;
				}

			}

			if (!authorized) {
				if (isDebugEnabled())
					LOGGER.debug("Requesting authorization credentials");

				response.setStatus(401);
				response.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
			}
			return authorized;
		}

		private boolean login(final String userName) {
			if (isDebugEnabled()) {
				LOGGER.debug("Login credential: " + userName);
			}
			String usernameLocal = userName;
			int domainSeparationIndex = -1;
			if ((domainSeparationIndex = usernameLocal.indexOf('/')) > 0) {
				usernameLocal = usernameLocal.substring(domainSeparationIndex + 1);
			}
			AuthenticationUtil.clearCurrentSecurityContext();
			SAMLAuthenticationFactory.this.authenticationComponent.setCurrentUser(usernameLocal);
			return true;
		}

		/**
		 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user-
		 * name- and-password-from-a-configuration-file
		 *
		 * @param plainText
		 *            is the plain text
		 * @return the encrypted password or throws runtime exception on error
		 */
		public String encrypt(String plainText) {
			// only the first 8 Bytes of the constructor argument are used
			// as material for generating the keySpec
			try {
				SecretKey key = SAMLSSOConfigurations.getDESEncryptKey();

				BASE64Encoder base64encoder = new BASE64Encoder();
				// ENCODE plainTextPassword String
				byte[] cleartext = plainText.getBytes(SAMLSSOConfigurations.UTF_8);

				Cipher cipher = Cipher.getInstance("DES"); // cipher is not
															// thread
															// safe
				cipher.init(Cipher.ENCRYPT_MODE, key);
				return base64encoder.encode(cipher.doFinal(cleartext));
			} catch (Exception e) {
				throw new SEIPRuntimeException(plainText + " is not encrypted, due to exception: " + e.getMessage(), e);
			}
		}

		/**
		 * Decrypt the encrypted text, with the current private key.
		 *
		 * @param encrypted
		 *            the encrypted is the text
		 * @return the decrypted text or throws runtime exception on error
		 */
		public String decrypt(String encrypted) {
			try {
				sun.misc.BASE64Decoder base64decoder = new BASE64Decoder();
				// DECODE encrypted String
				byte[] encrypedBytes = base64decoder.decodeBuffer(encrypted);

				SecretKey key = SAMLSSOConfigurations.getDESEncryptKey();

				Cipher cipher = Cipher.getInstance("DES");// cipher is not
															// thread safe
				cipher.init(Cipher.DECRYPT_MODE, key);
				return new String(cipher.doFinal(encrypedBytes), SAMLSSOConfigurations.UTF_8);
			} catch (Exception e) {
				throw new SEIPRuntimeException(encrypted + " is not decrypted, due to exception: " + e.getMessage(), e);
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.extensions.webscripts.Authenticator#
		 * emptyCredentials ()
		 */
		@Override
		public boolean emptyCredentials() {
			HttpServletRequest httpServletRequest = this.servletReq.getHttpServletRequest();
			String ticket = httpServletRequest.getParameter("alf_ticket");
			return (ticket == null) || (ticket.length() == 0);
		}
	}

	/**
	 * The Class BasicHttpAuthenticator.
	 */
	public class BasicHttpAuthenticator implements Authenticator {
		// dependencies
		/** The servlet req. */
		private WebScriptServletRequest servletReq;

		/** The servlet res. */
		private WebScriptServletResponse servletRes;

		/** The authorization. */
		private String authorization;

		/** The ticket. */
		private String ticket;

		/**
		 * Construct.
		 *
		 * @param req
		 *            the req
		 * @param res
		 *            the res
		 */
		public BasicHttpAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res) {
			this.servletReq = req;
			this.servletRes = res;

			HttpServletRequest httpReq = servletReq.getHttpServletRequest();

			this.authorization = httpReq.getHeader("Authorization");
			this.ticket = httpReq.getParameter("alf_ticket");
		}

		@Override
		public boolean authenticate(RequiredAuthentication required, boolean isGuest) {
			boolean authorized = false;

			//
			// validate credentials
			//

			HttpServletResponse res = servletRes.getHttpServletResponse();

			if (isDebugEnabled()) {
				LOGGER.debug("HTTP Authorization provided: " + (authorization != null && authorization.length() > 0));
				LOGGER.debug("URL ticket provided: " + (ticket != null && ticket.length() > 0));
			}

			// authenticate as guest, if service allows
			if (isGuest && RequiredAuthentication.guest == required) {
				if (isDebugEnabled())
					LOGGER.debug("Authenticating as Guest");

				try {
					authenticationService.authenticateAsGuest();
					authorized = true;
				} catch (AuthenticationException ex) {
					// failed authentication
				}
			}

			// authenticate as specified by explicit ticket on url
			else if (ticket != null && ticket.length() > 0) {
				try {
					if (isDebugEnabled())
						LOGGER.debug("Authenticating (URL argument) ticket " + ticket);

					// assume a ticket has been passed
					authenticationService.validate(ticket);
					authorized = true;
				} catch (AuthenticationException e) {
					// failed authentication
				}
			}

			// authenticate as specified by HTTP Basic Authentication
			else if (authorization != null && authorization.length() > 0) {
				try {
					String[] authorizationParts = authorization.split(" ");
					if (!authorizationParts[0].equalsIgnoreCase("basic")) {
						throw new WebScriptException("Authorization '" + authorizationParts[0] + "' not supported.");
					}

					String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
					Authorization auth = new Authorization(decodedAuthorisation);
					if (auth.isTicket()) {
						if (isDebugEnabled())
							LOGGER.debug("Authenticating (BASIC HTTP) ticket " + auth.getTicket());

						// assume a ticket has been passed
						authenticationService.validate(auth.getTicket());
						authorized = true;
					} else {
						if (isDebugEnabled())
							LOGGER.debug("Authenticating (BASIC HTTP) user " + auth.getUserName());

						// No longer need a special call to authenticate as
						// guest
						// Leave guest name resolution up to the services
						authenticationService.authenticate(auth.getUserName(), auth.getPassword().toCharArray());
						authorized = true;
					}
				} catch (AuthenticationException e) {
					// failed authentication
				}
			}

			//
			// request credentials if not authorized
			//
			if (!authorized) {
				if (isDebugEnabled())
					LOGGER.debug("Requesting authorization credentials");

				res.setStatus(401);
				res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
			}
			return authorized;
		}

		@Override
		public boolean emptyCredentials() {
			return ((ticket == null || ticket.length() == 0) && (authorization == null || authorization.length() == 0));
		}
	}

	/**
	 * Instantiates a new sAML authentication factory.
	 */
	public SAMLAuthenticationFactory() {
		ssoEnabled = SAMLSSOConfigurations.isSSOEnabled();
	}

	@Override
	public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res) {
		if (ssoEnabled) {
			if (client == null) {
				client = new WSO2SAMLClient(req.getHttpServletRequest());
			}
			return new SSOAuthenticator(req, res, client);
		}
		return new BasicHttpAuthenticator(req, res);
	}

	/**
	 * Gets the authentication component.
	 *
	 * @return the authentication component
	 */
	public AuthenticationComponent getAuthenticationComponent() {
		return this.authenticationComponent;
	}

	/**
	 * Sets the authentication component.
	 *
	 * @param authenticationComponent
	 *            the new authentication component
	 */
	public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
		this.authenticationComponent = authenticationComponent;
	}

	/**
	 * Gets the authentication service.
	 *
	 * @return the authentication service
	 */
	public AuthenticationService getAuthenticationService() {
		return this.authenticationService;
	}

	/**
	 * Sets the authentication service.
	 *
	 * @param authenticationService
	 *            the new authentication service
	 */
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * Gets the sys admin params.
	 *
	 * @return the sysAdminParams
	 */
	public SysAdminParams getSysAdminParams() {
		return sysAdminParams;
	}

	/**
	 * Sets the sys admin params.
	 *
	 * @param sysAdminParams
	 *            the sysAdminParams to set
	 */
	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	/**
	 * Debug enabled.
	 *
	 * @return true, if successful
	 */
	private boolean isDebugEnabled() {
		return debugEnabled;
	}
}
