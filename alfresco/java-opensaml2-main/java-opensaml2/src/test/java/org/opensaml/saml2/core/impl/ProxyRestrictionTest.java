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
import org.opensaml.saml2.core.ProxyRestriction;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.ProxyRestrictionImpl}.
 */
public class ProxyRestrictionTest extends BaseSAMLObjectProviderTestCase {

    /** Expected proxy Count */
    protected int expectedCount = 5;

    /** Count of Audience subelements */
    protected int expectedAudienceCount = 2;

    /** Constructor */
    public ProxyRestrictionTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/ProxyRestriction.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ProxyRestrictionChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ProxyRestriction proxyRestriction = (ProxyRestriction) unmarshallElement(singleElementFile);

        int count = proxyRestriction.getProxyCount();
        assertEquals("ProxyCount not as expected", expectedCount, count);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, ProxyRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        ProxyRestriction proxyRestriction = (ProxyRestriction) buildXMLObject(qname);

        proxyRestriction.setProxyCount(expectedCount);

        assertEquals(expectedDOM, proxyRestriction);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        // do nothing
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        ProxyRestriction proxyRestriction = (ProxyRestriction) unmarshallElement(childElementsFile);
        assertEquals("Audience Count", expectedAudienceCount, proxyRestriction.getAudiences().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, ProxyRestriction.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        ProxyRestriction proxyRestriction = (ProxyRestriction) buildXMLObject(qname);

        QName audienceQName = new QName(SAMLConstants.SAML20_NS, Audience.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        for (int i = 0; i < expectedAudienceCount; i++) {
            proxyRestriction.getAudiences().add((Audience) buildXMLObject(audienceQName));
        }

        assertEquals(expectedChildElementsDOM, proxyRestriction);
    }
}