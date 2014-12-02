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
import org.opensaml.saml2.metadata.ServiceDescription;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.ServiceDescription}.
 */
public class ServiceDescriptionTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected description */
    protected LocalizedString expectedDescription;
    
    /**
     * Constructor
     */
    public ServiceDescriptionTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/ServiceDescription.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedDescription = new LocalizedString("This is a description", "Language");
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ServiceDescription description = (ServiceDescription) unmarshallElement(singleElementFile);
        
        assertEquals("Description was not expected value", expectedDescription, description.getDescription());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, ServiceDescription.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        ServiceDescription description = (ServiceDescription) buildXMLObject(qname);
        
        description.setDescription(expectedDescription);

        assertEquals(expectedDOM, description);
    }
}