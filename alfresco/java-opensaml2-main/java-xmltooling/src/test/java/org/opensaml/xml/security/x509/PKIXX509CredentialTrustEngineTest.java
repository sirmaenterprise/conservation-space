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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.criteria.EntityIDCriteria;

/**
 * Tests the {@link PKIXX509CredentialTrustEngine} implementation.
 */
public class PKIXX509CredentialTrustEngineTest extends XMLObjectBaseTestCase {
    
    private static final String DATA_PATH = "/data/org/opensaml/xml/security/x509/";
    
    private static final Set<X509CRL> EMPTY_CRLS = new HashSet<X509CRL>();
    
    private static final Set<X509Certificate> EMPTY_ANCHORS = new HashSet<X509Certificate>();
    
    private static final Integer MAX_DEPTH  = 10;
    
    private PKIXX509CredentialTrustEngine engine;
    
    private X509Credential cred;
    
    private CriteriaSet criteriaSet;
    
    private String subjectCN;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        subjectCN = "foo.example.org";
        
        criteriaSet = new CriteriaSet( new EntityIDCriteria("dummy-entity-id") );
    }
    
    public void testGoodPathInAnchors() {
        cred = getCredential("foo-1A1-good.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateSuccess("Entity cert was good, path in trust anchors set");
    }
    
    public void testGoodPathInCred() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateSuccess("Entity cert was good, full path in cred");
    }
    
    public void testGoodPathNoTrustedNames() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH
                );
        
        testValidateSuccess("Entity cert was good, full path in cred, no trusted names");
    }
    
    public void testGoodPathBadTrustedName() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                "NOT"+subjectCN
                );
        
        testValidateFailure("Entity cert was good, full path in cred, bad trusted names");
    }
    
    public void testCertRevoked() {
        cred = getCredential("foo-1A1-revoked.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                getCRLS("inter1A1-v1.crl"),
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("Entity cert was revoked");
    }
    
    public void testCertExpired() {
        cred = getCredential("foo-1A1-expired.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("Entity cert was expired");
    }
    
    public void testMissingAnchor() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root2-ca.crt", "inter2A-ca.crt", "inter2B-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("No path to entity cert, root CA trust anchor missing");
    }
    
    public void testNoAnchors() {
        cred = getCredential("foo-1A1-good.crt", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                EMPTY_ANCHORS,
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("No trust anchors at all in validation set");
    }
    
    
    /********************
     * Helper methods.  *
     ********************/
    
    private void testValidateSuccess(String message) {
        try {
            if ( !engine.validate(cred, criteriaSet) ) {
                fail("Evaluation of X509Credential failed, success was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateFailure(String message) {
        try {
            if ( engine.validate(cred, criteriaSet) ) {
                fail("Evaluation of X509Credential succeeded, failure was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateProcessingError(String message) {
        try {
            if ( engine.validate(cred, criteriaSet) ) {
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
    
    private PKIXX509CredentialTrustEngine getEngine(Collection<X509Certificate> certs,
                Collection<X509CRL> crls, Integer depth, String ... trustedNames) {
        
        PKIXValidationInformation info = getPKIXInfoSet(certs, crls, depth);
        
        List<PKIXValidationInformation> infoList = new ArrayList<PKIXValidationInformation>();
        infoList.add(info);
        
        Set<String> names = new HashSet<String>();
        for (String trustedName : trustedNames) {
            names.add(trustedName);
        }
        
        StaticPKIXValidationInformationResolver resolver = new StaticPKIXValidationInformationResolver(infoList, names);
        
        return new PKIXX509CredentialTrustEngine(resolver);
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
        return  PKIXX509CredentialTrustEngineTest.class.getResourceAsStream(DATA_PATH + fileName);
    }

}