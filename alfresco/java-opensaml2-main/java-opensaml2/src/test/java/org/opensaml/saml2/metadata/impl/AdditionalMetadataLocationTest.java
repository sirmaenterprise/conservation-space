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

package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AdditionalMetadataLocation;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.AdditionalMetadataLocationImpl}.
 */
public class AdditionalMetadataLocationTest extends BaseSAMLObjectProviderTestCase {

    /** Expected value of namespace attribute */
    protected String expectedNamespace;

    /** Expected value of element content */
    protected String expectedContent;

    /**
     * Constructor
     */
    public AdditionalMetadataLocationTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/AdditionalMetadataLocation.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedNamespace = "http://example.org/xmlns";
        expectedContent = "http://example.org";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AdditionalMetadataLocation locationObj = (AdditionalMetadataLocation) unmarshallElement(singleElementFile);

        String location = locationObj.getLocationURI();
        assertEquals("Location URI was " + location + ", expected " + expectedContent, expectedContent, location);

        String namespace = locationObj.getNamespaceURI();
        assertEquals("Namepsace URI was " + namespace + ", expected " + expectedNamespace, expectedNamespace, namespace);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, AdditionalMetadataLocation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        AdditionalMetadataLocation location = (AdditionalMetadataLocation) buildXMLObject(qname);
        location.setLocationURI(expectedContent);
        location.setNamespaceURI(expectedNamespace);

        assertEquals(expectedDOM, location);
    }
}