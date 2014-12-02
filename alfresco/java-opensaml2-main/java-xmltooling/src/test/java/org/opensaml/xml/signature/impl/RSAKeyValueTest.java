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

package org.opensaml.xml.signature.impl;


import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.signature.Exponent;
import org.opensaml.xml.signature.Modulus;
import org.opensaml.xml.signature.RSAKeyValue;

/**
 *
 */
public class RSAKeyValueTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public RSAKeyValueTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/RSAKeyValue.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/RSAKeyValueChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        RSAKeyValue keyValue = (RSAKeyValue) unmarshallElement(singleElementFile);
        
        assertNotNull("RSAKeyValue", keyValue);
        assertNull("Modulus child element", keyValue.getModulus());
        assertNull("Exponent child element", keyValue.getExponent());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        RSAKeyValue keyValue = (RSAKeyValue) unmarshallElement(childElementsFile);
        
        assertNotNull("RSAKeyValue", keyValue);
        assertNotNull("Modulus child element", keyValue.getModulus());
        assertNotNull("Exponent child element", keyValue.getExponent());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        RSAKeyValue keyValue = (RSAKeyValue) buildXMLObject(RSAKeyValue.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, keyValue);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        RSAKeyValue keyValue = (RSAKeyValue) buildXMLObject(RSAKeyValue.DEFAULT_ELEMENT_NAME);
        
        keyValue.setModulus((Modulus) buildXMLObject(Modulus.DEFAULT_ELEMENT_NAME));
        keyValue.setExponent((Exponent) buildXMLObject(Exponent.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, keyValue);
    }

}
