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
import org.opensaml.saml2.core.AssertionURIRef;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AssertionURIRefImpl}.
 */
public class AssertionURIRefTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Assertion URI value */
    protected String expectedAssertionURI;

    /** Constructor */
    public AssertionURIRefTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AssertionURIRef.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedAssertionURI = "assertion URI";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AssertionURIRef assertionURIRef = (AssertionURIRef) unmarshallElement(singleElementFile);

        String assertionURI = assertionURIRef.getAssertionURI();
        assertEquals("AssertionURI was " + assertionURI + ", expected " + expectedAssertionURI, expectedAssertionURI,
                assertionURI);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AssertionURIRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AssertionURIRef assertionURIRef = (AssertionURIRef) buildXMLObject(qname);

        assertionURIRef.setAssertionURI(expectedAssertionURI);
        assertEquals(expectedDOM, assertionURIRef);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }
}