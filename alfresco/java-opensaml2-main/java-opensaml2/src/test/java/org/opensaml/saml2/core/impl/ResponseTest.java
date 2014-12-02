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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.ResponseImpl}.
 */
public class ResponseTest extends StatusResponseTestBase {
    
    /** Expected number of Assertion child elements. */
    private int expectedNumAssertions;
    
    /** Expected number of EncryptedAssertion child elements. */
    private int expectedNumEncryptedAssertions;


    /**
     * Constructor.
     *
     */
    public ResponseTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Response.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ResponseOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ResponseChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedNumAssertions = 3;
        expectedNumEncryptedAssertions = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = 
            new QName(SAMLConstants.SAML20P_NS, Response.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        Response resp = (Response) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        
        assertEquals(expectedDOM, resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = 
            new QName(SAMLConstants.SAML20P_NS, Response.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        Response resp = (Response) buildXMLObject(qname);
        
        super.populateRequiredAttributes(resp);
        super.populateOptionalAttributes(resp);
        
        assertEquals(expectedOptionalAttributesDOM, resp);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        Response resp = (Response) buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
        
        super.populateChildElements(resp);
        
        resp.getAssertions().add((Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        resp.getAssertions().add((Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        resp.getEncryptedAssertions().add((EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));
        resp.getEncryptedAssertions().add((EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));
        resp.getAssertions().add((Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Response resp = (Response) unmarshallElement(singleElementFile);
        
        assertNotNull("Response was null", resp);
        super.helperTestSingleElementUnmarshall(resp);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Response resp = (Response) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("Response was null", resp);
        super.helperTestSingleElementOptionalAttributesUnmarshall(resp);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Response resp = (Response) unmarshallElement(childElementsFile);
        
        assertEquals("Assertion count", expectedNumAssertions, resp.getAssertions().size());
        assertEquals("EncryptedAssertion count", expectedNumEncryptedAssertions, resp.getEncryptedAssertions().size());
        super.helperTestChildElementsUnmarshall(resp);
    }

}
