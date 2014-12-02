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
import org.opensaml.saml2.core.NameIDMappingResponse;

/**
 *
 */
public class NameIDMappingResponseTest extends StatusResponseTestBase {

    /**
     * Constructor
     *
     */
    public NameIDMappingResponseTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingResponse.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingResponseOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/NameIDMappingResponseChildElements.xml";
    }
    
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingResponse resp = (NameIDMappingResponse) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        
        assertEquals(expectedDOM, resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingResponse resp = (NameIDMappingResponse) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        super.populateOptionalAttributes(resp);
        
        assertEquals(expectedOptionalAttributesDOM, resp);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDMappingResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        NameIDMappingResponse req = (NameIDMappingResponse) buildXMLObject(qname);
        
        super.populateChildElements(req);
        
        QName nameIDQName = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setNameID((NameID) buildXMLObject(nameIDQName));
        
        assertEquals(expectedChildElementsDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        NameIDMappingResponse resp = (NameIDMappingResponse) unmarshallElement(singleElementFile);
        
        assertNotNull("NameIDMappingResponse was null", resp);
        super.helperTestSingleElementUnmarshall(resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        NameIDMappingResponse resp = (NameIDMappingResponse) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("NameIDMappingResponse was null", resp);
        super.helperTestSingleElementOptionalAttributesUnmarshall(resp);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        NameIDMappingResponse resp = (NameIDMappingResponse) unmarshallElement(childElementsFile);
        
        assertNotNull("Identifier was null", resp.getNameID());
        super.helperTestChildElementsUnmarshall(resp);
    }

}
