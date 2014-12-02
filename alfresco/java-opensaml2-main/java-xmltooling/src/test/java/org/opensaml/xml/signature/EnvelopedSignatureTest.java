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

package org.opensaml.xml.signature;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.mock.SimpleXMLObjectBuilder;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test to verify {@link org.opensaml.xml.signature.Signature} and its marshallers and unmarshallers.
 */
public class EnvelopedSignatureTest extends XMLObjectBaseTestCase {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EnvelopedSignatureTest.class);

    /** Credential used to sign and verify. */
    private Credential goodCredential;
    
    /** Invalid credential for verification. */
    private Credential badCredential;

    /** Builder of mock XML objects. */
    private SimpleXMLObjectBuilder sxoBuilder;

    /** Builder of Signature XML objects. */
    private SignatureBuilder sigBuilder;
    
    /** Signature algorithm URI. */
    private String algoURI = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        KeyPair keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        goodCredential = SecurityHelper.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());

        keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        badCredential = SecurityHelper.getSimpleCredential(keyPair.getPublic(), null);

        sxoBuilder = new SimpleXMLObjectBuilder();
        sigBuilder = new SignatureBuilder();

        parserPool = new BasicParserPool();
        parserPool.setNamespaceAware(true);
    }

    /**
     * Tests creating an enveloped signature and then verifying it.
     * 
     * @throws MarshallingException thrown if the XMLObject tree can not be marshalled
     * @throws ValidationException 
     * @throws SignatureException 
     */
    public void testSigningAndVerification() throws MarshallingException, ValidationException, SignatureException{
        SimpleXMLObject sxo = getXMLObjectWithSignature();
        Signature signature = sxo.getSignature();

        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(sxo);
        Element signedElement = marshaller.marshall(sxo);
        
        Signer.signObject(signature);
        
        if (log.isDebugEnabled()) {
            log.debug("Marshalled Signature: \n" + XMLHelper.nodeToString(signedElement));
        }
        
        SignatureValidator sigValidator = new SignatureValidator(goodCredential);
        sigValidator.validate(signature);

        try {
            sigValidator = new SignatureValidator(badCredential);
            sigValidator.validate(signature);
            fail("Validated signature with improper public key");
        } catch (ValidationException e) {
            // expected
        }
    }

    /**
     * Tests unmarshalling an enveloped signature.
     * 
     * @throws XMLParserException thrown if the XML can not be parsed
     * @throws UnmarshallingException thrown if the DOM can not be unmarshalled
     * @throws GeneralSecurityException 
     */
    public void testUnmarshall() throws XMLParserException, UnmarshallingException, GeneralSecurityException {
        String envelopedSignatureFile = "/data/org/opensaml/xml/signature/envelopedSignature.xml";
        InputStream ins = EnvelopedSignatureTest.class.getResourceAsStream(envelopedSignatureFile);
        Document envelopedSignatureDoc = parserPool.parse(ins);
        Element rootElement = envelopedSignatureDoc.getDocumentElement();

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(rootElement);
        SimpleXMLObject sxo = (SimpleXMLObject) unmarshaller.unmarshall(rootElement);

        assertEquals("Id attribute was not expected value", "FOO", sxo.getId());

        Signature signature = sxo.getSignature();
        assertNotNull("Signature was null", signature);

        KeyInfo keyInfo = signature.getKeyInfo();
        assertNotNull("Signature's KeyInfo was null", keyInfo);
        
        PublicKey pubKey = KeyInfoHelper.getPublicKeys(keyInfo).get(0);
        assertNotNull("KeyInfo did not contain the verification key", pubKey);
    }

    /**
     * Creates a XMLObject that has a Signature child element.
     * 
     * @return a XMLObject that has a Signature child element
     */
    private SimpleXMLObject getXMLObjectWithSignature() {
        SimpleXMLObject sxo = sxoBuilder.buildObject();
        sxo.setId("FOO");

        Signature sig = sigBuilder.buildObject();
        sig.setSigningCredential(goodCredential);
        sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        sig.setSignatureAlgorithm(algoURI);
        
        DocumentInternalIDContentReference contentReference = new DocumentInternalIDContentReference("FOO");
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE);
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        sig.getContentReferences().add(contentReference);

        sxo.setSignature(sig);
        return sxo;
    }
}