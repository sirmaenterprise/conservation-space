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

import java.io.InputStream;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.SecurityException;

/**
 * Tests the {@link CertPathPKIXTrustEvaluator} implementation.
 */
public class CertPathPKIXTrustEvaluatorTest extends XMLObjectBaseTestCase {
    
    private static final String DATA_PATH = "/data/org/opensaml/xml/security/x509/";
    
    private static final Set<X509CRL> EMPTY_CRLS = new HashSet<X509CRL>();
    
    private static final Set<X509Certificate> EMPTY_ANCHORS = new HashSet<X509Certificate>();
    
    private static final Integer MAX_DEPTH  = 10;
    
    private PKIXTrustEvaluator pkixEvaluator;
    
    private PKIXValidationInformation info;
    
    private X509Credential cred;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        pkixEvaluator = new CertPathPKIXTrustEvaluator();
        info = null;
        cred = null;
    }
    
    public void testGood() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateSuccess("Valid path was specified", info, cred);
    }
    
    public void testIncompletePath() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateFailure("Incomplete path was specified, missing issuing CA certificate", info, cred);
    }
    
    public void testNoAnchors() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                EMPTY_ANCHORS,
                EMPTY_CRLS,
                MAX_DEPTH );
        
        // Must have at least one trust anchor, otherwise it's a fatal processing error due to invalid inputs.
        testValidateProcessingError("No trust anchors specified", info, cred);
    }
    
    public void testNonRootIssuerAsTrustAnchor() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        // Note this validates, b/c the issuing CA cert is present and is treated as
        // a Java TrustAnchor (i.e. a "most trusted cert"). Doesn't matter that it's not a root CA cert.
        testValidateSuccess("Incomplete path was specified, missing (non-issuing) CA certificate in path", info, cred);
    }
    
    public void testRevokedV1() {
        cred = getCredential("foo-1A1-revoked.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateSuccess("Sanity check that revoked cert is otherwise good, sans CRLs", info, cred);
        
        cred = getCredential("foo-1A1-revoked.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                getCRLS("inter1A1-v1.crl"),
                MAX_DEPTH );
        
        testValidateFailure("Specified certificate was revoked, V1 CRL was processed", info, cred);
    }
    
    public void testRevokedV2() {
        cred = getCredential("foo-1A1-revoked.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateSuccess("Sanity check that revoked cert is otherwise good, sans CRLs", info, cred);
        
        cred = getCredential("foo-1A1-revoked.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                getCRLS("inter1A1-v2.crl"),
                MAX_DEPTH );
        
        testValidateFailure("Specified certificate was revoked, V2 CRL was processed", info, cred);
    }
    
    public void testEmptyCRL() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                getCRLS("inter1A1-v1-empty.crl"),
                MAX_DEPTH );
        
        testValidateSuccess("Certificate was valid, empty V1 CRL was processed", info, cred);
    }
    
    public void testExpiredCRL() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                getCRLS("inter1A1-v1-expired.crl"),
                MAX_DEPTH );
        
        // This is the expected behavior, apparently.
        testValidateFailure("Certificate was valid, expired V1 CRL was processed", info, cred);
    }
    
    public void testNonRevokedCertWithNonEmptyCRL() {
        cred = getCredential("foo-1A1-good.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                getCRLS("inter1A1-v1.crl"),
                MAX_DEPTH );
        
        testValidateSuccess("Certificate was valid, V1 CRL containing other revolcations was processed", info, cred);
    }
    
    public void testEntityCertExpired() {
        cred = getCredential("foo-1A1-expired.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateFailure("Specified certificate was expired", info, cred);
    }
    
    public void testGoodPathInCred() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateSuccess("Valid path was specified, intermediate path in credential chain", info, cred);
        
        cred = getCredential("foo-1A1-good.crt", "inter1A1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt", "inter1A-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateSuccess("Valid path was specified, intermediate path in credential chain", info, cred);
    }
    
    public void testGoodPathInCredNoAnchors() {
        cred = getCredential("foo-1A1-good.crt", "inter1A1-ca.crt", "inter1A-ca.crt", "root1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root2-ca.crt", "inter2A-ca.crt", "inter2B-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateFailure("Complete good path was specified in cred, but no relevant trust anchors", info, cred);
    }
      
    public void testIncompletePathInCred() {
        cred = getCredential("foo-1A1-good.crt", "inter1A1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH );
        
        testValidateFailure("Incomplete path was specified, neither contains required intermediate cert", info, cred);
    }
      
    public void testPathTooDeep() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                2 );
        
        testValidateSuccess("Valid path was specified, depth was equal to max path depth", info, cred);
        
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        info = getPKIXInfoSet(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                1 );
        
        testValidateFailure("Valid path was specified, but depth exceeded max path depth", info, cred);
    }
    
    
    /********************
     * Helper methods.  *
     ********************/
    
    private void testValidateSuccess(String message, PKIXValidationInformation info, X509Credential cred) {
        try {
            if ( !pkixEvaluator.validate(info, cred) ) {
                fail("Evaluation of X509Credential failed, success was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateFailure(String message, PKIXValidationInformation info, X509Credential cred) {
        try {
            if ( pkixEvaluator.validate(info, cred) ) {
                fail("Evaluation of X509Credential succeeded, failure was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateProcessingError(String message, PKIXValidationInformation info, X509Credential cred) {
        try {
            if ( pkixEvaluator.validate(info, cred) ) {
                fail("Evaluation of X509Credential succeeded, processing failure was expected: " + message);
            } else {
                fail("Evaluation of X509Credential failed, but processing failure was expected: " + message);
            }
        } catch (SecurityException e) {
            // do nothing, failure expected
        }
    }
    
    private BasicX509Credential getCredential(String entityCertFileName, String ... chainMembers) {
        BasicX509Credential cred = new BasicX509Credential();
        
        X509Certificate entityCert = getCertificate(entityCertFileName);
        cred.setEntityCertificate(entityCert);
        
        HashSet<X509Certificate> certChain = new HashSet<X509Certificate>();
        certChain.add(entityCert);
        
        for (String member: chainMembers) {
            certChain.add( getCertificate(member) );
        }
        
        cred.setEntityCertificateChain(certChain);
        
        return cred;
    }
    
    private PKIXValidationInformation getPKIXInfoSet(Collection<X509Certificate> certs,
                Collection<X509CRL> crls, Integer depth) {
        return new BasicPKIXValidationInformation(certs, crls, depth);
    }
    
    private Collection<X509Certificate> getCertificates(String ... certNames) {
        Set<X509Certificate> certs = new HashSet<X509Certificate>();
        for (String certName : certNames) {
           certs.add( getCertificate(certName) );
        }
        return certs;
    }
    
    private X509Certificate getCertificate(String fileName) {
        try {
            InputStream ins = getInputStream(fileName);
            byte[] encoded = new byte[ins.available()];
            ins.read(encoded);
            return X509Util.decodeCertificate(encoded).iterator().next();
        } catch (Exception e) {
            fail("Could not create certificate from file: " + fileName + ": " + e.getMessage());
        }
        return null;
    }
    
    private Collection<X509CRL> getCRLS(String ... crlNames) {
        Set<X509CRL> crls = new HashSet<X509CRL>();
        for (String crlName : crlNames) {
           crls.add( getCRL(crlName) );
        }
        return crls;
    }
    
    private X509CRL getCRL(String fileName) {
        try {
            InputStream ins = getInputStream(fileName);
            byte[] encoded = new byte[ins.available()];
            ins.read(encoded);
            return X509Util.decodeCRLs(encoded).iterator().next();
        } catch (Exception e) {
            fail("Could not create CRL from file: " + fileName + ": " + e.getMessage());
        }
        return null;
    }
    
    private InputStream getInputStream(String fileName) {
        return  CertPathPKIXTrustEvaluatorTest.class.getResourceAsStream(DATA_PATH + fileName);
    }

}