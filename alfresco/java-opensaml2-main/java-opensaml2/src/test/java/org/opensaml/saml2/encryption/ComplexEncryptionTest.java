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

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.encryption.Encrypter.KeyPlacement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DataReference;
import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoGenerator;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.RetrievalMethod;
import org.opensaml.xml.util.DatatypeHelper;

/**
 * Tests for encryption using single and multicast key encryption keys, 
 * and peer vs. inline key placement.
 */
public class ComplexEncryptionTest extends BaseTestCase {
    
    private Encrypter encrypter;
    private EncryptionParameters encParams;
    private List<KeyEncryptionParameters> kekParamsList;
    private KeyEncryptionParameters kekParamsRSA, kekParamsAES;
    
    private KeyInfo keyInfo, kekKeyInfoRSA, kekKeyInfoAES;
    
    private String algoURI, kekURIRSA, kekURIAES;
    private String expectedKeyName, expectedKeyNameRSA, expectedKeyNameAES;
    private String expectedRecipientRSA, expectedRecipientAES;

    /**
     * Constructor.
     *
     */
    public ComplexEncryptionTest() {
        super();
        
        expectedKeyName = "SuperSecretKey";
        expectedKeyNameRSA = "RSAKeyWrapper";
        expectedKeyNameAES = "AESKeyWrapper";
        expectedRecipientRSA = "RSARecipient";
        expectedRecipientAES = "AESRecipient";
        algoURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        kekURIRSA = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
        kekURIAES = EncryptionConstants.ALGO_ID_KEYWRAP_AES128;
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        Credential encCred = SecurityTestHelper.generateKeyAndCredential(algoURI);
        Credential kekCredAES = SecurityTestHelper.generateKeyAndCredential(kekURIAES);
        Credential kekCredRSA = SecurityTestHelper.generateKeyPairAndCredential(kekURIRSA, 2048, false);
        
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(algoURI);
        encParams.setEncryptionCredential(encCred);
        
        kekParamsAES = new KeyEncryptionParameters();
        kekParamsAES.setAlgorithm(kekURIAES);
        kekParamsAES.setEncryptionCredential(kekCredAES);
        
        kekParamsRSA = new KeyEncryptionParameters();
        kekParamsRSA.setAlgorithm(kekURIRSA);
        kekParamsRSA.setEncryptionCredential(kekCredRSA);
        
        kekParamsList = new ArrayList<KeyEncryptionParameters>();
        
        keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        kekKeyInfoRSA = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
        kekKeyInfoAES = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }

    /**
     * Test encryption with a single key encryption key with key placement inline.
     */
    public void testSingleKEKInline() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        KeyName keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyNameRSA);
        kekKeyInfoRSA.getKeyNames().add(keyName);
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        kekParamsList.add(kekParamsRSA);
        
        encrypter = new Encrypter(encParams, kekParamsList);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
        
        EncryptedAssertion encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAssertion);
        encTarget = (EncryptedAssertion) encObject;
        
        assertEquals("Number of inline EncryptedKeys", 1, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        assertEquals("Number of peer EncryptedKeys", 0, 
                encTarget.getEncryptedKeys().size());
        
        
        EncryptedKey encKey = encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);
        assertNotNull("EncryptedKey was null", encKey);
        
        assertEquals("Algorithm attribute", kekURIRSA, 
                encKey.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encKey.getKeyInfo());
        assertEquals("KeyName", expectedKeyNameRSA, 
                encKey.getKeyInfo().getKeyNames().get(0).getValue());
        
        assertFalse("EncryptedKey ID attribute was empty",
                DatatypeHelper.isEmpty(encKey.getID()));
        
        EncryptedData encData = encTarget.getEncryptedData();
        assertNotNull("EncryptedData KeyInfo wasn't null", encData.getKeyInfo());
        assertEquals("EncryptedData improperly contained a RetrievalMethod", 0,
                encData.getKeyInfo().getRetrievalMethods().size());
        
        assertNull("EncryptedKey ReferenceList wasn't null", encKey.getReferenceList());
        assertNull("EncryptedKey CarriedKeyName wasn't null", encKey.getCarriedKeyName());
    }
    
    /**
     * Test encryption with a single key encryption key with key placement as peer.
     */
    public void testSingleKEKPeer() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        KeyName keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyNameRSA);
        kekKeyInfoRSA.getKeyNames().add(keyName);
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        kekParamsList.add(kekParamsRSA);
        
        encrypter = new Encrypter(encParams, kekParamsList);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);
        
        EncryptedAssertion encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAssertion);
        encTarget = (EncryptedAssertion) encObject;
        
        assertEquals("Number of inline EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        assertEquals("Number of peer EncryptedKeys", 1, 
                encTarget.getEncryptedKeys().size());
        
        
        EncryptedKey encKey = encTarget.getEncryptedKeys().get(0);
        assertNotNull("EncryptedKey was null", encKey);
        
        assertEquals("Algorithm attribute", kekURIRSA, 
                encKey.getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encKey.getKeyInfo());
        assertEquals("KeyName", expectedKeyNameRSA, 
                encKey.getKeyInfo().getKeyNames().get(0).getValue());
        
        assertFalse("EncryptedKey ID attribute was empty",
                DatatypeHelper.isEmpty(encKey.getID()));
        
        EncryptedData encData = encTarget.getEncryptedData();
        assertNotNull("EncryptedData KeyInfo wasn't null", encData.getKeyInfo());
        assertEquals("EncryptedData contained invalid number RetrievalMethods", 1,
                encData.getKeyInfo().getRetrievalMethods().size());
        RetrievalMethod rm = encData.getKeyInfo().getRetrievalMethods().get(0);
        assertEquals("EncryptedData RetrievalMethod had incorrect type attribute",
                EncryptionConstants.TYPE_ENCRYPTED_KEY, rm.getType());
        assertEquals("EncryptedData RetrievalMethod had incorrect URI value",
                "#" + encKey.getID(), rm.getURI());
        
        assertNotNull("EncryptedKey ReferenceList was null", encKey.getReferenceList());
        assertEquals("EncryptedKey contained invalid number DataReferences", 1,
                encKey.getReferenceList().getDataReferences().size());
        DataReference dr = encKey.getReferenceList().getDataReferences().get(0);
        assertEquals("EncryptedKey DataReference had incorrect URI value",
                "#" + encData.getID(), dr.getURI());
        assertNull("EncryptedKey CarriedKeyName wasn't null", encKey.getCarriedKeyName());
    }
    
    /** Test encryption with multicast key encryption keys with key placement as peer. */
    public void testMulticastKEKPeer() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        String multicastKeyNameValue = "MulticastDataEncryptionKeyName";
        KeyName keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(multicastKeyNameValue);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        kekParamsRSA.setRecipient(expectedRecipientRSA);
        kekParamsList.add(kekParamsRSA);
        kekParamsAES.setRecipient(expectedRecipientAES);
        kekParamsList.add(kekParamsAES);
        
        encrypter = new Encrypter(encParams, kekParamsList);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);
        
        EncryptedAssertion encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAssertion);
        encTarget = (EncryptedAssertion) encObject;
        
        assertEquals("Number of inline EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        assertEquals("Number of peer EncryptedKeys", 2, 
                encTarget.getEncryptedKeys().size());
        
        
        EncryptedKey encKeyRSA = encTarget.getEncryptedKeys().get(0);
        EncryptedKey encKeyAES = encTarget.getEncryptedKeys().get(1);
        assertNotNull("EncryptedKey was null", encKeyRSA);
        assertNotNull("EncryptedKey was null", encKeyAES);
        
        assertEquals("Algorithm attribute", kekURIRSA, 
                encKeyRSA.getEncryptionMethod().getAlgorithm());
        assertEquals("Algorithm attribute", kekURIAES, 
                encKeyAES.getEncryptionMethod().getAlgorithm());
        
        assertFalse("EncryptedKey ID attribute was empty",
                DatatypeHelper.isEmpty(encKeyRSA.getID()));
        assertFalse("EncryptedKey ID attribute was empty",
                DatatypeHelper.isEmpty(encKeyAES.getID()));
        
        EncryptedData encData = encTarget.getEncryptedData();
        assertNotNull("EncryptedData KeyInfo wasn't null", encData.getKeyInfo());
        assertEquals("EncryptedData contained invalid number RetrievalMethods", 0,
                encData.getKeyInfo().getRetrievalMethods().size());
        assertEquals("EncryptedData contained invalid number KeyNames", 1,
                encData.getKeyInfo().getKeyNames().size());
        KeyName encDataKeyName = encData.getKeyInfo().getKeyNames().get(0);
        assertEquals("EncryptedData KeyName value", multicastKeyNameValue, encDataKeyName.getValue());
        
        DataReference dr = null;
        
        assertEquals("EncryptedKey recipient attribute had invalid value", expectedRecipientRSA,
                encKeyRSA.getRecipient());
        assertNotNull("EncryptedKey ReferenceList was null", encKeyRSA.getReferenceList());
        assertEquals("EncryptedKey contained invalid number DataReferences", 1,
                encKeyRSA.getReferenceList().getDataReferences().size());
        dr = encKeyRSA.getReferenceList().getDataReferences().get(0);
        assertEquals("EncryptedKey DataReference had incorrect URI value",
                "#" + encData.getID(), dr.getURI());
        assertNotNull("EncryptedKey CarriedKeyName wasn't null", encKeyRSA.getCarriedKeyName());
        assertEquals("EncrypteKey CarriedKeyName had incorrect value", multicastKeyNameValue,
                encKeyRSA.getCarriedKeyName().getValue());
        
        assertEquals("EncryptedKey recipient attribute had invalid value", expectedRecipientAES,
                encKeyAES.getRecipient());
        assertNotNull("EncryptedKey ReferenceList was null", encKeyAES.getReferenceList());
        assertEquals("EncryptedKey contained invalid number DataReferences", 1,
                encKeyAES.getReferenceList().getDataReferences().size());
        dr = encKeyAES.getReferenceList().getDataReferences().get(0);
        assertEquals("EncryptedKey DataReference had incorrect URI value",
                "#" + encData.getID(), dr.getURI());
        assertNotNull("EncryptedKey CarriedKeyName wasn't null", encKeyAES.getCarriedKeyName());
        assertEquals("EncrypteKey CarriedKeyName had incorrect value", multicastKeyNameValue,
                encKeyAES.getCarriedKeyName().getValue());
    }
    
    /** Test that reuse is allowed with same key encryption parameters. */
    public void testReuse() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        Attribute target = assertion.getAttributeStatements().get(0).getAttributes().get(0);
        Attribute target2 = assertion.getAttributeStatements().get(0).getAttributes().get(1);
        
        KeyName keyName = (KeyName) buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyNameRSA);
        kekKeyInfoRSA.getKeyNames().add(keyName);
        kekParamsRSA.setKeyInfoGenerator(new StaticKeyInfoGenerator(kekKeyInfoRSA));
        
        kekParamsList.add(kekParamsRSA);
        
        encrypter = new Encrypter(encParams, kekParamsList);
        encrypter.setKeyPlacement(KeyPlacement.PEER);
        
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAttribute);
        
        XMLObject encObject2 = null;
        try {
            encObject2 = encrypter.encrypt(target2);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject2);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject2 instanceof EncryptedAttribute);
    }

}
