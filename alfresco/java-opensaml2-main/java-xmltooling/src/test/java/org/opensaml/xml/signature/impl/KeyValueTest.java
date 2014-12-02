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
import org.opensaml.xml.signature.KeyValue;
import org.opensaml.xml.signature.RSAKeyValue;

/**
 *
 */
public class KeyValueTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public KeyValueTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/KeyValue.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/KeyValueChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        KeyValue keyValue = (KeyValue) unmarshallElement(singleElementFile);
        
        assertNotNull("KeyValue", keyValue);
        assertNull("RSAKeyValue child element", keyValue.getRSAKeyValue());
        assertNull("DSAKeyValue child element", keyValue.getDSAKeyValue());
        assertNull("Wildcard child element", keyValue.getUnknownXMLObject());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        KeyValue keyValue = (KeyValue) unmarshallElement(childElementsFile);
        
        assertNotNull("KeyValue", keyValue);
        assertNotNull("RSAKeyValue child element", keyValue.getRSAKeyValue());
        assertNull("DSAKeyValue child element", keyValue.getDSAKeyValue());
        assertNull("Wildcard child element", keyValue.getUnknownXMLObject());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        KeyValue keyValue = (KeyValue) buildXMLObject(KeyValue.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, keyValue);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        KeyValue keyValue = (KeyValue) buildXMLObject(KeyValue.DEFAULT_ELEMENT_NAME);
        
        keyValue.setRSAKeyValue((RSAKeyValue) buildXMLObject(RSAKeyValue.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, keyValue);
    }

}
