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

package org.opensaml.common.binding.security;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.CollectionCredentialResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;


/**
 * Test SAML protocol message XML signature rule.
 */
public class SAMLProtocolMessageXMLSignatureSecurityPolicyRuleTest 
    extends BaseSAMLSecurityPolicyRuleTestCase<AuthnRequest, Response, NameID> {
    
    private X509Certificate signingCert;
    private String signingCertBase64 = 
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
    
    private PrivateKey signingPrivateKey;
    private String signingPrivateKeyBase64 = 
        "MIIEogIBAAKCAQEAzVp5BZoctb2GuoDf8QUSpGcRct7FKtldC7GG+kN6XvUJW+vg" +
        "c2jOQ6zfLiKqq6ARN1qdC7a4CrkE6Q6TRQXUtqeWn4lLTmC1gQ7Ys0zs7N2d+jBj" +
        "IyD1GEOLNNyD98j4drnehCqQz4mKszW5EWoiMJmEorea/kTGL3en7ir0zp+oez2S" +
        "OQA+0XWu1VoeTlUqGV5Ucd6sRYaPpmYVtKuH1H04uZVsH+BIZHwZc4MP5OYH+HDo" +
        "uq6xqUUtc8Zm7V9UQIPiNtM+ndOINDdlrCubLbM4GCqCETiQol8I62mvP0qBXCC6" +
        "JVkKbbVRwSFGJcg5ZvJiBZXmX+EXhaX5vp1GMQIDAQABAoIBAC1P4lZvHBiqGll6" +
        "6G8pXGS0bXA4Ya9DyTk0UgFU9GKRlSAYWy18Gc9rDNAETD6Uklfxgae9CL0s+D1o" +
        "vuxDDh3DuwO26sv/oO06Vmyx87GMcThshuOQeSSCeuwOIHyDdvfTqZrmPY/d3KIQ" +
        "n6aNEcBBj7fL5cJncIe20nJGPkB9KuTAaGVnaKoOesxgWBr7SvjGq/SB7bRE1B3c" +
        "QxwUDWHkF0LljSIkXaV9ehKJcgBY2fV0rc8pI53WsUXEXk5HoqYZnQ5QjAZ4Hf2s" +
        "bRKevq+D2ENK+OuKNuCAS/oJbGSdS7q0/6jgHZ6cUGXi1r2qEEG7PIorCoSMkWQS" +
        "M1wMX0ECgYEA9c6/s9lKDrjzyjO9rlxzufGVRDjffiUZ1o8F3RD3JltdPLVcd429" +
        "CvGSNV730Yr/wSyRAum4vkGnmOR9tuQdi3PJHt3xGRsymTT5ym/5fnC4SvXVSR6v" +
        "LFPUY80yj+D6/0lwIaGE7x4JOclMXnHjqcpRl14onOjY844WORhxgjkCgYEA1d5N" +
        "Tqp938UbZYKX4Q9UvLf/pVR9xOFOCYnMywAFk0WnkUBPHmPoJuFgeNGeQ7gCmHi7" +
        "JFzwBjkj6DcGMdbXKWiUij1BoRxf9Mof+fZBWVSKw+/yVLbJkyK951+nywyiq3HC" +
        "NBti1eK/h/hXQd8t+dCBmDGj1ba1C2/3JZqLg7kCgYArxD1D85uJFYtq5F2Qryt3" +
        "3zj5pbq9hjOcjWi43O10qe3nAk/NhbI0QaEL2bX8XGh/Z8UGJMFdNul1grGTn/hW" +
        "vS4BTflAxCP1PYaAcgGVbtKRnkX0t/7uwJpfjsjC74chb10Ez/KQdOOlo17yrgqg" +
        "T8LJVd2bWqZOb20ri1uimQKBgFfJYSg6OWLh0IYRXfBmz5yLVmdx0BJBfTvTEXn+" +
        "L0utWsP3hsJttfxHpMbTHEilvoMBg6fAclHLoJ6P/33ztuvrXpWD4W2VbRnY4dlD" +
        "qL1XQ4J7+pelVAaOSy8vB3wEWr1O+61R1HcBFSdl28NRLdkOKjPjpGF0Fsp0Ehmg" +
        "X0YZAoGAXrM4+BUvcx2PLaeneTJoRdOi3GQbdAte03maDU6C474IdgR8IUygfspv" +
        "3fiGue9Wmk5ybUBlv/D6sIWVhnnedWsg2zAgZPfZ78HLLNhWeEx33wPFiK0wV5MJ" +
        "XQ224gQ5t9D3WXdZtmAxXIFoopj4zToCMBjXyep0u7zl3s7s00U=";
    
        
    private X509Certificate otherCert1;
    private String otherCert1Base64 = 
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
    
    private CollectionCredentialResolver credResolver;
    private List<Credential> trustedCredentials;
    private BasicX509Credential signingX509Cred;
    
    private String issuer;
    
    
    /** Constructor. */
    public SAMLProtocolMessageXMLSignatureSecurityPolicyRuleTest() {
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        issuer = "SomeCoolIssuer";
        
        signingCert = SecurityTestHelper.buildJavaX509Cert(signingCertBase64);
        //signingPrivateKey = SecurityTestHelper.buildJavaRSAPrivateKey(signingPrivateKeyBase64);
        
        signingX509Cred = new BasicX509Credential();
        signingX509Cred.setEntityCertificate(signingCert);
        signingX509Cred.setPrivateKey(signingPrivateKey);
        signingX509Cred.setEntityId(issuer);
        
        otherCert1 = SecurityTestHelper.buildJavaX509Cert(otherCert1Base64);
        
        BasicX509Credential otherCred1 = new BasicX509Credential();
        otherCred1.setEntityCertificate(otherCert1);
        otherCred1.setEntityId("other-1");
        
        trustedCredentials = new ArrayList<Credential>();
        trustedCredentials.add(otherCred1);
        
        credResolver = new CollectionCredentialResolver(trustedCredentials);
        
        //KeyInfoCredentialResolver kiResolver = new StaticKeyInfoCredentialResolver(new ArrayList<Credential>());
        //Testing with inline cert
        KeyInfoCredentialResolver kiResolver = SecurityTestHelper.buildBasicInlineKeyInfoResolver();
        TrustEngine<Signature> engine = new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);
        
        rule = new SAMLProtocolMessageXMLSignatureSecurityPolicyRule(engine);
        
        messageContext.setInboundMessageIssuer(issuer);
        ((SAMLMessageContext) messageContext).setInboundSAMLMessageAuthenticated(false);
        messageContext.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
    
    /**
     * Test context issuer set, valid signature with trusted credential.
     */
    public void testSuccess() {
        trustedCredentials.add(signingX509Cred);
        
        assertRuleSuccess("Protocol message was signed with trusted credential known to trust engine resolver");
        SAMLMessageContext samlContext = messageContext;
        assertEquals("Unexpected value for Issuer found", issuer, samlContext.getInboundMessageIssuer());
        assertTrue("Unexpected value for context authentication state", 
                samlContext.isInboundSAMLMessageAuthenticated());
    }
    
    /**
     * Test context issuer set, valid signature with untrusted credential.
     */
    public void testUntrustedCredential() {
        assertRuleFailure("Protocol message was signed with credential unknown to trust engine resolver");
    }
    
    /**
     * Test context issuer set, invalid signature with trusted credential.
     */
    public void testInvalidSignature() {
        trustedCredentials.add(signingX509Cred);
        
        AuthnRequest request  = 
            (AuthnRequest) unmarshallElement("/data/org/opensaml/common/binding/security/Signed-AuthnRequest-InvalidSignature.xml"); 
        messageContext.setInboundSAMLMessage(request);
        
        assertRuleFailure("Protocol message signature was invalid due to document modification");
    }
    
    /**
     * Test context issuer set, valid signature with untrusted credential.
     */
    public void testNoContextIssuer() {
        messageContext.setInboundMessageIssuer(null);
        assertRuleFailure("Protocol message signature should have been unevaluable due to absence of context issuer");
    }
    

    /** {@inheritDoc} */
    protected AuthnRequest buildInboundSAMLMessage() {
        AuthnRequest request = 
            (AuthnRequest) unmarshallElement("/data/org/opensaml/common/binding/security/Signed-AuthnRequest.xml");
        
        return request;
        
        /*
        AuthnRequest request = (AuthnRequest) buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        request.setIssuer(buildIssuer());
        request.setID("abc123");
        request.setIssueInstant(new DateTime());
        
        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setSigningCredential(signingX509Cred);
        
        X509KeyInfoGeneratorFactory kiFactory = new X509KeyInfoGeneratorFactory();
        kiFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = null;
        try {
            keyInfo = kiFactory.newInstance().generate(signingX509Cred);
        } catch (SecurityException e1) {
            fail("Error generating KeyInfo from signing credential");
        }
        
        signature.setKeyInfo(keyInfo);
        
        request.setSignature(signature);
        
        try {
            Configuration.getMarshallerFactory().getMarshaller(request).marshall(request);
        } catch (MarshallingException e) {
            fail("Error marshalling message for signing");
        }
        
        Signer.signObject(signature);
        
        try {
            XMLHelper.writeNode(request.getDOM(), new FileWriter("signed-authn-request-test.xml"));
        } catch (IOException e) {
            fail("Error writing node to file: " + e);
        }
        
        return request;
        */
    }
    
    /**
     * Build an Issuer with entity format.
     * 
     * @return a new Issuer
     */
    private Issuer buildIssuer() {
        Issuer issuerXO = (Issuer) buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuerXO.setValue(issuer);
        issuerXO.setFormat(NameIDType.ENTITY);
        return  issuerXO;
    }

}
