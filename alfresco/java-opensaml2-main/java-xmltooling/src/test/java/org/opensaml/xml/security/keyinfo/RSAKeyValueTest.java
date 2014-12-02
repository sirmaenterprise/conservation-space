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

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.signature.KeyInfo;


/**
 * Test resolution of credentials from RSAKeyValue child of KeyInfo.
 */
public class RSAKeyValueTest extends XMLObjectBaseTestCase {
    
    private KeyInfoCredentialResolver resolver;
    
    private String keyInfoFile;
    
    private RSAPublicKey pubKey;
    
    private final String rsaBase64 = 
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw/WnsbA8frhQ+8EoPgMr" +
    "QjpINjt20U/MvsvmoAgQnAgEF4OYt9Vj9/2YvMO4NvX1fNDFzoYRyOMrypF7skAP" +
    "cITUhdcPSEpI4nsf5yFZLziK/tQ26RsccE7WhpGB8eHu9tfseelgyioorvmt+JCo" +
    "P15c5rYUuIfVC+eEsYolw344q6N61OACHETuySL0a1+GFu3WoISXte1pQIst7HKv" +
    "BbHH41HEWAxT6e0hlD5PyKL4lBJadGHXg8Zz4r2jV2n6+Ox7raEWmtVCGFxsAoCR" +
    "alu6nvs2++5Nnb4C1SE640esfYhfeMd5JYfsTNMaQ8sZLpsWdglAGpa/Q87K19LI" +
    "wwIDAQAB";

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        List<KeyInfoProvider> providers = new ArrayList<KeyInfoProvider>();
        providers.add(new RSAKeyValueProvider());
        resolver = new BasicProviderKeyInfoCredentialResolver(providers);
        keyInfoFile = "/data/org/opensaml/xml/security/keyinfo/RSAKeyValue.xml";
        pubKey = SecurityTestHelper.buildJavaRSAPublicKey(rsaBase64);
    }
    
    /**
     * Test basic credential resolution.
     * 
     * @throws SecurityException on error resolving credentials
     */
    public void testCredResolution() throws SecurityException {
        KeyInfo keyInfo = (KeyInfo) unmarshallElement(keyInfoFile);
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Iterator<Credential> iter = resolver.resolve(criteriaSet).iterator();
        
        assertTrue("No credentials were found", iter.hasNext());
        
        Credential credential = iter.next();
        assertNotNull("Credential was null", credential);
        assertFalse("Too many credentials returned", iter.hasNext());
        assertTrue("Credential is not of the expected type", credential instanceof BasicCredential);
        
        
        assertNotNull("Public key was null", credential.getPublicKey());
        assertEquals("Expected public key value not found", pubKey, credential.getPublicKey());
        
        assertEquals("Wrong number of key names", 2, credential.getKeyNames().size());
        assertTrue("Expected key name value not found", credential.getKeyNames().contains("Foo"));
        assertTrue("Expected key name value not found", credential.getKeyNames().contains("Bar"));
    }
    

}
