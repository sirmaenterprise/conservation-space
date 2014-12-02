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
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDMappingRequest;
import org.opensaml.saml2.core.NameIDPolicy;

/**
 *
 */
public class NameIDMappingRequestTest extends RequestTestBase {

    /**
     * Constructor
     *
     */
    public NameIDMappingRequestTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingRequestOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingRequestChildElements.xml";
    }
    
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingRequest req = (NameIDMappingRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        
        assertEquals(expectedDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingRequest req = (NameIDMappingRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        super.populateOptionalAttributes(req);
        
        assertEquals(expectedOptionalAttributesDOM, req);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingRequest req = (NameIDMappingRequest) buildXMLObject(qname);
        
        super.populateChildElements(req);
        
        QName nameIDQName = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setNameID((NameID) buildXMLObject(nameIDQName));
        
        QName nameIDPolicyQName = new QName(SAMLConstants.SAML20P_NS, NameIDPolicy.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        req.setNameIDPolicy((NameIDPolicy) buildXMLObject(nameIDPolicyQName));
        
        assertEquals(expectedChildElementsDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        NameIDMappingRequest req = (NameIDMappingRequest) unmarshallElement(singleElementFile);
        
        assertNotNull("NameIDMappingRequest was null", req);
        super.helperTestSingleElementUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        NameIDMappingRequest req = (NameIDMappingRequest) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("NameIDMappingRequest was null", req);
        super.helperTestSingleElementOptionalAttributesUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        NameIDMappingRequest req = (NameIDMappingRequest) unmarshallElement(childElementsFile);
        
        assertNotNull("Identifier was null", req.getNameID());
        assertNotNull("NameIDPolicy was null", req.getNameIDPolicy());
        super.helperTestChildElementsUnmarshall(req);
    }

}
