package com.sirma.itt.seip.idp.wso2;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryService;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceStub;
import org.wso2.carbon.um.ws.api.WSRealmBuilder;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.idp.config.IDPConfiguration;
import com.sirma.itt.seip.idp.exception.IDPClientException;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * IDP service integration. Provides access to various services.
 *
 * @author Stepan Bahdikyan
 * @author bbanchev
 */
@ApplicationScoped
public class WSO2IdPStubManagement {

	private static final String SAMLSSO = "/samlsso";

	private static final String CANNOT_LOGIN_AS_ADMIN = "Cannot login as admin. ";

	private static final Logger LOGGER = LoggerFactory.getLogger(WSO2IdPStubManagement.class);

	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private IDPConfiguration idpConfiguration;
	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private Contextual<ConfigurationContext> configurationContext;

	private String idpServerURL;

	private String carbonServerURL;

	/**
	 * Initializes configuration context and constructs IDP server url
	 */
	@PostConstruct
	public void init() {
		try {
			// this will force trust store initialization
			securityConfiguration.getTrustStore().requireConfigured(
					"Trust store configuration is required to access the IDP management!");

			configurationContext.initializeWith(() -> {
				try {
					return ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
				} catch (AxisFault axisFault) {
					throw new EmfRuntimeException(axisFault);
				}
			});
			// update the service path
			setServerUrls(idpConfiguration.getIdpServerURL());

			idpConfiguration.getIdpServerURL().addConfigurationChangeListener(this::setServerUrls);
			LOGGER.info("Using base server address: " + idpServerURL);
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed during intialization." + e.getMessage(), e);
		}
	}

	private void setServerUrls(ConfigurationProperty<String> e) {
		idpServerURL = e.get().replace(SAMLSSO, idpConfiguration.getIdpServicesPath().get());
		carbonServerURL = e.get().replace(SAMLSSO, "/carbon");
	}

	/**
	 * Initializes AuthenticationAdminStub. Wraps errors in IDPClientException if cannot be initialize.
	 *
	 * @param serverUrl
	 *            wso2 server url
	 * @return AuthenticationAdminStub that is initialized
	 * @throws IDPClientException wraps on any error the original exception
	 */
	private AuthenticationAdminStub initializeAuthenticationAdminStub(String serverUrl) throws IDPClientException {
		try {
			AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(getConfigurationContext(),
					serverUrl + "AuthenticationAdmin");

			ConfigurationContext context = authenticationAdminStub
					._getServiceClient()
						.getServiceContext()
						.getConfigurationContext();

			MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setDefaultMaxConnectionsPerHost(300);
			params.setMaxTotalConnections(300);
			multiThreadedHttpConnectionManager.setParams(params);
			HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
			context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			return authenticationAdminStub;
		} catch (AxisFault e) {
			throw new IDPClientException("Cannot create AuthenticationAdminStub " + e.getMessage(), e);
		}
	}

	/**
	 * Initializes and returns user store manager from wsrealm. Throws wrapped in IDPClientException error if cannot
	 * create manager.
	 *
	 * @param adminUserName
	 *            administrators user name
	 * @param adminUserPassword
	 *            administrators pass word
	 * @return initialized {@link UserStoreManager}
	 * @throws IDPClientException
	 *             wraps on any error the original exception
	 */
	private UserStoreManager initializeUserStoreManager(String adminUserName, String adminUserPassword)
			throws IDPClientException {

		UserStoreManager storeManager;
		try {
			storeManager = initUserRealm(adminUserName, adminUserPassword).getUserStoreManager();
		} catch (UserStoreException e) {
			throw new IDPClientException("Cannot get user store manager. " + e.getMessage(), e);
		}
		return storeManager;
	}

	/**
	 * Initializes and returns claim manager from wsrealm. Throws wrapped in IDPClientException error if cannot
	 * create manager.
	 *
	 * @param adminUserName
	 *            administrators user name
	 * @param adminUserPassword
	 *            administrators pass word
	 * @return initialized {@link ClaimManager}
	 * @throws IDPClientException
	 *             wraps on any error the original exception
	 */
	private ClaimManager initializeClaimManager(String adminUserName, String adminUserPassword)
			throws IDPClientException {

		ClaimManager storeManager;
		try {
			storeManager = initUserRealm(adminUserName, adminUserPassword).getClaimManager();
		} catch (UserStoreException e) {
			throw new IDPClientException("Cannot get user store manager. " + e.getMessage(), e);
		}
		return storeManager;
	}

	/**
	 * Init a new user realm using stub initialization.
	 *
	 * @param adminUserName
	 *            the admin user name
	 * @param adminUserPassword
	 *            the admin user password
	 * @return the realm
	 * @throws IDPClientException
	 *             wraps on any error the original exception
	 */
	private UserRealm initUserRealm(String adminUserName, String adminUserPassword) throws IDPClientException {
		String cookie = getStubCookie(adminUserName, adminUserPassword);
		UserRealm realm;
		try {
			realm = WSRealmBuilder.createWSRealm(idpServerURL, cookie, getConfigurationContext());
		} catch (UserStoreException e) {
			throw new IDPClientException("Cannot create WS realm. " + e.getMessage(), e);
		}
		return realm;
	}

	private String getStubCookie(String adminUserName, String adminUserPassword) throws IDPClientException {
		String cookie = null;
		try {
			AuthenticationAdminStub authStub = initializeAuthenticationAdminStub(idpServerURL);
			if (authStub.login(adminUserName, adminUserPassword, null)) {
				cookie = (String) authStub
						._getServiceClient()
							.getServiceContext()
							.getProperty(HTTPConstants.COOKIE_STRING);
			}
		} catch (LoginAuthenticationExceptionException | RemoteException e) {
			throw new IDPClientException(CANNOT_LOGIN_AS_ADMIN + e.getMessage(), e);
		}
		return cookie;
	}

	/**
	 * Request a web based cookie for http client
	 */
	private String getWebCookie(CloseableHttpClient client) throws IOException {
		HttpPost post = new HttpPost(URI.create(carbonServerURL + "/admin/login_action.jsp"));
		post.setEntity(
				new StringEntity(
						"username=" + idpConfiguration.getIdpServerUserId().get() + "&password="
								+ idpConfiguration.getIdpServerUserPass().get(),
						ContentType.APPLICATION_FORM_URLENCODED));
		CloseableHttpResponse response = client.execute(post);
		Header cookie;
		if ((cookie = response.getFirstHeader("Set-Cookie")) != null) {
			return cookie.getValue();
		}
		return "";
	}

	private static CloseableHttpClient produceHttpClient()
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
		return HttpClients.custom().setSSLSocketFactory(sslsf).build();
	}

	private TenantMgtAdminServiceStub initializeTenantManager(String adminUserName, String adminUserPassword,
			String serverUrl) throws IDPClientException {

		try {
			TenantMgtAdminServiceStub tenantMgtAdminService = new TenantMgtAdminServiceStub(getConfigurationContext(),
					serverUrl + "TenantMgtAdminService");
			ServiceClient client = tenantMgtAdminService._getServiceClient();

			Options option = client.getOptions();
			option.setManageSession(true);
			AuthenticationAdminStub authStub = initializeAuthenticationAdminStub(serverUrl);
			if (authStub.login(adminUserName, adminUserPassword, null)) {
				String cookie = (String) authStub
						._getServiceClient()
							.getServiceContext()
							.getProperty(HTTPConstants.COOKIE_STRING);
				option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
			}
			return tenantMgtAdminService;
		} catch (Exception e) {
			throw new IDPClientException(CANNOT_LOGIN_AS_ADMIN + e.getMessage(), e);
		}

	}

	/**
	 * Creates the user store manager for the AIS users Identity server.
	 *
	 * @return the user store manager
	 */
	@Produces
	public UserStoreManager getIDPUserStoreManager() {
		LOGGER.trace("Creating UserStoreManager to IDP server@{}", idpServerURL);
		try {
			return initializeUserStoreManager(getAdminName(), getAdminPass());
		} catch (IDPClientException e) {
			LOGGER.warn("Failed to initialize UserStoreManager", e);
		}
		return null;
	}

	/**
	 * Creates the claim manager for the AIS users properties Identity server.
	 *
	 * @return the claim manager
	 */
	@Produces
	public ClaimManager getIDPClaimManager() {
		LOGGER.trace("Creating ClaimManager to IDP server@{}", idpServerURL);
		try {
			return initializeClaimManager(getAdminName(), getAdminPass());
		} catch (IDPClientException e) {
			LOGGER.warn("Failed to initialize ClaimManager", e);
		}
		return null;
	}

	private String getAdminName() {
		// if this is tenant mode use the tenant admin credentials,
		// otherwise system
		return (securityContextManager.getCurrentContext().isSystemTenant()
				? idpConfiguration.getIdpServerUserId()
				:  securityConfiguration.getAdminUserName()).get();
	}
	private String getAdminPass() {
		// if this is tenant mode use the tenant admin credentials,
		// otherwise system
		return (securityContextManager.getCurrentContext().isSystemTenant()
				? idpConfiguration.getIdpServerUserPass()
				:  securityConfiguration.getAdminUserPassword()).get();
	}

	@Produces
	public UserInformationRecoveryService getUserInformationRecoveryService() {
		try {
			UserInformationRecoveryServiceStub stub = new UserInformationRecoveryServiceStub(getConfigurationContext(),
					idpServerURL + "UserInformationRecoveryService");

			ServiceClient client = stub._getServiceClient();

			Options option = client.getOptions();
			option.setManageSession(true);
			AuthenticationAdminStub authStub = initializeAuthenticationAdminStub(idpServerURL);
			if (authStub.login(getAdminName(), getAdminPass(), null)) {
				String cookie = (String) authStub
						._getServiceClient()
						.getServiceContext()
						.getProperty(HTTPConstants.COOKIE_STRING);
				option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
			}
			return stub;
		} catch (Exception axisFault) {
			throw new EmfRuntimeException(axisFault);
		}
	}

	private ConfigurationContext getConfigurationContext() {
		return configurationContext.getContextValue();
	}

	/**
	 * Gets the IDP tenant manager.
	 *
	 * @return a new instance of {@link TenantMgtAdminServiceStub}
	 */
	@Produces
	@RequestScoped
	public TenantMgtAdminServiceStub getIDPTenantManager() {
		LOGGER.trace("Creating TenantMgtAdminServiceStub to IDP server@{}", idpServerURL);
		try {
			return initializeTenantManager(idpConfiguration.getIdpServerUserId().get(),
					idpConfiguration.getIdpServerUserPass().get(), idpServerURL);
		} catch (IDPClientException e) {
			LOGGER.warn("Failed to initialize TenantMgtAdminServiceStub", e);
		}
		return null;
	}

	/**
	 * Adds a second user store and waits 15 sec to populate the asynch container on success.
	 *
	 * @param rawConfig
	 *            the raw config as {@link IDPConfiguration#getIdpStoreTemplate()} with populated data
	 * @throws IDPClientException
	 *             wraps on any error the original exception
	 */
	public void addUserStore(String rawConfig) throws IDPClientException {
		// TO DO better use UserStoreConfigAdminServiceStub
		// closing the client will close the response as well
		try (CloseableHttpClient client = produceHttpClient()) {

			String cookie = getWebCookie(client);
			HttpPost post = new HttpPost(URI.create(carbonServerURL + "/userstore_config/userstore-config-finish.jsp"));
			post.setEntity(new StringEntity(rawConfig, ContentType.APPLICATION_FORM_URLENCODED));
			post.addHeader("Cookie", cookie);
			CloseableHttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new IDPClientException("Failure during domain store registration!");
			}
			// wait 15 sec for asynch store update in idp
			Thread.sleep(15000);
		} catch (Exception e) {
			throw new IDPClientException("Failure during domain store registration!", e);
		}
	}

	/**
	 * Assign role permissions to role. Roles are in format {tenantId/roleId}
	 *
	 * @param roleId
	 *            the role id if full format
	 * @param permissions
	 *            the permissions is list of permissions. See {@link IDPConfiguration#getIdpAdminPermissions()}
	 * @throws IDPClientException
	 *             wraps on any error the original exception
	 */
	public void assignRolePermissions(String roleId, List<String> permissions) throws IDPClientException {
		try (CloseableHttpClient client = produceHttpClient()) {
			String cookie = getWebCookie(client);
			StringBuilder request = new StringBuilder();
			for (String nextPermission : permissions) {
				request.append("selectedPermissions=").append(URLEncoder.encode(nextPermission, "UTF-8")).append("&");
			}
			request.append("prevUser=null&prevPage=null&roleName=").append(URLEncoder.encode(roleId, "UTF-8")).append(
					"&prevPageNumber=null");
			HttpPost post = new HttpPost(URI.create(carbonServerURL + "/role/edit-permissions-finish.jsp"));
			post.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_FORM_URLENCODED));
			post.addHeader("Cookie", cookie);
			client.execute(post);
		} catch (Exception e) {
			throw new IDPClientException("Failure during role permission assignment!", e);
		}
	}

}
