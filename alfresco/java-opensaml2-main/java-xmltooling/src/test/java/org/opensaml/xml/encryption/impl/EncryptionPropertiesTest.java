/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.encryption.impl;


import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.encryption.EncryptionProperties;
import org.opensaml.xml.encryption.EncryptionProperty;

/**
 *
 */
public class EncryptionPropertiesTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedID;
    private int expectedNumEncProps;
    
    /**
     * Constructor
     *
     */
    public EncryptionPropertiesTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/EncryptionProperties.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/encryption/impl/EncryptionPropertiesOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/EncryptionPropertiesChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedID = "someID";
        expectedNumEncProps = 3;
        
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptionProperties ep = (EncryptionProperties) unmarshallElement(singleElementFile);
        
        assertNotNull("EncryptionProperties", ep);
        assertNull("Id attribute", ep.getID());
        assertEquals("# of EncryptionProperty children", 0, ep.getEncryptionProperties().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EncryptionProperties ep = (EncryptionProperties) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("EncryptionProperties", ep);
        assertEquals("Id attribute", expectedID, ep.getID());
        assertEquals("# of EncryptionProperty children", 0, ep.getEncryptionProperties().size());
        
        assertEquals("ID lookup failed", ep, ep.resolveID(expectedID));
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptionProperties ep = (EncryptionProperties) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptionProperties", ep);
        assertNull("Id attribute", ep.getID());
        assertEquals("# of EncryptionProperty children", expectedNumEncProps, ep.getEncryptionProperties().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptionProperties ep = (EncryptionProperties) buildXMLObject(EncryptionProperties.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, ep);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        EncryptionProperties ep = (EncryptionProperties) buildXMLObject(EncryptionProperties.DEFAULT_ELEMENT_NAME);
        
        ep.setID(expectedID);
        
        assertEquals(expectedOptionalAttributesDOM, ep);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptionProperties ep = (EncryptionProperties) buildXMLObject(EncryptionProperties.DEFAULT_ELEMENT_NAME);
        
        ep.getEncryptionProperties().add((EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME));
        ep.getEncryptionProperties().add((EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME));
        ep.getEncryptionProperties().add((EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, ep);
    }

}
