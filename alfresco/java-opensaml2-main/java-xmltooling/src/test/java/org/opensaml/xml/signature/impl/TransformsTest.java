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
import org.opensaml.xml.signature.Transforms;
import org.opensaml.xml.signature.Transform;

/**
 *
 */
public class TransformsTest extends XMLObjectProviderBaseTestCase {
    
    private int expectedNumTransforms;
    
    /**
     * Constructor
     *
     */
    public TransformsTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/Transforms.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/TransformsChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedNumTransforms = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Transforms em = (Transforms) unmarshallElement(singleElementFile);
        
        assertNotNull("Transforms", em);
        assertEquals("Transform children", 0, em.getTransforms().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Transforms em = (Transforms) unmarshallElement(childElementsFile);
        
        assertNotNull("Transforms", em);
        assertEquals("Transform children", expectedNumTransforms, em.getTransforms().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        Transforms em = (Transforms) buildXMLObject(Transforms.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, em);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        Transforms em = (Transforms) buildXMLObject(Transforms.DEFAULT_ELEMENT_NAME);
        
        em.getTransforms().add( (Transform) buildXMLObject(Transform.DEFAULT_ELEMENT_NAME));
        em.getTransforms().add( (Transform) buildXMLObject(Transform.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, em);
    }

}
