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

package org.opensaml.xml.signature;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.SigningUtil;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.BasicPKIXValidationInformation;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.PKIXValidationInformation;
import org.opensaml.xml.security.x509.StaticPKIXValidationInformationResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.security.x509.X509Util;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Tests the {@link PKIXSignatureTrustEngine} implementation.
 */
public class PKIXSignatureTrustEngineTest extends XMLObjectBaseTestCase {
    
    private static final String DATA_PATH = "/data/org/opensaml/xml/security/x509/";
    
    private static final Set<X509CRL> EMPTY_CRLS = new HashSet<X509CRL>();
    
    private static final Set<X509Certificate> EMPTY_ANCHORS = new HashSet<X509Certificate>();
    
    private static final Integer MAX_DEPTH  = 10;
    
    private PKIXSignatureTrustEngine engine;
    
    private Signature signature;
    
    private CriteriaSet criteriaSet;
    
    private String subjectCN;
    
    private boolean tamperDocumentPostSigning;
    private boolean emitKeyInfo;
    private boolean emitKeyValueOnly;
    
    private String rawData;
    private byte[] rawSignedContent;
    private String rawAlgorithmURI;
    private byte[] rawSignature;
    private Credential rawCandidateCred;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        subjectCN = "foo.example.org";
        
        criteriaSet = new CriteriaSet( new EntityIDCriteria("dummy-entity-id") );
        
        // Used to test the tampered data case
        tamperDocumentPostSigning = false;
        
        // These toggle how the signing cred is represented in the Document's KeyInfo.
        emitKeyInfo = true;
        emitKeyValueOnly = false;
        
        rawData = "Hello, here is some secret data that is to be signed";
        rawSignedContent = rawData.getBytes();
        rawAlgorithmURI = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1;
    }
    
    public void testGoodPathInAnchors() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key");
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateSuccess("Entity cert was good, path in trust anchors set");
    }
    
    public void testGoodPathInCred() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateSuccess("Entity cert was good, full path in cred");
    }
    
    public void testGoodPathNoTrustedNames() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH
                );
        
        testValidateSuccess("Entity cert was good, full path in cred, no trusted names");
    }
    
    public void testGoodPathBadTrustedName() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                "NOT"+subjectCN
                );
        
        testValidateFailure("Entity cert was good, full path in cred, bad trusted names");
    }
    
    public void testCertRevoked() {
        signature = getSignature("foo-1A1-revoked.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                getCRLS("inter1A1-v1.crl"),
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("Entity cert was revoked");
    }
    
    public void testCertExpired() {
        signature = getSignature("foo-1A1-expired.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("Entity cert was expired");
    }
    
    public void testMissingAnchor() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                getCertificates("root2-ca.crt", "inter2A-ca.crt", "inter2B-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("No path to entity cert, root CA trust anchor missing");
    }
    
    public void testNoAnchors() {
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        engine = getEngine(
                EMPTY_ANCHORS,
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN
                );
        
        testValidateFailure("No trust anchors at all in validation set");
    }
    
    public void testTamperedData() throws SecurityException {
        tamperDocumentPostSigning = true;
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key");
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateFailure("Entity cert was good, data was tampered with");
    }
    
    public void testNoCandidateCred() throws SecurityException {
        emitKeyInfo = false;
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key");
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateFailure("Entity cert was good, but validation credential was not present in Signature's KeyInfo");
    }
    
    public void testWrongCredType() throws SecurityException {
        emitKeyValueOnly = true;
        signature = getSignature("foo-1A1-good.crt", "foo-1A1-good.key");
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testValidateFailure("Entity cert was good, but validation credential in KeyInfo was not an X509Credential");
    }
    
    public void testRawGoodPathInAnchors() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testRawValidateSuccess("Entity cert was good, path in trust anchors set");
    }
    
    public void testRawGoodPathInCred() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testRawValidateSuccess("Entity cert was good, path in cred set");
    }
    
    public void testRawGoodPathNoTrustedNames() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH
                );
        
        testRawValidateSuccess("Entity cert was good, empty trusted names");
    }
    
    public void testRawGoodPathBadTrustedName() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                "NOT"+subjectCN);
        
        testRawValidateFailure("Entity cert was good, bad trusted names");
    }
    
    public void testRawCertRevoked() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-revoked.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                getCRLS("inter1A1-v1.crl"),
                MAX_DEPTH,
                subjectCN);
        
        testRawValidateFailure("Entity cert was revoked");
    }
    
    public void testRawCertExpired() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-expired.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN);
        
        testRawValidateFailure("Entity cert was expired");
    }
    
    public void testRawCertMissingAnchor() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                getCertificates("root2-ca.crt", "inter2A-ca.crt", "inter2B-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN);
        
        testRawValidateFailure("No path to entity cert, root CA trust anchor missing");
    }
    
    public void testRawCertNoAnchors() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key", "inter1A-ca.crt", "inter1A1-ca.crt");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        engine = getEngine(
                EMPTY_ANCHORS,
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN);
        
        testRawValidateFailure("No trust anchors at all in validation set");
    }
    
    public void testRawTamperedData() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        rawSignedContent = (rawData + "HAHA All your base are belong to us").getBytes();
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testRawValidateFailure("Entity cert was good, data was tampered with");
    }
    
    public void testRawNoCandidateCred() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        rawCandidateCred = null;
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testRawValidateFailure("Entity cert was good, but candidate credential was not supplied to engine");
    }
    
    public void testRawWrongCredType() throws SecurityException {
        rawCandidateCred = getCredential("foo-1A1-good.crt", "foo-1A1-good.key");
        rawSignature = SigningUtil.signWithURI(rawCandidateCred, rawAlgorithmURI, rawSignedContent);
        rawCandidateCred = SecurityHelper.getSimpleCredential(rawCandidateCred.getPublicKey(), null);
        engine = getEngine(
                getCertificates("root1-ca.crt", "inter1A-ca.crt", "inter1A1-ca.crt"),
                EMPTY_CRLS,
                MAX_DEPTH,
                subjectCN );
        
        testRawValidateFailure("Entity cert was good, but candidate credential was not an X509Credential");
    }
    
    
    /********************
     * Helper methods.  *
     ********************/
    
    private void testValidateSuccess(String message) {
        try {
            if ( !engine.validate(signature, criteriaSet) ) {
                fail("Evaluation of Signature failed, success was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateFailure(String message) {
        try {
            if ( engine.validate(signature, criteriaSet) ) {
                fail("Evaluation of Signature succeeded, failure was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testValidateProcessingError(String message) {
        try {
            if ( engine.validate(signature, criteriaSet) ) {
                fail("Evaluation of Signature succeeded, processing failure was expected: " + message);
            } else {
                fail("Evaluation of Signature failed, but processing failure was expected: " + message);
            }
        } catch (SecurityException e) {
            // do nothing, failure expected
        }
    }
    
    private Signature getSignature(String entityCertFileName, String entityKeyFileName, String ... chainMembers) {
        X509Credential cred = getCredential(entityCertFileName, entityKeyFileName, chainMembers);
        
        SignableXMLObject sxo = null;
        try {
            sxo = buildSignedObject(cred);
        } catch (SignatureException e) {
            fail("Error building signed object: " + e.getMessage());
        }
        
        //System.out.println(XMLHelper.prettyPrintXML(sxo.getDOM()));
        
        //Unmarshall a new XMLObject tree around the DOM, just to avoid any xmlsec bugs or side effects.
        Element signedDOM = sxo.getDOM();
        if (tamperDocumentPostSigning) {
            Element newChild = signedDOM.getOwnerDocument().createElementNS(SimpleXMLObject.NAMESPACE, 
                    SimpleXMLObject.NAMESPACE_PREFIX + ":" + SimpleXMLObject.LOCAL_NAME);
            Text text = signedDOM.getOwnerDocument().createTextNode("HAHA, now you are tampered with");
            newChild.appendChild(text);
            signedDOM.insertBefore(newChild, signedDOM.getFirstChild());
        }
        
        SignableXMLObject verifiableSXO = null;
        try {
            verifiableSXO = (SignableXMLObject) unmarshallerFactory.getUnmarshaller(signedDOM).unmarshall(signedDOM);
        } catch (UnmarshallingException e) {
            fail("Error unmarshalling new signed object: " + e.getMessage());
        }
        
        //System.out.println(XMLHelper.prettyPrintXML(verifiableSXO.getDOM()));
        
        return verifiableSXO.getSignature();
    }

    private BasicX509Credential getCredential(String entityCertFileName, String entityKeyFileName, String ... chainMembers) {
        BasicX509Credential cred = new BasicX509Credential();
        
        X509Certificate entityCert = getCertificate(entityCertFileName);
        cred.setEntityCertificate(entityCert);
        
        PrivateKey privateKey = getPrivateKey(entityKeyFileName);
        cred.setPrivateKey(privateKey);
        
        HashSet<X509Certificate> certChain = new HashSet<X509Certificate>();
        certChain.add(entityCert);
        
        for (String member: chainMembers) {
            certChain.add( getCertificate(member) );
        }
        
        cred.setEntityCertificateChain(certChain);
        
        return cred;
    }
    
    private PKIXSignatureTrustEngine getEngine(Collection<X509Certificate> certs,
                Collection<X509CRL> crls, Integer depth, String ... trustedNames) {
        
        PKIXValidationInformation info = getPKIXInfoSet(certs, crls, depth);
        
        List<PKIXValidationInformation> infoList = new ArrayList<PKIXValidationInformation>();
        infoList.add(info);
        
        Set<String> names = new HashSet<String>();
        for (String trustedName : trustedNames) {
            names.add(trustedName);
        }
        
        StaticPKIXValidationInformationResolver resolver = new StaticPKIXValidationInformationResolver(infoList, names);
        
        return new PKIXSignatureTrustEngine(resolver, SecurityTestHelper.buildBasicInlineKeyInfoResolver());
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
    
    private PrivateKey getPrivateKey(String fileName) {
        try {
            InputStream ins = getInputStream(fileName);
            byte[] encoded = new byte[ins.available()];
            ins.read(encoded);
            return SecurityHelper.decodePrivateKey(encoded, null);
        } catch (Exception e) {
            fail("Could not create private key from file: " + fileName + ": " + e.getMessage());
        }
        return null;
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
        return  PKIXSignatureTrustEngineTest.class.getResourceAsStream(DATA_PATH + fileName);
    }
    
    private SignableXMLObject buildSignedObject(X509Credential signingX509Cred) throws SignatureException {
        SimpleXMLObject sxo = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        sxo.setId("abc123");
        
        SimpleXMLObject child = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        child.setValue("SomeSimpleValueAsTextContent");
        sxo.getSimpleXMLObjects().add(child);
        
        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setSigningCredential(signingX509Cred);
        
        DocumentInternalIDContentReference idContentRef = new DocumentInternalIDContentReference(sxo.getId());
        idContentRef.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        idContentRef.getTransforms().add(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE);
        idContentRef.getTransforms().add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        signature.getContentReferences().add(idContentRef);
        
        if (emitKeyInfo) {
            X509KeyInfoGeneratorFactory kiFactory = new X509KeyInfoGeneratorFactory();
            if (emitKeyValueOnly) {
                kiFactory.setEmitPublicKeyValue(true);
            } else {
                kiFactory.setEmitEntityCertificate(true);
                kiFactory.setEmitEntityCertificateChain(true);
                // This is important - we have multiple certs, and need to disambiguate the entity cert.
                kiFactory.setEmitX509SubjectName(true);
            }
            KeyInfo keyInfo = null;
            try {
                keyInfo = kiFactory.newInstance().generate(signingX509Cred);
            } catch (SecurityException e) {
                fail("Error generating KeyInfo from signing credential: " + e);
            }
            signature.setKeyInfo(keyInfo);
        }        
        
        sxo.setSignature(signature);
        
        try {
            Configuration.getMarshallerFactory().getMarshaller(sxo).marshall(sxo);
        } catch (MarshallingException e) {
            fail("Error marshalling object for signing: " + e);
        }
        
        Signer.signObject(signature);
        
        /*
        try {
            XMLHelper.writeNode(sxo.getDOM(), new FileWriter("signed-simple-object-test4.xml"));
        } catch (IOException e) {
            fail("Error writing node to file: " + e);
        }
        */
        
        return sxo;
    }
    
    private void testRawValidateSuccess(String message) {
        try {
            if ( !engine.validate(rawSignature, rawSignedContent, rawAlgorithmURI, criteriaSet, rawCandidateCred) ) {
                fail("Evaluation of Signature failed, success was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testRawValidateFailure(String message) {
        try {
            if ( engine.validate(rawSignature, rawSignedContent, rawAlgorithmURI, criteriaSet, rawCandidateCred) ) {
                fail("Evaluation of Signature succeeded, failure was expected: " + message);
            }
        } catch (SecurityException e) {
            fail("Evaluation failed due to processing exception: " + e.getMessage());
        }
    }
    
    private void testRawValidateProcessingError(String message) {
        try {
            if ( engine.validate(rawSignature, rawSignedContent, rawAlgorithmURI, criteriaSet, rawCandidateCred) ) {
                fail("Evaluation of Signature succeeded, processing failure was expected: " + message);
            } else {
                fail("Evaluation of Signature failed, but processing failure was expected: " + message);
            }
        } catch (SecurityException e) {
            // do nothing, failure expected
        }
    }

}