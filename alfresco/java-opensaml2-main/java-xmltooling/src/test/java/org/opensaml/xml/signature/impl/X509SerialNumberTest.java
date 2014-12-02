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


import java.math.BigInteger;

import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.signature.X509SerialNumber;

/**
 *
 */
public class X509SerialNumberTest extends XMLObjectProviderBaseTestCase {
    
    private BigInteger expectedBigIntegerContent;

    /**
     * Constructor.
     *
     */
    public X509SerialNumberTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/X509SerialNumber.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedBigIntegerContent = new BigInteger("123456789");
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        X509SerialNumber x509Element = (X509SerialNumber) unmarshallElement(singleElementFile);
        
        assertNotNull("X509SerialNumber", x509Element);
        assertEquals("X509SerialNumber value", x509Element.getValue(), expectedBigIntegerContent);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        X509SerialNumber x509Element = (X509SerialNumber) buildXMLObject(X509SerialNumber.DEFAULT_ELEMENT_NAME);
        x509Element.setValue(expectedBigIntegerContent);
        
        assertEquals(expectedDOM, x509Element);
    }

}
