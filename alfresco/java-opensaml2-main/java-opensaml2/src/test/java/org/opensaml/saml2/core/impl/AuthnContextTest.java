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

package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthenticatingAuthority;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextDecl;
import org.opensaml.saml2.core.AuthnContextDeclRef;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AuthnContextImpl}.
 */
public class AuthnContextTest extends BaseSAMLObjectProviderTestCase {

    /** Count of AuthenticatingAuthority subelements */
    protected int expectedAuthenticatingAuthorityCount = 2;

    /** Constructor */
    public AuthnContextTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AuthnContext.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AuthnContextChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AuthnContext authnContext = (AuthnContext) unmarshallElement(singleElementFile);

        assertNotNull(authnContext);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AuthnContext authnContext = (AuthnContext) buildXMLObject(qname);

        assertEquals(expectedDOM, authnContext);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AuthnContext authnContext = (AuthnContext) unmarshallElement(childElementsFile);

        assertNotNull("AuthnContextClassRef element not present", authnContext.getAuthnContextClassRef());
        assertNotNull("AuthnContextDecl element not present", authnContext.getAuthContextDecl());
        assertNotNull("AuthnContextDeclRef element not present", authnContext.getAuthnContextDeclRef());
        assertEquals("AuthenticatingAuthorityCount Count", expectedAuthenticatingAuthorityCount, authnContext
                .getAuthenticatingAuthorities().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AuthnContext authnContext = (AuthnContext) buildXMLObject(qname);

        QName authnContextClassRefQName = new QName(SAMLConstants.SAML20_NS, AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        authnContext.setAuthnContextClassRef((AuthnContextClassRef) buildXMLObject(authnContextClassRefQName));
        
        QName authnContextDeclQName = new QName(SAMLConstants.SAML20_NS, AuthnContextDecl.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        authnContext.setAuthnContextDecl((AuthnContextDecl) buildXMLObject(authnContextDeclQName));
        
        QName authnContextDeclRefQName = new QName(SAMLConstants.SAML20_NS, AuthnContextDeclRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        authnContext.setAuthnContextDeclRef((AuthnContextDeclRef) buildXMLObject(authnContextDeclRefQName));
        
        QName authenticatingAuthorityQName = new QName(SAMLConstants.SAML20_NS, AuthenticatingAuthority.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < expectedAuthenticatingAuthorityCount; i++) {
            authnContext.getAuthenticatingAuthorities().add((AuthenticatingAuthority) buildXMLObject(authenticatingAuthorityQName));
        }

        assertEquals(expectedChildElementsDOM, authnContext);
    }
}