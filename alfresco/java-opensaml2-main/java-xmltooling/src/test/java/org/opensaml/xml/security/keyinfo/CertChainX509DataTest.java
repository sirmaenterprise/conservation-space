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

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.provider.RSAKeyValueProvider;
import org.opensaml.xml.security.keyinfo.provider.InlineX509DataProvider;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.util.Base64;


/**
 * Test resolution of credentials from X509Data child of KeyInfo,
 * where the X509Data contains various identifiers for the entity cert
 * within a cert chain.
 */
public class CertChainX509DataTest extends XMLObjectBaseTestCase {
    
    private KeyInfoCredentialResolver resolver;
    
    private RSAPublicKey pubKey;
    private final String rsaBase64 = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzVp5BZoctb2GuoDf8QUS" +
        "pGcRct7FKtldC7GG+kN6XvUJW+vgc2jOQ6zfLiKqq6ARN1qdC7a4CrkE6Q6TRQXU" +
        "tqeWn4lLTmC1gQ7Ys0zs7N2d+jBjIyD1GEOLNNyD98j4drnehCqQz4mKszW5EWoi" +
        "MJmEorea/kTGL3en7ir0zp+oez2SOQA+0XWu1VoeTlUqGV5Ucd6sRYaPpmYVtKuH" +
        "1H04uZVsH+BIZHwZc4MP5OYH+HDouq6xqUUtc8Zm7V9UQIPiNtM+ndOINDdlrCub" +
        "LbM4GCqCETiQol8I62mvP0qBXCC6JVkKbbVRwSFGJcg5ZvJiBZXmX+EXhaX5vp1G" +
        "MQIDAQAB";
    
    private X509Certificate entityCert;
    private String entityCertBase64 = 
        "MIIDjDCCAnSgAwIBAgIBKjANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDQwOTA2MTIwOVoX" +
        "DTE3MDQwNjA2MTIwOVowMTESMBAGA1UEChMJSW50ZXJuZXQyMRswGQYDVQQDExJm" +
        "b29iYXIuZXhhbXBsZS5vcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB" +
        "AQDNWnkFmhy1vYa6gN/xBRKkZxFy3sUq2V0LsYb6Q3pe9Qlb6+BzaM5DrN8uIqqr" +
        "oBE3Wp0LtrgKuQTpDpNFBdS2p5afiUtOYLWBDtizTOzs3Z36MGMjIPUYQ4s03IP3" +
        "yPh2ud6EKpDPiYqzNbkRaiIwmYSit5r+RMYvd6fuKvTOn6h7PZI5AD7Rda7VWh5O" +
        "VSoZXlRx3qxFho+mZhW0q4fUfTi5lWwf4EhkfBlzgw/k5gf4cOi6rrGpRS1zxmbt" +
        "X1RAg+I20z6d04g0N2WsK5stszgYKoIROJCiXwjraa8/SoFcILolWQpttVHBIUYl" +
        "yDlm8mIFleZf4ReFpfm+nUYxAgMBAAGjgbIwga8wCQYDVR0TBAIwADAsBglghkgB" +
        "hvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYE" +
        "FDgRgTkjaKoK6DoZfUZ4g9LDJUWuMFUGA1UdIwROMEyAFNXuZVPeUdqHrULqQW7y" +
        "r9buRpQLoTGkLzAtMRIwEAYDVQQKEwlJbnRlcm5ldDIxFzAVBgNVBAMTDmNhLmV4" +
        "YW1wbGUub3JnggEBMA0GCSqGSIb3DQEBBQUAA4IBAQCPj3Si4Eiw9abNgPBUhBXW" +
        "d6eRYlIHaHcnez6j6g7foAOyuVIUso9Q5c6pvL87lmasK55l09YPXw1qmiH+bHMc" +
        "rwEPODpLx7xd3snlOCi7FyxahxwSs8yfTu8Pq95rWt0LNcfHxQK938Cpnav6jgDo" +
        "2uH/ywAOFFSnoBzGHAfScHMfj8asZ6THosYsklII7FSU8j49GV2utkvGB3mcu4ST" +
        "uLdeRCZmi93vq1D4JVGsXC4UaHjg114+a+9q0XZdz6a1UW4pt1ryXIPotCS62M71" +
        "pkJf5neHUinKAqgoRfPXowudZg1Zl8DjzoOBn+MNHRrR5KYbVGvdHcxoJLCwVB/v";
    
    private String entityCertSKIBase64 = "OBGBOSNoqgroOhl9RniD0sMlRa4=";


    private X509Certificate caCert;
    private String caCertBase64 = 
        "MIIDXTCCAkWgAwIBAgIBATANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDQwOTA1NDcxMloX" +
        "DTE3MDQwNjA1NDcxMlowLTESMBAGA1UEChMJSW50ZXJuZXQyMRcwFQYDVQQDEw5j" +
        "YS5leGFtcGxlLm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANxM" +
        "5/6mBCcX+S7HApcKtfqdFRZzi6Ra91nkEzXOUcO+BPUdYqSxKGnCCso25ZOZP3gn" +
        "JVkY8Pi7VWrCM6wRgIMyQDvNYqCpNjkZGFkrMoa6fm8BSaDHJ1fz6l/eEl0CVU3U" +
        "uUAf0mXQLGm6Jannq8aMolRujlhE5iRaOJ2qp6wqsvyatK+vTgDngnwYVa4Cqu0j" +
        "UeNF28quST5D3gIuZ0OeFHSM2Z1WUKkwwsHqVkxBBcH1QE1JOGIoSnrxxl/o4VlL" +
        "WGEI8zq5qixE8VYtBBmijBwIL5ETy2fwiqcsvimQaQAtAfbtpO3kBSs8n7nnzMUH" +
        "fRlcebGkwwcNfYcD5hcCAwEAAaOBhzCBhDAdBgNVHQ4EFgQU1e5lU95R2oetQupB" +
        "bvKv1u5GlAswVQYDVR0jBE4wTIAU1e5lU95R2oetQupBbvKv1u5GlAuhMaQvMC0x" +
        "EjAQBgNVBAoTCUludGVybmV0MjEXMBUGA1UEAxMOY2EuZXhhbXBsZS5vcmeCAQEw" +
        "DAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOCAQEAbqrozetM/iBVIUir9k14" +
        "HbLEP0lZ6jOPWFIUFXMOn0t8+Ul7PMp9Rdn+7OGQIoJw97/mam3kpQ/HmjJMFYv6" +
        "MGsDQ4vAOiQiaTKzgMhrnCdVpVH9uz4ARHiysCujnjH9sehTWgybY8pXzzSG5BAj" +
        "EGowHq01nXxq2K4yAJSdAUBYLfuSKW1uRU6cmEa9uzl9EvoZfAF3BLnGlPqu4Zaj" +
        "H2NC9ZY0y19LX4yeJLHL1sY4fyxb3x8QhcCXiI16awBTr/VnUpJjSe9vh+OudWGe" +
        "yCL/KhjlrDkjJ3hIxBF5mP/Y27cFpRnC2gECkieURvh52OyuqkzpbOrTN5rD9fNi" +
        "nA==";
    
    private X500Principal subjectName;
    private X500Principal issuerName;
    private int serialNumber;
    private byte[] subjectKeyIdentifier;



    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        List<KeyInfoProvider> providers = new ArrayList<KeyInfoProvider>();
        providers.add(new InlineX509DataProvider());
        providers.add(new RSAKeyValueProvider());
        resolver = new BasicProviderKeyInfoCredentialResolver(providers);
        
        pubKey = SecurityTestHelper.buildJavaRSAPublicKey(rsaBase64);
        entityCert = SecurityTestHelper.buildJavaX509Cert(entityCertBase64);
        caCert = SecurityTestHelper.buildJavaX509Cert(caCertBase64);
        
        subjectName = new X500Principal("cn=foobar.example.org, O=Internet2");
        issuerName = new X500Principal("cn=ca.example.org, O=Internet2");
        serialNumber = 42;
        subjectKeyIdentifier = Base64.decode(entityCertSKIBase64);
    }
    
    /**
     * Test resolution with multiple certs, end-entity cert identified by KeyValue.
     * 
     * @throws SecurityException on error resolving credentials
     */
    public void testResolutionWithKeyValue() throws SecurityException {
        KeyInfo keyInfo = 
            (KeyInfo) unmarshallElement("/data/org/opensaml/xml/security/keyinfo/X509CertificatesWithKeyValue.xml");
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Iterator<Credential> iter = resolver.resolve(criteriaSet).iterator();
        
        assertTrue("No credentials were found", iter.hasNext());
        
        Credential credential = iter.next();
        assertNotNull("Credential was null", credential);
        assertFalse("Too many credentials returned", iter.hasNext());
        
        assertTrue("Credential is not of the expected type", credential instanceof X509Credential);
        X509Credential x509Credential = (X509Credential) credential;
        
        assertNotNull("Public key was null", x509Credential.getPublicKey());
        assertEquals("Expected public key value not found", pubKey, x509Credential.getPublicKey());
        
        assertEquals("Wrong number of key names", 2, x509Credential.getKeyNames().size());
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Foo"));
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Bar"));
        
        assertNotNull("Entity certificate was null", x509Credential.getEntityCertificate());
        assertEquals("Expected X509Certificate value not found", entityCert, x509Credential.getEntityCertificate());
        
        assertEquals("Wrong number of certs in cert chain found", 2, x509Credential.getEntityCertificateChain().size());
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(entityCert));
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(caCert));
    }
    
    /**
     * Test resolution with multiple certs, end-entity cert identified by X509SubjectName.
     * 
     * @throws SecurityException on error resolving credentials
     */
    public void testResolutionWithSubjectName() throws SecurityException {
        KeyInfo keyInfo = 
            (KeyInfo) unmarshallElement("/data/org/opensaml/xml/security/keyinfo/X509CertificatesWithSubjectName.xml");
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Iterator<Credential> iter = resolver.resolve(criteriaSet).iterator();
        
        assertTrue("No credentials were found", iter.hasNext());
        
        Credential credential = iter.next();
        assertNotNull("Credential was null", credential);
        assertFalse("Too many credentials returned", iter.hasNext());
        
        assertTrue("Credential is not of the expected type", credential instanceof X509Credential);
        X509Credential x509Credential = (X509Credential) credential;
        
        assertNotNull("Public key was null", x509Credential.getPublicKey());
        assertEquals("Expected public key value not found", pubKey, x509Credential.getPublicKey());
        
        assertEquals("Wrong number of key names", 2, x509Credential.getKeyNames().size());
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Foo"));
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Bar"));
        
        assertNotNull("Entity certificate was null", x509Credential.getEntityCertificate());
        assertEquals("Expected X509Certificate value not found", entityCert, x509Credential.getEntityCertificate());
        
        assertEquals("Wrong number of certs in cert chain found", 2, x509Credential.getEntityCertificateChain().size());
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(entityCert));
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(caCert));
    }
    
    /**
     * Test resolution with multiple certs, end-entity cert identified by X509IssuerSerial.
     * 
     * @throws SecurityException on error resolving credentials
     */
    public void testResolutionWithIssuerSerial() throws SecurityException {
        KeyInfo keyInfo = 
            (KeyInfo) unmarshallElement("/data/org/opensaml/xml/security/keyinfo/X509CertificatesWithIssuerSerial.xml");
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Iterator<Credential> iter = resolver.resolve(criteriaSet).iterator();
        
        assertTrue("No credentials were found", iter.hasNext());
        
        Credential credential = iter.next();
        assertNotNull("Credential was null", credential);
        assertFalse("Too many credentials returned", iter.hasNext());
        
        assertTrue("Credential is not of the expected type", credential instanceof X509Credential);
        X509Credential x509Credential = (X509Credential) credential;
        
        assertNotNull("Public key was null", x509Credential.getPublicKey());
        assertEquals("Expected public key value not found", pubKey, x509Credential.getPublicKey());
        
        assertEquals("Wrong number of key names", 2, x509Credential.getKeyNames().size());
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Foo"));
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Bar"));
        
        assertNotNull("Entity certificate was null", x509Credential.getEntityCertificate());
        assertEquals("Expected X509Certificate value not found", entityCert, x509Credential.getEntityCertificate());
        
        assertEquals("Wrong number of certs in cert chain found", 2, x509Credential.getEntityCertificateChain().size());
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(entityCert));
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(caCert));
    }
    
    /**
     * Test resolution with multiple certs, end-entity cert identified by X509SubjectName.
     * 
     * @throws SecurityException on error resolving credentials
     */
    public void testResolutionWithSubjectKeyIdentifier() throws SecurityException {
        KeyInfo keyInfo = 
            (KeyInfo) unmarshallElement("/data/org/opensaml/xml/security/keyinfo/X509CertificatesWithSKI.xml");
        CriteriaSet criteriaSet = new CriteriaSet( new KeyInfoCriteria(keyInfo) );
        Iterator<Credential> iter = resolver.resolve(criteriaSet).iterator();
        
        assertTrue("No credentials were found", iter.hasNext());
        
        Credential credential = iter.next();
        assertNotNull("Credential was null", credential);
        assertFalse("Too many credentials returned", iter.hasNext());
        
        assertTrue("Credential is not of the expected type", credential instanceof X509Credential);
        X509Credential x509Credential = (X509Credential) credential;
        
        assertNotNull("Public key was null", x509Credential.getPublicKey());
        assertEquals("Expected public key value not found", pubKey, x509Credential.getPublicKey());
        
        assertEquals("Wrong number of key names", 2, x509Credential.getKeyNames().size());
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Foo"));
        assertTrue("Expected key name value not found", x509Credential.getKeyNames().contains("Bar"));
        
        assertNotNull("Entity certificate was null", x509Credential.getEntityCertificate());
        assertEquals("Expected X509Certificate value not found", entityCert, x509Credential.getEntityCertificate());
        
        assertEquals("Wrong number of certs in cert chain found", 2, x509Credential.getEntityCertificateChain().size());
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(entityCert));
        assertTrue("Cert not found in cert chain", x509Credential.getEntityCertificateChain().contains(caCert));
    }

}
