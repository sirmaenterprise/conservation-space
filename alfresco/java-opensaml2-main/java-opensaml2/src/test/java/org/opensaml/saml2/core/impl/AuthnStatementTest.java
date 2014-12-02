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

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.SubjectLocality;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AuthnStatementImpl}.
 */
public class AuthnStatementTest extends BaseSAMLObjectProviderTestCase {

    /** Expected AuthnInstant value */
    private DateTime expectedAuthnInstant;

    /** Expected SessionIndex value */
    private String expectedSessionIndex;

    /** Expected SessionNotOnOrAfter value */
    private DateTime expectedSessionNotOnOrAfter;

    /** Constructor */
    public AuthnStatementTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AuthnStatement.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AuthnStatementOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AuthnStatementChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedAuthnInstant = new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC());
        expectedSessionIndex = "index";
        expectedSessionNotOnOrAfter = new DateTime(1984, 8, 26, 10, 11, 30, 43, ISOChronology.getInstanceUTC());
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AuthnStatement authnStatement = (AuthnStatement) unmarshallElement(singleElementFile);

        DateTime authnInstant = authnStatement.getAuthnInstant();
        assertEquals("AuthnInstant was " + authnInstant + ", expected " + expectedAuthnInstant, expectedAuthnInstant,
                authnInstant);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AuthnStatement authnStatement = (AuthnStatement) unmarshallElement(singleElementOptionalAttributesFile);

        DateTime authnInstant = authnStatement.getAuthnInstant();
        assertEquals("AuthnInstant was " + authnInstant + ", expected " + expectedAuthnInstant, expectedAuthnInstant,
                authnInstant);

        String sessionIndex = authnStatement.getSessionIndex();
        assertEquals("SessionIndex was " + sessionIndex + ", expected " + expectedSessionIndex, expectedSessionIndex,
                sessionIndex);

        DateTime sessionNotOnOrAfter = authnStatement.getSessionNotOnOrAfter();
        assertEquals("SessionNotOnOrAfter was " + sessionNotOnOrAfter + ", expected " + expectedSessionNotOnOrAfter,
                expectedSessionNotOnOrAfter, sessionNotOnOrAfter);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AuthnStatement authnStatement = (AuthnStatement) buildXMLObject(qname);

        authnStatement.setAuthnInstant(expectedAuthnInstant);
        assertEquals(expectedDOM, authnStatement);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AuthnStatement authnStatement = (AuthnStatement) buildXMLObject(qname);

        authnStatement.setAuthnInstant(expectedAuthnInstant);
        authnStatement.setSessionIndex(expectedSessionIndex);
        authnStatement.setSessionNotOnOrAfter(expectedSessionNotOnOrAfter);

        assertEquals(expectedOptionalAttributesDOM, authnStatement);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AuthnStatement authnStatement = (AuthnStatement) unmarshallElement(childElementsFile);
        assertNotNull("AuthnContext element not present", authnStatement.getAuthnContext());
        assertNotNull("SubjectLocality element not present", authnStatement.getSubjectLocality());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AuthnStatement authnStatement = (AuthnStatement) buildXMLObject(qname);

        QName subjectLocalityQName = new QName(SAMLConstants.SAML20_NS, SubjectLocality.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        authnStatement.setSubjectLocality((SubjectLocality) buildXMLObject(subjectLocalityQName));
        
        QName authnContextQName = new QName(SAMLConstants.SAML20_NS, AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        authnStatement.setAuthnContext((AuthnContext) buildXMLObject(authnContextQName));
        
        assertEquals(expectedChildElementsDOM, authnStatement);
    }
}