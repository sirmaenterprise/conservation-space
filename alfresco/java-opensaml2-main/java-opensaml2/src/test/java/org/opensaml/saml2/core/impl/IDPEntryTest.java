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
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.IDPEntry;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.IDPEntryImpl}.
 */
public class IDPEntryTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected ProviderID */
    private String expectedProviderID;

    /** Expected ProviderID */
    private String expectedName;
    
    /** Expected ProviderID */
    private String expectedLocation;
    
    /**
     * Constructor
     *
     */
    public IDPEntryTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/IDPEntry.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/IDPEntryOptionalAttributes.xml";
    }
    

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedProviderID = "urn:string:providerid";
        expectedName = "Example IdP";
        expectedLocation = "http://idp.example.org/endpoint";
    }




    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, IDPEntry.DEFAULT_ELEMENT_LOCAL_NAME);
        IDPEntry entry = (IDPEntry) buildXMLObject(qname);
        
        entry.setProviderID(expectedProviderID);

        assertEquals(expectedDOM, entry);
    }
    
    
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, IDPEntry.DEFAULT_ELEMENT_LOCAL_NAME);
        IDPEntry entry = (IDPEntry) buildXMLObject(qname);
        
        entry.setProviderID(expectedProviderID);
        entry.setName(expectedName);
        entry.setLoc(expectedLocation);
        
        assertEquals(expectedOptionalAttributesDOM, entry);
    }


    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        IDPEntry entry = (IDPEntry) unmarshallElement(singleElementFile);
        
        assertEquals("The unmarshalled ProviderID attribute was not the expected value", expectedProviderID, entry.getProviderID());

    }


    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        IDPEntry entry = (IDPEntry) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("The unmarshalled Name attribute was not the expected value", expectedName, entry.getName());
        assertEquals("The unmarshalled Loc (location) attribute was not the expected value", expectedLocation, entry.getLoc());
    }
}