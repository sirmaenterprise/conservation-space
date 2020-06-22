package com.sirma.itt.cmf.security.sso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.web.bean.repository.User;
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
import org.opensaml.saml2.core.AuthnStatement;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;

/**
 * The Class WSO2SAMLClient responsible to handles credential requests. The
 * class is used to retrieve some params from the global config file.
 *
 * @author bbanchev
 */
public class WSO2SAMLClient {

	private static final String SAML_KEY_SUBJECT = "Subject";

	private static final String SAML_KEY_RESPONSE = "SAMLResponse";

	private static final String SAML_KEY_REQUEST = "SAMLRequest";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(WSO2SAMLClient.class);

	private static final String KEY_SESSION_INDEX = "SessionIndex";
	private static final String KEY_RETURN_URL = "returnURL";
	/** The relay state. */
	private static final String RELAY_STATE_URL = "/alfresco";

	/** The idp url. */
	private String idpUrl = null;

	/** The debug enabled. */
	private boolean debugEnabled;

	static {
		try {
			// to set default configs.
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new SEIPRuntimeException("Failed to configure SAML module!", e);
		}
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
	public void doPost(HttpServletRequest request, HttpServletResponse response, AlfrescoFacade alfrescoFacade)
			throws Exception {
		String responseMessage = request.getParameter(SAML_KEY_RESPONSE);
		if (responseMessage != null) { /* response from the identity provider */

			LOGGER.info("SAMLResponse received from IDP");

			Map<String, String> result = processResponseMessage(responseMessage);

			if (result == null) {
				// lets logout the user
				request.getSession().invalidate();
				response.sendRedirect(RELAY_STATE_URL);
			} else if (result.size() == 2 || result.size() == 1) {
				/*
				 * No user attributes are returned, so just goto the default
				 * home page.
				 */

				alfrescoFacade.setAuthenticatedUser(request, response, request.getSession(), getSubject(result), true);
				Object attribute = request.getSession().getAttribute(KEY_RETURN_URL);
				if (attribute != null && !attribute.toString().startsWith("/alfresco/ServiceLogin")) {
					response.sendRedirect(attribute.toString());
				} else {
					// set to home if no preference exists
					response.sendRedirect(RELAY_STATE_URL);
				}
				String sessionIndex = result.get(KEY_SESSION_INDEX);
				if (sessionIndex != null) {
					request.getSession().setAttribute(KEY_SESSION_INDEX, sessionIndex);
					alfrescoFacade.registerSession(sessionIndex, request.getSession());
				}
				request.getSession().removeAttribute(KEY_RETURN_URL);
			} else if (result.size() > 2) {
				/*
				 * We have received attributes, so lets show them in the
				 * attribute home page.
				 */
				String params = "/alfresco/home-attrib.jsp?";
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

		} else if (request.getParameter(SAML_KEY_REQUEST) != null) {
			String samlRequest = request.getParameter(SAML_KEY_REQUEST);
			processRequestMessage(samlRequest, alfrescoFacade);
		} else {
			/* time to create the authentication request or logout request */
			try {
				String requestMessage = buildRequestMessage(request);
				response.sendRedirect(requestMessage);
			} catch (IOException e) {
				LOGGER.error("SAML Message processing error: " + e.getMessage(), e);
			}
		}
	}

	private String getSubject(Map<String, String> result) {
		String subject = result.get(SAML_KEY_SUBJECT);
		int realmSplit = -1;
		if ((realmSplit = subject.indexOf('/')) > 1 && realmSplit < subject.length()) {
			subject = subject.substring(realmSplit + 1);
		}
		return subject;
	}

	/**
	 * Process request message.
	 *
	 * @param encodedRequestMessage
	 *            the encoded request message
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param alfrescoFacade
	 *            is the alfresco auth facade
	 * @throws UnsupportedEncodingException
	 *             on decode error
	 */
	private void processRequestMessage(String encodedRequestMessage, AlfrescoFacade alfrescoFacade)
			throws UnsupportedEncodingException {
		LogoutRequest processSAMLRequest = processSAMLRequest(
				new String(Base64.decode(encodedRequestMessage), SAMLSSOConfigurations.UTF_8));
		if (processSAMLRequest != null && processSAMLRequest.getSessionIndexes() != null) {
			for (SessionIndex nextSession : processSAMLRequest.getSessionIndexes()) {
				String sessionIndex = nextSession.getSessionIndex();
				HttpSession session = alfrescoFacade.getSession(sessionIndex);
				if (session != null) {
					debug("Automatic end of session ", sessionIndex);
					session.invalidate();
				}
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
		idpUrl = SAMLSSOConfigurations.getIdpURLInternal(req.getLocalAddr());
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
	 *         <artifactId>opensaml</artifactId>
	 *         <version>2.2.3</version> </dependency>
	 */
	public String buildRequestMessage(HttpServletRequest request) {

		RequestAbstractType requestMessage = null;

		// time to build the authentication request message
		if (!isLogoutRequest(request)) {
			requestMessage = buildAuthnRequestObject(request);

		} else { // ok, user needs to be single logged out
			Object user = request.getSession().getAttribute("_alfAuthTicket");
			String userName = null;
			if (user instanceof User) {
				userName = ((User) user).getUserName();
			}
			requestMessage = buildLogoutRequest(userName, request);
		}

		String encodedRequestMessage = null;
		try {
			encodedRequestMessage = encodeRequestMessage(requestMessage);
		} catch (MarshallingException e) {
			LOGGER.error("Encoding error:" + e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error("IO error:" + e.getMessage(), e);
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
		StringBuilder requestBuilder = new StringBuilder(idpUrl.length() + encodedRequestMessage.length() + 50);
		requestBuilder.append(idpUrl);
		requestBuilder.append("?SAMLRequest=");
		requestBuilder.append(encodedRequestMessage);
		requestBuilder.append("&RelayState=");
		requestBuilder.append(RELAY_STATE_URL);
		/* SAML2 Authentication Request is appended to IP's URL */
		return requestBuilder.toString();
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
		Object sessionIndex = request.getSession().getAttribute(KEY_SESSION_INDEX);
		LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();
		logoutReq.setID(Util.createID());

		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);
		logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + (5 * 60 * 1000)));

		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(getIssuerId(request));
		logoutReq.setIssuer(issuer);

		NameID nameId = new NameIDBuilder().buildObject();
		nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		nameId.setValue(user);
		logoutReq.setNameID(nameId);

		SessionIndex sessionIndexAttr = new SessionIndexBuilder().buildObject();
		sessionIndexAttr.setSessionIndex(sessionIndex != null ? sessionIndex.toString() : UUID.randomUUID().toString());
		logoutReq.getSessionIndexes().add(sessionIndexAttr);

		logoutReq.setReason("urn:oasis:names:tc:SAML:2.0:logout:user");

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
		Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
		issuer.setValue(getIssuerId(request));
		/* NameIDPolicy */
		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		nameIdPolicy.setSPNameQualifier("Issuer");
		nameIdPolicy.setAllowCreate(Boolean.TRUE);

		/* AuthnContextClass */
		AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
		AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder
				.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "AuthnContextClassRef", "saml");
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
		AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
				"AuthnRequest", "samlp");
		authRequest.setForceAuthn(Boolean.FALSE);
		authRequest.setIsPassive(Boolean.FALSE);
		authRequest.setIssueInstant(issueInstant);
		authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		authRequest.setAssertionConsumerServiceURL(buildURLForRedirect(request));
		authRequest.setIssuer(issuer);
		authRequest.setNameIDPolicy(nameIdPolicy);
		authRequest.setRequestedAuthnContext(requestedAuthnContext);
		authRequest.setID(Util.createID());
		authRequest.setVersion(SAMLVersion.VERSION_20);
		Integer attributeIndex = SAMLSSOConfigurations.getAttributeIndex();
		/* Requesting Attributes. This Index value is registered in the IDP */
		if (attributeIndex != null) {
			authRequest.setAssertionConsumerServiceIndex(attributeIndex);
		} else {
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
		return request.getServerName() + "_" + request.getServerPort();
	}

	/**
	 * Builds the url for redirect.
	 *
	 * @param request
	 *            the request
	 * @return the string
	 */
	protected String buildURLForRedirect(HttpServletRequest request) {
		StringBuilder requestBuilder = new StringBuilder(60);
		requestBuilder.append(request.getScheme());
		requestBuilder.append("://");
		requestBuilder.append(request.getServerName());
		requestBuilder.append(':');
		requestBuilder.append(request.getServerPort());
		String alfrescoContext = request.getContextPath();
		HttpSession session = request.getSession();
		if (session.getAttribute(KEY_RETURN_URL) == null) {
			String requestURI = request.getRequestURI();
			session.setAttribute(KEY_RETURN_URL, requestURI);
		}
		String urlOfIssuer = requestBuilder.append(alfrescoContext).append("/ServiceLogin").toString();
		debug("ISSUER URL: ", urlOfIssuer, ", RETURN URL: ", session.getAttribute(KEY_RETURN_URL));
		requestBuilder = null;
		return urlOfIssuer;
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
	private String encodeRequestMessage(RequestAbstractType requestMessage) throws MarshallingException, IOException {

		Marshaller marshaller = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(requestMessage);
		Element authDOM = marshaller.marshall(requestMessage);

		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);

		StringWriter rspWrt = new StringWriter();
		XMLHelper.writeNode(authDOM, rspWrt);
		deflaterOutputStream.write(rspWrt.toString().getBytes(SAMLSSOConfigurations.UTF_8));
		deflaterOutputStream.close();

		/* Encoding the compressed message */
		String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
		return URLEncoder.encode(encodedRequestMessage, SAMLSSOConfigurations.UTF_8).trim();
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
			responseXmlObj = unmarshall(responseMessage, SAMLSSOConfigurations.isEncryptedRequest());

		} catch (ConfigurationException e) {
			LOGGER.error("SAML response parsing error:" + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			LOGGER.error("SAML response paser error:" + e.getMessage(), e);
		} catch (SAXException e) {
			LOGGER.error("SAML response SAX error:" + e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("SAML response processing error:" + e.getMessage(), e);
		}

		return getResult(responseXmlObj);
	}

	/**
	 * Process standalone response message.
	 *
	 * @param responseMessage
	 *            the response message
	 * @return the map
	 */
	public Map<String, Object> processStandaloneResponseMessage(String responseMessage) {

		XMLObject responseXmlObj = null;

		try {
			boolean encrypted = false;
			if (responseMessage != null) {
				encrypted = !responseMessage.trim().startsWith("<?xml");
			}
			responseXmlObj = unmarshall(responseMessage, encrypted);
		} catch (ConfigurationException e) {
			LOGGER.error("SAML response parsing error:" + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			LOGGER.error("SAML response paser error:" + e.getMessage(), e);
		} catch (SAXException e) {
			LOGGER.error("SAML response SAX error:" + e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("SAML response processing error:" + e.getMessage(), e);
		}

		return getStandaloneResult(responseXmlObj);
	}

	/**
	 * Process saml request.
	 *
	 * @param <T>
	 *            the generic type
	 * @param decodedRequestMessage
	 *            the decoded request message
	 * @return the unmarshalled message as saml object or throws exception if
	 *         the message is not the required type
	 */
	@SuppressWarnings("unchecked")
	public <T extends RequestAbstractType> T processSAMLRequest(String decodedRequestMessage) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(
					decodedRequestMessage.getBytes(SAMLSSOConfigurations.UTF_8));

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject unmarshalled = unmarshaller.unmarshall(element);
			if (unmarshalled instanceof RequestAbstractType) {
				return (T) unmarshalled;
			}
			throw new IllegalArgumentException("Invalid message type: " + decodedRequestMessage);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (UnmarshallingException e) {
			throw new IllegalArgumentException(e);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Invalid message type: " + decodedRequestMessage, e);
		}
	}

	/**
	 * Unmarshall a response message.
	 *
	 * @param responseMessage
	 *            the response message
	 * @param encrypted
	 *            whether the request is {@link Base64} encrypted
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
	private XMLObject unmarshall(String responseMessage, boolean encrypted) throws ConfigurationException,
			ParserConfigurationException, SAXException, IOException, UnmarshallingException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		byte[] bytes = null;
		if (encrypted) {
			bytes = Base64.decode(responseMessage);
		} else {
			bytes = responseMessage.getBytes(SAMLSSOConfigurations.UTF_8);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);

		Document document = docBuilder.parse(is);
		Element element = document.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();

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

		Assertion assertion = response.getAssertions().get(0);
		Map<String, String> resutls = new HashMap<String, String>(1, 10);

		/*
		 * If the request has failed, the IDP shouldn't send an assertion. SSO
		 * profile spec 4.1.4.2 <Response> Usage
		 */
		if (assertion != null) {

			String subject = assertion.getSubject().getNameID().getValue();
			resutls.put(SAML_KEY_SUBJECT, subject); // get the subject

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
				List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
				if (authnStatements != null && !authnStatements.isEmpty()) {
					AuthnStatement authnStatement = authnStatements.get(0);
					resutls.put(KEY_SESSION_INDEX, authnStatement.getSessionIndex());
				}
			}
		}
		return resutls;
	}

	/**
	 * Gets the result for standalone request - not from ui.
	 *
	 * @param responseXmlObj
	 *            the response xml obj
	 * @return the result data
	 */
	private Map<String, Object> getStandaloneResult(XMLObject responseXmlObj) {

		if (responseXmlObj.getDOM().getNodeName().equals("saml2p:LogoutResponse")) {
			debug("SAML Logout response received");
			return null;
		}
		Map<String, Object> resutls = new HashMap<String, Object>();
		Response response = (Response) responseXmlObj;

		resutls.put("ResponseIssueInstant", response.getIssueInstant());
		Assertion assertion = response.getAssertions().get(0);

		/*
		 * If the request has failed, the IDP shouldn't send an assertion. SSO
		 * profile spec 4.1.4.2 <Response> Usage
		 */
		if (assertion != null) {
			String subject = assertion.getSubject().getNameID().getValue();
			resutls.put(SAML_KEY_SUBJECT, subject); // get the subject
			resutls.put("NotBefore", assertion.getConditions().getNotBefore());
			resutls.put("NotOnOrAfter", assertion.getConditions().getNotOnOrAfter());
		}
		return resutls;
	}

	/**
	 * Debug a message.
	 *
	 * @param msgs
	 *            the strings
	 */
	public void debug(Object... msgs) {
		if (debugEnabled) {
			if (msgs != null) {
				StringBuilder result = new StringBuilder(1024);
				for (Object msg : msgs) {
					result.append(msg);
				}
				LOGGER.debug(result.toString());
				result = null;
			}
		}
	}

}
