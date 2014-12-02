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

package org.opensaml.saml2.core.impl;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.xml.encryption.EncryptedData;
import org.opensaml.xml.encryption.EncryptedKey;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.EncryptedAssertionImpl}.
 */
public class EncryptedAssertionTest extends BaseSAMLObjectProviderTestCase {

    /** Count of EncryptedKey subelements. */
    private int encryptedKeyCount = 3;

    /** Constructor. */
    public EncryptedAssertionTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/EncryptedAssertion.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/EncryptedAssertionChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptedAssertion encElement = (EncryptedAssertion) unmarshallElement(singleElementFile);

        assertNotNull(encElement);
        assertNull("EncryptedData child element", encElement.getEncryptedData());
        assertEquals("# of EncryptedKey children", 0, encElement.getEncryptedKeys().size());
    }
    
    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptedAssertion encElement = (EncryptedAssertion) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptedAssertion was null", encElement);
        assertNotNull("EncryptedData child element", encElement.getEncryptedData());
        assertEquals("# of EncryptedKey children", encryptedKeyCount, encElement.getEncryptedKeys().size());

    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptedAssertion encElement = (EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME);

        assertEquals(expectedDOM, encElement);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptedAssertion encElement = (EncryptedAssertion) buildXMLObject(EncryptedAssertion.DEFAULT_ELEMENT_NAME);
        
        encElement.setEncryptedData((EncryptedData) buildXMLObject(EncryptedData.DEFAULT_ELEMENT_NAME));
        for (int i=0; i < encryptedKeyCount; i++) {
            encElement.getEncryptedKeys().add((EncryptedKey) buildXMLObject(EncryptedKey.DEFAULT_ELEMENT_NAME));
        }
        
        assertEquals(expectedChildElementsDOM, encElement);
    }
}