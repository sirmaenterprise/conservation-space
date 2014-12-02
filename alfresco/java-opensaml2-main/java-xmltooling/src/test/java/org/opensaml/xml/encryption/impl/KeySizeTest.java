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
import org.opensaml.xml.encryption.KeySize;

/**
 *
 */
public class KeySizeTest extends XMLObjectProviderBaseTestCase {
    
    private Integer expectedIntegerContent;

    /**
     * Constructor
     *
     */
    public KeySizeTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/KeySize.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedIntegerContent = 256;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        KeySize keySize = (KeySize) unmarshallElement(singleElementFile);
        
        assertNotNull("KeySize", keySize);
        assertEquals("KeySize value", keySize.getValue(), expectedIntegerContent);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        KeySize keySize = (KeySize) buildXMLObject(KeySize.DEFAULT_ELEMENT_NAME);
        keySize.setValue(expectedIntegerContent);
        
        assertEquals(expectedDOM, keySize);
    }

}
