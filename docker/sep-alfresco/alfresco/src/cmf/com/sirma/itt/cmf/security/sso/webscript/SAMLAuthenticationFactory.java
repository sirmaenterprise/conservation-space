package com.sirma.itt.cmf.security.sso.webscript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.sirma.itt.cmf.security.sso.WSO2SAMLClient;

/**
 * A factory for creating OpenSSOAuthentication objects.
 */
public class SAMLAuthenticationFactory implements ServletAuthenticatorFactory {
	/** The sso enabled. */
	private boolean ssoEnabled = false;
	/** The authentication service. */
	private AuthenticationService authenticationService;

	/** The logger. */
	private Logger logger = Logger.getLogger(getClass());
	/** The authentication component. */
	private AuthenticationComponent authenticationComponent;

	/** The client. */
	private WSO2SAMLClient client;
	/** The client. */
	private SysAdminParams sysAdminParams;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.extensions.webscripts.servlet.ServletAuthenticatorFactory
	 * #create(org.springframework.extensions.webscripts.servlet.
	 * WebScriptServletRequest,
	 * org.springframework.extensions.webscripts.servlet
	 * .WebScriptServletResponse)
	 */
	@Override
	public Authenticator create(WebScriptServletRequest req, WebScriptServletResponse res) {
		if (ssoEnabled) {
			client = new WSO2SAMLClient(req.getHttpServletRequest());
			return new SSOAuthenticator(req, res, client);
		}
		return new BasicHttpAuthenticator(req, res);
	}

	/**
	 * Instantiates a new sAML authentication factory.
	 */
	public SAMLAuthenticationFactory() {
		ssoEnabled = WSO2SAMLClient.isSSOEnabled();
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
		public SSOAuthenticator(WebScriptServletRequest req, WebScriptServletResponse res,
				WSO2SAMLClient client) {
			this.servletReq = req;
			this.servletRes = res;
			this.client = client;
			HttpServletRequest httpReq = servletReq.getHttpServletRequest();

			this.authorization = httpReq.getHeader("Authorization");
			Object attribute = httpReq.getAttribute("alf_ticket");
			if (attribute instanceof String) {
				this.ticket = attribute.toString();
			} else {
				this.ticket = httpReq.getParameter("alf_ticket");
			}
		}

		/** The authorization. */
		private String authorization;

		/** The ticket. */
		private String ticket;

		private SecretKey cipherKey;

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.extensions.webscripts.Authenticator#authenticate
		 * (org
		 * .springframework.extensions.webscripts.Description.RequiredAuthentication
		 * , boolean)
		 */
		@Override
		public boolean authenticate(Description.RequiredAuthentication required, boolean isGuest) {

			HttpServletRequest request = this.servletReq.getHttpServletRequest();
			HttpServletResponse response = this.servletRes.getHttpServletResponse();
			HttpSession httpSession = request.getSession();
			boolean authorized = false;
			//
			// validate credentials
			//
			if (logger.isDebugEnabled()) {
				logger.debug("HTTP Authorization provided: "
						+ (authorization != null && authorization.length() > 0));
				logger.debug("URL ticket provided: " + (ticket != null && ticket.length() > 0));
			}
			// authenticate as guest, if service allows
			if (isGuest && RequiredAuthentication.guest == required) {
				if (logger.isDebugEnabled())
					logger.debug("Authenticating as Guest");

				authenticationService.authenticateAsGuest();
				authorized = true;
			} else

			// authenticate as guest, if service allows
			if (ticket != null && ticket.length() > 0) {
				try {
					if (logger.isDebugEnabled())
						logger.debug("Authenticating (URL argument) ticket " + ticket);
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
						throw new WebScriptException("Authorization '" + authorizationParts[0]
								+ "' not supported.");
					}

					String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
					Authorization auth = new Authorization(decodedAuthorisation);
					if (auth.isTicket()) {
						if (logger.isDebugEnabled())
							logger.debug("Authenticating (BASIC HTTP) ticket " + auth.getTicket());

						// assume a ticket has been passed
						authenticationService.validate(auth.getTicket());
						authorized = true;
					} else {
						if (logger.isDebugEnabled())
							logger.debug("Authenticating (BASIC HTTP) user " + auth.getUserName());

						// No longer need a special call to authenticate as
						// guest
						// Leave guest name resolution up to the services
						authenticationService.authenticate(auth.getUserName(), auth.getPassword()
								.toCharArray());
						authorized = true;
					}
				} catch (AuthenticationException e) {
					// failed authentication
				}
			} else {
				//
				if (httpSession.getAttribute(KEY_ALF_AUTH_TICKET) == null) {
					HttpServletResponse httpServletResponse = this.servletRes
							.getHttpServletResponse();
					// httpSession.invalidate();
					String header = request.getHeader(KEY_SAML_TOKEN);

					if (header != null) {
						// as header terminated by a carriage return (CR) and
						// line feed (LF) character sequence
						Map<String, String> processResponseMessage = client
								.processResponseMessage(decrypt(header.replace("\t", "\r\n")));
						if (processResponseMessage.containsKey(KEY_SUBJECT)) {
							String userName = processResponseMessage.get(KEY_SUBJECT);
							SAMLAuthenticationFactory.this.authenticationComponent
									.setCurrentUser(userName);
							authorized = true;
						}
					} else {
						logger.debug("Header null");

						String requestMessage = client.buildRequestMessage(request);
						try {
							httpServletResponse.sendRedirect(requestMessage);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return false;
					}
				} else {
					org.alfresco.web.bean.repository.User attribute = (org.alfresco.web.bean.repository.User) httpSession
							.getAttribute(KEY_ALF_AUTH_TICKET);
					SAMLAuthenticationFactory.this.authenticationComponent.setCurrentUser(attribute
							.getUserName());
					request.setAttribute("alf_ticket", attribute.getTicket());
					return true;
				}

				//

			}

			if (!authorized) {
				if (logger.isDebugEnabled())
					logger.debug("Requesting authorization credentials");

				response.setStatus(401);
				response.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
			}
			return authorized;
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
				SecretKey key = getCipherKey();

				BASE64Encoder base64encoder = new BASE64Encoder();
				// ENCODE plainTextPassword String
				byte[] cleartext = plainText.getBytes("UTF-8");

				Cipher cipher = Cipher.getInstance("DES"); // cipher is not
															// thread
															// safe
				cipher.init(Cipher.ENCRYPT_MODE, key);
				String encryptedPwd = base64encoder.encode(cipher.doFinal(cleartext));
				return encryptedPwd;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(plainText + " is not encrypted, due to exception: "
						+ e.getMessage());
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

				SecretKey key = getCipherKey();

				Cipher cipher = Cipher.getInstance("DES");// cipher is not
															// thread safe
				cipher.init(Cipher.DECRYPT_MODE, key);
				return new String(cipher.doFinal(encrypedBytes));
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(encrypted + " is not decrypted, due to exception: "
						+ e.getMessage());
			}

		}

		/**
		 * Gets the cipher key.
		 * 
		 * @return the cipher key in DES for the configured private key.
		 * @throws InvalidKeyException
		 *             the invalid key exception
		 * @throws UnsupportedEncodingException
		 *             the unsupported encoding exception
		 * @throws NoSuchAlgorithmException
		 *             the no such algorithm exception
		 * @throws InvalidKeySpecException
		 *             the invalid key spec exception
		 */
		private SecretKey getCipherKey() throws InvalidKeyException, UnsupportedEncodingException,
				NoSuchAlgorithmException, InvalidKeySpecException {
			if (cipherKey == null) {
				DESKeySpec keySpec = new DESKeySpec(WSO2SAMLClient.getChipherKey().getBytes("UTF8"));
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				cipherKey = keyFactory.generateSecret(keySpec);
			}
			return cipherKey;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.springframework.extensions.webscripts.Authenticator#emptyCredentials
		 * ()
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

			if (logger.isDebugEnabled()) {
				logger.debug("HTTP Authorization provided: "
						+ (authorization != null && authorization.length() > 0));
				logger.debug("URL ticket provided: " + (ticket != null && ticket.length() > 0));
			}

			// authenticate as guest, if service allows
			if (isGuest && RequiredAuthentication.guest == required) {
				if (logger.isDebugEnabled())
					logger.debug("Authenticating as Guest");

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
					if (logger.isDebugEnabled())
						logger.debug("Authenticating (URL argument) ticket " + ticket);

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
						throw new WebScriptException("Authorization '" + authorizationParts[0]
								+ "' not supported.");
					}

					String decodedAuthorisation = new String(Base64.decode(authorizationParts[1]));
					Authorization auth = new Authorization(decodedAuthorisation);
					if (auth.isTicket()) {
						if (logger.isDebugEnabled())
							logger.debug("Authenticating (BASIC HTTP) ticket " + auth.getTicket());

						// assume a ticket has been passed
						authenticationService.validate(auth.getTicket());
						authorized = true;
					} else {
						if (logger.isDebugEnabled())
							logger.debug("Authenticating (BASIC HTTP) user " + auth.getUserName());

						// No longer need a special call to authenticate as
						// guest
						// Leave guest name resolution up to the services
						authenticationService.authenticate(auth.getUserName(), auth.getPassword()
								.toCharArray());
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
				if (logger.isDebugEnabled())
					logger.debug("Requesting authorization credentials");

				res.setStatus(401);
				res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
			}
			return authorized;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.alfresco.web.scripts.Authenticator#emptyCredentials()
		 */
		@Override
		public boolean emptyCredentials() {
			return ((ticket == null || ticket.length() == 0) && (authorization == null || authorization
					.length() == 0));
		}
	}
}
