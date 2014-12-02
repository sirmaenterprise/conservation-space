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

package org.opensaml.xml.security.x509;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.PublicKey;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.X509IssuerSerial;
import org.opensaml.xml.signature.X509SKI;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.DatatypeHelper;

/**
 * Tests the factory and impl for X509KeyInfoGenerator.
 */
public class X509KeyInfoGeneratorTest extends XMLObjectBaseTestCase {
    
    private BasicX509Credential credential;
    
    private X509KeyInfoGeneratorFactory factory;
    private KeyInfoGenerator generator;
    
    private String keyNameFoo = "FOO";
    private String keyNameBar = "BAR";
    private String entityID = "someEntityID";
    
    private PublicKey pubKey;
    
    private X509Certificate entityCert;
    private String entityCertBase64 = 
        "MIIDzjCCAragAwIBAgIBMTANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyMTE4MjM0MFoX" +
        "DTE3MDUxODE4MjM0MFowMTESMBAGA1UEChMJSW50ZXJuZXQyMRswGQYDVQQDExJm" +
        "b29iYXIuZXhhbXBsZS5vcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB" +
        "AQDNWnkFmhy1vYa6gN/xBRKkZxFy3sUq2V0LsYb6Q3pe9Qlb6+BzaM5DrN8uIqqr" +
        "oBE3Wp0LtrgKuQTpDpNFBdS2p5afiUtOYLWBDtizTOzs3Z36MGMjIPUYQ4s03IP3" +
        "yPh2ud6EKpDPiYqzNbkRaiIwmYSit5r+RMYvd6fuKvTOn6h7PZI5AD7Rda7VWh5O" +
        "VSoZXlRx3qxFho+mZhW0q4fUfTi5lWwf4EhkfBlzgw/k5gf4cOi6rrGpRS1zxmbt" +
        "X1RAg+I20z6d04g0N2WsK5stszgYKoIROJCiXwjraa8/SoFcILolWQpttVHBIUYl" +
        "yDlm8mIFleZf4ReFpfm+nUYxAgMBAAGjgfQwgfEwCQYDVR0TBAIwADAsBglghkgB" +
        "hvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYE" +
        "FDgRgTkjaKoK6DoZfUZ4g9LDJUWuMFUGA1UdIwROMEyAFNXuZVPeUdqHrULqQW7y" +
        "r9buRpQLoTGkLzAtMRIwEAYDVQQKEwlJbnRlcm5ldDIxFzAVBgNVBAMTDmNhLmV4" +
        "YW1wbGUub3JnggEBMEAGA1UdEQQ5MDeCEmFzaW1vdi5leGFtcGxlLm9yZ4YbaHR0" +
        "cDovL2hlaW5sZWluLmV4YW1wbGUub3JnhwQKAQIDMA0GCSqGSIb3DQEBBQUAA4IB" +
        "AQBLiDMyQ60ldIytVO1GCpp1S1sKJyTF56GVxHh/82hiRFbyPu+2eSl7UcJfH4ZN" +
        "bAfHL1vDKTRJ9zoD8WRzpOCUtT0IPIA/Ex+8lFzZmujO10j3TMpp8Ii6+auYwi/T" +
        "osrfw1YCxF+GI5KO49CfDRr6yxUbMhbTN+ssK4UzFf36UbkeJ3EfDwB0WU70jnlk" +
        "yO8f97X6mLd5QvRcwlkDMftP4+MB+inTlxDZ/w8NLXQoDW6p/8r91bupXe0xwuyE" +
        "vow2xjxlzVcux2BZsUZYjBa07ZmNNBtF7WaQqH7l2OBCAdnBhvme5i/e0LK3Ivys" +
        "+hcVyvCXs5XtFTFWDAVYvzQ6";


    
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
    
    private String subjectCN;
    private X500Principal subjectName;
    private X500Principal issuerName;
    private BigInteger serialNumber;
    private byte[] subjectKeyIdentifier;
    
    private X509Certificate altNameCert;
    private String altName1, altName2, altName3;
    private Integer altName1Type, altName2Type, altName3Type;
    
    private X509CRL caCRL;
    private String caCRLBase64 =
        "MIIBmjCBgwIBATANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRlcm5ldDIx" +
        "FzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnFw0wNzA1MjEwNTAwMzNaFw0wNzA2MjAw" +
        "NTAwMzNaMCIwIAIBKxcNMDcwNTIxMDQ1ODI5WjAMMAoGA1UdFQQDCgEBMA0GCSqG" +
        "SIb3DQEBBQUAA4IBAQAghL5eW9NsMRCk84mAZ+QMjoCuy7zZJr5vPHk7WrOffL7B" +
        "GWZ6u6D1cSCzZNvrBolip1yb8KSdB9PJqEV1kInXnZegeqjENq+9j8nGdyoYuofh" +
        "A5AU8L9n9fjwYTUkfNfAMWeVVuplJN4yAp03JSJULVqmC63EEP7u7kFS94Mze9sa" +
        "+VqBu7tGyZ55XX8AO39d1c3DoHIPfS1wHHLyuWxnys8GjANJxQiZmFtUfPztp3qH" +
        "/XlfFLgY5EBTanyOk5yycU/l+6P1RBhJZDPicp3iWVsjYHYWS+ovdyWuL7RrLRMb" +
        "zecnCa5eIhSevoMYUkg4h9ckAZUQeHsK08gB/dFh";


    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        factory = new X509KeyInfoGeneratorFactory();
        generator = null;
        
        entityCert = SecurityTestHelper.buildJavaX509Cert(entityCertBase64);
        pubKey = entityCert.getPublicKey();
        caCert = SecurityTestHelper.buildJavaX509Cert(caCertBase64);
        caCRL = SecurityTestHelper.buildJavaX509CRL(caCRLBase64);
        
        subjectCN = "foobar.example.org";
        subjectName = new X500Principal("cn=foobar.example.org, O=Internet2");
        issuerName = new X500Principal("cn=ca.example.org, O=Internet2");
        serialNumber = new BigInteger("49");
        subjectKeyIdentifier = Base64.decode(entityCertSKIBase64);
        
        altName1 = "asimov.example.org";
        altName1Type = X509Util.DNS_ALT_NAME;
        altName2 = "http://heinlein.example.org";
        altName2Type = X509Util.URI_ALT_NAME;
        altName3 = "10.1.2.3";
        altName3Type = X509Util.IP_ADDRESS_ALT_NAME;
        
        credential = new BasicX509Credential();
        credential.setEntityId(entityID);
        credential.getKeyNames().add(keyNameFoo);
        credential.getKeyNames().add(keyNameBar);
        credential.setEntityCertificate(entityCert);
        
        List<X509Certificate> chain = new ArrayList<X509Certificate>();
        chain.add(entityCert);
        chain.add(caCert);
        credential.setEntityCertificateChain(chain);
        
        List<X509CRL> crls = new ArrayList<X509CRL>();
        crls.add(caCRL);
        credential.setCRLs(crls);
    }
    
    /**
     * Test no options - should produce null KeyInfo.
     * @throws SecurityException
     */
    public void testNoOptions() throws SecurityException {
        // all options false by default
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo with no options should have been null", keyInfo);
    }
    
    /**
     * Test emit public key.
     * @throws SecurityException
     */
    public void testEmitPublicKey() throws SecurityException, KeyException {
        factory.setEmitPublicKeyValue(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyInfo children", 1, keyInfo.getOrderedChildren().size());
        assertEquals("Unexpected number of KeyValue elements", 1, keyInfo.getKeyValues().size());
        PublicKey generatedKey = KeyInfoHelper.getKey(keyInfo.getKeyValues().get(0));
        assertEquals("Unexpected key value", pubKey, generatedKey);
    }
    
    /**
     * Test emit credential key names.
     * @throws SecurityException
     */
    public void testEmitKeynames() throws SecurityException {
        factory.setEmitKeyNames(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 2, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameFoo));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameBar));
    }
    
    /**
     * Test emit entity ID as key name.
     * @throws SecurityException
     */
    public void testEmitEntityIDAsKeyName() throws SecurityException {
        factory.setEmitEntityIDAsKeyName(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 1, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(entityID));
    }
    
    /**
     * Test emit entity cert.
     * @throws SecurityException
     * @throws CertificateException 
     */
    public void testEmitEntityCert() throws SecurityException, CertificateException {
        factory.setEmitEntityCertificate(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509Certificate elements", 1, x509Data.getX509Certificates().size());
        List<X509Certificate> certs = KeyInfoHelper.getCertificates(x509Data);
        assertEquals("Unexpected certificate value found", entityCert, certs.get(0));
    }
    
    /**
     * Test emit entity cert chain in X509Data.
     * @throws SecurityException
     * @throws CertificateException 
     */
    public void testEmitEntityCertChain() throws SecurityException, CertificateException {
        factory.setEmitEntityCertificateChain(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509Certificate elements", 2, x509Data.getX509Certificates().size());
        List<X509Certificate> certs = KeyInfoHelper.getCertificates(x509Data);
        assertTrue("Expected certificate value not found", certs.contains(entityCert));
        assertTrue("Expected certificate value not found", certs.contains(caCert));
    }
    
    /**
     * Test combo options of cert and chain - don't emit duplicate of entity cert.
     * @throws SecurityException
     * @throws CertificateException 
     */
    public void testEmitCertAndChainCombo() throws SecurityException, CertificateException {
        factory.setEmitEntityCertificate(true);
        factory.setEmitEntityCertificateChain(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509Certificate elements", 2, x509Data.getX509Certificates().size());
        List<X509Certificate> certs = KeyInfoHelper.getCertificates(x509Data);
        assertTrue("Expected certificate value not found", certs.contains(entityCert));
        assertTrue("Expected certificate value not found", certs.contains(caCert));
    }
    
    /**
     * Test emit CRLs.
     * @throws SecurityException
     * @throws CRLException 
     */
    public void testEmitCRLs() throws SecurityException, CRLException {
        factory.setEmitCRLs(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509CRL elements", 1, x509Data.getX509CRLs().size());
        X509CRL crl = KeyInfoHelper.getCRL(x509Data.getX509CRLs().get(0));
        assertEquals("Unexpected CRL value found", caCRL, crl);
    }
    
    /**
     * Test emit subject name in X509Data.
     * @throws SecurityException
     */
    public void testEmitX509SubjectName() throws SecurityException {
        factory.setEmitX509SubjectName(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509SubjectName elements", 1, x509Data.getX509SubjectNames().size());
        String name = DatatypeHelper.safeTrimOrNullString(x509Data.getX509SubjectNames().get(0).getValue());
        assertEquals("Unexpected X509SubjectName value found", subjectName, new X500Principal(name));
    }
    
    /**
     * Test emit issuer name and serial number in X509Data.
     * @throws SecurityException
     */
    public void testEmitX509IssuerSerial() throws SecurityException {
        factory.setEmitX509IssuerSerial(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509IssuerSerial elements", 1, x509Data.getX509IssuerSerials().size());
        X509IssuerSerial issuerSerial = x509Data.getX509IssuerSerials().get(0);
        assertNotNull("X509IssuerName not present", issuerSerial.getX509IssuerName());
        assertNotNull("X509SerialNumber not present", issuerSerial.getX509SerialNumber());
        String name = DatatypeHelper.safeTrimOrNullString(issuerSerial.getX509IssuerName().getValue());
        assertEquals("Unexpected X509IssuerName value found", issuerName, new X500Principal(name));
        BigInteger number = issuerSerial.getX509SerialNumber().getValue();
        assertEquals("Unexpected serial number value found", serialNumber, number);
    }
    
    /**
     * Test emit subject key identifier in X509Data.
     * @throws SecurityException
     */
    public void testEmitX509SKI() throws SecurityException {
        factory.setEmitX509SKI(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of X509Data elements", 1, keyInfo.getX509Datas().size());
        X509Data x509Data = keyInfo.getX509Datas().get(0);
        assertEquals("Unexpected number of X509SKI elements", 1, x509Data.getX509SKIs().size());
        X509SKI ski = x509Data.getX509SKIs().get(0);
        byte[] skiValue = Base64.decode(DatatypeHelper.safeTrimOrNullString(ski.getValue()));
        assertTrue("Unexpected SKI value found", Arrays.equals(subjectKeyIdentifier, skiValue));
    }
    
    /**
     * Test emit subject DN as key name.
     * @throws SecurityException
     */
    public void testEmitSubjectDNAsKeyName() throws SecurityException {
        factory.setEmitSubjectDNAsKeyName(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 1, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        String name = DatatypeHelper.safeTrimOrNullString(keyNames.get(0));
        assertEquals("Unexpected subject DN key name value found", subjectName, new X500Principal(name));
    }
    
    /**
     * Test emit subject CN as key name.
     * @throws SecurityException
     */
    public void testEmitSubjectCNAsKeyName() throws SecurityException {
        factory.setEmitSubjectCNAsKeyName(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 1, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(subjectCN));
    }
    
    /**
     * Test emit subject alt names as key names.
     * @throws SecurityException
     * @throws CertificateParsingException 
     */
    public void testEmitSubjectAltNamesAsKeyNames() throws SecurityException, CertificateParsingException {
        factory.setEmitSubjectAltNamesAsKeyNames(true);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        // Haven't set any alt name types yet, so expect no output
        assertNull("Generated KeyInfo was not null", keyInfo);
        
        // Just a sanity check
        assertNotNull("Credential entity cert's Java native getSubjectAltenativeNames() was null", 
                credential.getEntityCertificate().getSubjectAlternativeNames());
        
        factory.getSubjectAltNames().add(altName1Type);
        
        generator = factory.newInstance();
        keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 1, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName1));
        
        factory.getSubjectAltNames().add(altName2Type);
        factory.getSubjectAltNames().add(altName3Type);
        
        generator = factory.newInstance();
        keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 3, keyInfo.getKeyNames().size());
        keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName1));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName2));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName3));
    }
    
    /**
     * Test emitting combinations of key names.
     * @throws SecurityException
     */
    public void testEmitKeyNamesCombo() throws SecurityException {
        factory.setEmitKeyNames(true);
        factory.setEmitEntityIDAsKeyName(true);
        factory.setEmitSubjectCNAsKeyName(true);
        
        factory.setEmitSubjectAltNamesAsKeyNames(true);
        factory.getSubjectAltNames().add(altName1Type);
        factory.getSubjectAltNames().add(altName2Type);
        factory.getSubjectAltNames().add(altName3Type);
        
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        
        assertEquals("Unexpected number of KeyName elements", 7, keyInfo.getKeyNames().size());
        List<String> keyNames = KeyInfoHelper.getKeyNames(keyInfo);
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameFoo));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(keyNameBar));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(entityID));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(subjectCN));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName1));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName2));
        assertTrue("Failed to find expected KeyName value", keyNames.contains(altName3));
    }
    
    /** Test that the options passed to the generator are really cloned. 
     * After newInstance() is called, changes to the factory options should not be 
     * reflected in the generator.
     * @throws SecurityException */
    public void testProperOptionsCloning() throws SecurityException {
        generator = factory.newInstance();
        KeyInfo keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo was null", keyInfo);
        
        factory.setEmitKeyNames(true);
        factory.setEmitEntityIDAsKeyName(true);
        factory.setEmitPublicKeyValue(true);
        
        keyInfo = generator.generate(credential);
        
        assertNull("Generated KeyInfo was null", keyInfo);
        
        generator = factory.newInstance();
        keyInfo = generator.generate(credential);
        
        assertNotNull("Generated KeyInfo was null", keyInfo);
        assertNotNull("Generated KeyInfo children list was null", keyInfo.getOrderedChildren());
        assertEquals("Unexpected # of KeyInfo children found", 4, keyInfo.getOrderedChildren().size());
    }

}
