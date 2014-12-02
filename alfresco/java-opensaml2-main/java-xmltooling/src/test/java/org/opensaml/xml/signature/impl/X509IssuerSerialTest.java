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
import org.opensaml.xml.signature.X509IssuerName;
import org.opensaml.xml.signature.X509IssuerSerial;
import org.opensaml.xml.signature.X509SerialNumber;

/**
 *
 */
public class X509IssuerSerialTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public X509IssuerSerialTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/X509IssuerSerial.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/X509IssuerSerialChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        X509IssuerSerial x509Element = (X509IssuerSerial) unmarshallElement(singleElementFile);
        
        assertNotNull("X509IssuerSerial", x509Element);
        assertNull("X509IssuerName child element", x509Element.getX509IssuerName());
        assertNull("X509SerialNumber child element", x509Element.getX509SerialNumber());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        X509IssuerSerial x509Element = (X509IssuerSerial) unmarshallElement(childElementsFile);
        
        assertNotNull("X509IssuerSerial", x509Element);
        assertNotNull("X509IssuerName child element", x509Element.getX509IssuerName());
        assertNotNull("X509SerialNumber child element", x509Element.getX509SerialNumber());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        X509IssuerSerial x509Element = (X509IssuerSerial) buildXMLObject(X509IssuerSerial.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, x509Element);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        X509IssuerSerial x509Element = (X509IssuerSerial) buildXMLObject(X509IssuerSerial.DEFAULT_ELEMENT_NAME);
        
        x509Element.setX509IssuerName((X509IssuerName) buildXMLObject(X509IssuerName.DEFAULT_ELEMENT_NAME));
        x509Element.setX509SerialNumber((X509SerialNumber) buildXMLObject(X509SerialNumber.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, x509Element);
    }

}
