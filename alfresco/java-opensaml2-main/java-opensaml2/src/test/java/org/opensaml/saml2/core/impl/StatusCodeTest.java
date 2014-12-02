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

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.StatusCode;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.StatusCodeImpl}.
 */
public class StatusCodeTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected Value attribute value*/
    private String expectedValue;

    /**
     * Constructor
     *
     */
    public StatusCodeTest() {
       singleElementFile = "/data/org/opensaml/saml2/core/impl/StatusCode.xml";
       childElementsFile = "/data/org/opensaml/saml2/core/impl/StatusCodeChildElements.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedValue = "urn:string";
    }
    
    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, StatusCode.DEFAULT_ELEMENT_LOCAL_NAME);
        StatusCode statusCode = (StatusCode) buildXMLObject(qname);
        
        statusCode.setValue(expectedValue);
        
        assertEquals(expectedDOM, statusCode);

    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, StatusCode.DEFAULT_ELEMENT_LOCAL_NAME);
        StatusCode statusCode = (StatusCode) buildXMLObject(qname);
        
        QName statusCodeQName = new QName(SAMLConstants.SAML20P_NS, StatusCode.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        statusCode.setStatusCode((StatusCode) buildXMLObject(statusCodeQName));
        
        assertEquals(expectedChildElementsDOM, statusCode);
    }



    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        StatusCode statusCode = (StatusCode) unmarshallElement(singleElementFile);
        
        assertEquals("Unmarshalled status code URI value was not the expected value", expectedValue, statusCode.getValue());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        StatusCode statusCode = (StatusCode) unmarshallElement(childElementsFile);
        
        assertNotNull(statusCode.getStatusCode());
    }
}