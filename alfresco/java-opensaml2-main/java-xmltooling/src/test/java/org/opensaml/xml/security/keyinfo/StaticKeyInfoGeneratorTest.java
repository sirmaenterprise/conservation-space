/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.xml.security.keyinfo;

import java.security.KeyException;
import java.security.PublicKey;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.w3c.dom.Element;

/**
 * Test the static KeyInfo generator.
 */
public class StaticKeyInfoGeneratorTest extends XMLObjectBaseTestCase {
    
    private StaticKeyInfoGenerator generator;
    
    private KeyInfo origKeyInfo;
    
    private String expectedKeyName1;
    private String expectedKeyName2;
    private String expectedKeyAlgorithm;
    private PublicKey expectedKeyValue;
    
    /**
     * Constructor.
     *
     */
    public StaticKeyInfoGeneratorTest() {
        expectedKeyName1 = "Foo";
        expectedKeyName2 = "Bar";
        expectedKeyAlgorithm = "RSA";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        origKeyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        
        KeyName keyname1 = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyname1.setValue(expectedKeyName1);
        origKeyInfo.getKeyNames().add(keyname1);
        
        KeyName keyname2 = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyname2.setValue(expectedKeyName2);
        origKeyInfo.getKeyNames().add(keyname2);
        
        expectedKeyValue = SecurityTestHelper.generateKeyPair(expectedKeyAlgorithm, 1024, null).getPublic();
        KeyInfoHelper.addPublicKey(origKeyInfo, expectedKeyValue);
        
        generator = new StaticKeyInfoGenerator(origKeyInfo);
    }
    
    /**
     * Simple test, should return the same instance every time.
     * 
     * @throws SecurityException
     * @throws KeyException
     */
    public void testSimple() throws SecurityException, KeyException {
        assertNull("Original KeyInfo should NOT have parent", origKeyInfo.getParent());
        
        KeyInfo keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertTrue("KeyInfo instances were not the same", origKeyInfo == keyInfo);
        
        keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertTrue("KeyInfo instances were not the same", origKeyInfo == keyInfo);
        
        keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertTrue("KeyInfo instances were not the same", origKeyInfo == keyInfo);
    }
    
    /**
     * Test with cloning, original KeyInfo has no cached DOM.
     * 
     * @throws SecurityException
     * @throws KeyException
     */
    public void testWithCloningNoDOMCache() throws SecurityException, KeyException {
        EncryptedData encData = (EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME);
        
        assertNull("Original KeyInfo should not have a cached DOM", origKeyInfo.getDOM());
        
        KeyInfo keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertTrue("KeyInfo instances were not the same", origKeyInfo == keyInfo);
        
        encData.setKeyInfo(origKeyInfo);
        assertNotNull("Original KeyInfo should have parent", origKeyInfo.getParent());
        
        keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertFalse("KeyInfo instances should have differed due to cloning", origKeyInfo == keyInfo);
        assertNotNull("Generated KeyInfo should have a cached DOM", keyInfo.getDOM());
        
        assertNull("Original KeyInfo marshalled DOM should have been cleared after cloning", origKeyInfo.getDOM());
    }
    
    /**
     * Test with cloning, original KeyInfo has a cached DOM.
     * 
     * @throws SecurityException
     * @throws KeyException
     * @throws MarshallingException 
     */
    public void testWithCloningWithDOMCache() throws SecurityException, KeyException, MarshallingException {
        EncryptedData encData = (EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME);
        
        Configuration.getMarshallerFactory().getMarshaller(origKeyInfo).marshall(origKeyInfo);
        assertNotNull("Original KeyInfo should have a cached DOM", origKeyInfo.getDOM());
        Element origDOM = origKeyInfo.getDOM();
        
        KeyInfo keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertTrue("KeyInfo instances were not the same", origKeyInfo == keyInfo);
        
        encData.setKeyInfo(origKeyInfo);
        assertNotNull("Original KeyInfo should have parent", origKeyInfo.getParent());
        
        keyInfo = generator.generate(null);
        checkKeyInfo(keyInfo);
        assertFalse("KeyInfo instances should have differed due to cloning", origKeyInfo == keyInfo);
        assertNull("Generated KeyInfo should NOT have a cached DOM", keyInfo.getDOM());
        
        assertNotNull("KeyInfo cached DOM should NOT have been cleared after cloning", origKeyInfo.getDOM());
        assertTrue("DOM Elements were not the same", origDOM.isSameNode(origKeyInfo.getDOM()));
    }
    
    /**
     * Check the correctness of the generated KeyInfo.
     * 
     * @param keyInfo the KeyInfo to check
     * @throws KeyException if there is an error extracting the Java key from the KeyInfo
     */
    private void checkKeyInfo(KeyInfo keyInfo) throws KeyException {
        assertNotNull("KeyInfo was null", keyInfo);
        
        assertEquals("Number of KeyNames", 2, keyInfo.getKeyNames().size());
        assertEquals("Unexpected value for KeyName", expectedKeyName1, keyInfo.getKeyNames().get(0).getValue());
        assertEquals("Unexpected value for KeyName", expectedKeyName2, keyInfo.getKeyNames().get(1).getValue());
        
        assertEquals("Number of KeyValues", 1, keyInfo.getKeyValues().size());
        PublicKey pubKey = KeyInfoHelper.getKey(keyInfo.getKeyValues().get(0));
        assertEquals("Unexpected public key value", expectedKeyValue, pubKey);
    }

}
