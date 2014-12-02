/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

/**
 * 
 */
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Scoping;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * Unit test for {@link AuthnRequest}.
 */
public class AuthnRequestTest extends RequestTestBase {
    
            
        /** Expected ForceAuthn attribute */    
        private XSBooleanValue expectedForceAuthn;
        
        /** Expected IsPassive attribute */    
        private XSBooleanValue expectedIsPassive;
        
        /** Expected ProtocolBinding attribute */    
        private String expectedProtocolBinding;
        
        /** Expected AssertionConsumerServiceIndex attribute */    
        private Integer expectedAssertionConsumerServiceIndex;
        
        /** Expected AssertionConsumerServiceURL attribute */    
        private String expectedAssertionConsumerServiceURL;
        
        /** Expected AttributeConsumingServiceIndex attribute */    
        private Integer expectedAttributeConsumingServiceIndex;
        
        /** Expected ProviderName attribute */    
        private String expectedProviderName;

    /**
     * Constructor
     *
     */
    public AuthnRequestTest() {
        super();
        
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AuthnRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AuthnRequestOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AuthnRequestChildElements.xml";
    }
    

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedForceAuthn = new XSBooleanValue(Boolean.TRUE, false);
        expectedIsPassive = new XSBooleanValue(Boolean.TRUE, false);
        expectedProtocolBinding = "urn:string:protocol-binding";
        expectedAssertionConsumerServiceIndex = new Integer(3);
        expectedAssertionConsumerServiceURL = "http://sp.example.org/acs";
        expectedAttributeConsumingServiceIndex = new Integer(2);
        expectedProviderName = "Example Org";
    }



    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthnRequest req = (AuthnRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        
        assertEquals(expectedDOM, req);

    }
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthnRequest req = (AuthnRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        super.populateOptionalAttributes(req);
        
        req.setForceAuthn(expectedForceAuthn);
        req.setIsPassive(expectedIsPassive);
        req.setProtocolBinding(expectedProtocolBinding);
        req.setAssertionConsumerServiceIndex(expectedAssertionConsumerServiceIndex);
        req.setAssertionConsumerServiceURL(expectedAssertionConsumerServiceURL);
        req.setAttributeConsumingServiceIndex(expectedAttributeConsumingServiceIndex);
        req.setProviderName(expectedProviderName);
        
        assertEquals(expectedOptionalAttributesDOM, req);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AuthnRequest req = (AuthnRequest) buildXMLObject(qname);
        
        super.populateChildElements(req);
        
        QName subjectQName = new QName(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setSubject((Subject) buildXMLObject(subjectQName));
        
        QName nameIDPolicyQName = new QName(SAMLConstants.SAML20P_NS, NameIDPolicy.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        req.setNameIDPolicy((NameIDPolicy) buildXMLObject(nameIDPolicyQName));
        
        QName conditionsQName = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setConditions((Conditions) buildXMLObject(conditionsQName));
        
        QName requestedAuthnContextQName = new QName(SAMLConstants.SAML20P_NS, RequestedAuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        req.setRequestedAuthnContext((RequestedAuthnContext) buildXMLObject(requestedAuthnContextQName));
        
        QName scopingQName = new QName(SAMLConstants.SAML20P_NS, Scoping.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        req.setScoping((Scoping) buildXMLObject(scopingQName));
        
        assertEquals(expectedChildElementsDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AuthnRequest req = (AuthnRequest) unmarshallElement(singleElementFile);
        
        assertNotNull("AuthnRequest was null", req);
        assertEquals("ForceAuthn (empty) was not default value", Boolean.FALSE, req.isForceAuthn());
        assertEquals("IsPassive (empty) was not default value", Boolean.FALSE, req.isPassive());
        assertNull("ProtocolBinding was not null", req.getProtocolBinding());
        assertNull("AssertionConsumerServiceIndex was not null", req.getAssertionConsumerServiceIndex());
        assertNull("AssertionConsumerServiceURL was not null", req.getAssertionConsumerServiceURL());
        assertNull("AttributeConsumingServiceIndex was not null", req.getAttributeConsumingServiceIndex());
        assertNull("ProviderName was not null", req.getProviderName());
        
        super.helperTestSingleElementUnmarshall(req);

    }
 
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthnRequest req = (AuthnRequest) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Unmarshalled ForceAuthn was not the expected value", expectedForceAuthn, req.isForceAuthnXSBoolean());
        assertEquals("Unmarshalled IsPassive was not the expected value", expectedIsPassive, req.isPassiveXSBoolean());
        assertEquals("Unmarshalled ProtocolBinding was not the expected value", expectedProtocolBinding, req.getProtocolBinding());
        assertEquals("Unmarshalled AssertionConsumerServiceIndex was not the expected value", expectedAssertionConsumerServiceIndex, req.getAssertionConsumerServiceIndex());
        assertEquals("Unmarshalled AssertionConsumerServiceURL was not the expected value", expectedAssertionConsumerServiceURL, req.getAssertionConsumerServiceURL());
        assertEquals("Unmarshalled AttributeConsumingServiceIndex was not the expected value", expectedAttributeConsumingServiceIndex, req.getAttributeConsumingServiceIndex());
        assertEquals("Unmarshalled ProviderName was not the expected value", expectedProviderName, req.getProviderName());
        
        super.helperTestSingleElementOptionalAttributesUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AuthnRequest req = (AuthnRequest) unmarshallElement(childElementsFile);
        
        assertNotNull("Subject was null", req.getSubject());
        assertNotNull("NameIDPolicy was null", req.getNameIDPolicy());
        assertNotNull("Conditions was null", req.getConditions());
        assertNotNull("RequestedAuthnContext was null", req.getRequestedAuthnContext());
        assertNotNull("Scoping was null", req.getScoping());
        
        super.helperTestChildElementsUnmarshall(req);
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        AuthnRequest req = (AuthnRequest) buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        
        // ForceAuthn attribute
        req.setForceAuthn(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, req.isForceAuthn());
        assertNotNull("XSBooleanValue was null", req.isForceAuthnXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                req.isForceAuthnXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true", req.isForceAuthnXSBoolean().toString());
        
        req.setForceAuthn(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, req.isForceAuthn());
        assertNotNull("XSBooleanValue was null", req.isForceAuthnXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                req.isForceAuthnXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false", req.isForceAuthnXSBoolean().toString());
        
        req.setForceAuthn((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, req.isForceAuthn());
        assertNull("XSBooleanValue was not null", req.isForceAuthnXSBoolean());
        
        
        // IsPassive attribute
        req.setIsPassive(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, req.isPassive());
        assertNotNull("XSBooleanValue was null", req.isPassiveXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                req.isPassiveXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true", req.isPassiveXSBoolean().toString());
        
        req.setIsPassive(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, req.isPassive());
        assertNotNull("XSBooleanValue was null", req.isPassiveXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                req.isPassiveXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false", req.isPassiveXSBoolean().toString());
        
        req.setIsPassive((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, req.isPassive());
        assertNull("XSBooleanValue was not null", req.isPassiveXSBoolean());
    }
    
}