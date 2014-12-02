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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509IssuerSerialCriteria;
import org.opensaml.xml.security.x509.X509SubjectKeyIdentifierCriteria;
import org.opensaml.xml.util.Base64;

/**
 *
 */
public class EvaluableX509SubjectKeyIdentifierCredentialCriteriaTest extends TestCase {
    
    private BasicX509Credential credential;
    private String entityCertSKIBase64 = "OBGBOSNoqgroOhl9RniD0sMlRa4=";
    private byte[] subjectKeyIdentifier;
    
    private X509SubjectKeyIdentifierCriteria criteria;
    
    
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
    
    private X509Certificate entityCertNoSKI;
    private String entityCertNoSKIBase64 = 
        "MIIBwjCCASugAwIBAgIJAMrW6QSeKNBJMA0GCSqGSIb3DQEBBAUAMCMxITAfBgNV" +
        "BAMTGG5vZXh0ZW5zaW9ucy5leGFtcGxlLm9yZzAeFw0wNzA1MTkxNzU2NTVaFw0w" +
        "NzA2MTgxNzU2NTVaMCMxITAfBgNVBAMTGG5vZXh0ZW5zaW9ucy5leGFtcGxlLm9y" +
        "ZzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAw8xxu6TLqEdmnyXVZjiUoRHN" +
        "6yHyobZaRK+tBEmWkD4nTlOVmTKWBCO/F4OnugaJbSTH+7Jk37l8/XYYBSIkW0+L" +
        "2BglzQ2JCux/uoRu146QDIk9f5PIFs+Fxy7VRVUUZiOsonB/PNVqA7OVbPxzr1SK" +
        "PSE0s9CHaDjCaEs2BnMCAwEAATANBgkqhkiG9w0BAQQFAAOBgQAuI/l80wb8K6RT" +
        "1EKrAcfr9JAlJR4jmVnCK7j3Ulx++U98ze2G6/cluLxrbnqwXmxJNC3nt6xkQVJU" +
        "X1UFg+zkmRrst2Nv8TTrR7S30az068BHfrZLRSUConG9jXXj+hJq+w/ojmrq8Mzv" +
        "JSczkA2BvsEUBARYo53na7RMgk+xWg==";

    
    
    public EvaluableX509SubjectKeyIdentifierCredentialCriteriaTest() {
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        entityCert = SecurityTestHelper.buildJavaX509Cert(entityCertBase64);
        entityCertNoSKI = SecurityTestHelper.buildJavaX509Cert(entityCertNoSKIBase64);
        subjectKeyIdentifier = Base64.decode(entityCertSKIBase64);
        
        credential = new BasicX509Credential();
        credential.setEntityCertificate(entityCert);
        
        criteria = new X509SubjectKeyIdentifierCriteria(subjectKeyIdentifier);
    }
    
    public void testSatifsy() {
        EvaluableX509SubjectKeyIdentifierCredentialCriteria evalCrit = new EvaluableX509SubjectKeyIdentifierCredentialCriteria(criteria);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }

    public void testNotSatisfy() {
        criteria.setSubjectKeyIdentifier("abcdef123456".getBytes());
        EvaluableX509SubjectKeyIdentifierCredentialCriteria evalCrit = new EvaluableX509SubjectKeyIdentifierCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testNotSatisfyWrongCredType() {
        BasicCredential basicCred = new BasicCredential();
        EvaluableX509SubjectKeyIdentifierCredentialCriteria evalCrit = new EvaluableX509SubjectKeyIdentifierCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(basicCred));
    }
    
    public void testNotSatisfyNoCert() {
        credential.setEntityCertificate(null);
        EvaluableX509SubjectKeyIdentifierCredentialCriteria evalCrit = new EvaluableX509SubjectKeyIdentifierCredentialCriteria(criteria);
        assertFalse("Credential should NOT have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
    
    public void testCanNotEvaluate() {
        credential.setEntityCertificate(entityCertNoSKI);
        EvaluableX509SubjectKeyIdentifierCredentialCriteria evalCrit = new EvaluableX509SubjectKeyIdentifierCredentialCriteria(criteria);
        assertNull("Credential should have been unevaluable against the criteria", evalCrit.evaluate(credential));
    }
    
    public void testRegistry() throws SecurityException {
        EvaluableCredentialCriteria evalCrit = EvaluableCredentialCriteriaRegistry.getEvaluator(criteria);
        assertNotNull("Evaluable criteria was unavailable from the registry", evalCrit);
        assertTrue("Credential should have matched the evaluable criteria", evalCrit.evaluate(credential));
    }
}
