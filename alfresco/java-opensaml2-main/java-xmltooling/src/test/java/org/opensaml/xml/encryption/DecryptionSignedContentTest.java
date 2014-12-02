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

package org.opensaml.xml.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;

import org.apache.xml.security.utils.IdResolver;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.signature.DocumentInternalIDContentReference;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test decryption of signed content.
 */
public class DecryptionSignedContentTest extends XMLObjectBaseTestCase {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(DecryptionSignedContentTest.class);

    /** Credential used to sign and verify. */
    private Credential signingCredential;

    /** The data encryption parameters object. */
    private EncryptionParameters encParams;

    /** Resolver for the data encryption key. */
    private KeyInfoCredentialResolver encKeyResolver;

    /** The ID value used as the signature Reference URI attribute value, set on root SimpleXMLObject. */
    private String idValue;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        KeyPair keyPair = SecurityTestHelper.generateKeyPair("RSA", 1024, null);
        signingCredential = SecurityHelper.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());

        String encURI = EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
        Credential encCred = SecurityTestHelper.generateKeyAndCredential(encURI);
        encParams = new EncryptionParameters();
        encParams.setAlgorithm(encURI);
        encParams.setEncryptionCredential(encCred);
        encKeyResolver = new StaticKeyInfoCredentialResolver(encCred);

        idValue = "IDValueFoo";
    }

    /**
     * Test decryption of signed object and then verify signature.
     * 
     * @throws IOException
     * @throws SignatureException 
     */
    public void testDecryptAndVerifySignedElement() throws MarshallingException, ValidationException,
            UnmarshallingException, EncryptionException, DecryptionException, XMLParserException, IOException, SignatureException {
        // Get signed element
        Element signedElement = getSignedElement();

        // Unmarshall to XMLObject
        XMLObject signedXMLObject = unmarshallerFactory.getUnmarshaller(signedElement).unmarshall(signedElement);
        assertTrue(signedXMLObject instanceof SimpleXMLObject);
        SimpleXMLObject sxo = (SimpleXMLObject) signedXMLObject;

        // Encrypt object
        Encrypter encrypter = new Encrypter();
        EncryptedData encryptedData = encrypter.encryptElement(sxo, encParams);

        // Dump EncryptedData to temp file and reparse and unmarshall, just to eliminate any possible side effects
        // or error possibilities re: accidentially reusing the existing cached DOM
        // or the XMLObject instances.
        File tempfile = File.createTempFile("encdata", ".xml");
        printXML(encryptedData, tempfile.getAbsolutePath());
        InputStream input = new FileInputStream(tempfile);
        Document document = parserPool.parse(input);
        tempfile.delete();
        Element encDataElement = document.getDocumentElement();
        XMLObject encryptedXMLObject = unmarshallerFactory.getUnmarshaller(encDataElement).unmarshall(encDataElement);
        assertTrue(encryptedXMLObject instanceof EncryptedData);
        EncryptedData encryptedData2 = (EncryptedData) encryptedXMLObject;

        // Decrypt object. Use 2-arg variant to make decrypted element
        // the root of a new Document.
        Decrypter decrypter = new Decrypter(encKeyResolver, null, null);
        XMLObject decryptedXMLObject = decrypter.decryptData(encryptedData2, true);
        assertTrue(decryptedXMLObject instanceof SimpleXMLObject);
        SimpleXMLObject decryptedSXO = (SimpleXMLObject) decryptedXMLObject;

        Signature decryptedSignature = decryptedSXO.getSignature();

        // Sanity check that DOM-based ID resolution using Apache XML Security IdResolver
        // is working correctly
        Element apacheResolvedElement = IdResolver.getElementById(decryptedSignature.getDOM().getOwnerDocument(),
                idValue);
        assertNotNull("Apache ID resolver found no element", apacheResolvedElement);
        assertTrue("Apache ID resolver found different element", decryptedSXO.getDOM()
                .isSameNode(apacheResolvedElement));

        // Verify signature of the decrypted content - this is where bug was reported.
        SignatureValidator sigValidator = new SignatureValidator(signingCredential);
        sigValidator.validate(decryptedSignature);
    }

    /** Just a sanity check that unit test is set up correctly. 
     * @throws SignatureException */
    public void testPlainRoundTripSignature() throws MarshallingException, UnmarshallingException, SignatureException {
        Element signedElement = getSignedElement();

        XMLObject xmlObject = unmarshallerFactory.getUnmarshaller(signedElement).unmarshall(signedElement);
        assertTrue(xmlObject instanceof SimpleXMLObject);
        SimpleXMLObject sxo = (SimpleXMLObject) xmlObject;

        SignatureValidator sigValidator = new SignatureValidator(signingCredential);
        try {
            sigValidator.validate(sxo.getSignature());
        } catch (ValidationException e) {
            fail("Signature validation failed: " + e);
        }
    }

    /**
     * Creates a signed SimpleXMLObject element.
     * 
     * @return a XMLObject that has a Signature child element
     * @throws MarshallingException
     * @throws SignatureException 
     */
    private Element getSignedElement() throws MarshallingException, SignatureException {
        SimpleXMLObject sxo = (SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME);
        sxo.setId(idValue);

        Signature sig = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        sig.setSigningCredential(signingCredential);
        sig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        sig.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);

        DocumentInternalIDContentReference contentReference = new DocumentInternalIDContentReference(idValue);
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_ENVELOPED_SIGNATURE);
        contentReference.getTransforms().add(SignatureConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        contentReference.setDigestAlgorithm(SignatureConstants.ALGO_ID_DIGEST_SHA1);
        sig.getContentReferences().add(contentReference);

        sxo.setSignature(sig);

        Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(sxo);
        Element signedElement = marshaller.marshall(sxo);

        Signer.signObject(sig);
        return signedElement;
    }

}
