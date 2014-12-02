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
import org.opensaml.xml.encryption.KeyReference;
import org.opensaml.xml.mock.SimpleXMLObject;

/**
 *
 */
public class KeyReferenceTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedURI;
    private int expectedNumUnknownChildren;
    
    /**
     * Constructor
     *
     */
    public KeyReferenceTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/KeyReference.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/KeyReferenceChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedURI = "urn:string:foo";
        expectedNumUnknownChildren = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        KeyReference ref = (KeyReference) unmarshallElement(singleElementFile);
        
        assertNotNull("KeyReference", ref);
        assertEquals("URI attribute", expectedURI, ref.getURI());
        assertEquals("Unknown children", 0, ref.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        KeyReference ref = (KeyReference) unmarshallElement(childElementsFile);
        
        assertNotNull("KeyReference", ref);
        assertEquals("URI attribute", expectedURI, ref.getURI());
        assertEquals("Unknown children", expectedNumUnknownChildren, ref.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        KeyReference ref = (KeyReference) buildXMLObject(KeyReference.DEFAULT_ELEMENT_NAME);
        
        ref.setURI(expectedURI);
        
        assertEquals(expectedDOM, ref);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        KeyReference ref = (KeyReference) buildXMLObject(KeyReference.DEFAULT_ELEMENT_NAME);
        
        ref.setURI(expectedURI);
        ref.getUnknownXMLObjects().add((SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        ref.getUnknownXMLObjects().add((SimpleXMLObject) buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, ref);
    }

}
