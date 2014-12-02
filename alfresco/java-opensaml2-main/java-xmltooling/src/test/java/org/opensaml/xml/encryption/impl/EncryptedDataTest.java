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
import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.encryption.EncryptionMethod;
import org.opensaml.xml.encryption.EncryptionProperties;
import org.opensaml.xml.signature.KeyInfo;

/**
 *
 */
public class EncryptedDataTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedId;
    
    private String expectedType;
    
    private String expectedMimeType;
    
    private String expectedEncoding;

    /**
     * Constructor
     *
     */
    public EncryptedDataTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/EncryptedData.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/encryption/impl/EncryptedDataOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/EncryptedDataChildElements.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedId = "abc123";
        expectedType = "someType";
        expectedMimeType = "someMimeType";
        expectedEncoding = "someEncoding";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptedData ed = (EncryptedData) unmarshallElement(singleElementFile);
        
        assertNotNull("EncryptedData", ed);
        assertNull("EncryptionMethod child", ed.getEncryptionMethod());
        assertNull("KeyInfo child", ed.getKeyInfo());
        assertNull("CipherData child", ed.getCipherData());
        assertNull("EncryptionProperties child", ed.getEncryptionProperties());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptedData ed = (EncryptedData) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptedData", ed);
        assertNotNull("EncryptionMethod child", ed.getEncryptionMethod());
        assertNotNull("KeyInfo child", ed.getKeyInfo());
        assertNotNull("CipherData child", ed.getCipherData());
        assertNotNull("EncryptionProperties child", ed.getEncryptionProperties());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EncryptedData ed = (EncryptedData) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("EncryptedData", ed);
        assertEquals("Id attribute", expectedId, ed.getID());
        assertEquals("Type attribute", expectedType, ed.getType());
        assertEquals("MimeType attribute", expectedMimeType, ed.getMimeType());
        assertEquals("Encoding attribute", expectedEncoding, ed.getEncoding());
        
        assertEquals("ID lookup failed", ed, ed.resolveID(expectedId));
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptedData ed = (EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, ed);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptedData ed = (EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME);
        
        ed.setEncryptionMethod((EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        ed.setKeyInfo((KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
        ed.setCipherData((CipherData) buildXMLObject(CipherData.DEFAULT_ELEMENT_NAME));
        ed.setEncryptionProperties((EncryptionProperties) buildXMLObject(EncryptionProperties.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, ed);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        EncryptedData ed = (EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME);
        
        ed.setID(expectedId);
        ed.setType(expectedType);
        ed.setMimeType(expectedMimeType);
        ed.setEncoding(expectedEncoding);
        
        assertEquals(expectedOptionalAttributesDOM, ed);
    }
    
    

}
