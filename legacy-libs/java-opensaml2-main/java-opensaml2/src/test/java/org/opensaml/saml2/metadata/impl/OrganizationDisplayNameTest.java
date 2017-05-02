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
import org.opensaml.saml2.metadata.OrganizationDisplayName;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.OrganizationName}.
 */
public class OrganizationDisplayNameTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected name */
    protected LocalizedString expectName;
    
    /**
     * Constructor
     */
    public OrganizationDisplayNameTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/OrganizationDisplayName.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectName = new LocalizedString("MyOrg", "Language");
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        OrganizationDisplayName name = (OrganizationDisplayName) unmarshallElement(singleElementFile);
        
        assertEquals("Name was not expected value", expectName, name.getName());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, OrganizationDisplayName.DEFAULT_ELEMENT_LOCAL_NAME);
        OrganizationDisplayName name = (OrganizationDisplayName) buildXMLObject(qname);
        
        name.setName(expectName);

        assertEquals(expectedDOM, name);
    }

}