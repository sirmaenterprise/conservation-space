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
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.RequestedAuthnContext;


/**
 *
 */
public class RequestedAuthnContextTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected Comparison attribute */
    private AuthnContextComparisonTypeEnumeration expectedComparison;
    
    /** Expected Comparison attribute */
    private int expectedNumClassRefs;


    /**
     * Constructor
     *
     */
    public RequestedAuthnContextTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/RequestedAuthnContext.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/RequestedAuthnContextOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/RequestedAuthnContextChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedComparison = AuthnContextComparisonTypeEnumeration.EXACT;
        expectedNumClassRefs = 3;
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, RequestedAuthnContext.DEFAULT_ELEMENT_LOCAL_NAME);
        RequestedAuthnContext rac = (RequestedAuthnContext) buildXMLObject(qname);
        
        assertEquals(expectedDOM, rac);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, RequestedAuthnContext.DEFAULT_ELEMENT_LOCAL_NAME);
        RequestedAuthnContext rac = (RequestedAuthnContext) buildXMLObject(qname);
        
        rac.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        
        assertEquals(expectedOptionalAttributesDOM, rac);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, RequestedAuthnContext.DEFAULT_ELEMENT_LOCAL_NAME);
        RequestedAuthnContext rac = (RequestedAuthnContext) buildXMLObject(qname);
        
        QName authnContextClassRefQName = new QName(SAMLConstants.SAML20_NS, AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i=0; i< expectedNumClassRefs; i++){
            rac.getAuthnContextClassRefs().add((AuthnContextClassRef) buildXMLObject(authnContextClassRefQName));
        }
        
        assertEquals(expectedChildElementsDOM, rac);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        RequestedAuthnContext rac = (RequestedAuthnContext) unmarshallElement(singleElementFile);
        
        assertNotNull("RequestedAuthnContext", rac);
        assertNull("Comparison", rac.getComparison());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        RequestedAuthnContext rac = (RequestedAuthnContext) unmarshallElement(singleElementOptionalAttributesFile);
        assertNotNull("Comparison", rac.getComparison());
        assertEquals("The unmarshalled Comparison attribute was not the expected value", expectedComparison.toString(), rac.getComparison().toString());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        RequestedAuthnContext rac = (RequestedAuthnContext) unmarshallElement(childElementsFile);
       
        assertEquals("AuthnContextClassRef", expectedNumClassRefs, rac.getAuthnContextClassRefs().size());
    }
}