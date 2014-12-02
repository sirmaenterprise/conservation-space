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

package org.opensaml.xml.encryption;

import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoGenerator;
import org.opensaml.xml.signature.DigestMethod;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.SignatureConstants;
import org.w3c.dom.Document;

/**
 * Simple tests for encryption.
 */
public class SimpleEncryptionTest extends XMLObjectBaseTestCase {
    
    private Encrypter encrypter;
    
    private EncryptionParameters encParams;
    private String algoURI;
    
    private List<KeyEncryptionParameters> kekParamsList;
    
    private KeyEncryptionParameters kekParamsAES;
    private String kekURIAES;
    
    private KeyEncryptionParameters kekParamsRSA;
    private String kekURIRSA;
    
    private KeyInfo keyInfo;
    private KeyInfo kekKeyInfoAES;
    private KeyInfo kekKeyInfoRSA;
    
    private String expectedKeyName;
    private String expectedKEKKeyNameAES;
    private String expectedKEKKeyNameRSA;
    private String expectedRecipientRSA;
    private String expectedRecipientAES;
    private String targetFile;
    

    /**
     * Constructor.
     *
     */
    public SimpleEncryptionTest() {
        super();
        
        expectedKeyName = "SuperSecretKey";
        expectedKEKKeyNameAES = "KEKKeyAES";
        expectedKEKKeyNameAES = "KEKKeyRSA";
        expectedRecipientRSA = "CoolRecipientRSA";
        expectedRecipientAES = "CoolRecipientAES";
        targetFile = "/data/org/opensaml/xml/encryption/SimpleEncryptionTest.xml";
        
        algoURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        kekURIRSA = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
        kekURIAES = EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        encrypter = new Encrypter();
        
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(algoURI);
        encParams.setEncryptionCredential(SecurityTestHelper.generateKeyAndCredential(algoURI));
        
        kekParamsList = new ArrayList<KeyEncryptionParameters>();
        
        kekParamsAES = new KeyEncryptionParameters();
        kekParamsAES.setAlgorithm(kekURIAES);
        kekParamsAES.setEncryptionCredential(SecurityTestHelper.generateKeyAndCredential(kekURIAES));
        kekParamsAES.setRecipient(expectedRecipientAES);
        
        kekParamsRSA = new KeyEncryptionParameters();
        kekParamsRSA.setAlgorithm(kekURIRSA);
        kekParamsRSA.setEncryptionCredential(SecurityTestHelper.generateKeyPairAndCredential(kekURIRSA, 1024, false));
        kekParamsRSA.setRecipient(expectedRecipientRSA);
        
        keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        KeyName keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        
        kekKeyInfoAES = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKEKKeyNameAES);
        kekKeyInfoAES.getKeyNames().add(keyName);
        
        kekKeyInfoRSA = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKEKKeyNameRSA);
        kekKeyInfoRSA.getKeyNames().add(keyName);
    }

    /**
     *  Test data basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testEncryptDataWithKeyNameNoKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElement(sxo, encParams);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        assertEquals("KeyName", expectedKeyName, encData.getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, encData.getKeyInfo().getEncryptedKeys().size());
    }
    
    /**
     *  Test data basic encryption with symmetric key, one KEK.
     */
    public void testEncryptDataSingleKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsRSA);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        
        List<EncryptedKey> encKeys = encData.getKeyInfo().getEncryptedKeys();
        assertEquals("Number of EncryptedKeys", 1, encData.getKeyInfo().getEncryptedKeys().size());
        checkKEKRSA(encKeys.get(0), true);
    }
    
    /**
     *  Test basic data encryption with symmetric key, one KEK.
     */
    public void testEncryptDataMultipleKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        kekParamsAES.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoAES));
        
        kekParamsList.add(kekParamsRSA);
        kekParamsList.add(kekParamsAES);
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsList);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        
        List<EncryptedKey> encKeys = encData.getKeyInfo().getEncryptedKeys();
        assertEquals("Number of EncryptedKeys", 2, encData.getKeyInfo().getEncryptedKeys().size());
        checkKEKRSA(encKeys.get(0), true);
        checkKEKAES(encKeys.get(1), true);
    }
    
    /**
     *  Test basic content encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testEncryptContentWithKeyNameNoKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElementContent(sxo, encParams);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_CONTENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        assertEquals("KeyName", expectedKeyName, encData.getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, encData.getKeyInfo().getEncryptedKeys().size());
    }
    
    /**
     *  Test basic content encryption with symmetric key, one KEK.
     */
    public void testEncryptContentSingleKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElementContent(sxo, encParams, kekParamsRSA);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_CONTENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        
        List<EncryptedKey> encKeys = encData.getKeyInfo().getEncryptedKeys();
        assertEquals("Number of EncryptedKeys", 1, encData.getKeyInfo().getEncryptedKeys().size());
        checkKEKRSA(encKeys.get(0), true);
    }
    
    /**
     *  Test basic encryption with symmetric key, one KEK.
     */
    public void testEncryptContentMultipleKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        kekParamsAES.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoAES));
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        kekParamsList.add(kekParamsRSA);
        kekParamsList.add(kekParamsAES);
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElementContent(sxo, encParams, kekParamsList);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull(encData);
        assertEquals("Type attribute", EncryptionConstants.TYPE_CONTENT, encData.getType());
        assertEquals("Algorithm attribute", algoURI, encData.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encData.getKeyInfo());
        
        List<EncryptedKey> encKeys = encData.getKeyInfo().getEncryptedKeys();
        assertEquals("Number of EncryptedKeys", 2, encData.getKeyInfo().getEncryptedKeys().size());
        checkKEKRSA(encKeys.get(0), true);
        checkKEKAES(encKeys.get(1), true);
    }
    
    /**
     *  Test basic encryption of a symmetric key into an EncryptedKey,
     *  set key encrypting key name in passed KeyInfo object.
     * @throws NoSuchProviderException bad JCA provider
     * @throws NoSuchAlgorithmException  bad JCA algorithm
     * @throws XMLParserException error creating new Document from pool
     */
    public void testEncryptKeySingleKEK() throws NoSuchAlgorithmException, NoSuchProviderException, 
            XMLParserException {
        
        Key targetKey = SecurityTestHelper.generateKeyFromURI(algoURI);
        
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        EncryptedKey encKey = null;
        Document ownerDocument = parserPool.newDocument();
        try {
            encKey = encrypter.encryptKey(targetKey, kekParamsRSA, ownerDocument);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        } 
        
        checkKEKRSA(encKey, true);
    }
    
    /**
     *  Test basic encryption of a symmetric key into an EncryptedKey,
     *  set key encrypting key name in passed KeyInfo object.
     * @throws NoSuchProviderException bad JCA provider
     * @throws NoSuchAlgorithmException  bad JCA algorithm
     * @throws XMLParserException error creating new Document from pool
     */
    public void testEncryptKeyMultipleKEK() throws NoSuchAlgorithmException, NoSuchProviderException, 
            XMLParserException {
        
        Key targetKey = SecurityTestHelper.generateKeyFromURI(algoURI);
        
        kekParamsAES.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoAES));
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        kekParamsList.add(kekParamsAES);
        kekParamsList.add(kekParamsRSA);
        
        List<EncryptedKey> encKeys = null;
        Document ownerDocument = parserPool.newDocument();
        try {
            encKeys = encrypter.encryptKey(targetKey, kekParamsList, ownerDocument);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        
        assertEquals("Number of EncryptedKeys", 2, encKeys.size());
        checkKEKAES(encKeys.get(0), true);
        checkKEKRSA(encKeys.get(1), true);
    }
    
    /**
     *  Test basic encryption with auto-generated symmetric key.
     *  
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     */
    public void testAutoKeyGen() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        encParams.setEncryptionCredential(null);
        
        kekParamsList.add(kekParamsRSA);
        
        EncryptedData encData = null;
        
        // try with single KEK
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsRSA);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        assertNotNull(encData);
        
        // try with multiple KEK
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsList);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        assertNotNull(encData);
    }
    
    /**
     *  Test failure with auto-generated symmetric key and no KEK(s).
     *  
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     */
    public void testAutoKeyGenNoKEK() {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        encParams.setEncryptionCredential(null);
        
        kekParamsList.clear();
        
        EncryptedData encData = null;
        
        // try with no KEK
        try {
            encData = encrypter.encryptElement(sxo, encParams);
            fail("Object encryption should have failed: no KEK supplied with auto key generation for data encryption");
        } catch (EncryptionException e) {
            // do nothing, should fail
        }
        
        // try with empty KEK list
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsList);
            fail("Object encryption should have failed: no KEK supplied with auto key generation for data encryption");
        } catch (EncryptionException e) {
            // do nothing, should fail
        }
    }
    
    /**
     * Test code for the Apache XML-Security issue workaround that requires we 
     * expliclty express SHA-1 DigestMethod on EncryptionMethod,
     * only when key transport algorithm is RSA-OAEP.
     *  
     * @throws NoSuchProviderException bad JCA provider
     * @throws NoSuchAlgorithmException  bad JCA algorithm
     * @throws XMLParserException error creating new Document from pool
     */
    public void testEncryptKeyDigestMethodsRSAOAEP() throws NoSuchAlgorithmException, NoSuchProviderException, 
            XMLParserException {
        
        Key targetKey = SecurityTestHelper.generateKeyFromURI(algoURI);
        
        kekParamsRSA.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        
        EncryptedKey encKey = null;
        Document ownerDocument = parserPool.newDocument();
        try {
            encKey = encrypter.encryptKey(targetKey, kekParamsRSA, ownerDocument);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        } 
        
        assertFalse("EncryptedKey/EncryptionMethod/DigestMethod list was empty",
                encKey.getEncryptionMethod().getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME).isEmpty());
        DigestMethod dm = 
                (DigestMethod) encKey.getEncryptionMethod()
                .getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME).get(0);
        assertEquals("DigestMethod algorithm URI had unexpected value", 
                SignatureConstants.ALGO_ID_DIGEST_SHA1, dm.getAlgorithm());
    }
    
    /**
     * Test code for the Apache XML-Security issue workaround that requires we 
     * expliclty express SHA-1 DigestMethod on EncryptionMethod,
     * only when key transport algorithm is RSA-OAEP.
     *  
     * @throws NoSuchProviderException bad JCA provider
     * @throws NoSuchAlgorithmException  bad JCA algorithm
     * @throws XMLParserException error creating new Document from pool
     */
    public void testEncryptKeyDigestMethodsRSAv15() throws NoSuchAlgorithmException, NoSuchProviderException, 
            XMLParserException {
        
        Key targetKey = SecurityTestHelper.generateKeyFromURI(algoURI);
        
        kekParamsRSA.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
        
        EncryptedKey encKey = null;
        Document ownerDocument = parserPool.newDocument();
        try {
            encKey = encrypter.encryptKey(targetKey, kekParamsRSA, ownerDocument);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        } 
        
        assertTrue("EncryptedKey/EncryptionMethod/DigestMethod list was NOT empty",
                encKey.getEncryptionMethod().getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME).isEmpty());
    }
    
    /**
     *  Test proper error handling of attempt to encrypt with a DSA key.
     *  
     * @throws NoSuchProviderException 
     * @throws NoSuchAlgorithmException 
     */
    public void testEncryptDataBadKEKDSA() throws NoSuchAlgorithmException, NoSuchProviderException {
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshallElement(targetFile);
        
        KeyEncryptionParameters kekParamsDSA = new KeyEncryptionParameters();
        KeyPair kp = SecurityTestHelper.generateKeyPair("DSA", 1024, null);
        kekParamsDSA.setEncryptionCredential(SecurityHelper.getSimpleCredential(kp.getPublic(), null));
        
        EncryptedData encData = null;
        try {
            encData = encrypter.encryptElement(sxo, encParams, kekParamsDSA);
            fail("Object encryption succeeded, should have failed with DSA key attempt");
        } catch (EncryptionException e) {
            // do nothing failure expected
        }
    }
    
    /**
     * Helper method to test AES KEK.
     * 
     * @param encKey EncryptedKey to test
     * @param hasKeyInfo flag indicating expectation of KeyInfo presence
     */
    private void checkKEKAES(EncryptedKey encKey, boolean hasKeyInfo) {
        assertNotNull("EncryptedKey was null", encKey);
        assertEquals("Algorithm attribute", kekURIAES, encKey.getEncryptionMethod().getAlgorithm());
        assertEquals("Recipient attribute", expectedRecipientAES, encKey.getRecipient());
        if (! hasKeyInfo) {
            assertNull("Unexpected KeyInfo was present", encKey.getKeyInfo());
            return;
        } else {
            assertNotNull("KeyInfo was not present", encKey.getKeyInfo());
            assertNotNull("KeyName was not present", encKey.getKeyInfo().getKeyNames().get(0));
            assertEquals("Unexpected KEK KeyName", expectedKEKKeyNameAES, 
                    encKey.getKeyInfo().getKeyNames().get(0).getValue());
        }
    }
 
    /**
     * Helper method to test RSA KEK.
     * 
     * @param encKey EncryptedKey to test
     * @param hasKeyInfo flag indicating expectation of KeyInfo presence
     */
    private void checkKEKRSA(EncryptedKey encKey, boolean hasKeyInfo) {
        assertNotNull("EncryptedKey was null", encKey);
        assertEquals("Algorithm attribute", kekURIRSA, encKey.getEncryptionMethod().getAlgorithm());
        assertEquals("Recipient attribute", expectedRecipientRSA, encKey.getRecipient());
        if (! hasKeyInfo) {
            assertNull("Unexpected KeyInfo was present", encKey.getKeyInfo());
            return;
        } else {
            assertNotNull("KeyInfo was not present", encKey.getKeyInfo());
            assertNotNull("KeyName was not present", encKey.getKeyInfo().getKeyNames().get(0));
            assertEquals("Unexpected KEK KeyName", expectedKEKKeyNameRSA, 
                    encKey.getKeyInfo().getKeyNames().get(0).getValue());
        }
    }
    
}
