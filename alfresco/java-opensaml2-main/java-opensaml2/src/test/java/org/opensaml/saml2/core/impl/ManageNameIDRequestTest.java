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
import org.opensaml.saml2.core.ManageNameIDRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NewID;

/**
 *
 */
public class ManageNameIDRequestTest extends RequestTestBase {

    /**
     * Constructor
     *
     */
    public ManageNameIDRequestTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDRequestOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ManageNameIDRequestChildElements.xml";
    }
    
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDRequest req = (ManageNameIDRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        
        assertEquals(expectedDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDRequest req = (ManageNameIDRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        super.populateOptionalAttributes(req);
        
        assertEquals(expectedOptionalAttributesDOM, req);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ManageNameIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ManageNameIDRequest req = (ManageNameIDRequest) buildXMLObject(qname);
        
        super.populateChildElements(req);
        
        QName nameIDQName = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setNameID((NameID) buildXMLObject(nameIDQName));
        
        QName newIDQName = new QName(SAMLConstants.SAML20P_NS, NewID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        req.setNewID((NewID) buildXMLObject(newIDQName));
        
        assertEquals(expectedChildElementsDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ManageNameIDRequest req = (ManageNameIDRequest) unmarshallElement(singleElementFile);
        
        assertNotNull("ManageNameIDRequest was null", req);
        super.helperTestSingleElementUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        ManageNameIDRequest req = (ManageNameIDRequest) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("ManageNameIDRequest was null", req);
        super.helperTestSingleElementOptionalAttributesUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        ManageNameIDRequest req = (ManageNameIDRequest) unmarshallElement(childElementsFile);
        
        assertNotNull("NameID was null", req.getNameID());
        assertNotNull("NewID was null", req.getNewID());
        super.helperTestChildElementsUnmarshall(req);
    }

}
