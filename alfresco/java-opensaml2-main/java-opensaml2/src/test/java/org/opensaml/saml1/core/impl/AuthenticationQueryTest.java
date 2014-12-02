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

package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AuthenticationQuery;
import org.opensaml.saml1.core.Subject;

/**
 * Test class for org.opensaml.saml1.core.AuthenticationQuery
 */
public class AuthenticationQueryTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    private final String expectedAuthenticationMethod;

    /**
     * Constructor
     */
    public AuthenticationQueryTest() {
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAuthenticationQuery.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAuthenticationQueryAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml1/impl/AuthenticationQueryWithChildren.xml";
        expectedAuthenticationMethod = "Trust Me";
        qname = new QName(SAMLConstants.SAML10P_NS, AuthenticationQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {

        AuthenticationQuery authenticationQuery;
        
        authenticationQuery = (AuthenticationQuery) unmarshallElement(singleElementFile);

        assertNull("AuthenticationQuery attribute present", authenticationQuery.getAuthenticationMethod());;
        assertNull("Subject element present", authenticationQuery.getSubject());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthenticationQuery authenticationQuery;
        
        authenticationQuery = (AuthenticationQuery) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("AuthenticationQuery attribute", expectedAuthenticationMethod, authenticationQuery.getAuthenticationMethod());;
        assertNull("Subject element present", authenticationQuery.getSubject());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AuthenticationQuery authenticationQuery;
        
        authenticationQuery = (AuthenticationQuery) unmarshallElement(childElementsFile);

        assertNotNull("No Subject element found", authenticationQuery.getSubject());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        AuthenticationQuery authenticationQuery = (AuthenticationQuery) buildXMLObject(qname);

        authenticationQuery.setAuthenticationMethod(expectedAuthenticationMethod);
        assertEquals(expectedOptionalAttributesDOM, authenticationQuery);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        AuthenticationQuery authenticationQuery = (AuthenticationQuery) buildXMLObject(qname);

        authenticationQuery.setSubject((Subject) buildXMLObject(new QName(SAMLConstants.SAML1_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX)));
        assertEquals(expectedChildElementsDOM, authenticationQuery);

    }

}
