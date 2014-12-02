/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.KeyDescriptorImpl}.
 */
public class KeyDescriptorTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Name attribute value. */
    protected UsageType expectedUse;
    
    /** Expected number of EncrptionMethod children. */
    protected int expectedNumEncMethods;

    /**
     * Constructor.
     */
    public KeyDescriptorTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/KeyDescriptor.xml";
        singleElementOptionalAttributesFile = 
                "/data/org/opensaml/saml2/metadata/impl/KeyDescriptorOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/KeyDescriptorChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedUse = UsageType.ENCRYPTION;
        expectedNumEncMethods = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        KeyDescriptor keyDescriptor = (KeyDescriptor) unmarshallElement(singleElementFile);
        
        assertNotNull("KeyDescriptor", keyDescriptor);
        assertEquals("Unexpected use attribute value", UsageType.UNSPECIFIED, keyDescriptor.getUse());

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        KeyDescriptor keyDescriptor = (KeyDescriptor) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("KeyDescriptor", keyDescriptor);
        assertEquals("Use attribute", expectedUse, keyDescriptor.getUse());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        KeyDescriptor keyDescriptor = (KeyDescriptor) unmarshallElement(childElementsFile);

        assertNotNull("KeyDescriptor", keyDescriptor);
        assertNotNull("KeyInfo Child element", keyDescriptor.getKeyInfo());
        assertEquals("# of EncryptionMethod child elements", expectedNumEncMethods,
                keyDescriptor.getEncryptionMethods().size());
   }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, KeyDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, 
                SAMLConstants.SAML20MD_PREFIX);
        KeyDescriptor keyDescriptor = (KeyDescriptor) buildXMLObject(qname);

        assertEquals(expectedDOM, keyDescriptor);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, KeyDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, 
                SAMLConstants.SAML20MD_PREFIX);
        KeyDescriptor keyDescriptor = (KeyDescriptor) buildXMLObject(qname);

        keyDescriptor.setUse(UsageType.ENCRYPTION);

        assertEquals(expectedOptionalAttributesDOM, keyDescriptor);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, KeyDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, 
                SAMLConstants.SAML20MD_PREFIX);
        KeyDescriptor keyDescriptor = (KeyDescriptor) buildXMLObject(qname);
        
        keyDescriptor.setKeyInfo((KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
        keyDescriptor.getEncryptionMethods()
            .add((EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));
        keyDescriptor.getEncryptionMethods()
            .add((EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME));

        assertEquals(expectedChildElementsDOM, keyDescriptor);
    }
}