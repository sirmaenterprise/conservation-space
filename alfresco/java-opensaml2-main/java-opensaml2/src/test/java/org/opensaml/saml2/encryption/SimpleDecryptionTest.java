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

package org.opensaml.saml2.encryption;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.SecretKey;

import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NewEncryptedID;
import org.opensaml.saml2.core.NewID;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.w3c.dom.Document;

/**
 * Simple tests for SAML 2 decrypter, using a hardcoded key (so not testing complex encrypted key resolution, etc).
 */
public class SimpleDecryptionTest extends BaseTestCase {
    
    private KeyInfoCredentialResolver keyResolver;
    
    private String encURI;
    private Key encKey;
    private EncryptionParameters encParams;
    
    private Encrypter encrypter;
    
    /**
     * Constructor.
     *
     */
    public SimpleDecryptionTest() {
        super();
        
        encURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        Credential encCred = SecurityTestHelper.generateKeyAndCredential(encURI);
        encKey = encCred.getSecretKey();
        keyResolver = new StaticKeyInfoCredentialResolver(encCred);
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(encURI);
        encParams.setEncryptionCredential(encCred);
        
        encrypter = new Encrypter(encParams);
        
    }
    
    /**
     * Test decryption of an EncryptedAssertion.
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     */
    public void testEncryptedAssertion() throws XMLParserException, EncryptionException {
        String filename = "/data/org/opensaml/saml2/encryption/Assertion.xml";
        Document targetDOM = getDOM(filename);
        
        Assertion target = (Assertion) unmarshallElement(filename);
        EncryptedAssertion encryptedTarget = encrypter.encrypt(target);
        
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
        } catch (DecryptionException e) {
            fail("Error on decryption of encrypted SAML 2 type to element: " + e);
        }
        
        assertNotNull("Decrypted target was null", decryptedTarget);
        assertTrue("Decrypted target was not the expected type", decryptedTarget instanceof Assertion);
        
        assertEquals(targetDOM, decryptedTarget);
    }
    
    /**
     * Test decryption of an Assertion as an EncryptedID.
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     */
    public void testEncryptedAssertionAsID() throws XMLParserException, EncryptionException {
        String filename = "/data/org/opensaml/saml2/encryption/Assertion.xml";
        Document targetDOM = getDOM(filename);
        
        Assertion target = (Assertion) unmarshallElement(filename);
        EncryptedID encryptedTarget = encrypter.encryptAsID(target);
        
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
        } catch (DecryptionException e) {
            fail("Error on decryption of encrypted SAML 2 type to element: " + e);
        }
        
        assertNotNull("Decrypted target was null", decryptedTarget);
        assertTrue("Decrypted target was not the expected type", decryptedTarget instanceof Assertion);
        
        assertEquals(targetDOM, decryptedTarget);
    }
    
    /**
     * Test decryption of an NameID as an EncryptedID.
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     */
    public void testEncryptedNameID() throws XMLParserException, EncryptionException {
        String filename = "/data/org/opensaml/saml2/encryption/NameID.xml";
        Document targetDOM = getDOM(filename);
        
        NameID target = (NameID) unmarshallElement(filename);
        EncryptedID encryptedTarget = encrypter.encrypt(target);
        
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
        } catch (DecryptionException e) {
            fail("Error on decryption of encrypted SAML 2 type to element: " + e);
        }
        
        assertNotNull("Decrypted target was null", decryptedTarget);
        assertTrue("Decrypted target was not the expected type", decryptedTarget instanceof NameID);
        
        assertEquals(targetDOM, decryptedTarget);
    }
    
    /**
     * Test decryption of an NewID as an NewEncryptedID.
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     */
    public void testEncryptedNewID() throws XMLParserException, EncryptionException {
        String filename = "/data/org/opensaml/saml2/encryption/NewID.xml";
        Document targetDOM = getDOM(filename);
        
        NewID target = (NewID) unmarshallElement(filename);
        NewEncryptedID encryptedTarget = encrypter.encrypt(target);
        
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
        } catch (DecryptionException e) {
            fail("Error on decryption of encrypted SAML 2 type to element: " + e);
        }
        
        assertNotNull("Decrypted target was null", decryptedTarget);
        assertTrue("Decrypted target was not the expected type", decryptedTarget instanceof NewID);
        
        assertEquals(targetDOM, decryptedTarget);
    }
    
    /**
     * Test decryption of an EncryptedAttribute.
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     */
    public void testEncryptedAttribute() throws XMLParserException, EncryptionException {
        String filename = "/data/org/opensaml/saml2/encryption/Attribute.xml";
        Document targetDOM = getDOM(filename);
        
        Attribute target = (Attribute) unmarshallElement(filename);
        EncryptedAttribute encryptedTarget = encrypter.encrypt(target);
        
        Decrypter decrypter = new Decrypter(keyResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
        } catch (DecryptionException e) {
            fail("Error on decryption of encrypted SAML 2 type to element: " + e);
        }
        
        assertNotNull("Decrypted target was null", decryptedTarget);
        assertTrue("Decrypted target was not the expected type", decryptedTarget instanceof Attribute);
        
        assertEquals(targetDOM, decryptedTarget);
    }
    
    /**
     *  Test error condition of invalid data decryption key.
     * @throws EncryptionException 
     *  
     * @throws XMLParserException  thrown if there is an error parsing the control XML file
     * @throws EncryptionException  thrown if there is an error encrypting the control XML
     * @throws NoSuchProviderException security provider was invalid
     * @throws NoSuchAlgorithmException security/key algorithm was invalid
     */
    public void testErrorInvalidDataDecryptionKey() 
            throws XMLParserException, EncryptionException, NoSuchAlgorithmException, NoSuchProviderException {
        Key badKey = SecurityTestHelper.generateKeyFromURI(encURI);
        BasicCredential encCred = new BasicCredential();
        encCred.setSecretKey((SecretKey) badKey);
        KeyInfoCredentialResolver badEncResolver = new StaticKeyInfoCredentialResolver(encCred);
        
        String filename = "/data/org/opensaml/saml2/encryption/Assertion.xml";
        
        Assertion target = (Assertion) unmarshallElement(filename);
        EncryptedAssertion encryptedTarget = encrypter.encrypt(target);
        
        Decrypter decrypter = new Decrypter(badEncResolver, null, null);
        
        SAMLObject decryptedTarget = null;
        try {
            decryptedTarget = decrypter.decrypt(encryptedTarget);
            fail("Decryption should have failed due to bad decryption key");
        } catch (DecryptionException e) {
            // do nothing, should faile
        }
        
    }
    
    /**
     * Parse the XML file and return the DOM Document.
     * 
     * @param filename file containing control XML
     * @return parsed Document
     * @throws XMLParserException if parser encounters an error
     */
    private Document getDOM(String filename) throws XMLParserException {
        Document targetDOM = parser.parse(SimpleDecryptionTest.class.getResourceAsStream(filename));
        return targetDOM;
    }
    
}
