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

import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NewEncryptedID;
import org.opensaml.saml2.core.NewID;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoGenerator;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.util.DatatypeHelper;

/**
 * Simple tests for encryption.
 */
public class SimpleEncryptionTest extends BaseTestCase {
    
    private Encrypter encrypter;
    private EncryptionParameters encParams;
    private KeyEncryptionParameters kekParamsRSA;
    private List<KeyEncryptionParameters> kekParamsList;
    
    private KeyInfo keyInfo;
    
    private String algoURI;
    private String expectedKeyName;
    
    private String kekURIRSA;
    private String kekExpectedKeyNameRSA;

    /**
     * Constructor.
     *
     */
    public SimpleEncryptionTest() {
        super();
        
        expectedKeyName = "SuperSecretKey";
        algoURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        
        kekExpectedKeyNameRSA = "RSAKeyEncryptionKey";
        kekURIRSA = EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15;
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(algoURI);
        encParams.setEncryptionCredential(SecurityTestHelper.generateKeyAndCredential(algoURI));
        
        kekParamsRSA = new KeyEncryptionParameters();
        kekParamsRSA.setAlgorithm(kekURIRSA);
        kekParamsRSA.setEncryptionCredential(SecurityTestHelper.generateKeyPairAndCredential(kekURIRSA, 1024, false));
        
        kekParamsList = new ArrayList<KeyEncryptionParameters>();
        
        keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }

    /**
     *  Test basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testAssertion() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
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
        
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encTarget.getEncryptedData().getType());
        assertEquals("Algorithm attribute", algoURI, 
                encTarget.getEncryptedData().getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encTarget.getEncryptedData().getKeyInfo());
        assertEquals("KeyName", expectedKeyName, 
                encTarget.getEncryptedData().getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        
        assertFalse("EncryptedData ID attribute was empty",
                DatatypeHelper.isEmpty(encTarget.getEncryptedData().getID()));
    }
    
    /**
     *  Test basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testAssertionAsID() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        EncryptedID encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encryptAsID(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedID);
        encTarget = (EncryptedID) encObject;
        
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encTarget.getEncryptedData().getType());
        assertEquals("Algorithm attribute", algoURI, 
                encTarget.getEncryptedData().getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encTarget.getEncryptedData().getKeyInfo());
        assertEquals("KeyName", expectedKeyName, 
                encTarget.getEncryptedData().getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        
        assertFalse("EncryptedData ID attribute was empty",
                DatatypeHelper.isEmpty(encTarget.getEncryptedData().getID()));
    }
    
    /**
     *  Test basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testNameID() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        NameID target = assertion.getSubject().getNameID();
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        EncryptedID encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedID);
        encTarget = (EncryptedID) encObject;
        
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encTarget.getEncryptedData().getType());
        assertEquals("Algorithm attribute", algoURI, 
                encTarget.getEncryptedData().getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encTarget.getEncryptedData().getKeyInfo());
        assertEquals("KeyName", expectedKeyName, 
                encTarget.getEncryptedData().getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        
        assertFalse("EncryptedData ID attribute was empty",
                DatatypeHelper.isEmpty(encTarget.getEncryptedData().getID()));
    }
    
    /**
     *  Test basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testAttribute() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        Attribute target = assertion.getAttributeStatements().get(0).getAttributes().get(0);
        
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        EncryptedAttribute encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAttribute);
        encTarget = (EncryptedAttribute) encObject;
        
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encTarget.getEncryptedData().getType());
        assertEquals("Algorithm attribute", algoURI, 
                encTarget.getEncryptedData().getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encTarget.getEncryptedData().getKeyInfo());
        assertEquals("KeyName", expectedKeyName, 
                encTarget.getEncryptedData().getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        
        assertFalse("EncryptedData ID attribute was empty",
                DatatypeHelper.isEmpty(encTarget.getEncryptedData().getID()));
    }
    
    /**
     *  Test basic encryption with symmetric key, no key wrap,
     *  set key name in passed KeyInfo object.
     */
    public void testNewID() {
        NewID target = (NewID) buildXMLObject(NewID.DEFAULT_ELEMENT_NAME);
        target.setNewID("SomeNewID");
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        NewEncryptedID encTarget = null;
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof NewEncryptedID);
        encTarget = (NewEncryptedID) encObject;
        
        assertEquals("Type attribute", EncryptionConstants.TYPE_ELEMENT, encTarget.getEncryptedData().getType());
        assertEquals("Algorithm attribute", algoURI, 
                encTarget.getEncryptedData().getEncryptionMethod().getAlgorithm());
        assertNotNull("KeyInfo", encTarget.getEncryptedData().getKeyInfo());
        assertEquals("KeyName", expectedKeyName, 
                encTarget.getEncryptedData().getKeyInfo().getKeyNames().get(0).getValue());
        
        assertEquals("Number of EncryptedKeys", 0, 
                encTarget.getEncryptedData().getKeyInfo().getEncryptedKeys().size());
        
        assertFalse("EncryptedData ID attribute was empty",
                DatatypeHelper.isEmpty(encTarget.getEncryptedData().getID()));
        
    }
    
    /** Test that reuse of the encrypter with the same encryption and key encryption parameters is allowed. */
    public void testReuse() {
        Assertion assertion = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        Attribute target = assertion.getAttributeStatements().get(0).getAttributes().get(0);
        Attribute target2 = assertion.getAttributeStatements().get(0).getAttributes().get(1);
        
        KeyName keyName = (KeyName) buildXMLObject(org.opensaml.xml.signature.KeyName.DEFAULT_ELEMENT_NAME);
        keyName.setValue(expectedKeyName);
        keyInfo.getKeyNames().add(keyName);
        encParams.setKeyInfoGenerator(new StaticKeyInfoGenerator(keyInfo));
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
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
    
    /** Test that a data encryption key is auto-generated if it is not supplied. */
    public void testAutoKeyGen() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        encParams.setEncryptionCredential(null);
        
        kekParamsList.add(kekParamsRSA);
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
        } catch (EncryptionException e) {
            fail("Object encryption failed: " + e);
        }
        
        assertNotNull("Encrypted object was null", encObject);
        assertTrue("Encrypted object was not an instance of the expected type", 
                encObject instanceof EncryptedAssertion);
    }
    
    /** Test that an error is thrown if the no data encryption credential is supplied and no KEK is specified. */
    public void testAutoKeyGenNoKEK() {
        Assertion target = (Assertion) unmarshallElement("/data/org/opensaml/saml2/encryption/Assertion.xml");
        
        encParams.setEncryptionCredential(null);
        
        kekParamsList.clear();
        
        encrypter = new Encrypter(encParams, kekParamsList);
        
        XMLObject encObject = null;
        try {
            encObject = encrypter.encrypt(target);
            fail("Object encryption should have failed: no KEK supplied with auto key generation for data encryption");
        } catch (EncryptionException e) {
            //do nothing, should fail
        }
    }
    
}
