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
import org.opensaml.saml2.core.Advice;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionURIRef;
import org.opensaml.saml2.core.EncryptedAssertion;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AdviceImpl}.
 */
public class AdviceTest extends BaseSAMLObjectProviderTestCase {

    /** Count of AssertionIDRef subelements */
    protected int assertionIDRefCount = 3;

    /** Count of AssertionURIRef subelements */
    protected int assertionURIRefCount = 2;

    /** Count of Assertion subelements */
    protected int assertionCount = 3;

    /** Count of Assertion subelements */
    protected int encryptedAssertionCount = 2;

    /** Constructor */
    public AdviceTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Advice.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AdviceChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Advice advice = (Advice) unmarshallElement(singleElementFile);

        assertNotNull(advice);
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Advice advice = (Advice) unmarshallElement(childElementsFile);

        assertEquals("AssertionIDRef count not as expected", assertionIDRefCount, advice.getAssertionIDReferences()
                .size());
        assertEquals("AssertionURIRef count not as expected", assertionURIRefCount, advice.getAssertionURIReferences()
                .size());
        assertEquals("Assertion count not as expected", assertionCount, advice.getAssertions().size());
        assertEquals("EncryptedAssertion count not as expected", encryptedAssertionCount, advice.getEncryptedAssertions().size());
    }
    
    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        Advice advice = (Advice) buildXMLObject(Advice.DEFAULT_ELEMENT_NAME);

        assertEquals(expectedDOM, advice);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        Advice advice = (Advice) buildXMLObject(Advice.DEFAULT_ELEMENT_NAME);
        
        advice.getChildren().add(buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(AssertionURIRef.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(AssertionIDRef.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME));
        advice.getChildren().add(buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME));

        assertEquals(expectedChildElementsDOM, advice);
    }
}