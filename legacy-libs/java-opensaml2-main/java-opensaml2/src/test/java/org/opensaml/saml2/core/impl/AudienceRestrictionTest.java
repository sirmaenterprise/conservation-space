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
import org.opensaml.saml2.core.AudienceRestriction;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AudienceRestrictionImpl}.
 */
public class AudienceRestrictionTest extends BaseSAMLObjectProviderTestCase {

    /** Count of Audience subelements */
    protected int expectedAudienceCount = 2;

    /** Constructor */
    public AudienceRestrictionTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AudienceRestriction.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AudienceRestrictionChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AudienceRestriction audienceRestriction = (AudienceRestriction) unmarshallElement(singleElementFile);

        assertNotNull(audienceRestriction);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AudienceRestriction audienceRestriction = (AudienceRestriction) buildXMLObject(qname);

        assertEquals(expectedDOM, audienceRestriction);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AudienceRestriction audienceRestriction = (AudienceRestriction) unmarshallElement(childElementsFile);
        assertEquals("Audience Count", expectedAudienceCount, audienceRestriction.getAudiences().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, AudienceRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        AudienceRestriction audienceRestriction = (AudienceRestriction) buildXMLObject(qname);

        QName audienceQName = new QName(SAMLConstants.SAML20_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < expectedAudienceCount; i++) {
            audienceRestriction.getAudiences().add((Audience) buildXMLObject(audienceQName));
        }

        assertEquals(expectedChildElementsDOM, audienceRestriction);
    }
}