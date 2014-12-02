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

package org.opensaml.security;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.Configuration;
import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.xml.security.BasicSecurityConfiguration;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.w3c.dom.Document;

/**
 * Testing the metadata credential resolver.
 */
public class MetadataCredentialResolverTest extends BaseTestCase {
    
    private String idpRSAPubKeyName = "IDP-SSO-RSA-Key";
    private RSAPublicKey idpRSAPubKey;
    private String idpRSAPubKeyBase64 = 
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfCVgF2Lvhu0Q35FvmAVGMXc3i" +
        "1MojcqybcfVbfn0Tg/Aj5FvuAiDFg9KpGvMHDKdLOY+1xsKZqyIm58SFhW+5z51Y" +
        "pnblHGjuDtPtPbtspQ7pAOsknnvbKZrx7RGNOJyQZE3Qn88Y5ZBNzABusqNXjrWl" +
        "U9m4a+XNIFqM4YbJLwIDAQAB";
    
    private X509Certificate idpDSACert;
    private String idpDSACertBase64 = 
        "MIIECTCCAvGgAwIBAgIBMzANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyNTIwMTYxMVoX" +
        "DTE3MDUyMjIwMTYxMVowGjEYMBYGA1UEAxMPaWRwLmV4YW1wbGUub3JnMIIBtjCC" +
        "ASsGByqGSM44BAEwggEeAoGBAI+ktw7R9m7TxjaCrT2MHwWNQUAyXPrqbFCcu+DC" +
        "irr861U6R6W/GyqWdcy8/D1Hh/I1U94POQn5yfqVPpVH2ZRS4OMFndHWaoo9V5LJ" +
        "oXTXHiDYB3W4t9tn0fm7It0n7VoUI5C4y9LG32Hq+UIGF/ktNTmo//mEqLS6aJNd" +
        "bMFpAhUArmKGh0hcpmjukYArWcMRvipB4CMCgYBuCiCrUaHBRRtqrk0P/Luq0l2M" +
        "2718GwSGeLPZip06gACDG7IctMrgH1J+ZIjsx6vffi977wnMDiktqacmaobV+SCR" +
        "W9ijJRdkYpUHmlLvuJGnDPjkvewpbGWJsCabpWEvWdYw3ma8RuHOPj4Jkrdd4VcR" +
        "aFwox/fPJ7cG6kBydgOBhAACgYBxQIPv9DCsmiMHG1FAxSARX0GcRiELJPJ+MtaS" +
        "tdTrVobNa2jebwc3npLiTvUR4U/CDo1mSZb+Sp/wian8kNZHmGcR6KbtJs9UDsa3" +
        "V0pbbgpUar4HcxV+NQJBbhn9RGu85g3PDILUrINiUAf26mhPN5Y0paM+HbM68nUf" +
        "1OLv16OBsjCBrzAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdl" +
        "bmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUIHFAEB/3jIIZzJEJ/qdsuI8v" +
        "N3kwVQYDVR0jBE4wTIAU1e5lU95R2oetQupBbvKv1u5GlAuhMaQvMC0xEjAQBgNV" +
        "BAoTCUludGVybmV0MjEXMBUGA1UEAxMOY2EuZXhhbXBsZS5vcmeCAQEwDQYJKoZI" +
        "hvcNAQEFBQADggEBAJt4Q34+pqjW5tHHhkdzTITSBjOOf8EvYMgxTMRzhagLSHTt" +
        "9RgO5i/G7ELvnwe1j6187m1XD9iEAWKeKbB//ljeOpgnwzkLR9Er5tr1RI3cbil0" +
        "AX+oX0c1jfRaQnR50Rfb5YoNX6G963iphlxp9C8VLB6eOk/S270XoWoQIkO1ioQ8" +
        "JY4HE6AyDsOpJaOmHpBaxjgsiko52ZWZeZyaCyL98BXwVxeml7pYnHlXWWidB0N/" +
        "Zy+LbvWg3urUkiDjMcB6nGImmEfDSxRdybitcMwbwL26z2WOpwL3llm3mcCydKXg" +
        "Xt8IQhfDhOZOHWckeD2tStnJRP/cqBgO62/qirw=";
    
    private X509Certificate idpRSACert;
    private String idpRSACertBase64 = 
        "MIIC8TCCAdmgAwIBAgIBMjANBgkqhkiG9w0BAQUFADAtMRIwEAYDVQQKEwlJbnRl" +
        "cm5ldDIxFzAVBgNVBAMTDmNhLmV4YW1wbGUub3JnMB4XDTA3MDUyNTIwMDk1MVoX" +
        "DTE3MDUyMjIwMDk1MVowGjEYMBYGA1UEAxMPaWRwLmV4YW1wbGUub3JnMIGfMA0G" +
        "CSqGSIb3DQEBAQUAA4GNADCBiQKBgQDfCVgF2Lvhu0Q35FvmAVGMXc3i1Mojcqyb" +
        "cfVbfn0Tg/Aj5FvuAiDFg9KpGvMHDKdLOY+1xsKZqyIm58SFhW+5z51YpnblHGju" +
        "DtPtPbtspQ7pAOsknnvbKZrx7RGNOJyQZE3Qn88Y5ZBNzABusqNXjrWlU9m4a+XN" +
        "IFqM4YbJLwIDAQABo4GyMIGvMAkGA1UdEwQCMAAwLAYJYIZIAYb4QgENBB8WHU9w" +
        "ZW5TU0wgR2VuZXJhdGVkIENlcnRpZmljYXRlMB0GA1UdDgQWBBT2qDRFTzawttBG" +
        "jN6wxni/12tQQjBVBgNVHSMETjBMgBTV7mVT3lHah61C6kFu8q/W7kaUC6ExpC8w" +
        "LTESMBAGA1UEChMJSW50ZXJuZXQyMRcwFQYDVQQDEw5jYS5leGFtcGxlLm9yZ4IB" +
        "ATANBgkqhkiG9w0BAQUFAAOCAQEAlJYAou5ko3ujHVhOc4OB2AOOqdXAjThiXg6z" +
        "Tjezs7/F53b9IRt4in/k92y1tKZ87F/JcnH6MrzKfb8m5XtcYwtUSvmFTCp5rrFp" +
        "z1JhXlgnaWVJJ2G2vKLDGuPQvLV9zsWhnkbTPuzocvOotxl7w7LJvO3D/tzTAnnU" +
        "bgg1AfP+CTDs3F/ceHzWGVWTMUAmNGX8gMS2/xh66QoEzl7LBG8Xzpo0j+gSxe7h" +
        "Scb5iS4U/XUEbZylMUbbK57h9Bez8VVeO1jfwAniIBT0Ur9ksiYsAdyXYoXssGiF" +
        "bKW1K3QG1GA9wwGy5GvjyALuuXL4lEzFB0kMsGucNMfyyojX9A==";
    
    private X509Certificate keyAuthorityCert;
    private String keyAuthorityCertBase64 = 
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
    
    
    // On IDPSSODescriptor, has RSAKeyValue (usage = encryption) and DSA cert (usage = signing)
    private String protocolFoo = "PROTOCOL_FOO";
    
    // On IDPSSODescriptor, has RSA cert (no usage)
    private String protocolBar = "PROTOCOL_BAR";
    
    private QName idpRole = IDPSSODescriptor.DEFAULT_ELEMENT_NAME;
    
    private String idpEntityID = "http://idp.example.org/shibboleth";
    
    private String mdFileName = "/data/org/opensaml/security/test1-metadata.xml";
    
    private DOMMetadataProvider mdProvider;
    
    private MetadataCredentialResolver mdResolver;
    
    private KeyInfoCredentialResolver keyInfoResolver;
    
    private EntityIDCriteria entityCriteria;
    
    private MetadataCriteria mdCriteria;
    
    private CriteriaSet criteriaSet;
    
    private SecurityConfiguration origGlobalSecurityConfig;
    

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        idpRSAPubKey = SecurityTestHelper.buildJavaRSAPublicKey(idpRSAPubKeyBase64);
        idpDSACert = SecurityTestHelper.buildJavaX509Cert(idpDSACertBase64);
        idpRSACert = SecurityTestHelper.buildJavaX509Cert(idpRSACertBase64);
        keyAuthorityCert = SecurityTestHelper.buildJavaX509Cert(keyAuthorityCertBase64);
        
        Document mdDoc = parser.parse(MetadataCredentialResolverTest.class.getResourceAsStream(mdFileName));
        
        mdProvider = new DOMMetadataProvider(mdDoc.getDocumentElement());
        mdProvider.initialize();
        
        //For testing, use default KeyInfo resolver from global security config, per metadata resolver constructor
        origGlobalSecurityConfig = Configuration.getGlobalSecurityConfiguration();
        BasicSecurityConfiguration newSecConfig = new BasicSecurityConfiguration();
        newSecConfig.setDefaultKeyInfoCredentialResolver( SecurityTestHelper.buildBasicInlineKeyInfoResolver() );
        Configuration.setGlobalSecurityConfiguration(newSecConfig);
        
        mdResolver = new MetadataCredentialResolver(mdProvider);
        
        entityCriteria = new EntityIDCriteria(idpEntityID);
        // by default set protocol to null
        mdCriteria = new MetadataCriteria(idpRole, null);
        
        criteriaSet = new CriteriaSet();
        criteriaSet.add(entityCriteria);
        criteriaSet.add(mdCriteria);
    }
    
    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
        super.tearDown();
        Configuration.setGlobalSecurityConfiguration(origGlobalSecurityConfig);
    }

    /**
     * Test protocol null, and no usage.
     * Should get 3 credentials, 2 from protocolFoo and 1 from protocolBar.
     * 
     * @throws SecurityException 
     */
    public void testNoProtocolNoUsage() throws SecurityException {
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 3, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpDSACert, x509Cred.getEntityCertificate());
                    break;
                case ENCRYPTION:
                    assertTrue("Expected value for key name not found", 
                            credential.getKeyNames().contains(idpRSAPubKeyName));
                    assertEquals("Unexpected value for key", idpRSAPubKey, credential.getPublicKey());
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Test protocol null, and usage = encryption.
     * Should get 2 credentials, 1 from protocolFoo and 1 from protocolBar.
     * 
     * @throws SecurityException 
     */
    public void testNoProtocolUsageEncryption() throws SecurityException {
        criteriaSet.add( new UsageCriteria(UsageType.ENCRYPTION) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 2, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    fail("Credential with invalid usage was resolved");
                    break;
                case ENCRYPTION:
                    assertTrue("Expected value for key name not found", 
                            credential.getKeyNames().contains(idpRSAPubKeyName));
                    assertEquals("Unexpected value for key", idpRSAPubKey, credential.getPublicKey());
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Test protocol null, and usage = signing.
     * Should get 2 credentials, 1 from protocolFoo and 1 from protocolBar.
     * 
     * @throws SecurityException 
     */
    public void testNoProtocolUsageSigning() throws SecurityException {
        criteriaSet.add( new UsageCriteria(UsageType.SIGNING) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 2, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpDSACert, x509Cred.getEntityCertificate());
                    break;
                case ENCRYPTION:
                    fail("Credential with invalid usage was resolved");
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Test 1 protocol (FOO), and no usage .
     * Should get 2 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolFOONoUsage() throws SecurityException {
        mdCriteria.setProtocol(protocolFoo);
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 2, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpDSACert, x509Cred.getEntityCertificate());
                    break;
                case ENCRYPTION:
                    assertTrue("Expected value for key name not found", 
                            credential.getKeyNames().contains(idpRSAPubKeyName));
                    assertEquals("Unexpected value for key", idpRSAPubKey, credential.getPublicKey());
                    break;
                case UNSPECIFIED:
                    fail("Credential was resolved from invalid protocol");
                    break;
                default:
            }
        }
    }
    
    /**
     * Test 1 protocol (FOO), and usage = signing.
     * Should get 1 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolFOOUsageSigning() throws SecurityException {
        mdCriteria.setProtocol(protocolFoo);
        criteriaSet.add( new UsageCriteria(UsageType.SIGNING) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 1, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpDSACert, x509Cred.getEntityCertificate());
                    break;
                case ENCRYPTION:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case UNSPECIFIED:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                default:
            }
        }
    }
    
    /**
     * Test 1 protocol (FOO), and usage encryption.
     * Should get 1 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolFOOUsageEncryption() throws SecurityException {
        mdCriteria.setProtocol(protocolFoo);
        criteriaSet.add( new UsageCriteria(UsageType.ENCRYPTION) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 1, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case ENCRYPTION:
                    assertTrue("Expected value for key name not found", 
                            credential.getKeyNames().contains(idpRSAPubKeyName));
                    assertEquals("Unexpected value for key", idpRSAPubKey, credential.getPublicKey());
                    break;
                case UNSPECIFIED:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                default:
            }
        }
    }
        
    /**
     * Test 1 protocol (BAR), and no usage.
     * Should get 1 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolBARNoUsage() throws SecurityException {
        mdCriteria.setProtocol(protocolBar);
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 1, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    fail("Credential was resolved from invalid protocol");
                    break;
                case ENCRYPTION:
                    fail("Credential was resolved from invalid protocol");
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Test 1 protocol (BAR), and usage = signing.
     * Should get 1 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolBARUsageSigning() throws SecurityException {
        mdCriteria.setProtocol(protocolBar);
        criteriaSet.add( new UsageCriteria(UsageType.SIGNING) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 1, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case ENCRYPTION:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Test 1 protocol (BAR), and usage = encryption.
     * Should get 1 credentials.
     * 
     * @throws SecurityException 
     */
    public void testProtocolBARUsageEncryption() throws SecurityException {
        mdCriteria.setProtocol(protocolBar);
        criteriaSet.add( new UsageCriteria(UsageType.ENCRYPTION) );
        
        List<Credential> resolved = new ArrayList<Credential>();
        for (Credential credential : mdResolver.resolve(criteriaSet)) {
           resolved.add(credential);
           checkContextAndID(credential, idpEntityID, idpRole);
        }
        
        assertEquals("Incorrect number of credentials resolved", 1, resolved.size());
        
        for (Credential credential : resolved) {
            X509Credential x509Cred;
            switch(credential.getUsageType()) {
                case SIGNING:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case ENCRYPTION:
                    fail("Credential was resolved from invalid protocol or usage");
                    break;
                case UNSPECIFIED:
                    x509Cred = (X509Credential) credential;
                    assertEquals("Unexpected value for certificate", idpRSACert, x509Cred.getEntityCertificate());
                    break;
                default:
            }
        }
    }
    
    /**
     * Check expected entity ID and also that expected data is available from the metadata context.
     * 
     * @param credential the credential to evaluate
     * @param entityID the expected entity ID value
     * @param role the expected type of role from the context role descriptor data
     */
    private void checkContextAndID(Credential credential, String entityID, QName role) {
        assertEquals("Unexpected value found for credential entityID", entityID, credential.getEntityId());
        
        SAMLMDCredentialContext mdContext = credential.getCredentalContextSet().get(SAMLMDCredentialContext.class);
        assertNotNull("SAMLMDCredentialContext was not available", mdContext);
        
        assertNotNull(mdContext.getRoleDescriptor());
        RoleDescriptor contextRole = mdContext.getRoleDescriptor();
        assertEquals("Unexpected value for context role descriptor", role, contextRole.getElementQName());
        
        assertTrue(contextRole.getParent() instanceof EntityDescriptor);
        EntityDescriptor entityDescriptor = (EntityDescriptor) mdContext.getRoleDescriptor().getParent();
        assertEquals("Unexpected value for entity descriptor entity ID", entityID, entityDescriptor.getEntityID());
        
        assertTrue(entityDescriptor.getParent() instanceof EntitiesDescriptor);
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) entityDescriptor.getParent();
        
        assertNotNull(entitiesDescriptor.getExtensions());
        assertNotNull(entitiesDescriptor.getExtensions().getUnknownXMLObjects().get(0));
    }

}
