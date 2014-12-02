package com.sirma.itt.emf.authentication.sso.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;

/**
 * Constructs SAML requests and processes SAML responses.
 */
@ApplicationScoped
public class SAMLMessageProcessor implements Serializable {

	private static final long serialVersionUID = 834281906560124296L;

	private static Boolean requestEncrypted;

	/** The attrib index. */
	private String attribIndex;

	@Inject
	@Config(name = SSOConfiguration.SIGNATURE_VALIDATION_ENABLED, defaultValue = "false")
	private Boolean signatureValidationEnabled;

	@Inject
	@Config(name = SecurityConfigurationProperties.TRUSTSTORE_PATH, defaultValue = "")
	private String keystorePath;

	@Inject
	@Config(name = SecurityConfigurationProperties.TRUSTSTORE_PASSWORD, defaultValue = "")
	private String keystorePassword;

	@Inject
	@Config(name = SSOConfiguration.SIGNATURE_TRUSTSTORE_CERTIFICATE_ALIAS, defaultValue = "")
	private String certificateAlias;

	@Inject
	@Config(name = SSOConfiguration.SIGNATURE_TRUSTSTORE_CERTIFICATE_PASSWORD, defaultValue = "")
	private String certificatePassword;

	/** The log. */
	private static final Logger LOGGER = Logger.getLogger(SAMLMessageProcessor.class);

	/** The debug. */
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	private Credential credential;

	/**
	 * Initializes OpenSAML
	 */
	@PostConstruct
	public void init() {
		try {
			// Initializing the OpenSAML library, loading default configurations
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}

		if (signatureValidationEnabled) {
			initSignatureValidationCredential();
		}
	}

	/**
	 * Initializes the Credential used for SAML signature validation.
	 */
	private void initSignatureValidationCredential() {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			FileInputStream inputStream = new FileInputStream(keystorePath);
			keystore.load(inputStream, keystorePassword.toCharArray());
			inputStream.close();

			Map<String, String> passwordMap = new HashMap<String, String>();
			passwordMap.put(certificateAlias, certificatePassword);
			KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore,
					passwordMap);

			Criteria criteria = new EntityIDCriteria(certificateAlias);
			CriteriaSet criteriaSet = new CriteriaSet(criteria);

			credential = resolver.resolveSingle(criteriaSet);
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
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
		// TODO send consumer index to retrieve attributes
		// TODO get token from passive sts

		/* Building Issuer object */
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion",
				"Issuer", "samlp");
		issuer.setValue(issuerId);

		/* NameIDPolicy */
		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		nameIdPolicy.setSPNameQualifier("Isser");
		nameIdPolicy.setAllowCreate(Boolean.TRUE);

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

		/* Creation of AuthRequestObject */
		AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
		AuthnRequest authRequest = authRequestBuilder.buildObject(
				"urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
		authRequest.setForceAuthn(Boolean.FALSE);
		authRequest.setIsPassive(Boolean.FALSE);
		authRequest.setIssueInstant(new DateTime());
		authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		authRequest.setAssertionConsumerServiceURL(samlAssertionUrl);
		authRequest.setIssuer(issuer);
		authRequest.setNameIDPolicy(nameIdPolicy);
		authRequest.setRequestedAuthnContext(requestedAuthnContext);
		authRequest.setID(Integer.toHexString(new Double(Math.random()).intValue()));
		authRequest.setVersion(SAMLVersion.VERSION_20);

		/* Requesting Attributes. This Index value is registered in the IDP */
		if ((attribIndex != null) && !attribIndex.equals("")) {
			authRequest.setAttributeConsumingServiceIndex(Integer.parseInt(attribIndex));
		}

		try {
			return encodeRequestMessage(authRequest);
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
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
	public String buildLogoutRequest(String userId, String issuerId, Object sessionIndex) {
		LogoutRequest logoutReq = new LogoutRequestBuilder().buildObject();

		logoutReq.setID(UUID.randomUUID().toString());

		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);
		logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + (5 * 60 * 1000)));

		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerId);
		logoutReq.setIssuer(issuer);

		NameID nameId = new NameIDBuilder().buildObject();
		nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
		nameId.setValue(userId);
		logoutReq.setNameID(nameId);

		SessionIndex sessionIndexAttr = new SessionIndexBuilder().buildObject();
		sessionIndexAttr.setSessionIndex(sessionIndex != null ? sessionIndex.toString() : UUID
				.randomUUID().toString());
		logoutReq.getSessionIndexes().add(sessionIndexAttr);

		logoutReq.setReason("urn:oasis:names:tc:SAML:2.0:logout:user");

		try {
			return encodeRequestMessage(logoutReq);
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Checks if the requests are encrypted. After first check, result is cached
	 *
	 * @param msg
	 *            is the message to check if it is encrypted
	 * @return true, if is request encrypted
	 */
	public boolean isRequestEncrypted(String msg) {
		// cache for optimization
		if (requestEncrypted == null) {
			if (msg != null && !msg.trim().startsWith("<?xml")) {
				requestEncrypted = Boolean.TRUE;
			} else {
				requestEncrypted = Boolean.FALSE;
			}
		}
		return requestEncrypted.booleanValue();
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
	 * Processes a SAML response message.
	 *
	 * @param samlResponse
	 *            response message as xml.
	 * @return map containing the parsed values
	 */
	public Map<String, String> processSAMLResponse(String samlResponse) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			byte[] bytes = null;
			if (isRequestEncrypted(samlResponse)) {
				bytes = Base64.decode(samlResponse);
			} else {
				bytes = samlResponse.getBytes();
			}
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration
					.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject xmlObject = unmarshaller.unmarshall(element);

			if (!(xmlObject instanceof Response)) {
				return Collections.emptyMap();
			}
			Response response = (Response) xmlObject;

			if (signatureValidationEnabled) {
				validateSignature(response.getSignature());
			}

			if (TRACE_ENABLED) {
				LOGGER.trace("SAML Response: " + response);
			}
			Assertion assertion = response.getAssertions().get(0);
			Map<String, String> result = new HashMap<String, String>();

			// If the request has failed, the IDP shouldn't send an assertion. SSO profile spec
			// 4.1.4.2 <Response> Usage
			if (assertion != null) {
				String subject = assertion.getSubject().getNameID().getValue();
				result.put("Subject", subject); // get the subject

				List<AttributeStatement> attributeStatementList = assertion
						.getAttributeStatements();

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
				List<AuthnStatement> authnStatements = assertion.getAuthnStatements();
				if (authnStatements != null && !authnStatements.isEmpty()) {
					AuthnStatement authnStatement = authnStatements.get(0);
					result.put("SessionIndex", authnStatement.getSessionIndex());
				}
			}
			return result;
		} catch (ParserConfigurationException e) {
			throw new EmfRuntimeException(e);
		} catch (SAXException e) {
			throw new EmfRuntimeException(e);
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		} catch (UnmarshallingException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Process saml request that contains object that is subtype of {@link RequestAbstractType}. If
	 * it is not or it is not of type T exception is thrown. On any other error is throw exception
	 * as well of type {@link EmfRuntimeException}
	 *
	 * @param <T>
	 *            the generic type
	 * @param decodedRequestMessage
	 *            the decoded request message
	 * @return the unmarshalled message as saml object or throws exception if the message is not the
	 *         required type
	 */
	@SuppressWarnings("unchecked")
	public <T extends RequestAbstractType> T processSAMLRequest(String decodedRequestMessage) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			ByteArrayInputStream is = new ByteArrayInputStream(decodedRequestMessage.getBytes());

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration
					.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject unmarshalled = unmarshaller.unmarshall(element);
			if (unmarshalled instanceof RequestAbstractType) {
				return (T) unmarshalled;
			}
			throw new EmfRuntimeException("Invalid message type: " + decodedRequestMessage);
		} catch (ParserConfigurationException e) {
			throw new EmfRuntimeException(e);
		} catch (SAXException e) {
			throw new EmfRuntimeException(e);
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		} catch (UnmarshallingException e) {
			throw new EmfRuntimeException(e);
		} catch (ClassCastException e) {
			throw new EmfRuntimeException("Invalid message type: " + decodedRequestMessage, e);
		}
	}

	/**
	 * Decodes SAML response message sent back from the IdP.
	 *
	 * @param message
	 *            message to decode
	 * @return decoded message
	 */
	public String decodeResponseMessage(String message) {
		try {
			byte[] base64DecodedByteArray = Base64.decode(message);

			Inflater inflater = new Inflater(true);
			inflater.setInput(base64DecodedByteArray);
			// since the operation is decompression, it is impossible to know how much space is
			// needed. so an enough number should be allocated
			byte[] xmlMessageBytes = new byte[5000];

			int resultLength = inflater.inflate(xmlMessageBytes);
			if (!inflater.finished()) {
				throw new RuntimeException("didn't allocate enough space to hold "
						+ "decompressed data");
			}

			inflater.end();

			return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new EmfRuntimeException(e);
		} catch (DataFormatException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Checks the validity of a signature - is the request consistent with the signature and is the
	 * signature signed with a trusted key (not signed by other party).
	 *
	 * @param signature
	 *            signature to validate.
	 * @throws SAMLMessageValidationException
	 *             when the signature is not valid.
	 */
	public void validateSignature(Signature signature) throws SAMLMessageValidationException {
		if (signature == null) {
			throw new SAMLMessageValidationException("The SAML response is not signed!");
		}
		try {
			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			profileValidator.validate(signature);
			SignatureValidator sigValidator = new SignatureValidator(credential);
			sigValidator.validate(signature);
		} catch (ValidationException e) {
			throw new SAMLMessageValidationException(e);
		}
	}

	/**
	 * Validates a security token for expiration time and digital signing. TODO check time period
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

			ByteArrayInputStream is = new ByteArrayInputStream(token.getBytes());

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration
					.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject xmlObject = unmarshaller.unmarshall(element);

			Response response = (Response) xmlObject;

			validateSignature(response.getSignature());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
