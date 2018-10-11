package com.sirma.itt.cmf.security.sso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The Class WSO2SAMLClient responsible to handles credential requests. The
 * class is used to retrieve some params from the global config file.
 * 
 * @author bbanchev
 */
public class WSO2SAMLClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WSO2SAMLClient.class);
	/** The Constant KEY_RETURN_URL. */
	private static final String KEY_RETURN_URL = "returnURL";

	/** The auth req random id. */
	private String authReqRandomId = Integer.toHexString(new Double(Math.random()).intValue());

	/** The relay state. */
	private String relayState = null;

	/** The idp url. */
	private String idpUrl = null;

	/** The attrib index. */
	private String attribIndex = null;

	/** The debug enabled. */
	private boolean debugEnabled;
	/** the read global properties from file. */
	private static Properties globalProperties;
	/** the retrieved cache. */
	private static String chiper;
	static {
		try {
			// to set default configs.
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if is sSO enabled.
	 * 
	 * @return true, if is sSO enabled
	 */
	public static boolean isSSOEnabled() {
		if (globalProperties == null) {
			ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
			try {
				globalProperties = new Properties();
				globalProperties.load(globalProps.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Object object = globalProperties.get("security.sso.enabled");
		return object == null ? true : Boolean.valueOf(object.toString()).booleanValue();
	}

	/**
	 * Retrieve <strong>security.sso.idpUrl</strong> from properties file.
	 * 
	 * @param req
	 *            is the current request
	 * 
	 * @return the url of idp or <code>https://127.0.0.1:9448/samlsso</code> as
	 *         fallback
	 */
	public static String getIdpURL(HttpServletRequest req) {
		if (globalProperties == null) {
			ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
			try {
				globalProperties = new Properties();
				globalProperties.load(globalProps.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Object object = globalProperties.get("security.sso.idpUrl." + req.getLocalAddr());
		return object == null || object.toString().trim().isEmpty() ? "https://127.0.0.1:9448/samlsso"
				: object.toString();
	}

	/**
	 * Retrieve <strong>cmf.encrypt.key</strong> from properties file.
	 * 
	 * 
	 * @return the chiper key or the default value of none is found
	 */
	public static String getChipherKey() {
		// cache for optimization
		if (chiper == null) {
			if (globalProperties == null) {
				ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
				try {
					globalProperties = new Properties();
					globalProperties.load(globalProps.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Object object = globalProperties.get("cmf.encrypt.key");
			chiper = object == null || object.toString().trim().isEmpty() ? "somePassword" : object
					.toString();
		}
		return chiper;
	}

	/**
	 * Do post.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param alfrescoFacade
	 *            the alfresco facade
	 * @throws Exception
	 *             the exception
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response,
			AlfrescoFacade alfrescoFacade) throws Exception {
		String responseMessage = request.getParameter("SAMLResponse");

		if (responseMessage != null) { /* response from the identity provider */

			LOGGER.info("SAMLResponse received from IDP");

			Map<String, String> result = processResponseMessage(responseMessage);

			if (result == null) {
				// lets logout the user
				response.sendRedirect("index.jsp");
			} else if (result.size() == 1) {
				/*
				 * No user attributes are returned, so just goto the default
				 * home page.
				 */

				alfrescoFacade.setAuthenticatedUser(request, response, request.getSession(),
						result.get("Subject"), true);
				Object attribute = request.getSession().getAttribute(KEY_RETURN_URL);
				if (attribute != null && !attribute.toString().startsWith("/alfresco/ServiceLogin")) {
					response.sendRedirect(attribute.toString());
				} else {
					// set to home if no preference exists
					response.sendRedirect("/alfresco/faces/jsp/dashboards/container.jsp");
				}
				request.getSession().removeAttribute(KEY_RETURN_URL);
			} else if (result.size() > 1) {
				/*
				 * We have received attributes, so lets show them in the
				 * attribute home page.
				 */
				String params = "home-attrib.jsp?";
				Object[] keys = result.keySet().toArray();
				for (int i = 0; i < result.size(); i++) {
					String key = (String) keys[i];
					String value = result.get(key);
					if (i != result.size()) {
						params = params + key + "=" + value + "&";
					} else {
						params = params + key + "=" + value;
					}
				}
				response.sendRedirect(params);
			} else {
				// something wrong, re-login
				response.sendRedirect("index.jsp");
			}

		} else { /* time to create the authentication request or logout request */

			try {
				String requestMessage = buildRequestMessage(request);

				response.sendRedirect(requestMessage);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Instantiates a new WSO2SAMLClient. SSO must be enabled. URL for samlsso
	 * is obtained by invoking {@link #getIdpURL()}
	 * 
	 * @param req
	 *            is the current request
	 */
	public WSO2SAMLClient(HttpServletRequest req) {
		idpUrl = getIdpURL(req);
		debugEnabled = LOGGER.isDebugEnabled();

	}

	/**
	 * Checks if is logout request.
	 * 
	 * @param request
	 *            the request
	 * @return true, if is logout request
	 */
	private boolean isLogoutRequest(HttpServletRequest request) {
		@SuppressWarnings("rawtypes")
		Enumeration parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameter = (String) parameterNames.nextElement();
			String[] string = request.getParameterValues(parameter);
			for (int i = 0; i < string.length; i++) {
				if ((string[i] != null) && (string[i].contains(":logout"))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the redirection URL with the appended SAML2 Request message.
	 * 
	 * @param request
	 *            the request
	 * @return redirectionUrl<dependency> <groupId>org.opensaml</groupId>
	 *         <artifactId>opensaml</artifactId> <version>2.2.3</version>
	 *         </dependency>
	 */
	public String buildRequestMessage(HttpServletRequest request) {

		RequestAbstractType requestMessage = null;

		// time to build the authentication request message
		if (!isLogoutRequest(request)) {
			requestMessage = buildAuthnRequestObject(request);

		} else { // ok, user needs to be single logged out
			requestMessage = buildLogoutRequest((String) request.getSession().getAttribute("user"),
					request);
		}

		String encodedRequestMessage = null;
		try {
			encodedRequestMessage = encodeRequestMessage(requestMessage);
		} catch (MarshallingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return requestMessage(encodedRequestMessage);
	}

	/**
	 * Request message.
	 * 
	 * @param encodedRequestMessage
	 *            the encoded request message
	 * @return the string
	 */
	public String requestMessage(String encodedRequestMessage) {
		/* SAML2 Authentication Request is appended to IP's URL */
		return idpUrl + "?SAMLRequest=" + encodedRequestMessage + "&RelayState=" + relayState;
	}

	/**
	 * Builds the logout request.
	 * 
	 * @param user
	 *            the user
	 * @param request
	 *            the request
	 * @return the logout request
	 */
	private LogoutRequest buildLogoutRequest(String user, HttpServletRequest request) {

		LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();

		logoutReq.setID(Util.createID());

		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);
		logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(getIssuerId(request));
		logoutReq.setIssuer(issuer);

		NameID nameId = new NameIDBuilder().buildObject();
		nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		nameId.setValue(user);
		logoutReq.setNameID(nameId);

		SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
		sessionIndex.setSessionIndex("1011238544");
		logoutReq.getSessionIndexes().add(sessionIndex);

		logoutReq.setReason("Single Logout");

		return logoutReq;
	}

	/**
	 * Builds the authn request object.
	 * 
	 * @param request
	 *            the request
	 * @return the authn request
	 */
	private AuthnRequest buildAuthnRequestObject(HttpServletRequest request) {

		/* Building Issuer object */
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion",
				"Issuer", "samlp");
		issuer.setValue(getIssuerId(request));
		/* NameIDPolicy */
		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		nameIdPolicy.setSPNameQualifier("Isser");
		nameIdPolicy.setAllowCreate(new Boolean(true));

		/* AuthnContextClass */
		AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
		AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder.buildObject(
				"urn:oasis:names:tc:SAML:2.0:assertion", "AuthnContextClassRef", "saml");
		authnContextClassRef
				.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

		/* AuthnContex */
		RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
		RequestedAuthnContext requestedAuthnContext = requestedAuthnContextBuilder.buildObject();
		requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
		requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

		DateTime issueInstant = new DateTime();

		/* Creation of AuthRequestObject */
		AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
		AuthnRequest authRequest = authRequestBuilder.buildObject(
				"urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
		authRequest.setForceAuthn(new Boolean(false));
		authRequest.setIsPassive(new Boolean(false));
		authRequest.setIssueInstant(issueInstant);
		authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		authRequest.setAssertionConsumerServiceURL(buildURLForRedirect(request));
		authRequest.setIssuer(issuer);
		authRequest.setNameIDPolicy(nameIdPolicy);
		authRequest.setRequestedAuthnContext(requestedAuthnContext);
		authRequest.setID(authReqRandomId);
		authRequest.setVersion(SAMLVersion.VERSION_20);

		/* Requesting Attributes. This Index value is registered in the IDP */
		if (attribIndex != null && !attribIndex.equals("")) {
			authRequest.setAttributeConsumingServiceIndex(1011238544);
		}

		return authRequest;
	}

	/**
	 * Gets the issuer id as dynamic data.
	 * 
	 * @param request
	 *            the request
	 * @return the issuer id
	 */
	private String getIssuerId(HttpServletRequest request) {
		String issuerID = request.getServerName() + "_" + request.getServerPort();
		debug("Issuer ID" + issuerID);
		return issuerID;
	}

	/**
	 * Builds the url for redirect.
	 * 
	 * @param request
	 *            the request
	 * @return the string
	 */
	protected String buildURLForRedirect(HttpServletRequest request) {
		String serverURL = request.getScheme() + "://" + request.getServerName() + ":"
				+ request.getServerPort();
		String alfrescoContext = request.getContextPath();
		HttpSession session = request.getSession();
		debug("RETURN URL: " + session.getAttribute(KEY_RETURN_URL));
		if (session.getAttribute(KEY_RETURN_URL) == null) {
			String requestURI = request.getRequestURI();
			session.setAttribute(KEY_RETURN_URL, requestURI);
		}
		String urlOfIssuer = serverURL + alfrescoContext + "/ServiceLogin";
		debug("ISSUER URL: " + urlOfIssuer);
		return urlOfIssuer;
	}

	/**
	 * Debug.
	 * 
	 * @param string
	 *            the string
	 */
	public void debug(String string) {
		if (debugEnabled) {
			LOGGER.debug(string);
		}
	}

	/**
	 * Encode request message.
	 * 
	 * @param requestMessage
	 *            the request message
	 * @return the string
	 * @throws MarshallingException
	 *             the marshalling exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String encodeRequestMessage(RequestAbstractType requestMessage)
			throws MarshallingException, IOException {

		Marshaller marshaller = org.opensaml.xml.Configuration.getMarshallerFactory()
				.getMarshaller(requestMessage);
		Element authDOM = marshaller.marshall(requestMessage);

		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream,
				deflater);

		StringWriter rspWrt = new StringWriter();
		XMLHelper.writeNode(authDOM, rspWrt);
		deflaterOutputStream.write(rspWrt.toString().getBytes());
		deflaterOutputStream.close();

		/* Encoding the compressed message */
		String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(),
				Base64.DONT_BREAK_LINES);
		return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();
	}

	/**
	 * Process response message.
	 * 
	 * @param responseMessage
	 *            the response message
	 * @return the map
	 */
	public Map<String, String> processResponseMessage(String responseMessage) {

		XMLObject responseXmlObj = null;

		try {
			responseXmlObj = unmarshall(responseMessage);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnmarshallingException e) {
			e.printStackTrace();
		}

		return getResult(responseXmlObj);
	}

	/**
	 * Unmarshall.
	 * 
	 * @param responseMessage
	 *            the response message
	 * @return the xML object
	 * @throws ConfigurationException
	 *             the configuration exception
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws SAXException
	 *             the sAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnmarshallingException
	 *             the unmarshalling exception
	 */
	private XMLObject unmarshall(String responseMessage) throws ConfigurationException,
			ParserConfigurationException, SAXException, IOException, UnmarshallingException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

		ByteArrayInputStream is = new ByteArrayInputStream(responseMessage.getBytes());

		Document document = docBuilder.parse(is);
		Element element = document.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration
				.getUnmarshallerFactory();

		// XMLConfigurator config =new XMLConfigurator();
		// config.load(Configuration.class.getResourceAsStream("saml2-assertion-config.xml"));
		// DefaultBootstrap.bootstrap();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
		return unmarshaller.unmarshall(element);

	}

	/*
	 * Process the response and returns the results
	 */
	/**
	 * Gets the result.
	 * 
	 * @param responseXmlObj
	 *            the response xml obj
	 * @return the result
	 */
	private Map<String, String> getResult(XMLObject responseXmlObj) {

		if (responseXmlObj.getDOM().getNodeName().equals("saml2p:LogoutResponse")) {
			debug("SAML Logout response received");
			return null;
		}

		Response response = (Response) responseXmlObj;
		debug("SAML Response: " + response);

		Assertion assertion = response.getAssertions().get(0);
		Map<String, String> resutls = new HashMap<String, String>();

		/*
		 * If the request has failed, the IDP shouldn't send an assertion. SSO
		 * profile spec 4.1.4.2 <Response> Usage
		 */
		if (assertion != null) {

			String subject = assertion.getSubject().getNameID().getValue();
			resutls.put("Subject", subject); // get the subject

			List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

			if (attributeStatementList != null) {
				// we have received attributes of user
				Iterator<AttributeStatement> attribStatIter = attributeStatementList.iterator();
				while (attribStatIter.hasNext()) {
					AttributeStatement statment = attribStatIter.next();
					List<Attribute> attributesList = statment.getAttributes();
					Iterator<Attribute> attributesIter = attributesList.iterator();
					while (attributesIter.hasNext()) {
						Attribute attrib = attributesIter.next();
						Element value = attrib.getAttributeValues().get(0).getDOM();
						String attribValue = value.getTextContent();
						resutls.put(attrib.getName(), attribValue);
					}
				}
			}
		}
		return resutls;
	}

}
