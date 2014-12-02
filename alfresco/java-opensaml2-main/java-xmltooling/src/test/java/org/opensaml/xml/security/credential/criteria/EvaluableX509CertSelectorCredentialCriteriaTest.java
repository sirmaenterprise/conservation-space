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

package org.opensaml.xml.security.credential.criteria;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.x509.BasicX509Credential;

/**
 *
 */
public class EvaluableX509CertSelectorCredentialCriteriaTest extends TestCase {
    
    private BasicX509Credential credential;
    private X509CertSelector certSelector;
    private X500Principal subjectName;
    private PublicKey pubKey;
    private EvaluableX509CertSelectorCredentialCriteria evalCrit;
    
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
    
    
    public EvaluableX509CertSelectorCredentialCriteriaTest() {
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        entityCert = SecurityTestHelper.buildJavaX509Cert(entityCertBase64);
        pubKey = entityCert.getPublicKey();
        subjectName = new X500Principal("cn=foobar.example.org, O=Internet2");
        
        credential = new BasicX509Credential();
        credential.setEntityCertificate(entityCert);
        
        certSelector = new X509CertSelector();
        
        evalCrit = new EvaluableX509CertSelectorCredentialCriteria(certSelector);
    }
    
    public void testSatifsyByCert() {
        certSelector.setCertificate(entityCert);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testSatifsyByKey() {
        certSelector.setSubjectPublicKey(entityCert.getPublicKey());
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testSatifsyBySubjectName() {
        certSelector.setSubject(subjectName);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }

    public void testNotSatisfy() throws NoSuchAlgorithmException, NoSuchProviderException {
        certSelector.setSubjectPublicKey( SecurityTestHelper.generateKeyPair("RSA", 1024, null).getPublic() );
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testNotSatisfyWrongCredType() {
        certSelector.setCertificate(entityCert);
        BasicCredential basicCred = new BasicCredential();
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(basicCred));
    }
    
    public void testNotSatisfyNoCert() {
        certSelector.setCertificate(entityCert);
        credential.setEntityCertificate(null);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
}
