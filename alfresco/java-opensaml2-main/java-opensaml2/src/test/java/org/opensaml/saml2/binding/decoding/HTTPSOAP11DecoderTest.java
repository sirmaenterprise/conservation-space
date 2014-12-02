/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml2.binding.decoding;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;

import org.opensaml.common.BaseTestCase;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test case for HTTP SOAP 1.1 decoder.
 */
public class HTTPSOAP11DecoderTest extends BaseTestCase {
    
    private String attribQueryDestination = "https://idp.example.com/idp/aa";
    
    private SAMLMessageDecoder decoder;
    
    private BasicSAMLMessageContext messageContext;
    
    private MockHttpServletRequest httpRequest;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        httpRequest = new MockHttpServletRequest();
        httpRequest.setMethod("POST");
        
        messageContext = new BasicSAMLMessageContext();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(httpRequest));
        
        decoder = new HTTPSOAP11Decoder();
    }

    /**
     * Tests decoding a SOAP 1.1 message.
     */
    public void testDecoding() throws Exception {
        String requestContent = "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap11:Body><samlp:Response ID=\"foo\" IssueInstant=\"1970-01-01T00:00:00.000Z\" Version=\"2.0\" "
                + "xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"><samlp:Status><samlp:StatusCode "
                + "Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/></samlp:Status></samlp:Response>"
                + "</soap11:Body></soap11:Envelope>";
        httpRequest.setContent(requestContent.getBytes());

        decoder.decode(messageContext);

        assertTrue(messageContext.getInboundMessage() instanceof Envelope);
        assertTrue(messageContext.getInboundSAMLMessage() instanceof Response);
    }
    
    public void testMessageEndpointGood() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        AttributeQuery samlRequest = (AttributeQuery) soapEnvelope.getBody().getUnknownXMLObjects().get(0);
        String deliveredEndpointURL = samlRequest.getDestination();
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);
        
        try {
            decoder.decode(messageContext);
        } catch (SecurityException e) {
            fail("Caught SecurityException: " + e.getMessage());
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointGoodWithQueryParams() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        AttributeQuery samlRequest = (AttributeQuery) soapEnvelope.getBody().getUnknownXMLObjects().get(0);
        String deliveredEndpointURL = samlRequest.getDestination() + "?paramFoo=bar&paramBar=baz";
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
        } catch (SecurityException e) {
            fail("Caught SecurityException: " + e.getMessage());
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointInvalidURI() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        AttributeQuery samlRequest = (AttributeQuery) soapEnvelope.getBody().getUnknownXMLObjects().get(0);
        String deliveredEndpointURL = samlRequest.getDestination() + "/some/other/endpointURI";
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
            fail("Passed delivered endpoint check, should have failed");
        } catch (SecurityException e) {
            // do nothing, failure expected
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointInvalidHost() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        String deliveredEndpointURL = "https://bogusidp.example.com/idp/sso";
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
            fail("Passed delivered endpoint check, should have failed");
        } catch (SecurityException e) {
            // do nothing, failure expected
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointMissingDestinationNotSigned() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        AttributeQuery samlRequest = (AttributeQuery) soapEnvelope.getBody().getUnknownXMLObjects().get(0);
        samlRequest.setDestination(null);
        
        String deliveredEndpointURL = attribQueryDestination;
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
        } catch (SecurityException e) {
            fail("Caught SecurityException: " + e.getMessage());
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointMissingDestinationSigned() throws Exception {
        Envelope soapEnvelope = (Envelope) unmarshallElement("/data/org/opensaml/saml2/binding/AttributeQuerySOAP.xml");
        
        AttributeQuery samlRequest = (AttributeQuery) soapEnvelope.getBody().getUnknownXMLObjects().get(0);
        samlRequest.setDestination(null);
        
        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        KeyPair kp = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        Credential signingCred = SecurityHelper.getSimpleCredential(kp.getPublic(), kp.getPrivate());
        signature.setSigningCredential(signingCred);
        samlRequest.setSignature(signature);
        SecurityHelper.prepareSignatureParams(signature, signingCred, null, null);
        marshallerFactory.getMarshaller(soapEnvelope).marshall(soapEnvelope);
        Signer.signObject(signature);
        
        String deliveredEndpointURL = attribQueryDestination;
        
        httpRequest.setContent(encodeMessage(soapEnvelope).getBytes());
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
            // SOAP binding doesn't require the Destination, even when signed
        } catch (SecurityException e) {
            fail("Caught SecurityException: " + e.getMessage());
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    private void populateRequestURL(MockHttpServletRequest request, String requestURL) {
        URL url = null;
        try {
            url = new URL(requestURL);
        } catch (MalformedURLException e) {
            fail("Malformed URL: " + e.getMessage());
        }
        request.setScheme(url.getProtocol());
        request.setServerName(url.getHost());
        if (url.getPort() != -1) {
            request.setServerPort(url.getPort());
        } else {
            if ("https".equalsIgnoreCase(url.getProtocol())) {
                request.setServerPort(443);
            } else if ("http".equalsIgnoreCase(url.getProtocol())) {
                request.setServerPort(80);
            }
        }
        request.setRequestURI(url.getPath());
        request.setQueryString(url.getQuery());
    }
    
    protected String encodeMessage(XMLObject message) throws MessageEncodingException, MarshallingException {
        marshallerFactory.getMarshaller(message).marshall(message);
        return XMLHelper.nodeToString(message.getDOM());
    }
}