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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.CollectionCredentialResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.signature.KeyInfo;

/**
 * Test the local credential resolver specialization of the KeyInfo credential resolver.
 */
public class LocalKeyInfoCredentialResolverTest extends XMLObjectBaseTestCase {
    
    private String keyName;
    private KeyPair keyPair;
    private BasicCredential localCred;
    
    private CollectionCredentialResolver localCredResolver;
    private LocalKeyInfoCredentialResolver keyInfoResolver;
    
    private KeyInfo keyInfo;

    protected void setUp() throws Exception {
        super.setUp();
        
        keyName = "MyKey";
        keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        
        localCred = new BasicCredential();
        localCred.setPublicKey(keyPair.getPublic());
        localCred.setPrivateKey(keyPair.getPrivate());
        localCred.getKeyNames().add(keyName);
        
        localCredResolver = new CollectionCredentialResolver();
        localCredResolver.getCollection().add(localCred);
        
        ArrayList<KeyInfoProvider> providers = new ArrayList<KeyInfoProvider>();
        providers.add( new RSAKeyValueProvider() );
        keyInfoResolver = new LocalKeyInfoCredentialResolver(providers, localCredResolver);
        
        keyInfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
    }
    
    public void testKeyInfoWithKeyName() throws SecurityException {
        KeyInfoHelper.addKeyName(keyInfo, keyName);
        
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Credential resolvedCred = keyInfoResolver.resolveSingle(criteriaSet);
        
        assertEquals("Unexpected local credential resolved", localCred, resolvedCred);
    }

    public void testKeyInfoWithKnownPublicKey() throws SecurityException {
        KeyInfoHelper.addPublicKey(keyInfo, keyPair.getPublic());
        
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Credential resolvedCred = keyInfoResolver.resolveSingle(criteriaSet);
        
        assertEquals("Unexpected local credential resolved", localCred, resolvedCred);
    }
    
    public void testKeyInfoWithUnknownPublicKey() throws SecurityException, IllegalArgumentException,
        NoSuchAlgorithmException, NoSuchProviderException {
        
        KeyInfoHelper.addPublicKey(keyInfo, 
                SecurityTestHelper.generateKeyPair("RSA", 1024, null).getPublic());
        
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Credential resolvedCred = keyInfoResolver.resolveSingle(criteriaSet);
        
        assertNull("Expected no credential to be resolved", resolvedCred);
    }
    
}
