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

package org.opensaml.xml.security;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import junit.framework.TestCase;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignatureConstants;

/**
 * Test the SigningUtil operations for generating and verifying simple, raw signatures and MAC's.
 */
public class SigningUtilTest extends TestCase {
    
    private SecretKey secretKeyAES128;
    private KeyPair keyPairRSA;
    private Credential credAES;
    private Credential credRSA;
    
    private String data;
    private byte[] controlSignatureRSA;
    private byte[] controlSignatureHMAC;
    
    private String rsaAlgorithmURI;
    private String rsaJCAAlgorithm;
    private String hmacAlgorithmURI;
    private String hmacJCAAlgorithm;
    
    public SigningUtilTest() throws NoSuchAlgorithmException, NoSuchProviderException {
        data = "Hello, here is some secret data that is to be signed";
        
        rsaAlgorithmURI = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1;
        rsaJCAAlgorithm = "SHA1withRSA";
        
        hmacAlgorithmURI = SignatureConstants.ALGO_ID_MAC_HMAC_SHA1;
        hmacJCAAlgorithm = "HmacSHA1";
    }
    

    protected void setUp() throws Exception {
        super.setUp();
        
        secretKeyAES128 = SecurityTestHelper.generateKey("AES", 128, null);
        credAES = SecurityHelper.getSimpleCredential(secretKeyAES128);
        keyPairRSA = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        credRSA = SecurityHelper.getSimpleCredential(keyPairRSA.getPublic(), keyPairRSA.getPrivate());
        
        controlSignatureRSA = getControlSignature(data.getBytes(), keyPairRSA.getPrivate(), rsaJCAAlgorithm);
        assertNotNull(controlSignatureRSA);
        assertTrue(controlSignatureRSA.length > 0);
        
        controlSignatureHMAC = getControlSignature(data.getBytes(), secretKeyAES128, hmacJCAAlgorithm);
        assertNotNull(controlSignatureHMAC);
        assertTrue(controlSignatureHMAC.length > 0);
    }

    public void testSigningWithPrivateKey() throws SecurityException {
        byte[] signature = SigningUtil.signWithURI(credRSA, rsaAlgorithmURI, data.getBytes());
        assertNotNull(signature);
        assertTrue("Signature was not the expected value", Arrays.equals(controlSignatureRSA, signature));
    }
    
    public void testSigningWithHMAC() throws SecurityException {
        byte[] signature = SigningUtil.signWithURI(credAES, hmacAlgorithmURI, data.getBytes());
        assertNotNull(signature);
        assertTrue("Signature was not the expected value", Arrays.equals(controlSignatureHMAC, signature));
    }
    
    public void testVerificationWithPublicKey() throws SecurityException, NoSuchAlgorithmException, NoSuchProviderException {
        assertTrue("Signature failed to verify, should have succeeded",
                SigningUtil.verifyWithURI(credRSA, rsaAlgorithmURI, controlSignatureRSA, data.getBytes()));
        
        KeyPair badKP = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        Credential badCred = SecurityHelper.getSimpleCredential(badKP.getPublic(), badKP.getPrivate());
        
        assertFalse("Signature verified successfully, should have failed due to wrong verification key",
                SigningUtil.verifyWithURI(badCred, rsaAlgorithmURI, controlSignatureRSA, data.getBytes()));
        
        String tamperedData = data + "HAHA All your base are belong to us";
        
        assertFalse("Signature verified successfully, should have failed due to tampered data",
                SigningUtil.verifyWithURI(credRSA, rsaAlgorithmURI, controlSignatureRSA, tamperedData.getBytes()));
    }

    public void testVerificationWithHMAC() throws SecurityException, NoSuchAlgorithmException, NoSuchProviderException {
        assertTrue("Signature failed to verify, should have succeeded",
                SigningUtil.verifyWithURI(credAES, hmacAlgorithmURI, controlSignatureHMAC, data.getBytes()));
        
        SecretKey badKey = SecurityTestHelper.generateKey("AES", 128, null);
        Credential badCred = SecurityHelper.getSimpleCredential(badKey);
        
        assertFalse("Signature verified successfully, should have failed due to wrong verification key",
                SigningUtil.verifyWithURI(badCred, hmacAlgorithmURI, controlSignatureHMAC, data.getBytes()));
        
        String tamperedData = data + "HAHA All your base are belong to us";
        
        assertFalse("Signature verified successfully, should have failed due to tampered data",
                SigningUtil.verifyWithURI(credAES, hmacAlgorithmURI, controlSignatureHMAC, tamperedData.getBytes()));
        
    }
    
    private byte[] getControlSignature(byte[] data, SecretKey secretKey, String algorithm) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeyAES128);
        return mac.doFinal(data);
    }

    private byte[] getControlSignature(byte[] data, PrivateKey privateKey, String algorithm) 
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(algorithm);
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }
}
