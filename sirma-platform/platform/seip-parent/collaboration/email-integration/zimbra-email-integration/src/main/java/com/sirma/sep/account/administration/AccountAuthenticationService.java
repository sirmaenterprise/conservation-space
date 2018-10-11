package com.sirma.sep.account.administration;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.PreAuthUtility;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.SchemaValidationFeature;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import com.zimbra.wsdl.zimbraservice.ZcsAdminPortType;
import com.zimbra.wsdl.zimbraservice.ZcsAdminService;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;
import com.zimbra.wsdl.zimbraservice.ZcsService;

import zimbra.AccountBy;
import zimbra.AccountSelector;
import zimbra.AuthTokenControl;
import zimbra.HeaderContext;
import zimbra.ObjectFactory;
import zimbraaccount.AuthRequest;
import zimbraaccount.AuthResponse;
import zimbraaccount.PreAuth;

/**
 * Creates and authenticates admin {@link ZcsAdminPortType} or user port {@link ZcsPortType}
 *
 * @author S.Djulgerova
 */
@ApplicationScoped
public class AccountAuthenticationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountAuthenticationService.class);

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Inject
	private Contextual<PortCache> tenantAdminPort;

	@Inject
	private Contextual<ZcsAdminPortType> adminPort;

	private Map<String, PortCache> clientPortsMap = new HashMap<>();

	/**
	 * Init admin and client ports
	 */
	@PostConstruct
	public void initPorts() {
		tenantAdminPort.initializeWith(() -> new PortCache(new PortWrapper(initAdminPort())));
		adminPort.initializeWith(this::initAdminPort);
	}

	/**
	 * Creates and authenticates a {@link ZcsPortType} port with given id and password.
	 *
	 * @param accountName
	 *            account name to authenticate to.
	 * @param password
	 *            account password.
	 * @return authenticated client port.
	 * @throws EmailIntegrationException
	 *             if fails to communicate with the remote server to authenticate the client
	 */
	public ZcsPortType getClientPort(String accountName, String password) throws EmailIntegrationException {
		return (ZcsPortType) getOrCreatePortCache(accountName)
				.getPortWrapper(port -> authenticateClientPort((ZcsPortType) port.getPort(), accountName, password))
				.getPort();
	}

	/**
	 * Creates and authenticates a {@link ZcsPortType} port with given id and using generated pre auth token.
	 *
	 * @param accountName
	 *            account name to authenticate to.
	 * @return authenticated client port.
	 * @throws EmailIntegrationException
	 *             if fails to communicate with the remote server to authenticate the client
	 */
	public ZcsPortType getClientPort(String accountName) throws EmailIntegrationException {
		return (ZcsPortType) getOrCreatePortCache(accountName)
				.getPortWrapper(port -> authenticateClientPort((ZcsPortType) port.getPort(), accountName)).getPort();
	}

	private PortCache getOrCreatePortCache(String accountName) {
		PortCache clientPort = clientPortsMap.get(accountName);
		if (clientPort == null) {
			clientPort = new PortCache(initClientPort());
			clientPortsMap.put(accountName, clientPort);
		}
		return clientPort;
	}

	/**
	 * Gets or creates and authenticates a {@link ZcsPortType} with tenant admin credential.
	 *
	 * @return port authenticated as tenant admin.
	 * @throws EmailIntegrationException
	 *             if fails to communicate with the remote server to authenticate the client
	 */
	public ZcsAdminPortType getTenantAdminPort() throws EmailIntegrationException {
		return (ZcsAdminPortType) tenantAdminPort.getContextValue()
				.getPortWrapper(port -> authenticateAdminPort((ZcsAdminPortType) port.getPort(),
						EmailIntegrationHelper.generateEmailAddress(
								emailIntegrationConfiguration.getTenantAdminAccount().get().getName(),
								emailIntegrationConfiguration.getTenantDomainAddress().get(),
								emailIntegrationConfiguration.getTestEmailPrefix().get()),
						emailIntegrationConfiguration.getTenantAdminAccount().get().getCredentials().toString()))
				.getPort();
	}

	/**
	 * Recreates the tenant admin port if for some reason tenant admin has been deleted and recreated. Such situation
	 * might happen if admin name and password configuration have been deleted and on next restart recreated by the
	 * system. Then the cached tenant admin port would be invalid unless its recreated or expires.
	 *
	 * @param username
	 *            The tenant user name as configured.
	 * @param password
	 *            The tenant user password as configured.
	 * @throws EmailIntegrationException
	 *             if fails to communicate with the remote server to authenticate the client
	 */
	public void resetTenantAdminPort(String username, String password) throws EmailIntegrationException {
		tenantAdminPort.reset();
		tenantAdminPort.getContextValue()
				.getPortWrapper(port -> authenticateAdminPort((ZcsAdminPortType) port.getPort(),
						EmailIntegrationHelper.generateEmailAddress(username,
								emailIntegrationConfiguration.getTenantDomainAddress().get(),
								emailIntegrationConfiguration.getTestEmailPrefix().get()),
						password))
				.getPort();
	}

	/**
	 * Creates and authenticates system admin port.
	 *
	 * @return authenticated admin port.
	 */
	public ZcsAdminPortType getAdminPort() {
		authenticateAdminPort(adminPort.getContextValue(),
				emailIntegrationConfiguration.getAdminAccount().get().getName(),
				emailIntegrationConfiguration.getAdminAccount().get().getCredentials().toString());
		return adminPort.getContextValue();
	}

	private ZcsAdminPortType initAdminPort() {
		ZcsAdminService zcsSvc = new ZcsAdminService(
				generateWsdlURL(emailIntegrationConfiguration.getWebmailProtocol().get(),
						emailIntegrationConfiguration.getWebmailUrl().get(),
						emailIntegrationConfiguration.getWebmailAdminPort().get()));

		SchemaValidationFeature feature = new SchemaValidationFeature();
		return zcsSvc.getZcsAdminServicePort(feature);
	}

	private PortWrapper initClientPort() {
		ZcsService zcsSvc = new ZcsService(generateWsdlURL(emailIntegrationConfiguration.getWebmailProtocol().get(),
				emailIntegrationConfiguration.getWebmailUrl().get(),
				emailIntegrationConfiguration.getWebmailPort().get()));
		return new PortWrapper(zcsSvc.getZcsServicePort());
	}

	private long authenticateAdminPort(ZcsAdminPortType port, String username, String password) {
		zimbraadmin.AuthRequest req = new zimbraadmin.AuthRequest();
		req.setName(username);
		req.setPassword(password);

		zimbraadmin.AuthResponse response = port.authRequest(req);
		addSoapAcctAuthHeader((WSBindingProvider) port, response.getAuthToken());

		return response.getLifetime();
	}

	/**
	 * Creates and authenticates a {@link ZcsPortType} port for given account name using preAuth.
	 *
	 * @param accountName
	 *            account name
	 * @return authenticated port
	 */
	private long authenticateClientPort(ZcsPortType port, String accountName) throws EmailIntegrationException {
		String preAuthToken = PreAuthUtility.getPreAuthToken(accountName);

		AuthRequest req = new AuthRequest();
		AccountSelector accSelector = new AccountSelector();
		accSelector.setBy(AccountBy.NAME);
		accSelector.setValue(accountName);

		req.setAccount(accSelector);
		PreAuth preAuth = new PreAuth();
		preAuth.setValue(preAuthToken);
		preAuth.setTimestamp(PreAuthUtility.preAuthRequestTimestamp);
		req.setPreauth(preAuth);
		try {
			AuthResponse response = port.authRequest(req);

			addSoapAcctAuthHeader((WSBindingProvider) port, response.getAuthToken());
			return response.getLifetime();
		} catch (ServerSOAPFaultException e) {
			throw new EmailIntegrationException("Fail to communicate with server due to: " + e.getMessage(), e);
		}
	}

	private long authenticateClientPort(ZcsPortType port, String accountName, String password) {
		AuthRequest req = new AuthRequest();
		AccountSelector accSelector = new AccountSelector();
		accSelector.setBy(AccountBy.NAME);
		accSelector.setValue(accountName);

		req.setAccount(accSelector);
		req.setPassword(password);
		AuthResponse response = port.authRequest(req);

		addSoapAcctAuthHeader((WSBindingProvider) port, response.getAuthToken());
		return response.getLifetime();
	}

	void addSoapAcctAuthHeader(WSBindingProvider bp, String authToken) {
		try {
			JAXBContext.newInstance(HeaderContext.class);

			HeaderContext zimbraSoapHdrContext = new HeaderContext();
			zimbraSoapHdrContext.setAuthToken(authToken);

			AuthTokenControl tokenControl = new AuthTokenControl();
			tokenControl.setVoidOnExpired(true);
			zimbraSoapHdrContext.setAuthTokenControl(tokenControl);

			Header soapHdr = Headers.create(makeZimbraSoapHeaderContext(zimbraSoapHdrContext));
			bp.setOutboundHeaders(soapHdr);
		} catch (JAXBException e) {
			LOGGER.error("Error occurred while creating the JAXBRIContext", e);
		} catch (ParserConfigurationException e) {
			LOGGER.error("Error occured while creating the soap authentication header.", e);
		}
	}

	private static Element makeZimbraSoapHeaderContext(HeaderContext contextJaxb)
			throws ParserConfigurationException, JAXBException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().newDocument();
		JAXBContext jaxb = JAXBContext.newInstance(HeaderContext.class);
		Marshaller marshaller = jaxb.createMarshaller();

		ObjectFactory fact = new ObjectFactory();
		JAXBElement<HeaderContext> zimbraSoapHdrCtxt = fact.createContext(contextJaxb);
		marshaller.marshal(zimbraSoapHdrCtxt, doc);
		return doc.getDocumentElement();
	}

	private static URL generateWsdlURL(String protocol, String address, String port) {
		String url = new StringBuilder(protocol).append("://").append(address).append(":").append(port)
				.append("/service/wsdl/ZimbraService.wsdl").toString();
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			LOGGER.error("Could not generate proper WSDL location URL:" + url, e.getMessage());
		}
		return null;
	}

	/**
	 * Port cache used to encapsulate information for given port and authentication lifetime.
	 *
	 * @author S.Djulgerova
	 */
	public static class PortCache {

		private final Lock lock = new ReentrantLock();
		private PortWrapper portWrapper;
		private long authTimestamp = 0;
		private long lifetime = 0;

		/**
		 * Instantiates a new port cache.
		 *
		 * @param portWrapper
		 *            port wrapper
		 */
		public PortCache(PortWrapper portWrapper) {
			this.portWrapper = portWrapper;
		}

		/**
		 * Calculates if account should be re authenticated.
		 *
		 * @return true if account should be re authenticated false otherwise
		 */
		public boolean reauthenticate() {
			return (Instant.now().getEpochSecond() - authTimestamp) > lifetime;
		}

		public PortWrapper getPortWrapper() {
			return portWrapper;
		}

		public void setAuthTimestamp(long clientTimestamp) {
			this.authTimestamp = clientTimestamp;
		}

		public void setLifetime(long lifetime) {
			this.lifetime = lifetime;
		}

		/**
		 * Gets port and re authenticate if needed.
		 *
		 * @param authenticator
		 *            port authenticator function
		 * @return authenticated port.
		 */
		PortWrapper getPortWrapper(PortAuthenticator authenticator) throws EmailIntegrationException {
			lock.lock();
			try {
				if (reauthenticate()) {
					setAuthTimestamp(Instant.now().getEpochSecond());
					setLifetime(authenticator.authenticate(getPortWrapper()) / 1000);
				}
				return getPortWrapper();
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * Authenticate client for the port cache
	 *
	 * @author BBonev
	 */
	interface PortAuthenticator {

		/**
		 * Do authentication and returns the authenticated port
		 *
		 * @param portWrapper
		 *            the wrapping port to authenticate
		 * @return the authentication lifetime. When this time elapses the authentication will no longer be valid
		 * @throws EmailIntegrationException
		 *             if failed to communicate with server
		 */
		Long authenticate(PortWrapper portWrapper) throws EmailIntegrationException;
	}

	/**
	 * Admin and client ports wrapper class
	 *
	 * @author S.Djulgerova
	 */
	public static class PortWrapper {
		private Object port;

		public PortWrapper(Object port) {
			this.port = port;
		}

		public Object getPort() {
			return port;
		}
	}

}
