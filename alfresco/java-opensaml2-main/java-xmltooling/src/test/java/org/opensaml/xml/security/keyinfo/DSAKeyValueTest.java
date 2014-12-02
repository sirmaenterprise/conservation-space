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

import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.provider.DSAKeyValueProvider;
import org.opensaml.xml.signature.KeyInfo;


/**
 * Test resolution of credentials from RSAKeyValue child of KeyInfo.
 */
public class DSAKeyValueTest extends XMLObjectBaseTestCase {
    
    private KeyInfoCredentialResolver resolver;
    
    private String keyInfoFile;
    
    private DSAPublicKey pubKey;
    
    private final String dsaBase64 = 
        "MIIDOjCCAi0GByqGSM44BAEwggIgAoIBAQCWV7IK073aK2C3yggy69qXkxCw30j5" +
        "Ig0s1/GHgq5jEZf8FTGVpehX5qaYlRC3TBMSN4WAgkG+nFnsjHb6kIYkayV8ZVvI" +
        "IgEBCeaZg016f90G+Rre5C38G3OwsODKjPsVZCV5YQ9rm6lWMOfMRSUzJuFA0fdx" +
        "RLssAfKLI5JmzupliO2iH5FU3+dQr0UvcPwPjjRDA9JIi3ShKdmq9f/SzRM9AJPs" +
        "sjc0v4lRVMKWkTHLjbRH2XiOxsok/oL7NVTJ9hvd3xqi1/O3MM2pNhYaQoA0kLqq" +
        "sr006dNftgo8n/zrBFMC6iP7tmxhuRxgXXkNo5xiQCvAX7HsGno4y9ilAhUAjKlv" +
        "CQhbGeQo3fWbwVJMdokSK5ECggEAfERqa+S8UwjuvNxGlisuBGzR7IqqHSQ0cjFI" +
        "BD61CkYh0k0Y9am6ZL2jiAkRICdkW6f9lmGy0HidCwC56WeAYpLyfJslBAjC4r0t" +
        "6U8a822fECVcbsPNLDULoQG0KjVRtYfFH5GedNQ8LRkG8b+XIe4G74+vXOatVu8L" +
        "9QXQKYx9diOAHx8ghpt1pC0UAqPzAgVGNWIPQ+VO7WEYOYuVw+/uFoHiaU1OZOTF" +
        "C4VXk2+33AasT4i6It7DIESp+ye9lPnNU6nLEBNSnXdnBgaH27m8QnFRTfrjimiG" +
        "BwBTQvbjequRvM5dExfUqyCd2BUOK1lbaQmjZnCMH6k3ZFiAYgOCAQUAAoIBAGnD" +
        "wMuRoRGJHUhjjeePKwP9BgCc7dtKlB7QMnIHGPv03hdVPo9ezaQ5mFxdzQdXoLR2" +
        "BFucDtSj1je3e5L9KEnHZ5fHnislBnzSvYR5V8LwTa5mbNS4VHkAv8Eh3WG9tp1S" +
        "/f9ymefKHB7ISlskT7kODCIbr5HHU/n1zXtMRjoslY1A+nFlWiAaIvjnj/C8x0BW" +
        "BkhuSKX/2PbljnmIdGV7mJK9/XUHnyKgZBxXEul2mlvGkrgUvyv+qYsCFsKSSrkB" +
        "1Mj2Ql5xmTMaePMEmvOr6fDAP0OH8cvADEZjx0s/5vvoBFPGGmPrHJluEVS0Fu8I" +
        "9sROg9YjyuhRV0b8xHo=";

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        List<KeyInfoProvider> providers = new ArrayList<KeyInfoProvider>();
        providers.add(new DSAKeyValueProvider());
        resolver = new BasicProviderKeyInfoCredentialResolver(providers);
        keyInfoFile = "/data/org/opensaml/xml/security/keyinfo/DSAKeyValue.xml";
        pubKey = SecurityTestHelper.buildJavaDSAPublicKey(dsaBase64);
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
