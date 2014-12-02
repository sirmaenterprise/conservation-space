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
import org.opensaml.xml.encryption.CarriedKeyName;
import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.encryption.EncryptionMethod;
import org.opensaml.xml.encryption.EncryptionProperties;
import org.opensaml.xml.encryption.ReferenceList;
import org.opensaml.xml.signature.KeyInfo;

/**
 *
 */
public class EncryptedKeyTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedId;
    
    private String expectedType;
    
    private String expectedMimeType;
    
    private String expectedEncoding;
    
    private String expectedRecipient;

    /**
     * Constructor
     *
     */
    public EncryptedKeyTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/EncryptedKey.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/encryption/impl/EncryptedKeyOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/EncryptedKeyChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedId = "abc123";
        expectedType = "someType";
        expectedMimeType = "someMimeType";
        expectedEncoding = "someEncoding";
        expectedRecipient = "someRecipient";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptedKey ek = (EncryptedKey) unmarshallElement(singleElementFile);
        
        assertNotNull("EncryptedKey", ek);
        assertNull("EncryptionMethod child", ek.getEncryptionMethod());
        assertNull("KeyInfo child", ek.getKeyInfo());
        assertNull("CipherData child", ek.getCipherData());
        assertNull("EncryptionProperties child", ek.getEncryptionProperties());
        assertNull("ReferenceList child", ek.getReferenceList());
        assertNull("CarriedKeyName child", ek.getCarriedKeyName());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptedKey ek = (EncryptedKey) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptedKey", ek);
        assertNotNull("EncryptionMethod child", ek.getEncryptionMethod());
        assertNotNull("KeyInfo child", ek.getKeyInfo());
        assertNotNull("CipherData child", ek.getCipherData());
        assertNotNull("EncryptionProperties child", ek.getEncryptionProperties());
        assertNotNull("ReferenceList child", ek.getReferenceList());
        assertNotNull("CarriedKeyName child", ek.getCarriedKeyName());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EncryptedKey ek = (EncryptedKey) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("EncryptedKey", ek);
        assertEquals("Id attribute", expectedId, ek.getID());
        assertEquals("Type attribute", expectedType, ek.getType());
        assertEquals("MimeType attribute", expectedMimeType, ek.getMimeType());
        assertEquals("Encoding attribute", expectedEncoding, ek.getEncoding());
        assertEquals("Recipient attribute", expectedRecipient, ek.getRecipient());
        
        assertEquals("ID lookup failed", ek, ek.resolveID(expectedId));
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptedKey ek = (EncryptedKey) buildXMLObject(EncryptedKey.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, ek);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptedKey ek = (EncryptedKey) buildXMLObject(EncryptedKey.DEFAULT_ELEMENT_NAME);
        
        
        ek.setEncryptionMethod((EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        ek.setKeyInfo((KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
        ek.setCipherData((CipherData) buildXMLObject(CipherData.DEFAULT_ELEMENT_NAME));
        ek.setEncryptionProperties((EncryptionProperties) buildXMLObject(EncryptionProperties.DEFAULT_ELEMENT_NAME));
        ek.setReferenceList((ReferenceList) buildXMLObject(ReferenceList.DEFAULT_ELEMENT_NAME));
        ek.setCarriedKeyName((CarriedKeyName) buildXMLObject(CarriedKeyName.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, ek);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        EncryptedKey ek = (EncryptedKey) buildXMLObject(EncryptedKey.DEFAULT_ELEMENT_NAME);
        
        ek.setID(expectedId);
        ek.setType(expectedType);
        ek.setMimeType(expectedMimeType);
        ek.setEncoding(expectedEncoding);
        ek.setRecipient(expectedRecipient);
        
        assertEquals(expectedOptionalAttributesDOM, ek);
    }

}
