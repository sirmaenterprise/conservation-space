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
import org.opensaml.xml.encryption.CarriedKeyName;

/**
 *
 */
public class CarriedKeyNameTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedStringContent;

    /**
     * Constructor
     *
     */
    public CarriedKeyNameTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/CarriedKeyName.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedStringContent = "someKeyName";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        CarriedKeyName ckn = (CarriedKeyName) unmarshallElement(singleElementFile);
        
        assertNotNull("CarriedKeyName", ckn);
        assertEquals("CarriedKeyName value", ckn.getValue(), expectedStringContent);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        CarriedKeyName ckn = (CarriedKeyName) buildXMLObject(CarriedKeyName.DEFAULT_ELEMENT_NAME);
        ckn.setValue(expectedStringContent);
        
        assertEquals(expectedDOM, ckn);
    }

}
