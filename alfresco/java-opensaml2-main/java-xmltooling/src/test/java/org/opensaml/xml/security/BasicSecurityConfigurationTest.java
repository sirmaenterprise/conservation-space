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

import java.security.KeyPair;

import junit.framework.TestCase;

import org.apache.xml.security.Init;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignatureConstants;

/**
 * Testing some aspects of the basic security config impl.
 */
public class BasicSecurityConfigurationTest extends TestCase {
    
    private BasicSecurityConfiguration config;
    
    private Credential rsaCred;
    private Credential aes128Cred;

    protected void setUp() throws Exception {
        super.setUp();
        if (!Init.isInitialized()) {
            Init.init();
        }
        
        KeyPair kp = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        rsaCred = SecurityHelper.getSimpleCredential(kp.getPublic(), kp.getPrivate());
        aes128Cred = SecurityTestHelper.generateKeyAndCredential(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        
        config = new BasicSecurityConfiguration();
    }
    
    public void testGetSigAlgURI() {
        assertNull(config.getSignatureAlgorithmURI("RSA"));
        assertNull(config.getSignatureAlgorithmURI(rsaCred));
        
        config.registerSignatureAlgorithmURI("RSA", SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, config.getSignatureAlgorithmURI("RSA"));
        assertEquals(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, config.getSignatureAlgorithmURI(rsaCred));
    }

    public void testGetDataEncURI() {
        assertNull(config.getDataEncryptionAlgorithmURI("AES", 128));
        assertNull(config.getDataEncryptionAlgorithmURI("AES", 256));
        assertNull(config.getDataEncryptionAlgorithmURI("AES", null));
        
        config.registerDataEncryptionAlgorithmURI("AES", 128, EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        config.registerDataEncryptionAlgorithmURI("AES", 256, EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256);
        config.registerDataEncryptionAlgorithmURI("AES", null, EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256);
        
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128, 
                config.getDataEncryptionAlgorithmURI("AES", 128));
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256, 
                config.getDataEncryptionAlgorithmURI("AES", 256));
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256, 
                config.getDataEncryptionAlgorithmURI("AES", null));
        
        assertEquals(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128, 
                config.getDataEncryptionAlgorithmURI(aes128Cred));
    }
    
    public void testGetKeyTransportEncURI() {
        assertNull(config.getKeyTransportEncryptionAlgorithmURI("RSA", null, "AES"));
        assertNull(config.getKeyTransportEncryptionAlgorithmURI("RSA", null, "DESede"));
        assertNull(config.getKeyTransportEncryptionAlgorithmURI("RSA", null, null));
        assertNull(config.getKeyTransportEncryptionAlgorithmURI("AES", 256, "AES"));
        assertNull(config.getKeyTransportEncryptionAlgorithmURI("AES", 256, "DESede"));
        
        config.registerKeyTransportEncryptionAlgorithmURI("RSA", null, "AES", 
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        config.registerKeyTransportEncryptionAlgorithmURI("RSA", null, "DESede", 
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
        config.registerKeyTransportEncryptionAlgorithmURI("RSA", null, null, 
                EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15);
        config.registerKeyTransportEncryptionAlgorithmURI("AES", 128, null, 
                EncryptionConstants.ALGO_ID_KEYWRAP_AES128);
        config.registerKeyTransportEncryptionAlgorithmURI("AES", 256, null, 
                EncryptionConstants.ALGO_ID_KEYWRAP_AES256);
        config.registerKeyTransportEncryptionAlgorithmURI("AES", null, "AES", 
                EncryptionConstants.ALGO_ID_KEYWRAP_AES128);
        config.registerKeyTransportEncryptionAlgorithmURI("AES", null, "DESede", 
                EncryptionConstants.ALGO_ID_KEYWRAP_AES256);
        
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP, 
                config.getKeyTransportEncryptionAlgorithmURI("RSA", null, "AES"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15, 
                config.getKeyTransportEncryptionAlgorithmURI("RSA", null, "DESede"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15, 
                config.getKeyTransportEncryptionAlgorithmURI("RSA", null, "FOO"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15, 
                config.getKeyTransportEncryptionAlgorithmURI("RSA", null, null));
        
        assertEquals(EncryptionConstants.ALGO_ID_KEYWRAP_AES128,
                config.getKeyTransportEncryptionAlgorithmURI("AES", 128, null));
        assertEquals(EncryptionConstants.ALGO_ID_KEYWRAP_AES256,
                config.getKeyTransportEncryptionAlgorithmURI("AES", 256, null));
        assertEquals(EncryptionConstants.ALGO_ID_KEYWRAP_AES128,
                config.getKeyTransportEncryptionAlgorithmURI("AES", null, "AES"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYWRAP_AES256,
                config.getKeyTransportEncryptionAlgorithmURI("AES", null, "DESede"));
        
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP, 
                config.getKeyTransportEncryptionAlgorithmURI(rsaCred, "AES"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15, 
                config.getKeyTransportEncryptionAlgorithmURI(rsaCred, "DESede"));
        assertEquals(EncryptionConstants.ALGO_ID_KEYWRAP_AES128,
                config.getKeyTransportEncryptionAlgorithmURI(aes128Cred, null));
        
    }
}
