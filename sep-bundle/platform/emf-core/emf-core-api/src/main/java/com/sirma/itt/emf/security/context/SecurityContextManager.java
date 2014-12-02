package com.sirma.itt.emf.security.context;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Stack;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Security and authentication management per thread. Provides methods for managing the actual and
 * effective authentications. Stores and manages the currently logged user for the current execution
 * thread. Provides methods for running a code in a specific security context.
 * 
 * @author BBonev
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@SuppressWarnings("restriction")
public class SecurityContextManager {

	public static final String SERVICE_NAME = "SecurityContextManager";

	public static final String NO_CONTAINER = "$NO_CONTAINER$";

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextManager.class);

	private static final boolean TRACE = LOGGER.isTraceEnabled();

	private static boolean USE_TIME_CONSTRAINTS;

	private static String encryptKey;

	private static volatile SecretKey cipherKey;

	private static String adminUser;

	private static String adminPass;

	private static String systemUserDisplayName;

	private static boolean initializedAdmin = false;

	private static String systemLanguage;

	private static final BASE64Encoder BASE64ENCODER = new BASE64Encoder();

	private static final User SYSTEM_USER = new EmfUser("system");

	private static String systemContainer;

	private static ThreadLocal<Stack<User>> threadLocalFullAuthenticationStack = new ThreadLocalStack();

	private static ThreadLocal<Stack<User>> threadLocalRunAsAuthenticationStack = new ThreadLocalStack();

	@Inject
	@Config(name = EmfConfigurationProperties.ADMIN_USERNAME)
	private String configAdminUser;

	@Inject
	@Config(name = EmfConfigurationProperties.ADMIN_PASSWORD)
	private String configAdminPass;

	/** The system user display name from configuration. */
	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_USER_DISPLAY_NAME, defaultValue = "System")
	private String configSystemUserDisplayName;

	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_ENCRYPT_KEY, defaultValue = "somePassword")
	private String configEncryptKey;

	/** The use time constraints. */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_TIME_CONSTRAINTS_USED, defaultValue = "false")
	private Boolean useTimeConstraints;

	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	@Inject
	@Config(name = SecurityConfigurationProperties.TRUSTSTORE_PATH)
	private String trustStorePath;

	@Inject
	@Config(name = SecurityConfigurationProperties.TRUSTSTORE_PASSWORD)
	private String trustStorePassword;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String defaultLanguage;

	/**
	 * Initialize the admin user information.
	 */
	@PostConstruct
	public void init() {
		if (StringUtils.isNotNullOrEmpty(trustStorePath)) {
			System.setProperty("javax.net.ssl.trustStore", trustStorePath);
			if (StringUtils.isNullOrEmpty(trustStorePassword)) {
				throw new EmfConfigurationException(
						"The trust store password must be set. Missing configuration: "
								+ SecurityConfigurationProperties.TRUSTSTORE_PASSWORD);
			}
			System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
		}

		if (StringUtils.isNullOrEmpty(defaultContainer)) {
			throw new EmfConfigurationException(
					"The default container must be set. Missing configuration: "
							+ EmfConfigurationProperties.DEFAULT_CONTAINER);
		}

		SecurityContextManager.systemLanguage = defaultLanguage;

		setAdminCredentials(configAdminUser, configAdminPass, configSystemUserDisplayName,
				configEncryptKey, defaultContainer);
		EmfUser emfUser = (EmfUser) SYSTEM_USER;
		emfUser.setId("emf:" + emfUser.getIdentifier());
		emfUser.getProperties().put(ResourceProperties.FIRST_NAME, systemUserDisplayName);
		emfUser.getProperties().put(ResourceProperties.LANGUAGE, systemLanguage);
		emfUser.setTenantId(defaultContainer);
		SecurityContextManager.USE_TIME_CONSTRAINTS = useTimeConstraints;
	}

	/**
	 * Gets the system language from the config as specified by
	 * {@link EmfConfigurationProperties#SYSTEM_LANGUAGE}
	 * 
	 * @return the system language id
	 */
	public static String getSystemLanguage() {
		return systemLanguage;
	}

	/**
	 * Return the language for given user. If it set in the user info it is returned, if not the
	 * default system language {@link #systemLanguage} is returned. If user is null or not
	 * {@link User}, null is returned
	 * 
	 * @param resource
	 *            the user to get language for
	 * @return the language id
	 */
	public static String getUserLanguage(Resource resource) {
		if ((resource == null) || !(resource instanceof User)) {
			return systemLanguage;
		}
		User user = (User) resource;
		String language = user.getLanguage();
		if (language == null) {
			return systemLanguage;
		}
		return language;
	}

	/**
	 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user-name-
	 * and-password-from-a-configuration-file. <br>
	 * Throws runtime exception on some error
	 * 
	 * @param plainText
	 *            is the plain text
	 * @return the encrypted plain text
	 */
	public static String encrypt(String plainText) {
		// only the first 8 Bytes of the constructor argument are used
		// as material for generating the keySpec
		try {
			SecretKey key = getCipherKey();
			// ENCODE plainText String
			byte[] cleartext = plainText.getBytes("UTF-8");
			// cipher is not thread safe
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return BASE64ENCODER.encode(cipher.doFinal(cleartext));
		} catch (Exception e) {
			LOGGER.error("Failed to encript text is not encrypted.", e);
			throw new IllegalStateException(plainText + " is not encrypted, due to exception", e);
		}
	}

	/**
	 * http://stackoverflow.com/questions/339004/java-encrypt-decrypt-user-name-
	 * and-password-from-a-configuration-file. <br>
	 * Descrypts the text provided<br>
	 * 
	 * @param encrypted
	 *            is the encrypted text
	 * @return the plain text or throws runtime exception on some error
	 */
	public static String decrypt(String encrypted) {
		try {
			sun.misc.BASE64Decoder base64decoder = new BASE64Decoder();
			// DECODE encrypted String
			byte[] encrypedBytes = base64decoder.decodeBuffer(encrypted);

			SecretKey key = getCipherKey();

			// cipher is not thread safe
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(encrypedBytes), "UTF-8");
		} catch (Exception e) {
			throw new IllegalStateException(encrypted + " is not decrypted, due to exception: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Gets the DES cipher key.
	 * 
	 * @return the cipher key currently configured.
	 * @throws InvalidKeyException
	 *             the invalid key exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws InvalidKeySpecException
	 *             the invalid key spec exception
	 */
	private static SecretKey getCipherKey() throws InvalidKeyException,
			UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (cipherKey == null) {
			DESKeySpec keySpec = new DESKeySpec(encryptKey.getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			cipherKey = keyFactory.generateSecret(keySpec);
		}
		return cipherKey;
	}

	/**
	 * Sets the admin credentials.
	 * 
	 * @param adminUser
	 *            the admin user
	 * @param adminPass
	 *            the admin password
	 * @param configSystemUserName
	 *            the system user name
	 * @param configEncrypt
	 *            the config encrypt
	 * @param systemContainer
	 *            the system container
	 */
	private static void setAdminCredentials(String adminUser, String adminPass,
			String configSystemUserName, String configEncrypt, String systemContainer) {
		if (StringUtils.isNullOrEmpty(adminUser)) {
			throw new IllegalArgumentException("System user and/or password cannot be null");
		}
		SecurityContextManager.adminUser = adminUser;
		SecurityContextManager.adminPass = adminPass;
		SecurityContextManager.systemUserDisplayName = configSystemUserName;
		SecurityContextManager.encryptKey = configEncrypt;
		SecurityContextManager.initializedAdmin = true;
		SecurityContextManager.systemContainer = systemContainer;
	}

	/**
	 * Creates the token.
	 * 
	 * @param user
	 *            the admin user
	 * @return the string
	 */
	private static String createToken(String user) {
		return encrypt(SecurityContextManager.createResponse(
				"http://localhost:8080/alfresco/ServiceLogin", "http://localhost:8081",
				"https://localhost:9448/samlsso", user).toString());
	}

	/**
	 * Update user token against current time.If {@link #USE_TIME_CONSTRAINTS} is false update is
	 * skipped.
	 * 
	 * @param user
	 *            the user to update
	 * @return the user with credentials
	 */
	public static UserWithCredentials updateUserToken(UserWithCredentials user) {
		if (USE_TIME_CONSTRAINTS) {
			user.setTicket(createToken(user.getIdentifier()));
		}
		return user;

	}

	/**
	 * Creates the response for authentication in DMS. The time should be synchronized
	 * 
	 * @param alfrescoURL
	 *            the alfresco url
	 * @param audianceURL
	 *            the audiance url
	 * @param samlURL
	 *            the saml url
	 * @param user
	 *            the user to authenticate
	 * @return the resulted saml2 message
	 */
	public static StringBuffer createResponse(String alfrescoURL, String audianceURL,
			String samlURL, String user) {

		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTime barrier = now.plusMinutes(10);
		StringBuffer saml = new StringBuffer();
		saml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("<saml2p:Response ID=\"inppcpljfhhckioclinjenlcneknojmngnmgklab\" IssueInstant=\"")
				.append(now.toString())
				.append("\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\">")
				.append("<saml2p:Status>")
				.append("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>")
				.append("</saml2p:Status>")
				.append("<saml2:Assertion ID=\"ehmifefpmmlichdcpeiogbgcmcbafafckfgnjfnk\" IssueInstant=\"")
				.append(now.toString())
				.append("\" Version=\"2.0\" xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">")
				.append("<saml2:Issuer Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:entity\">")
				.append(samlURL)
				.append("</saml2:Issuer>")
				.append("<saml2:Subject>")
				.append("<saml2:NameID>")
				.append(user)
				.append("</saml2:NameID>")
				.append("<saml2:SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">")
				.append("<saml2:SubjectConfirmationData InResponseTo=\"0\" NotOnOrAfter=\"")
				.append(barrier.toString())
				.append("\" Recipient=\"")
				.append(alfrescoURL)
				.append("\"/>")
				.append("</saml2:SubjectConfirmation>")
				.append("</saml2:Subject>")
				.append("<saml2:Conditions NotBefore=\"")
				.append(now.toString())
				.append("\" NotOnOrAfter=\"")
				.append(barrier.toString())
				.append("\">")
				.append("<saml2:AudienceRestriction>")
				.append("<saml2:Audience>")
				.append(audianceURL)
				.append("</saml2:Audience>")
				.append("</saml2:AudienceRestriction>")
				.append("</saml2:Conditions>")
				.append("<saml2:AuthnStatement AuthnInstant=\"")
				.append(now.toString())
				.append("\">")
				.append("<saml2:AuthnContext>")
				.append("<saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml2:AuthnContextClassRef>")
				.append("</saml2:AuthnContext>").append("</saml2:AuthnStatement>")
				.append("</saml2:Assertion>").append("</saml2p:Response>");
		return saml;
	}

	/**
	 * Gets the system user.
	 * 
	 * @return the system user
	 */
	public static User getSystemUser() {
		return SYSTEM_USER;
	}

	/**
	 * Gets the Admin user.
	 * 
	 * @return the admin user
	 */
	public synchronized static User getAdminUser() {
		if (initializedAdmin) {
			EmfUser emfUser = new EmfUser(adminUser, adminPass);
			emfUser.getProperties().put(ResourceProperties.FIRST_NAME, adminUser);
			emfUser.getProperties().put(ResourceProperties.LANGUAGE, systemLanguage);
			if (emfUser.getTicket() == null) {
				emfUser.setTicket(createToken(adminUser));
			}
			updateUserToken(emfUser);
			return emfUser;
		}
		return null;
	}

	/**
	 * Authenticate as admin.
	 */
	public static void authenticateAsAdmin() {
		authenticateAs(getAdminUser());
	}

	/**
	 * Sets the given user as effective authentication. If the current security context is empty
	 * then the actual authentication will also be set to the given user.
	 * 
	 * @param user
	 *            the user to authentica as
	 */
	public static void authenticateAs(User user) {
		SecurityContext context = SecurityContextHolder.getContext();
		if ((context == null) || !(context instanceof EmfSecurityContext)) {
			EmfSecurityContext ctx = new EmfSecurityContext();
			ctx.setAuthentication(user);
			SecurityContextHolder.setContext(ctx);
			context = ctx;
		}
		((EmfSecurityContext) context).setEffectiveAuthentication(user);
	}

	/**
	 * Remove the current security information.
	 */
	public static void clearCurrentSecurityContext() {
		SecurityContextHolder.setContext(null);
	}

	/**
	 * Gets a copy of the current security context.
	 * 
	 * @return the current security context
	 */
	public static SecurityContext getCurrentSecurityContext() {
		SecurityContext context = SecurityContextHolder.getContext();
		if ((context != null) && (context instanceof EmfSecurityContext)) {
			return ((EmfSecurityContext) context).clone();
		}
		return null;
	}

	/**
	 * Sets the given user as effective authentication. If the current security context is empty
	 * then the actual authentication will also be set to the given user.<br>
	 * <b>NOTE:</b> The authentication set will not have a password so it cannot be used for DMS
	 * authentication. If you need to authenticate in the DMS use the method
	 * {@link #authenticateAs(User)} providing an instance with
	 * 
	 * @param userName
	 *            the user name {@link com.sirma.itt.emf.security.model.UserWithCredentials}
	 *            implementation
	 */
	public static void authenticateAs(String userName) {
		authenticateAs(new EmfUser(userName));
	}

	/**
	 * Sets the actual and effective authentications as user with the given user name.<br>
	 * <b>NOTE:</b> The authentication set will not have a password so it cannot be used for DMS
	 * authentication. If you need to authenticate in the DMS use the method
	 * {@link #authenticateFullyAs(User)} providing an instance with
	 * {@link com.sirma.itt.emf.security.model.UserWithCredentials} implementation
	 * 
	 * @param userName
	 *            the user name
	 * @return the user
	 */
	public static User authenticateFullyAs(String userName) {
		return authenticateFullyAs(new EmfUser(userName));
	}

	/**
	 * Sets the actual and effective authentications as the given user.
	 * 
	 * @param user
	 *            the user, if <code>null</code> will clear the current security context
	 * @return the user
	 */
	public static User authenticateFullyAs(User user) {
		if (user == null) {
			clearCurrentSecurityContext();
			return null;
		}
		SecurityContext context = SecurityContextHolder.getContext();
		EmfSecurityContext sc = null;
		if ((context == null) || !(context instanceof EmfSecurityContext)) {
			sc = new EmfSecurityContext();
			SecurityContextHolder.setContext(sc);
		} else {
			sc = (EmfSecurityContext) context;
		}
		if (TRACE && isSystemUser(user)) {
			LOGGER.trace("Setting full authentication as System user!");
			// TODO: add stack trace printing
		}
		// Sets real and effective
		sc.setAuthentication(user);
		sc.setEffectiveAuthentication(user);
		// System.out.println("Authenticated " + user);
		return user;
	}

	/**
	 * Get the current authentication for application of permissions. This includes the any overlay
	 * details set by {@link #authenticateAs(String)} or {@link #authenticateAs(User)}.
	 * 
	 * @return the running authentication
	 */
	public static User getRunAsAuthentication() {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			return context.getEffectiveAuthentication();
		}
		return null;
	}

	/**
	 * <b>WARN: Advanced usage only.</b><br/>
	 * Get the authentication for that was set by an real authentication.
	 * 
	 * @return the real authentication
	 */
	public static User getFullAuthentication() {
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			return context.getAuthentication();
		}
		return null;
	}

	/**
	 * Runs the given callable as the given users. The full and effective authentications should be
	 * provided. The method overrides the given user at in the context for the method call.
	 * <p>
	 * <b>Note:</b>> If both users are <code>null</code> then the method does nothing!
	 * 
	 * @param <E>
	 *            the element type
	 * @param executer
	 *            the executer
	 * @param authenticatedAs
	 *            the authenticated as
	 * @param callable
	 *            the callable
	 * @return the e
	 */
	public static <E> E callAs(User executer, User authenticatedAs, Callable<E> callable) {
		User originalFullAuthentication = getFullAuthentication();
		User originalRunAsAuthentication = getRunAsAuthentication();

		final E result;
		try {
			if (executer != null) {
				// NOTE: the method updates both authentications
				authenticateFullyAs(executer);
			}
			// update the run as or restore the current if present
			if (authenticatedAs != null) {
				authenticateAs(authenticatedAs);
			} else if (originalRunAsAuthentication != null) {
				// if the new authentication is not present then we keep the old authentication
				// This is needed because the method #authenticateFullyAs overrides both of them.
				authenticateAs(originalRunAsAuthentication);
			}
			result = callable.call();
			return result;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception exception) {
			throw new EmfRuntimeException("Error during run as.", exception);
		} finally {
			if (originalFullAuthentication == null) {
				clearCurrentSecurityContext();
			} else {
				authenticateFullyAs(originalFullAuthentication);
				authenticateAs(originalRunAsAuthentication);
			}
		}
	}

	/**
	 * Runs the given callable as the given users. The full and effective authentications should be
	 * provided
	 * 
	 * @param <E>
	 *            the produced type
	 * @param executer
	 *            the user to set execution to
	 * @param authenticatedAs
	 *            the user that will be used to login to DMS
	 * @param callable
	 *            the callable operation to execute
	 * @return the result from the operation
	 */
	public static <E> E callAs(String executer, String authenticatedAs, Callable<E> callable) {
		return callAs(executer == null ? null : new EmfUser(executer),
				authenticatedAs == null ? null : new EmfUser(authenticatedAs), callable);
	}

	/**
	 * Runs the given callable as the given user. The full and effective authentications will be the
	 * same
	 * 
	 * @param <E>
	 *            the produced type
	 * @param executer
	 *            the user to set execution to
	 * @param callable
	 *            the callable operation to execute
	 * @return the result from the operation
	 */
	public static <E> E callAs(User executer, Callable<E> callable) {
		return callAs(executer, executer, callable);
	}

	/**
	 * Runs the given callable as the system user for full authentication and effective
	 * authentications will be admin user for DMS system.
	 * 
	 * @param <E>
	 *            the produced type
	 * @param callable
	 *            the callable operation to execute
	 * @return the result from the operation
	 */
	public static <E> E callAsSystem(Callable<E> callable) {
		return callAs(SYSTEM_USER, getAdminUser(), callable);
	}

	/**
	 * Gets the default tenant/container. If user session is missing this is the default tenant to
	 * be used.
	 * 
	 * @return the default tenant
	 */
	public static String getDefaultTenant() {
		return systemContainer;
	}

	/**
	 * Gets the current container. First the methods will try to fetch the container from the
	 * currently logged in user. If no such user is available then the method will try to fetch it
	 * from the given authentication service if present. If as last resort the method will try to
	 * return the default container that is returned by the method {@link #getDefaultTenant()} if
	 * present otherwise {@link #NO_CONTAINER} constant will be returned.
	 * 
	 * @param authenticationServiceInstance
	 *            the authentication service instance to get the current container if present, can
	 *            be <code>null</code>
	 * @return the current container
	 */
	public static String getCurrentContainer(
			Instance<AuthenticationService> authenticationServiceInstance) {
		User fullAuthentication = SecurityContextManager.getFullAuthentication();
		if (fullAuthentication == null) {
			fullAuthentication = SecurityContextManager.getRunAsAuthentication();
		}
		if (fullAuthentication != null) {
			String tenantId = fullAuthentication.getTenantId();
			if (StringUtils.isNotNullOrEmpty(tenantId)) {
				return tenantId;
			}
		}
		javax.enterprise.inject.Instance<AuthenticationService> instance = authenticationServiceInstance;
		if ((instance == null) || instance.isUnsatisfied() || instance.isAmbiguous()) {
			if (StringUtils.isNullOrEmpty(getDefaultTenant())) {
				return NO_CONTAINER;
			}
			return getDefaultTenant();
		}
		try {
			return instance.get().getCurrentContainer();
		} catch (RuntimeException e) {
			if (TRACE) {
				LOGGER.trace("Failed to set current user: ", e);
			}
			if (StringUtils.isNullOrEmpty(getDefaultTenant())) {
				return NO_CONTAINER;
			}
			return getDefaultTenant();
		}
	}

	/**
	 * Gets the current user.
	 * 
	 * @param authenticationService
	 *            the authentication service
	 * @return the current user
	 */
	public static User getCurrentUser(AuthenticationService authenticationService) {
		try {
			return authenticationService.getCurrentUser();
		} catch (ContextNotActiveException e) {
			return SecurityContextManager.getFullAuthentication();
		}
	}

	/**
	 * Push the current authentication context onto a thread local stack.
	 */
	public static void pushAuthentication() {
		User originalFullAuthentication = SecurityContextManager.getFullAuthentication();
		User originalRunAsAuthentication = SecurityContextManager.getRunAsAuthentication();
		threadLocalFullAuthenticationStack.get().push(originalFullAuthentication);
		threadLocalRunAsAuthenticationStack.get().push(originalRunAsAuthentication);
	}

	/**
	 * Pop the authentication context from a thread local stack.
	 */
	public static void popAuthentication() {
		User originalFullAuthentication = threadLocalFullAuthenticationStack.get().pop();
		User originalRunAsAuthentication = threadLocalRunAsAuthenticationStack.get().pop();
		if (originalFullAuthentication == null) {
			SecurityContextManager.clearCurrentSecurityContext();
		} else {
			SecurityContextManager.authenticateFullyAs(originalFullAuthentication);
			SecurityContextManager.authenticateAs(originalRunAsAuthentication);
		}
	}

	/**
	 * The Class ThreadLocalStack.
	 * 
	 * @author BBonev
	 */
	static class ThreadLocalStack extends ThreadLocal<Stack<User>> {

		@Override
		protected Stack<User> initialValue() {
			return new Stack<>();
		}

	}

	/**
	 * Checks if the provided user is system user.
	 * 
	 * @param user
	 *            the user
	 * @return true, if is system user
	 */
	public static boolean isSystemUser(User user) {
		if (user == null) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(getSystemUser().getId(), user.getId());
	}
}
