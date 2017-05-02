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
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.OrganizationName;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.OrganizationName}.
 */
public class OrganizationNameTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected name */
    protected LocalizedString expectName;
    
    /**
     * Constructor
     */
    public OrganizationNameTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/OrganizationName.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectName = new LocalizedString("MyOrg", "Language");
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        OrganizationName name = (OrganizationName) unmarshallElement(singleElementFile);
        
        assertEquals("Name was not expected value", expectName, name.getName());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, OrganizationName.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        OrganizationName name = (OrganizationName) buildXMLObject(qname);
        
        name.setName(expectName);

        assertEquals(expectedDOM, name);
    }
}