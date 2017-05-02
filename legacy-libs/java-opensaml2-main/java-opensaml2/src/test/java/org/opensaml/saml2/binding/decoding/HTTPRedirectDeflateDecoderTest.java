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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.decoding.SAMLMessageDecoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 */
public class HTTPRedirectDeflateDecoderTest extends BaseTestCase {
    
    private String authnRequestDestination = "https://idp.example.com/idp/sso";
    
    private String expectedRelayValue = "relay";
    
    private SAMLMessageDecoder decoder;
    
    private BasicSAMLMessageContext messageContext;
    
    private MockHttpServletRequest httpRequest;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        httpRequest = new MockHttpServletRequest();
        httpRequest.setMethod("GET");
        httpRequest.setParameter("RelayState", expectedRelayValue);
        
        messageContext = new BasicSAMLMessageContext();
        messageContext.setInboundMessageTransport(new HttpServletRequestAdapter(httpRequest));
        
        decoder = new HTTPRedirectDeflateDecoder();
    }

    public void testResponseDecoding() throws Exception{
        // Note, Spring's Mock objects don't do URL encoding/decoding, so this is the URL decoded form
        httpRequest.setParameter("SAMLResponse", "fZAxa8NADIX3/opDe3yXLG2F7VASCoF2qdMM3Y6LkhrOp8PSlfz8uqYdvBTeIMHT08ert7chmi8apefUwLpyYCgFPvfp2sD78Xn1ANv2rhY/xIxvJJmTkNmTaJ+8zkefqhmtpZsfcqSKxyuYw76BC/M0iBQ6JFGfdMp/vHcrt550dA5nVc65DzCnP4TND8IElQTnpw2UMSF76QWTH0hQA3ZPry84OTGPrBw4QvuL2KnXIsttx2cyJx8L/R8msxu7EgKJgG1ruwy1yxrabw==");
        
        populateRequestURL(httpRequest, "http://example.org");

        decoder.decode(messageContext);

        assertTrue(messageContext.getInboundMessage() instanceof Response);
        assertTrue(messageContext.getInboundSAMLMessage() instanceof Response);
        assertEquals(expectedRelayValue, messageContext.getRelayState());
    }
    
    public void testRequestDecoding() throws Exception{
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        samlRequest.setDestination(null);
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));

        decoder.decode(messageContext);

        assertTrue(messageContext.getInboundMessage() instanceof RequestAbstractType);
        assertTrue(messageContext.getInboundSAMLMessage() instanceof RequestAbstractType);
        assertEquals(expectedRelayValue, messageContext.getRelayState());
    }
    
    public void testMessageEndpointGood() throws Exception {
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        
        String deliveredEndpointURL = samlRequest.getDestination();
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        
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
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        
        String deliveredEndpointURL = samlRequest.getDestination();
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        
        populateRequestURL(httpRequest, deliveredEndpointURL);
        //Additional query parameters
        httpRequest.setParameter("paramFoo", "bar");
        httpRequest.setParameter("paramBar", "baz");

        try {
            decoder.decode(messageContext);
        } catch (SecurityException e) {
            fail("Caught SecurityException: " + e.getMessage());
        } catch (MessageDecodingException e) {
            fail("Caught MessageDecodingException: " + e.getMessage());
        }
    }
    
    public void testMessageEndpointInvalidURI() throws Exception {
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        
        String deliveredEndpointURL = samlRequest.getDestination() + "/some/other/endpointURI";
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        
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
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        
        String deliveredEndpointURL = "https://bogusidp.example.com/idp/sso";
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        
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
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        samlRequest.setDestination(null);
        
        String deliveredEndpointURL = authnRequestDestination;
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        
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
        AuthnRequest samlRequest = (AuthnRequest) unmarshallElement("/data/org/opensaml/saml2/binding/AuthnRequest.xml");
        samlRequest.setDestination(null);
        
        String deliveredEndpointURL = authnRequestDestination;
        
        httpRequest.setParameter("SAMLRequest", encodeMessage(samlRequest));
        // simulate simple signature, won't really get evaluated
        httpRequest.setParameter("Signature", "someSigValue");
        
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
        try {
            marshallerFactory.getMarshaller(message).marshall(message);
            String messageStr = XMLHelper.nodeToString(message.getDOM());

            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.DEFLATED, true);
            DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
            deflaterStream.write(messageStr.getBytes());
            deflaterStream.finish();

            return Base64.encodeBytes(bytesOut.toByteArray(), Base64.DONT_BREAK_LINES);
        } catch (IOException e) {
            throw new MessageEncodingException("Unable to DEFLATE and Base64 encode SAML message", e);
        }
    }
}