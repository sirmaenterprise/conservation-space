/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml2.core;

import java.security.KeyPair;

import org.joda.time.DateTime;
import org.opensaml.common.BaseTestCase;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SAMLObjectContentReference;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;

public class SignedAssertionTest extends BaseTestCase {
    
    /** Credential used for signing. */
    private BasicCredential goodCredential;

    /** Verification credential that should fail to verify signature. */
    private BasicCredential badCredential;
    
    /** Builder of Assertions. */
    private AssertionBuilder assertionBuilder;
    
    /** Builder of Issuers. */
    private IssuerBuilder issuerBuilder;
    
    /** Builder of AuthnStatements. */
    private AuthnStatementBuilder authnStatementBuilder;
    
    /** Builder of AuthnStatements. */
    private SignatureBuilder signatureBuilder;
    
    /** Generator of element IDs. */
    private SecureRandomIdentifierGenerator idGenerator;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        KeyPair keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        goodCredential = SecurityHelper.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());
        
        keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        badCredential = SecurityHelper.getSimpleCredential(keyPair.getPublic(), null);
        
        assertionBuilder = (AssertionBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        authnStatementBuilder = (AuthnStatementBuilder) builderFactory.getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
        signatureBuilder = (SignatureBuilder) builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        
        idGenerator = new SecureRandomIdentifierGenerator();
    }
    
    /**
     * Creates a simple Assertion, signs it and then verifies the signature.
     * 
     * @throws MarshallingException thrown if the Assertion can not be marshalled into a DOM
     * @throws ValidationException thrown if the Signature does not validate
     * @throws SignatureException 
     * @throws UnmarshallingException 
     * @throws SecurityException 
     */
    public void testAssertionSignature() 
        throws MarshallingException, ValidationException, SignatureException, UnmarshallingException, SecurityException{
        DateTime now = new DateTime();
        
        Assertion assertion = assertionBuilder.buildObject();
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setID(idGenerator.generateIdentifier());
        assertion.setIssueInstant(now);
        
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue("urn:example.org:issuer");
        assertion.setIssuer(issuer);
        
        AuthnStatement authnStmt = authnStatementBuilder.buildObject();
        authnStmt.setAuthnInstant(now);
        assertion.getAuthnStatements().add(authnStmt);
        
        Signature signature = signatureBuilder.buildObject();
        signature.setSigningCredential(goodCredential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
        assertion.setSignature(signature);
        
        Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
        marshaller.marshall(assertion);
        Signer.signObject(signature);
        
        
        // Unmarshall new tree around DOM to avoid side effects and Apache xmlsec bug.
        Assertion signedAssertion = 
            (Assertion) unmarshallerFactory.getUnmarshaller(assertion.getDOM()).unmarshall(assertion.getDOM());
        
        StaticCredentialResolver credResolver = new StaticCredentialResolver(goodCredential);
        KeyInfoCredentialResolver kiResolver = SecurityTestHelper.buildBasicInlineKeyInfoResolver();
        ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);
        
        CriteriaSet criteriaSet = new CriteriaSet( new EntityIDCriteria("urn:example.org:issuer") );
        assertTrue("Assertion signature was not valid",
                trustEngine.validate(signedAssertion.getSignature(), criteriaSet));
    }
}