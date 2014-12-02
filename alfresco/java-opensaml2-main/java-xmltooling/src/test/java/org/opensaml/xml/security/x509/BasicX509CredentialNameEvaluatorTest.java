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

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;

/**
 * Tests the {@link CertPathPKIXTrustEvaluator} implementation.
 */
public class BasicX509CredentialNameEvaluatorTest extends XMLObjectBaseTestCase {
    
    private X509Certificate entityCert3AltNamesDNS_URL_IP;
    private String entityCert3AltNamesDNS_URL_IPBase64 = 
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

    private BasicX509CredentialNameEvaluator evaluator;
    
    private X509Credential cred;
    
    private Set<String> names;
    
    private String subjectCN;
    private String subjectDN;
    private String altNameDNS, altNameURN, altNameURL, altNameIP;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        entityCert3AltNamesDNS_URL_IP = SecurityTestHelper.buildJavaX509Cert(entityCert3AltNamesDNS_URL_IPBase64);
        cred = SecurityHelper.getSimpleCredential(entityCert3AltNamesDNS_URL_IP, null);
        
        subjectCN = "foobar.example.org";
        subjectDN = "cn=foobar.example.org, O=Internet2"; 
        
        altNameDNS = "asimov.example.org";
        altNameURN = "urn:foo:example.org:idp";
        altNameURL = "http://heinlein.example.org";
        altNameIP = "10.1.2.3";
        
        names = new HashSet<String>();
        
        // Reset evaluator checks to a known starting state, in case defaults change in future.
        evaluator = new BasicX509CredentialNameEvaluator();
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(true);
        evaluator.setCheckSubjectDNCommonName(true);
        evaluator.getSubjectAltNameTypes().clear();
        evaluator.getSubjectAltNameTypes().add(X509Util.DNS_ALT_NAME);
        evaluator.getSubjectAltNameTypes().add(X509Util.URI_ALT_NAME);
    }
    
    public void testCommonNameSuccess() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(true);
        
        names.add(subjectCN);
        
        testEvaluateSuccess("Subject common name was valid", names, cred);
    }
    
    public void testCommonNameFail() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(true);
        
        names.add("blah.internet2.edu");
        
        testEvaluateFailure("Subject common name was invalid", names, cred);
    }
    
    public void testSubjectDNSuccess() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(true);
        evaluator.setCheckSubjectDNCommonName(false);
        
        names.add(subjectDN);
        
        testEvaluateSuccess("Subject DN was valid", names, cred);
    }
    
    public void testSubjectDNFail() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(true);
        evaluator.setCheckSubjectDNCommonName(false);
        
        names.add("cn=blah.internet2.edu,OU=ShibDev,O=Internet2");
        
        testEvaluateFailure("Subject DN was invalid", names, cred);
    }
    
    public void testSubjectDNInputNotDN() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(true);
        evaluator.setCheckSubjectDNCommonName(false);
        
        names.add(subjectCN);
        
        testEvaluateFailure("Subject DN was invalid, was not in DN syntax", names, cred);
    }
    
    public void testDNSAltNameSuccess() {
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        evaluator.getSubjectAltNameTypes().clear();
        evaluator.getSubjectAltNameTypes().add(X509Util.DNS_ALT_NAME);
        
        names.add(altNameDNS);
        
        testEvaluateSuccess("DNS subject alt name was valid", names, cred);
    }
    
    public void testDNSAltNameFail() {
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        evaluator.getSubjectAltNameTypes().clear();
        evaluator.getSubjectAltNameTypes().add(X509Util.DNS_ALT_NAME);
        
        names.add("wacky.internet2.edu");
        
        testEvaluateFailure("DNS subject alt name was invalid", names, cred);
    }
 
    public void testURLAltNameSuccess() {
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        evaluator.getSubjectAltNameTypes().clear();
        evaluator.getSubjectAltNameTypes().add(X509Util.URI_ALT_NAME);
        
        names.add(altNameURL);
        
        testEvaluateSuccess("URL subject alt name was valid", names, cred);
    }
    
    public void testURLAltNameFail() {
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        evaluator.getSubjectAltNameTypes().clear();
        evaluator.getSubjectAltNameTypes().add(X509Util.URI_ALT_NAME);
        
        names.add("http://wacky.internet2.edu/idp");
        
        testEvaluateFailure("URL subject alt name was invalid", names, cred);
    }
    
    public void testAltNamesEnabledNoTypes() {
        evaluator.setCheckSubjectAltNames(true);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        evaluator.getSubjectAltNameTypes().clear();
        
        names.add(altNameDNS);
        
        testEvaluateFailure("Alt names were enabled but no types configured to be extracted", names, cred);
    }
    
    public void testAllOptionsEnabled() {
        names.clear();
        names.add(subjectCN);
        names.add("blah.somewhere.org");
        testEvaluateSuccess("Common name was valid", names, cred);
        
        names.clear();
        names.add(subjectDN);
        names.add("blah.somewhere.org");
        names.add("cn=blah.somewhere.org,o=MyOrg");
        testEvaluateSuccess("DN was valid", names, cred);
        
        names.clear();
        names.add(altNameDNS);
        names.add("blah.somewhere.org");
        testEvaluateSuccess("DNS alt name was valid", names, cred);
        
        names.clear();
        names.add("blah.somewhere.org");
        names.add("cn=blah.somewhere.org,o=MyOrg");
        testEvaluateFailure("No trusted names were valid", names, cred);
    }
    
    public void testNameCheckNotActive() {
        evaluator.setCheckSubjectAltNames(false);
        evaluator.setCheckSubjectDN(false);
        evaluator.setCheckSubjectDNCommonName(false);
        
        names.add(subjectCN);
        
        testEvaluateSuccess("Name checking was not active", names, cred);
    }
    
    public void testNoTrustedNames() {
        names.clear();
        
        testEvaluateSuccess("Trusted name set was empty", names, cred);
    }
    
    public void testTrustedNamesNull() {
        names.clear();
        
        testEvaluateSuccess("Trusted name set was null", null, cred);
    }
    
    /********************
     * Helper methods.  *
     ********************/
    
    private void testEvaluateSuccess(String message, Set<String> trustedNames, X509Credential untrustedCred) {
        try {
            if ( !evaluator.evaluate(untrustedCred, trustedNames) ) {
                fail("Evaluation of X509Credential failed, success was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testEvaluateFailure(String message, Set<String> trustedNames, X509Credential untrustedCred) {
        try {
            if ( evaluator.evaluate(untrustedCred, trustedNames) ) {
                fail("Evaluation of X509Credential succeeded, failure was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testEvaluateProcessingError(String message, Set<String> trustedNames, X509Credential untrustedCred) {
        try {
            if ( evaluator.evaluate(untrustedCred, trustedNames) ) {
                fail("Evaluation of X509Credential succeeded, processing failure was expected: " + message);
            } else {
                fail("Evaluation of X509Credential failed, but processing failure was expected: " + message);
            }
        } catch (SecurityException e) {
            // do nothing, failure expected
        }
    }
}