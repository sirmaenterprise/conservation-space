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

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionURIRef;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Evidence;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.EvidenceImpl}.
 */
public class EvidenceTest extends BaseSAMLObjectProviderTestCase {

    /** Count of AssertionIDRef subelements. */
    private int assertionIDRefCount = 3;

    /** Count of AssertionURIRef subelements. */
    private int assertionURIRefCount = 4;

    /** Count of Assertion subelements. */
    private int assertionCount = 2;
    
    /** Count of EncryptedAssertion subelements. */
    private int encryptedAssertionCount = 2;


    /** Constructor. */
    public EvidenceTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Evidence.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/EvidenceChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Evidence evidence = (Evidence) unmarshallElement(singleElementFile);

        assertNotNull(evidence);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Evidence evidence = (Evidence) unmarshallElement(childElementsFile);

        assertEquals("AssertionIDRef count not as expected", assertionIDRefCount, evidence.getAssertionIDReferences()
                .size());
        assertEquals("AssertionURIRef count not as expected", assertionURIRefCount, evidence
                .getAssertionURIReferences().size());
        assertEquals("Assertion count not as expected", assertionCount, evidence.getAssertions().size());
        assertEquals("EncryptedAssertion count not as expected", 
                encryptedAssertionCount, evidence.getEncryptedAssertions().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        Evidence evidence = (Evidence) buildXMLObject(Evidence.DEFAULT_ELEMENT_NAME);

        assertEquals(expectedDOM, evidence);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        Evidence evidence = (Evidence) buildXMLObject(Evidence.DEFAULT_ELEMENT_NAME);
        
        evidence.getAssertionIDReferences()
            .add((AssertionIDRef) buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionIDReferences()
            .add((AssertionIDRef) buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionURIReferences()
            .add((AssertionURIRef) buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionIDReferences()
            .add((AssertionIDRef) buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionURIReferences()
            .add((AssertionURIRef) buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionURIReferences()
            .add((AssertionURIRef) buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionURIReferences()
            .add((AssertionURIRef) buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        evidence.getAssertions()
            .add((Assertion) buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        evidence.getEncryptedAssertions()
            .add((EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));
        evidence.getEncryptedAssertions()
            .add((EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));
        evidence.getAssertionURIReferences()
            .add((AssertionURIRef) buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, evidence);
    }
}