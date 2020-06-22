package com.sirma.itt.emf.authentication.sso.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
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
import org.opensaml.saml2.core.LogoutResponse;
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
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Constructs SAML requests and processes SAML responses.
 */
@Singleton
public class SAMLMessageProcessor {
	/** Key for session index used in httpsession. */
	public static final String SAML_KEY_SESSION = "SessionIndex";
	private static final String SAML_KEY_SUBJECT = "Subject";
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLMessageProcessor.class);

	@Inject
	private SSOConfiguration ssoConfiguration;

	/**
	 * Initializes OpenSAML.
	 */
	@SuppressWarnings("static-method")
	@PostConstruct
	public void init() {
		try {
			// Initializing the OpenSAML library, loading default configurations
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new EmfRuntimeException("Failed to configure opensaml bootstrap!", e);
		}
	}

	/**
	 * Constructs a SAML authentication request based on http servlet request. Config properties for
	 * {@code issuerIdProperty} and {@code #assertionURL} are honored
	 *
	 * @param request
	 *            the http request to generate for
	 * @return the SAML request message encoded in base64.
	 */
	public String buildAuthenticationRequest(HttpServletRequest request) {
		return buildAuthenticationRequest(getIssuerId(request), ssoConfiguration.getAssertionURL().get());
	}

	/**
	 * Constructs a SAML authentication request.
	 *
	 * @param issuerId
	 *            issuer id used by the IdP.
	 * @param samlAssertionUrl
	 *            assertion return url - browser is redirected to this url after successful login.
	 * @return the SAML request message encoded in base64.
	 */
	public String buildAuthenticationRequest(String issuerId, String samlAssertionUrl) {
		/* Building Issuer object */
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
		issuer.setValue(issuerId);

		/* NameIDPolicy */
		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		nameIdPolicy.setSPNameQualifier("Isser");
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

		/* Creation of AuthRequestObject */
		AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
		AuthnRequest authRequest = authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol",
				"AuthnRequest", "samlp");
		authRequest.setForceAuthn(Boolean.FALSE);
		authRequest.setIsPassive(Boolean.FALSE);
		authRequest.setIssueInstant(new DateTime());
		authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		authRequest.setAssertionConsumerServiceURL(samlAssertionUrl);
		authRequest.setIssuer(issuer);
		authRequest.setNameIDPolicy(nameIdPolicy);
		authRequest.setRequestedAuthnContext(requestedAuthnContext);
		authRequest.setID(UUID.randomUUID().toString());
		authRequest.setVersion(SAMLVersion.VERSION_20);

		try {
			return encodeRequestMessage(authRequest);
		} catch (Exception e) {
			throw new EmfRuntimeException("Authentication request encoding failed!", e);
		}
	}

	/**
	 * Builds the logout request.
	 *
	 * @param userId
	 *            is the user to be sent as NameId property
	 * @param issuerId
	 *            issuer id.
	 * @param sessionIndex
	 *            is the index for the current session. When null, random id is generated
	 * @return the logout request
	 */
	public static String buildLogoutRequest(String userId, String issuerId, Object sessionIndex) {
		LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();

		StringBuilder responseId = new StringBuilder();
		responseId.append(userId);
		responseId.append('/');
		responseId.append(UUID.randomUUID());

		logoutReq.setID(responseId.toString());

		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);
		logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerId);
		logoutReq.setIssuer(issuer);

		NameID nameId = new NameIDBuilder().buildObject();
		nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		nameId.setValue(userId);
		logoutReq.setNameID(nameId);

		SessionIndex sessionIndexAttr = new SessionIndexBuilder().buildObject();
		if (sessionIndex == null) {
			throw new IllegalArgumentException("Session index is required");
		}
		sessionIndexAttr.setSessionIndex(sessionIndex.toString());
		logoutReq.getSessionIndexes().add(sessionIndexAttr);

		logoutReq.setReason("urn:oasis:names:tc:SAML:2.0:logout:user");

		try {
			return encodeRequestMessage(logoutReq);
		} catch (Exception e) {
			throw new EmfRuntimeException("Deauthentication request encoding failed!", e);
		}
	}

	/**
	 * Checks if the requests are encrypted. After first check, the result is cached.
	 *
	 * @param samlMessage
	 *            is the message to check if it is encrypted
	 * @return true, if is request encrypted
	 */
	public static boolean isEncodedSAMLMessage(byte[] samlMessage) {
		return org.apache.commons.codec.binary.Base64.isBase64(samlMessage);
	}

	/**
	 * Decode saml message.
	 *
	 * @param samlMessage
	 *            is the message to be decoded
	 * @return the decoded message as byte[]
	 */
	public static byte[] decodeSAMLMessage(byte[] samlMessage) {
		return org.apache.commons.codec.binary.Base64.decodeBase64(samlMessage);
	}

	/**
	 * Encode saml message.
	 *
	 * @param samlMessage
	 *            the saml response
	 * @return the encoded message as byte[]
	 */
	public static byte[] encodeSAMLMessage(byte[] samlMessage) {
		return org.apache.commons.codec.binary.Base64.encodeBase64(samlMessage);
	}

	/**
	 * Base64 encode SAML request message.
	 *
	 * @param requestMessage
	 *            the request message
	 * @return the string
	 * @throws MarshallingException
	 *             the marshalling exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static String encodeRequestMessage(RequestAbstractType requestMessage)
			throws MarshallingException, IOException {
		Marshaller marshaller = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(requestMessage);
		Element authDOM = marshaller.marshall(requestMessage);

		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater)) {
			StringWriter rspWrt = new StringWriter();
			XMLHelper.writeNode(authDOM, rspWrt);
			deflaterOutputStream.write(rspWrt.toString().getBytes(StandardCharsets.UTF_8));
		}

		/* Encoding the compressed message */
		String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
		return URLEncoder.encode(encodedRequestMessage, "UTF-8").trim();
	}

	/**
	 * Processes a SAML response message.
	 *
	 * @param token
	 *            response message as byte[] array xml - might be encoded.
	 * @return map containing the parsed values
	 */
	public Map<String, String> processSAMLResponse(byte[] token) {
		XMLObject xmlObject = parseResponseXML(token);

		if (xmlObject instanceof Response) {

			Response response = (Response) xmlObject;
			LOGGER.trace("SAML Response {}", response);

			if (ssoConfiguration.isSignatureValidationEnabled()) {
				validateSignature(response.getSignature());
			}

			return extractResponseData(response);
		} else if (xmlObject instanceof LogoutResponse) {
			LogoutResponse response = (LogoutResponse) xmlObject;
			return extractLogoutResponseData(response);
		}
		return Collections.emptyMap();
	}

	private Map<String, String> extractLogoutResponseData(LogoutResponse response) {
		String[] idParts = response.getInResponseTo().split("/");
		if (idParts.length == 2) {
			return Collections.singletonMap(SAML_KEY_SUBJECT, idParts[0]);
		}
		LOGGER.error("Missing user id during logout!");
		return Collections.emptyMap();
	}

	/**
	 * Extracts data from the SAML {@link Response}.
	 *
	 * @param response
	 *            saml response.
	 * @return extracted data.
	 */
	private static Map<String, String> extractResponseData(Response response) {
		Assertion assertion = response.getAssertions().get(0);
		Map<String, String> result = new HashMap<>();

		// If the request has failed, the IDP shouldn't send an assertion. SSO
		// profile spec
		// 4.1.4.2 <Response> Usage
		if (assertion != null) {
			String subject = assertion.getSubject().getNameID().getValue();
			result.put(SAML_KEY_SUBJECT, cleanUserName(subject)); // get the
																	// subject

			List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

			readAttributesList(result, attributeStatementList);
			List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
			if (authnStatements != null && !authnStatements.isEmpty()) {
				AuthnStatement authnStatement = authnStatements.get(0);
				result.put(SAML_KEY_SESSION, authnStatement.getSessionIndex());
			}
		}
		return result;
	}

	/**
	 * Removes the store from the full name if present. The store name is expected to be at the beginning of the name
	 * ending with /. The method also converts the provided tenant id to lower case to ignore further case checks.
	 *
	 * @param fullName
	 *            the full name
	 * @return the string
	 */
	private static String cleanUserName(String fullName) {
		String updated = fullName;
		int indexOf = updated.indexOf('/');
		if (indexOf > 0 && indexOf < updated.length()) {
			updated = updated.substring(indexOf + 1);
		}
		indexOf = updated.lastIndexOf('@');
		// convert domain name to lower case so that it matches with actual tenant names.
		if (indexOf > 0 && indexOf < updated.length()) {
			updated = updated.substring(0, indexOf) + updated.substring(indexOf).toLowerCase();
		}
		return updated;
	}

	private static void readAttributesList(Map<String, String> result,
			List<AttributeStatement> attributeStatementList) {
		if (attributeStatementList != null) {
			// we have received attributes of user
			for (AttributeStatement statement : attributeStatementList) {
				for (Attribute attribute : statement.getAttributes()) {
					Element value = attribute.getAttributeValues().get(0).getDOM();
					String attribValue = value.getTextContent();
					result.put(attribute.getName(), attribValue);
				}
			}
		}
	}

	/**
	 * Parses a SAML response XML document.
	 *
	 * @param samlResponse
	 *            saml response.
	 * @return parsed response as an XML object.
	 */
	private static XMLObject parseResponseXML(byte[] samlResponse) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			byte[] bytes = null;
			if (isEncodedSAMLMessage(samlResponse)) {
				bytes = decodeSAMLMessage(samlResponse);
			} else {
				bytes = samlResponse;
			}
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			return unmarshaller.unmarshall(element);
		} catch (ParserConfigurationException | SAXException | IOException | UnmarshallingException e) {
			throw new EmfRuntimeException("Unable to parse the SAML response", e);
		}
	}

	/**
	 * Process saml request that contains object that is subtype of {@link RequestAbstractType}. If it is not or it is
	 * not of type T exception is thrown. On any other error is throw exception as well of type
	 * {@link EmfRuntimeException}
	 *
	 * @param <T>
	 *            the generic type
	 * @param decodedRequestMessage
	 *            the decoded request message
	 * @return the unmarshalled message as saml object or throws exception if the message is not the required type
	 */
	@SuppressWarnings("unchecked")
	public <T extends RequestAbstractType> T processSAMLRequest(String decodedRequestMessage) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(decodedRequestMessage.getBytes(StandardCharsets.UTF_8));

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject unmarshalled = unmarshaller.unmarshall(element);
			if (unmarshalled instanceof RequestAbstractType) {
				return (T) unmarshalled;
			}
			throw new EmfRuntimeException("Invalid message type: " + decodedRequestMessage);
		} catch (ParserConfigurationException | SAXException | IOException | UnmarshallingException
				| ClassCastException e) {
			throw new EmfRuntimeException("Unable to process the SAML request", e);
		}
	}

	/**
	 * Decodes SAML response message sent back from the IdP.
	 *
	 * @param message
	 *            message to decode
	 * @return decoded message
	 */
	public static String decodeResponseMessage(String message) {
		try {
			byte[] base64DecodedByteArray = Base64.decode(message);

			Inflater inflater = new Inflater(true);
			inflater.setInput(base64DecodedByteArray);
			// since the operation is decompression, it is impossible to know
			// how much space is
			// needed. so an enough number should be allocated
			byte[] xmlMessageBytes = new byte[5000];

			int resultLength = inflater.inflate(xmlMessageBytes);
			if (!inflater.finished()) {
				throw new IllegalStateException("Didn't allocate enough space to hold " + "decompressed data");
			}

			inflater.end();

			return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new EmfRuntimeException("Unsupported encoding error!", e);
		} catch (DataFormatException e) {
			throw new EmfRuntimeException("Data format error!", e);
		}
	}

	/**
	 * Checks the validity of a signature - is the request consistent with the signature and is the signature signed
	 * with a trusted key (not signed by other party).
	 *
	 * @param signature
	 *            signature to validate.
	 */
	public void validateSignature(Signature signature) {
		if (signature == null) {
			throw new SAMLMessageValidationException("The SAML response is not signed!");
		}
		try {
			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			profileValidator.validate(signature);
			SignatureValidator sigValidator = new SignatureValidator(ssoConfiguration.getCredential().get());
			sigValidator.validate(signature);
		} catch (ValidationException e) {
			throw new SAMLMessageValidationException(e);
		}
	}

	/**
	 * Validates a security token for expiration time and digital signing.<br>
	 * TODO check time period
	 *
	 * @param token
	 *            token to validate.
	 * @return true if valid, false otherwise.
	 */
	public boolean validateToken(String token) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

			ByteArrayInputStream is = new ByteArrayInputStream(token.getBytes(StandardCharsets.UTF_8));

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject xmlObject = unmarshaller.unmarshall(element);

			Response response = (Response) xmlObject;

			validateSignature(response.getSignature());
			return true;
		} catch (Exception e) {
			String message = "Token validation failed:";
			LOGGER.warn(message + e.getMessage());
			LOGGER.trace(message, e);
			return false;
		}
	}

	/**
	 * Gets the default issuer id.
	 *
	 * @return the issuer id
	 */
	public ConfigurationProperty<String> getIssuerId() {
		return ssoConfiguration.getIssuerId();
	}

	/**
	 * Finds the default request issuers or if it is provided - the {@link SSOConfiguration#getIssuerId()} config property
	 * used for proxy access.
	 *
	 * @param request
	 *            to check if no config is provided
	 * @return the request or the configured issuer
	 */
	public String getIssuerId(HttpServletRequest request) {
		// set the original return url on invoking servlet
		return ssoConfiguration.getIssuerId().isNotSet() ? request.getServerName() + "_" + request.getServerPort()
				: ssoConfiguration.getIssuerId().get();
	}

	/**
	 * Build a logout message for idp saml2 processor using the arguments for the current session to logout.
	 *
	 * @param userId
	 *            is user that would be logged out
	 * @param relayTo
	 *            where to redirect after logout
	 * @param request
	 *            is the http request
	 * @return the builder url for the request.
	 */
	public String buildLogoutMessage(String userId, String relayTo, HttpServletRequest request) {
		Object sessionIndex = request.getSession().getAttribute(SAML_KEY_SESSION);
		return buildLogoutMessage(userId, relayTo, request, sessionIndex);
	}

	/**
	 * Build a logout message for idp saml2 processor using the arguments for the given session to logout.
	 *
	 * @param userId
	 *            is user that would be logged out
	 * @param relayTo
	 *            where to redirect after logout
	 * @param request
	 *            is the http request
	 * @param sessionIndex
	 *            the session to logout
	 * @return the builder url for the request.
	 */
	public String buildLogoutMessage(String userId, String relayTo, HttpServletRequest request, Object sessionIndex) {
		String issuerId = getIssuerId(request);
		String encodedRequestMessage = buildLogoutRequest(userId, issuerId, sessionIndex);

		StringBuilder requestURI = new StringBuilder();
		requestURI.append(ssoConfiguration.getIdpUrlForInterface(request.getLocalAddr()));
		requestURI.append("?SAMLRequest=");
		requestURI.append(encodedRequestMessage);
		requestURI.append("&RelayState=");
		try {
			if (StringUtils.isNotBlank(relayTo)) {
				requestURI.append(URLEncoder.encode(relayTo, StandardCharsets.UTF_8.name()));
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unable to encode RelayState: {}", relayTo, e);
			requestURI.append(request.getContextPath());
		}
		return requestURI.toString();
	}

	/**
	 * Gets the subject from parsed saml resposne.
	 *
	 * @param samlResponse
	 *            the saml response map
	 * @return the subject
	 */
	public static String getSubject(Map<String, String> samlResponse) {
		return samlResponse.get(SAML_KEY_SUBJECT);
	}

	/**
	 * Gets the session index from parsed saml resposne.
	 *
	 * @param samlResponse
	 *            the saml response map
	 * @return the session index
	 */
	public static String getSessionIndex(Map<String, ? extends Serializable> samlResponse) {
		return (String) samlResponse.get(SAML_KEY_SESSION);
	}
}
