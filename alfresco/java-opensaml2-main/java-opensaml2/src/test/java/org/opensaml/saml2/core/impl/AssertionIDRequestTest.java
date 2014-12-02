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
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionIDRequest;

/**
 *
 */
public class AssertionIDRequestTest extends RequestTestBase {
    
    private int expectedNumAssertionIDRefs;

    /**
     * Constructor
     *
     */
    public AssertionIDRequestTest() {
        super();
        
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AssertionIDRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AssertionIDRequestOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AssertionIDRequestChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedNumAssertionIDRefs = 3;
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AssertionIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AssertionIDRequest req = (AssertionIDRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        
        assertEquals(expectedDOM, req);
    }
    

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, AssertionIDRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        AssertionIDRequest req = (AssertionIDRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        super.populateOptionalAttributes(req);
        
        assertEquals(expectedOptionalAttributesDOM, req);
    }


    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        AssertionIDRequestBuilder builder = (AssertionIDRequestBuilder) builderFactory.getBuilder(AssertionIDRequest.DEFAULT_ELEMENT_NAME);
        AssertionIDRequest req = builder.buildObject();
        
        super.populateChildElements(req);
        
        QName assertionIDRefQName = new QName(SAMLConstants.SAML20_NS, AssertionIDRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i< expectedNumAssertionIDRefs; i++)
            req.getAssertionIDRefs().add((AssertionIDRef) buildXMLObject(assertionIDRefQName));
        
        assertEquals(expectedChildElementsDOM, req);
    }
    
    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AssertionIDRequest req = (AssertionIDRequest) unmarshallElement(singleElementFile);
        
        super.helperTestSingleElementUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AssertionIDRequest req = (AssertionIDRequest) unmarshallElement(singleElementOptionalAttributesFile);
        
        super.helperTestSingleElementOptionalAttributesUnmarshall(req);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AssertionIDRequest req = (AssertionIDRequest) unmarshallElement(childElementsFile);
        
        super.helperTestChildElementsUnmarshall(req);
        assertEquals("AssertionIDRef count", expectedNumAssertionIDRefs, req.getAssertionIDRefs().size());
    }
}