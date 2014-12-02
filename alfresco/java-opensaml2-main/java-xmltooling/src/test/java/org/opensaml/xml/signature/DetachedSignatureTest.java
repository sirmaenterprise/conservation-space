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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

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
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoCriteria;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class DetachedSignatureTest extends XMLObjectBaseTestCase {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(EnvelopedSignatureTest.class);

    /** Key resolver containing proper verification key. */
    private BasicCredential goodCredential;

    /** Key resolver containing invalid verification key. */
    private BasicCredential badCredential;

    /** Builder of mock XML objects. */
    private SimpleXMLObjectBuilder sxoBuilder;

    /** Builder of Signature XML objects. */
    private SignatureBuilder sigBuilder;

    /** Parser pool used to parse example config files. */
    private BasicParserPool parserPool;

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
     * Tests creating a detached signature within the same document as the element signed and then verifying it.
     * 
     * @throws MarshallingException thrown if the XMLObject tree can not be marshalled
     * @throws ValidationException thrown if there is a problem attempting to validate the signature
     * @throws UnmarshallingException thrown if the signature can not be unmarshalled
     * @throws SignatureException 
     */
    public void testInternalSignatureAndVerification() throws MarshallingException, UnmarshallingException,
            ValidationException, SignatureException {
        SimpleXMLObject sxo = getXMLObjectWithSignature();
        Signature signature = sxo.getSignature();

        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(sxo);
        Element signedElement = marshaller.marshall(sxo);

        Signer.signObject(signature);
        if (log.isDebugEnabled()) {
            log.debug("Marshalled deatched Signature: \n" + XMLHelper.nodeToString(signedElement));
        }

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(signedElement);
        sxo = (SimpleXMLObject) unmarshaller.unmarshall(signedElement);
        signature = (Signature) sxo.getOrderedChildren().get(1);

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
     * Tests creating a detached signature within a different document as the element signed and then verifying it. The
     * external references used are the InCommon and InQueue metadata files.
     * 
     * @throws MarshallingException thrown if the XMLObject tree can not be marshalled
     * @throws ValidationException thrown if the signature verification fails
     * @throws SignatureException 
     */
    public void testExternalSignatureAndVerification() throws MarshallingException, ValidationException, SignatureException {
        Signature signature = sigBuilder.buildObject();
        signature.setSigningCredential(goodCredential);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);

        String incommonMetadata = "http://wayf.incommonfederation.org/InCommon/InCommon-metadata.xml";
        URIContentReference contentReference = new URIContentReference(incommonMetadata);
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        signature.getContentReferences().add(contentReference);

        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(signature);
        Element signatureElement = marshaller.marshall(signature);

        Signer.signObject(signature);
        if (log.isDebugEnabled()) {
            log.debug("Marshalled deatched Signature: \n" + XMLHelper.nodeToString(signatureElement));
        }

        SignatureValidator sigValidator = new SignatureValidator(goodCredential);
        sigValidator.validate(signature);
    }

    /**
     * Unmarshalls the XML DSIG spec RSA example signature and verifies it with the key contained in the KeyInfo.
     * 
     * @throws IOException thrown if the signature can not be fetched from the W3C site
     * @throws MalformedURLException thrown if the signature can not be fetched from the W3C site
     * @throws XMLParserException thrown if the signature is not valid XML
     * @throws UnmarshallingException thrown if the signature DOM can not be unmarshalled
     * @throws ValidationException thrown if the Signature does not validate against the key
     * @throws GeneralSecurityException
     * @throws SecurityException
     */
    public void testUnmarshallExternalSignatureAndVerification() throws IOException, XMLParserException,
            UnmarshallingException, ValidationException, GeneralSecurityException, SecurityException {
        String signatureLocation = "http://www.w3.org/TR/xmldsig-core/signature-example-rsa.xml";
        InputStream ins = new URL(signatureLocation).openStream();
        Element signatureElement = parserPool.parse(ins).getDocumentElement();

        Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(signatureElement);
        Signature signature = (Signature) unmarshaller.unmarshall(signatureElement);

        KeyInfoCredentialResolver resolver = SecurityTestHelper.buildBasicInlineKeyInfoResolver();

        KeyInfoCriteria criteria = new KeyInfoCriteria(signature.getKeyInfo());
        CriteriaSet criteriaSet = new CriteriaSet(criteria);
        Credential credential = resolver.resolveSingle(criteriaSet);
        SignatureValidator sigValidator = new SignatureValidator(credential);
        sigValidator.validate(signature);
    }

    /**
     * Creates a XMLObject that has another XMLObject and a Signature as children. The Signature is is a detached
     * signature of its sibling.
     * 
     * @return the XMLObject
     */
    private SimpleXMLObject getXMLObjectWithSignature() {
        SimpleXMLObject rootSXO = sxoBuilder.buildObject();

        SimpleXMLObject childSXO = sxoBuilder.buildObject();
        childSXO.setId("FOO");
        rootSXO.getSimpleXMLObjects().add(childSXO);

        Signature sig = sigBuilder.buildObject();
        sig.setSigningCredential(goodCredential);
        sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        sig.setSignatureAlgorithm(algoURI);

        DocumentInternalIDContentReference contentReference = new DocumentInternalIDContentReference("FOO");
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        sig.getContentReferences().add(contentReference);

        rootSXO.setSignature(sig);
        return rootSXO;
    }
}