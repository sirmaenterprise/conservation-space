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

package org.opensaml.saml1.binding.decoding;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;

import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml1.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test case for SAML 1 HTTP POST decoding.
 */
public class HTTPPostDecoderTest extends BaseTestCase {
    
    private String responseRecipient = "https://sp.example.org/sso/acs";
    
    private String expectedRelayValue = "relay";
    
    private SAMLMessageDecoder decoder;
    
    private BasicSAMLMessageContext messageContext;
    
    private MockHttpServletRequest httpRequest;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        httpRequest = new MockHttpServletRequest();
        httpRequest.setMethod("POST");
        httpRequest.setParameter("TARGET", expectedRelayValue);
        
        messageContext = new BasicSAMLMessageContext();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(httpRequest));
        
        decoder = new HTTPPostDecoder(null);
    }

    /** Test decoding message. */
    public void testDecode() throws Exception {
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        
        String deliveredEndpointURL = samlResponse.getRecipient();
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
        populateRequestURL(httpRequest, deliveredEndpointURL);
        
        decoder.decode(messageContext);

        assertTrue(messageContext.getInboundMessage() instanceof Response);
        assertTrue(messageContext.getInboundSAMLMessage() instanceof Response);
        assertEquals(expectedRelayValue, messageContext.getRelayState());
    }
    
    public void testMessageEndpointGood() throws Exception {
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        
        String deliveredEndpointURL = samlResponse.getRecipient();
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
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
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        
        String deliveredEndpointURL = samlResponse.getRecipient() + "?paramFoo=bar&paramBar=baz";
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
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
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        
        String deliveredEndpointURL = samlResponse.getRecipient() + "/some/other/endpointURI";
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
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
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        
        String deliveredEndpointURL = "https://bogus-sp.example.com/sso/acs";
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
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
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        samlResponse.setRecipient(null);
        
        String deliveredEndpointURL = responseRecipient;
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
            fail("Passed delivered endpoint check, should have failed, binding requires endpoint on unsigned message");
        } catch (SecurityException e) {
            // do nothing, failure expected
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointMissingDestinationSigned() throws Exception {
        Response samlResponse = (Response) unmarshallElement("/data/org/opensaml/saml1/binding/Response.xml");
        samlResponse.setRecipient(null);
        
        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        KeyPair kp = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        Credential signingCred = SecurityHelper.getSimpleCredential(kp.getPublic(), kp.getPrivate());
        signature.setSigningCredential(signingCred);
        samlResponse.setSignature(signature);
        SecurityHelper.prepareSignatureParams(signature, signingCred, null, null);
        marshallerFactory.getMarshaller(samlResponse).marshall(samlResponse);
        Signer.signObject(signature);
        
        String deliveredEndpointURL = responseRecipient;
        
        httpRequest.setParameter("SAMLResponse", encodeMessage(samlResponse));
        
        populateRequestURL(httpRequest, deliveredEndpointURL);

        try {
            decoder.decode(messageContext);
            fail("Passed delivered endpoint check, should have failed, binding requires endpoint on signed message");
        } catch (SecurityException e) {
            // do nothing, failure expected
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
    
    protected String encodeMessage(SAMLObject message) throws MessageEncodingException, MarshallingException {
        marshallerFactory.getMarshaller(message).marshall(message);
        String messageStr = XMLHelper.nodeToString(message.getDOM());
        
        return Base64.encodeBytes(messageStr.getBytes(), Base64.DONT_BREAK_LINES);
    }
}