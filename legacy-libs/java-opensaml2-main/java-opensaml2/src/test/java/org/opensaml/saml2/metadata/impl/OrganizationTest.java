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

package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.OrganizationURL;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.metadata.OrganizationName}.
 */
public class OrganizationTest extends BaseSAMLObjectProviderTestCase {

    /**
     * Constructor
     */
    public OrganizationTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/Organization.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/OrganizationChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Organization org = (Organization) unmarshallElement(singleElementFile);
        assertEquals("Display names", 0, org.getDisplayNames().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Organization org = (Organization) unmarshallElement(childElementsFile);

        assertNotNull("Extensions", org.getExtensions());
        assertEquals("OrganizationName count", 3, org.getOrganizationNames().size());
        assertEquals("DisplayNames count", 2, org.getDisplayNames().size());
        assertEquals("URL count", 1, org.getURLs().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME);
        Organization org = (Organization) buildXMLObject(qname);

        assertEquals(expectedDOM, org);
    }

    /**
     * 
     */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME);
        Organization org = (Organization) buildXMLObject(qname);

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        org.setExtensions((Extensions) buildXMLObject(extensionsQName));

        QName nameQName = new QName(SAMLConstants.SAML20MD_NS, OrganizationName.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 3; i++) {
            org.getOrganizationNames().add((OrganizationName) buildXMLObject(nameQName));
        }

        QName displayNameQName = new QName(SAMLConstants.SAML20MD_NS, OrganizationDisplayName.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 2; i++) {
            org.getDisplayNames().add((OrganizationDisplayName) buildXMLObject(displayNameQName));
        }

        QName urlQName = new QName(SAMLConstants.SAML20MD_NS, OrganizationURL.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        org.getURLs().add((OrganizationURL) buildXMLObject(urlQName));
        
        assertEquals(expectedChildElementsDOM, org);
    }
}