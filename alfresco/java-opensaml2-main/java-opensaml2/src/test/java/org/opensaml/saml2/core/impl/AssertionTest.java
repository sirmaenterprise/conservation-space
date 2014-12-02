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
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Advice;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AssertionImpl}.
 */
public class AssertionTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Version value */
    private SAMLVersion expectedVersion;
    
    /** Expected IssueInstant value */
    private DateTime expectedIssueInstant;

    /** Expected ID value */
    private String expectedID;

    /** Count of Statement subelements */
    private int statementCount = 7;

    /** Count of AuthnStatement subelements */
    private int authnStatementCount = 2;

    /** Count of AuthzDecisionStatement submelements */
    private int authzDecisionStatementCount = 2;

    /** Count of AttributeStatement subelements */
    private int attributeStatementCount = 3;

    /** Constructor */
    public AssertionTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Assertion.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AssertionOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AssertionChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedVersion = SAMLVersion.VERSION_20;
        expectedIssueInstant = new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC());
        expectedID = "id";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement(singleElementFile);

        DateTime notBefore = assertion.getIssueInstant();
        assertEquals("IssueInstant was " + notBefore + ", expected " + expectedIssueInstant, expectedIssueInstant,
                notBefore);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement(singleElementOptionalAttributesFile);

        DateTime issueInstant = assertion.getIssueInstant();
        assertEquals("IssueInstant was " + issueInstant + ", expected " + expectedIssueInstant, expectedIssueInstant,
                issueInstant);

        String id = assertion.getID();
        assertEquals("ID was " + id + ", expected " + expectedID, expectedID, id);
        
        SAMLVersion version = assertion.getVersion();
        assertEquals("Version was " + version + ", expected " + expectedVersion, expectedVersion, version);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Assertion assertion = (Assertion) buildXMLObject(qname);

        assertion.setIssueInstant(expectedIssueInstant);

        assertEquals(expectedDOM, assertion);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Assertion assertion = (Assertion) buildXMLObject(qname);

        assertion.setIssueInstant(expectedIssueInstant);
        assertion.setID(expectedID);
        assertion.setVersion(expectedVersion);

        assertEquals(expectedOptionalAttributesDOM, assertion);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement(childElementsFile);

        assertNotNull("Issuer element not present", assertion.getIssuer());
        assertNotNull("Subject element not present", assertion.getSubject());
        assertNotNull("Conditions element not present", assertion.getConditions());
        assertNotNull("Advice element not present", assertion.getAdvice());
        assertEquals("Statement count not as expected", statementCount, assertion.getStatements().size());
        assertEquals("AuthnStatement count not as expected", authnStatementCount, assertion.getAuthnStatements().size());
        assertEquals("AuthzDecisionStatment count not as expected", authzDecisionStatementCount, assertion
                .getAuthzDecisionStatements().size());
        assertEquals("AttributeStatement count not as expected", attributeStatementCount, assertion
                .getAttributeStatements().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Assertion assertion = (Assertion) buildXMLObject(qname);

        QName issuerQName = new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        assertion.setIssuer((Issuer) buildXMLObject(issuerQName));
        
        QName subjectQName = new QName(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        assertion.setSubject((Subject) buildXMLObject(subjectQName));
        
        QName conditionsQName = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        assertion.setConditions((Conditions) buildXMLObject(conditionsQName));
        
        QName adviceQName = new QName(SAMLConstants.SAML20_NS, Advice.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        assertion.setAdvice((Advice) buildXMLObject(adviceQName));

        QName authnStatementQName = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < authnStatementCount; i++) {
            assertion.getAuthnStatements().add((AuthnStatement) buildXMLObject(authnStatementQName));
        }
        
        QName authzDecisionStatementQName = new QName(SAMLConstants.SAML20_NS, AuthzDecisionStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < authzDecisionStatementCount; i++) {
            assertion.getAuthzDecisionStatements().add((AuthzDecisionStatement) buildXMLObject(authzDecisionStatementQName));
        }
        
        QName attributeStatementQName = new QName(SAMLConstants.SAML20_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < attributeStatementCount; i++) {
            assertion.getAttributeStatements().add((AttributeStatement) buildXMLObject(attributeStatementQName));
        }
        
        assertEquals(expectedChildElementsDOM, assertion);
    }
}