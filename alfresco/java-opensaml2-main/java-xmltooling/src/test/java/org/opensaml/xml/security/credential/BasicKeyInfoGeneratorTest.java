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

package org.opensaml.xml.security.credential;

import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;

/**
 * Tests the factory and impl for BasicKeyInfoGenerator.
 */
public class BasicKeyInfoGeneratorTest extends XMLObjectBaseTestCase {
    
    private BasicCredential credential;
    
    private BasicKeyInfoGeneratorFactory factory;
    private KeyInfoGenerator generator;
    
    private String keyNameFoo = "FOO";
    private String keyNameBar = "BAR";
    private String entityID = "someEntityID";
    
    private PublicKey pubKey;
    private final String rsaBase64 = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzVp5BZoctb2GuoDf8QUS" +
        "pGcRct7FKtldC7GG+kN6XvUJW+vgc2jOQ6zfLiKqq6ARN1qdC7a4CrkE6Q6TRQXU" +
        "tqeWn4lLTmC1gQ7Ys0zs7N2d+jBjIyD1GEOLNNyD98j4drnehCqQz4mKszW5EWoi" +
        "MJmEorea/kTGL3en7ir0zp+oez2SOQA+0XWu1VoeTlUqGV5Ucd6sRYaPpmYVtKuH" +
        "1H04uZVsH+BIZHwZc4MP5OYH+HDouq6xqUUtc8Zm7V9UQIPiNtM+ndOINDdlrCub" +
        "LbM4GCqCETiQol8I62mvP0qBXCC6JVkKbbVRwSFGJcg5ZvJiBZXmX+EXhaX5vp1G" +
        "MQIDAQAB";

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        factory = new BasicKeyInfoGeneratorFactory();
        generator = null;
        
        pubKey = SecurityTestHelper.buildJavaRSAPublicKey(rsaBase64);
        
        credential = new BasicCredential();
        credential.setEntityId(entityID);
        credential.getKeyNames().add(keyNameFoo);
        credential.getKeyNames().add(keyNameBar);
        credential.setPublicKey(pubKey);
    }
    
    /**
     * Test no options - should produce null KeyInfo.
     * @throws SecurityException
     */
    public void testNoOptions() throws SecurityException {
        // all options false by default
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo with no options should have been null", keyInfo);
    }
    
    /**
     * Test emit public key.
     * @throws SecurityException
     */
    public void testEmitPublicKey() throws SecurityException, KeyException {
        factory.setEmitPublicKeyValue(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyInfo children", 1, keyInfo.getOrderedChildren().size());
        assertEquals("Unexpected number of KeyValue elements", 1, keyInfo.getKeyValues().size());
        PublicKey generatedKey = KeyInfoHelper.getKey(keyInfo.getKeyValues().get(0));
        assertEquals("Unexpected key value", pubKey, generatedKey);
    }
    
    /**
     * Test emit credential key names.
     * @throws SecurityException
     */
    public void testEmitKeynames() throws SecurityException {
        factory.setEmitKeyNames(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 2, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameFoo));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameBar));
    }
    
    /**
     * Test emit entity ID as key name.
     * @throws SecurityException
     */
    public void testEmitEntityIDAsKeyName() throws SecurityException {
        factory.setEmitEntityIDAsKeyName(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 1, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(entityID));
    }
    
    /** 
     * Test that the options passed to the generator are really cloned. 
     * After newInstance() is called, changes to the factory options should not be 
     * reflected in the generator.
     * @throws SecurityException */
    public void testProperOptionsCloning() throws SecurityException {
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo was null", keyInfo);
        
        factory.setEmitKeyNames(true);
        factory.setEmitEntityIDAsKeyName(true);
        factory.setEmitPublicKeyValue(true);
        
        keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo was null", keyInfo);
        
        generator = factory.newInstance();
        keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        assertEquals("Unexpected # of KeyInfo children found", 4, keyInfo.getOrderedChildren().size());
    }

}
