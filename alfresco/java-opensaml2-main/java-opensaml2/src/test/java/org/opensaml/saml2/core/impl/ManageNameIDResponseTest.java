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
import org.opensaml.saml2.core.ManageNameIDResponse;

/**
 *
 */
public class ManageNameIDResponseTest extends StatusResponseTestBase {

    /**
     * Constructor
     *
     */
    public ManageNameIDResponseTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDResponse.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDResponseOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDResponseChildElements.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDResponse resp = (ManageNameIDResponse) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        
        assertEquals(expectedDOM, resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDResponse resp = (ManageNameIDResponse) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        super.populateOptionalAttributes(resp);
        
        assertEquals(expectedOptionalAttributesDOM, resp);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDResponse resp = (ManageNameIDResponse) buildXMLObject(qname);
        
        super.populateChildElements(resp);
        
        assertEquals(expectedChildElementsDOM, resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ManageNameIDResponse resp = (ManageNameIDResponse) unmarshallElement(singleElementFile);
        
        super.helperTestSingleElementUnmarshall(resp);
    }
 
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        ManageNameIDResponse resp = (ManageNameIDResponse) unmarshallElement(singleElementOptionalAttributesFile);

        super.helperTestSingleElementOptionalAttributesUnmarshall(resp);
    }
 
    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        ManageNameIDResponse resp = (ManageNameIDResponse) unmarshallElement(childElementsFile);
        
        super.helperTestChildElementsUnmarshall(resp);
    }
    
}
