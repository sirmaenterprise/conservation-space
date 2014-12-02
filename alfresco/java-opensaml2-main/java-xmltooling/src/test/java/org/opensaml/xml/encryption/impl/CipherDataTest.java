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

package org.opensaml.xml.encryption.impl;


import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.encryption.CipherData;
import org.opensaml.xml.encryption.CipherReference;
import org.opensaml.xml.encryption.CipherValue;

/**
 *
 */
public class CipherDataTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public CipherDataTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/CipherData.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/CipherDataChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        CipherData cipherData = (CipherData) unmarshallElement(singleElementFile);
        
        assertNotNull("CipherData", cipherData);
        assertNull("CipherValue child element", cipherData.getCipherValue());
        assertNull("CipherReference child element", cipherData.getCipherReference());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        CipherData cipherData = (CipherData) unmarshallElement(childElementsFile);
        
        assertNotNull("CipherData", cipherData);
        assertNotNull("CipherValue child element", cipherData.getCipherValue());
        assertNotNull("CipherReference child element", cipherData.getCipherReference());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        CipherData cipherData = (CipherData) buildXMLObject(CipherData.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, cipherData);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        CipherData cipherData = (CipherData) buildXMLObject(CipherData.DEFAULT_ELEMENT_NAME);
        
        cipherData.setCipherValue((CipherValue) buildXMLObject(CipherValue.DEFAULT_ELEMENT_NAME));
        cipherData.setCipherReference((CipherReference) buildXMLObject(CipherReference.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, cipherData);
    }

}
