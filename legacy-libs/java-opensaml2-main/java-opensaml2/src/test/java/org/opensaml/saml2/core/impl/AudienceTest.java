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
import org.opensaml.saml2.core.Audience;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AudienceImpl}.
 */
public class AudienceTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Audience URI value */
    protected String expectedAudienceURI;

    /** Constructor */
    public AudienceTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Audience.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedAudienceURI = "audience URI";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Audience audience = (Audience) unmarshallElement(singleElementFile);

        String audienceURI = audience.getAudienceURI();
        assertEquals("AssertionURI was " + audienceURI + ", expected " + expectedAudienceURI, expectedAudienceURI,
                audienceURI);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Audience audience = (Audience) buildXMLObject(qname);

        audience.setAudienceURI(expectedAudienceURI);
        assertEquals(expectedDOM, audience);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }
}