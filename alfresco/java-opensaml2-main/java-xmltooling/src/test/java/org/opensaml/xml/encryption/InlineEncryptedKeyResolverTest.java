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

package org.opensaml.xml.encryption;

import java.util.ArrayList;
import java.util.List;

import org.opensaml.xml.XMLObjectBaseTestCase;

/**
 * Test the inline encrypted key resolver.
 */
public class InlineEncryptedKeyResolverTest extends XMLObjectBaseTestCase {
    
    /** The resolver instance to be tested. */
    private InlineEncryptedKeyResolver resolver;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        resolver = new InlineEncryptedKeyResolver();
    }

    /** No recipients specified to resolver, one inline EncryptedKey in instance. */
    public void  testSingleEKNoRecipients() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverSingle.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().clear();
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 1, resolved.size());
        
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(0) == allKeys.get(0));
    }
    
    /** One recipient specified to resolver, one matching inline EncryptedKey in instance. */
    public void  testSingleEKOneRecipientWithMatch() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverSingle.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().add("foo");
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 1, resolved.size());
        
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(0) == allKeys.get(0));
    }
    
    /** One recipient specified to resolver, zero matching inline EncryptedKey in instance. */
    public void  testSingleEKOneRecipientNoMatch() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverSingle.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().add("bar");
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 0, resolved.size());
    }
    
    /** No recipients specified to resolver. */
    public void  testMultiEKNoRecipients() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverMultiple.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().clear();
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 4, resolved.size());
        
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(0) == allKeys.get(0));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(1) == allKeys.get(1));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(2) == allKeys.get(2));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(3) == allKeys.get(3));
    }
    
    /** One recipient specified to resolver, one matching & and one recipient-less 
     *  inline EncryptedKey in instance. */
    public void  testMultiEKOneRecipientWithMatch() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverMultiple.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().add("foo");
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 2, resolved.size());
        
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(0) == allKeys.get(0));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(1) == allKeys.get(2));
    }
    
    /** Multi recipient specified to resolver, several matching inline EncryptedKey in instance. */
    public void  testMultiEKOneRecipientWithMatches() {
        String filename = "/data/org/opensaml/xml/encryption/InlineEncryptedKeyResolverMultiple.xml";
        EncryptedData encData = (EncryptedData) unmarshallElement(filename);
        
        assertNotNull(encData);
        assertNotNull(encData.getKeyInfo());
        List<EncryptedKey> allKeys = encData.getKeyInfo().getEncryptedKeys();
        assertFalse(allKeys.isEmpty());
        
        resolver.getRecipients().add("foo");
        resolver.getRecipients().add("baz");
        
        List<EncryptedKey> resolved = generateList(encData, resolver);
        assertEquals("Incorrect number of resolved EncryptedKeys found", 3, resolved.size());
        
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(0) == allKeys.get(0));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(1) == allKeys.get(2));
        assertTrue("Unexpected EncryptedKey instance found", resolved.get(2) == allKeys.get(3));
    }
    
    /**
     * Resolve EncryptedKeys and put them in an ordered list.
     * 
     * @param encData the EncryptedData context
     * @param ekResolver the resolver to test
     * @return list of resolved EncryptedKeys
     */
    private List<EncryptedKey> generateList(EncryptedData encData, EncryptedKeyResolver ekResolver) {
        List<EncryptedKey> resolved = new ArrayList<EncryptedKey>();
        for (EncryptedKey encKey : ekResolver.resolve(encData)) {
            resolved.add(encKey);
        }
        return resolved;
    }

}
