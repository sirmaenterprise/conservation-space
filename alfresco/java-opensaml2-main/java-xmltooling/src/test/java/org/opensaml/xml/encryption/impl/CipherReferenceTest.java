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
import org.opensaml.xml.encryption.CipherReference;
import org.opensaml.xml.encryption.Transforms;

/**
 *
 */
public class CipherReferenceTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedURI;
    
    /**
     * Constructor
     *
     */
    public CipherReferenceTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/CipherReference.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/CipherReferenceChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedURI = "urn:string:foo";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        CipherReference cr = (CipherReference) unmarshallElement(singleElementFile);
        
        assertNotNull("CipherReference", cr);
        assertEquals("URI attribute", expectedURI, cr.getURI());
        assertNull("Transforms child", cr.getTransforms());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        CipherReference cr = (CipherReference) unmarshallElement(childElementsFile);
        
        assertNotNull("CipherReference", cr);
        assertEquals("URI attribute", expectedURI, cr.getURI());
        assertNotNull("Transforms child", cr.getTransforms());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        CipherReference cr = (CipherReference) buildXMLObject(CipherReference.DEFAULT_ELEMENT_NAME);
        
        cr.setURI(expectedURI);
        
        assertEquals(expectedDOM, cr);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        CipherReference cr = (CipherReference) buildXMLObject(CipherReference.DEFAULT_ELEMENT_NAME);
        
        cr.setURI(expectedURI);
        cr.setTransforms((Transforms) buildXMLObject(Transforms.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, cr);
    }

}
