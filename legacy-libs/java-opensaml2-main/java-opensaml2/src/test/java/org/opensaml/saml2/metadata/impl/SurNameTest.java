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
import org.opensaml.saml2.metadata.SurName;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.SurName}.
 */
public class SurNameTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected description */
    protected String expectedName;
    
    /**
     * Constructor
     */
    public SurNameTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/SurName.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedName = "Smith";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SurName name = (SurName) unmarshallElement(singleElementFile);
        
        assertEquals("Name was not expected value", expectedName, name.getName());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, SurName.DEFAULT_ELEMENT_LOCAL_NAME);
        SurName name = (SurName) buildXMLObject(qname);
        
        name.setName(expectedName);

        assertEquals(expectedDOM, name);
    }
}