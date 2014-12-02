package com.sirma.itt.emf.authentication.idp;

import java.rmi.RemoteException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.WSRealmBuilder;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import com.sirma.itt.emf.authentication.sso.saml.SSOConfiguration;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * IDP service integration. Provides access to various services.
 *
 * @author Stepan Bahdikyan
 * @author bbanchev
 */
@ApplicationScoped
public class IdentityServerConnector {

	private static final Logger LOGGER = Logger.getLogger(IdentityServerConnector.class);

	@Inject
	@Config(name = SSOConfiguration.SECURITY_IDP_SERVER_ADMIN_USERNAME)
	private String idpServerUserId;

	@Inject
	@Config(name = SSOConfiguration.SECURITY_IDP_SERVER_ADMIN_PASSWORD)
	private String idpServerUserPass;

	@Inject
	@Config(name = SSOConfiguration.SECURITY_SSO_IDP_URL)
	private String idpServerURL;

	private static ConfigurationContext CONFIG_CONTEXT;

	/**
	 * initializes configuration context
	 */
	@PostConstruct
	private void init() {
		try {
			CONFIG_CONTEXT = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
					null, null);
			// remove the context
			if (idpServerURL != null) {
				idpServerURL = idpServerURL.replace("/samlsso", "/services/");
			}
			LOGGER.info("Using base server address: " + idpServerURL);
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed during intialization." + e.getMessage(), e);
		}
	}

	/**
	 * Initializes AuthenticationAdminStub.
	 *
	 * @param serverUrl
	 *            wso2 server url
	 * @return AuthenticationAdminStub that is initialized
	 * @throws EmfRuntimeException
	 *             if cannot initialize.
	 */
	private AuthenticationAdminStub initializeAuthenticationAdminStub(String serverUrl)
			throws EmfRuntimeException {
		try {
			AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(
					CONFIG_CONTEXT, serverUrl + "AuthenticationAdmin");

			ConfigurationContext context = authenticationAdminStub._getServiceClient()
					.getServiceContext().getConfigurationContext();

			MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setDefaultMaxConnectionsPerHost(300);
			params.setMaxTotalConnections(300);
			multiThreadedHttpConnectionManager.setParams(params);
			HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
			context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			return authenticationAdminStub;
		} catch (AxisFault e) {
			throw new EmfRuntimeException(
					"Cannot create AuthenticationAdminStub " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes and returns user store manager from wsrealm
	 *
	 * @param adminUserName
	 *            administrators user name
	 * @param adminUserPassword
	 *            administrators pass word
	 * @param serverUrl
	 *            wso2 server url
	 * @return initialized {@link UserStoreManager}
	 * @throws EmfRuntimeException
	 *             if cannot create manager.
	 */
	private UserStoreManager initializeUserStoreManager(String adminUserName,
			String adminUserPassword, String serverUrl) throws EmfRuntimeException {

		String cookie = null;
		try {
			AuthenticationAdminStub authStub = initializeAuthenticationAdminStub(serverUrl);
			if (authStub.login(adminUserName, adminUserPassword, null)) {
				cookie = ((String) authStub._getServiceClient().getServiceContext()
						.getProperty(HTTPConstants.COOKIE_STRING));
			}
		} catch (RemoteException e) {
			throw new EmfRuntimeException("Cannot login as admin. " + e.getMessage(), e);
		} catch (LoginAuthenticationExceptionException e) {
			throw new EmfRuntimeException("Cannot login as admin. " + e.getMessage(), e);
		}
		UserRealm realm;
		try {
			realm = WSRealmBuilder.createWSRealm(serverUrl, cookie, CONFIG_CONTEXT);
		} catch (UserStoreException e) {
			throw new EmfRuntimeException("Cannot create WS realm. " + e.getMessage(), e);
		}
		UserStoreManager storeManager;
		try {
			storeManager = realm.getUserStoreManager();
		} catch (UserStoreException e) {
			throw new EmfRuntimeException("Cannot get user store manager. " + e.getMessage(), e);
		}
		return storeManager;
	}

	/**
	 * Creates the user store manager for the AIS users Identity server.
	 *
	 * @return the user store manager
	 */
	@Produces
	@RequestScoped
	public UserStoreManager getIDPUserStoreManager() {
		trace("Creating UserStoreManager to IDP server@" + idpServerURL);
		return initializeUserStoreManager(idpServerUserId, idpServerUserPass, idpServerURL);
	}

	/**
	 * Dump the passed message into trace log if the latter is trace enabled
	 *
	 * @param messages
	 *            the messages
	 */
	private void trace(Object... messages) {
		if (LOGGER.isTraceEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object message : messages) {
				builder.append(message.toString());
			}
			LOGGER.trace(builder.toString());
		}
	}

}