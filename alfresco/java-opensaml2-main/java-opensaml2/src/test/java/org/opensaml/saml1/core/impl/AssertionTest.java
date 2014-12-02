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

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Advice;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.AttributeStatement;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.AuthorizationDecisionStatement;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.Statement;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test for {@link org.opensaml.saml1.core.impl.Assertion}
 */
public class AssertionTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    private final int expectedMinorVersion;

    private final String expectedIssuer;

    private final DateTime expectedIssueInstant;

    private final String expectedID;
    /**
     * Constructor
     */
    public AssertionTest() {
        super();
        expectedID = "ident";
        expectedMinorVersion = 1;
        expectedIssuer = "issuer";
        //
        // IssueInstant="1970-01-02T01:01:02.100Z"
        //
        expectedIssueInstant = new DateTime(1970, 1, 2, 1, 1, 2, 100, ISOChronology.getInstanceUTC());

        singleElementFile = "/data/org/opensaml/saml1/impl/singleAssertion.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAssertionAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml1/impl/AssertionWithChildren.xml";
        qname = Assertion.DEFAULT_ELEMENT_NAME;
    }

    /** {@inheritDoc} */

    public void testSingleElementUnmarshall() {

        Assertion assertion = (Assertion) unmarshallElement(singleElementFile);

        assertNull("Issuer attribute", assertion.getIssuer());
        assertNull("IssueInstant attribute", assertion.getIssueInstant());
        assertNull("ID attribute", assertion.getID());

        assertNull("Conditions element", assertion.getConditions());
        assertNull("Advice element", assertion.getAdvice());
        assertNull("Signature element", assertion.getSignature());

        assertEquals("Statement element count", 0, assertion.getStatements().size());
        assertEquals("AttributeStatements element count", 0, assertion.getAttributeStatements().size());
        assertEquals("SubjectStatements element count", 0, assertion.getSubjectStatements().size());
        assertEquals("AuthenticationStatements element count", 0, assertion.getAuthenticationStatements().size());
        assertEquals("AuthorizationDecisionStatements element count", 0, assertion.getAuthorizationDecisionStatements().size());
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("Issuer attribute", expectedIssuer, assertion.getIssuer());
        assertEquals("IssueInstant attribute", expectedIssueInstant, assertion.getIssueInstant());
        assertEquals("ID attribute", expectedID, assertion.getID());
        assertEquals("Issuer expectedMinorVersion", expectedMinorVersion, assertion.getMinorVersion());

        assertNull("Conditions element", assertion.getConditions());
        assertNull("Advice element", assertion.getAdvice());
        assertNull("Signature element", assertion.getSignature());

        assertEquals("Statement element count", 0, assertion.getStatements().size());
        assertEquals("AttributeStatements element count", 0, assertion.getAttributeStatements().size());
        assertEquals("SubjectStatements element count", 0, assertion.getSubjectStatements().size());
        assertEquals("AuthenticationStatements element count", 0, assertion.getAuthenticationStatements().size());
        assertEquals("AuthorizationDecisionStatements element count", 0, assertion.getAuthorizationDecisionStatements().size());
    }

    /**
     * Test an XML file with children
     */

    public void testChildElementsUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement(childElementsFile);

        assertNull("Issuer attribute", assertion.getIssuer());
        assertNull("ID attribute", assertion.getID());
        assertNull("IssueInstant attribute", assertion.getIssueInstant());

        assertNotNull("Conditions element null", assertion.getConditions());
        assertNotNull("Advice element null", assertion.getAdvice());
        assertNull("Signature element", assertion.getSignature());

        assertNotNull("No Authentication Statements", assertion.getAuthenticationStatements());
        assertEquals("AuthenticationStatements element count", 2, assertion.getAuthenticationStatements().size());

        assertNotNull("No Attribute Statements", assertion.getAttributeStatements());
        assertEquals("AttributeStatements element count", 3, assertion.getAttributeStatements().size());

        assertNotNull("No AuthorizationDecisionStatements ", assertion.getAuthorizationDecisionStatements());
        assertEquals("AuthorizationDecisionStatements element count", 3, assertion.getAuthorizationDecisionStatements()
                .size());
    }

    /** {@inheritDoc} */

    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */

    public void testSingleElementOptionalAttributesMarshall() {
        Assertion assertion = (Assertion) buildXMLObject(qname);

        assertion.setIssueInstant(expectedIssueInstant);
        assertion.setID(expectedID);
        assertion.setIssuer(expectedIssuer);
        assertEquals(expectedOptionalAttributesDOM, assertion);
    }

    /**
     * Test an XML file with Children
     * @throws MarshallingException 
     */

    public void testChildElementsMarshall() {
        Assertion assertion = (Assertion) buildXMLObject(qname);
        
        assertion.setConditions((Conditions) buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME));
        assertion.setAdvice((Advice) buildXMLObject(Advice.DEFAULT_ELEMENT_NAME));

        QName authenticationQname = AuthenticationStatement.DEFAULT_ELEMENT_NAME;
        QName authorizationQname = AuthorizationDecisionStatement.DEFAULT_ELEMENT_NAME;
        QName attributeQname = AttributeStatement.DEFAULT_ELEMENT_NAME;
        
        assertion.getStatements().add((Statement) buildXMLObject(authenticationQname));
        assertion.getStatements().add((Statement) buildXMLObject(authorizationQname));
        assertion.getStatements().add((Statement) buildXMLObject(attributeQname));
        assertion.getStatements().add((Statement) buildXMLObject(authenticationQname));
        assertion.getStatements().add((Statement) buildXMLObject(authorizationQname));
        assertion.getStatements().add((Statement) buildXMLObject(attributeQname));
        assertion.getStatements().add((Statement) buildXMLObject(authorizationQname));
        assertion.getStatements().add((Statement) buildXMLObject(attributeQname));

        assertEquals(expectedChildElementsDOM, assertion);
    }
    
    public void testSignatureUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml1/impl/AssertionWithSignature.xml");
        
        assertNotNull("Assertion was null", assertion);
        assertNotNull("Signature was null", assertion.getSignature());
        assertNotNull("KeyInfo was null", assertion.getSignature().getKeyInfo());
    }
    
    public void testDOMIDResolutionUnmarshall() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml1/impl/AssertionWithSignature.xml");
        
        assertNotNull("Assertion was null", assertion);
        assertNotNull("Signature was null", assertion.getSignature());
        Document document = assertion.getSignature().getDOM().getOwnerDocument();
        Element idElem = assertion.getDOM();
        
        assertNotNull("DOM ID resolution returned null", document.getElementById(expectedID));
        assertTrue("DOM elements were not equal", idElem.isSameNode(document.getElementById(expectedID)));
    }

    public void testDOMIDResolutionMarshall() throws MarshallingException {
        Assertion assertion = (Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID(expectedID);
        assertion.getAttributeStatements().add((AttributeStatement) buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME));
        
        marshallerFactory.getMarshaller(assertion).marshall(assertion);
        
        Document document = assertion.getStatements().get(0).getDOM().getOwnerDocument();
        Element idElem = assertion.getDOM();
        
        assertNotNull("DOM ID resolution returned null", document.getElementById(expectedID));
        assertTrue("DOM elements were not equal", idElem.isSameNode(document.getElementById(expectedID)));
    }
    
}
