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
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.signature.Transform;
import org.opensaml.xml.signature.XPath;

/**
 *
 */
public class TransformTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedAlgorithm;
    private int expectedTotalChildren;
    private int expectedXPathChildren;
    
    /**
     * Constructor
     *
     */
    public TransformTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/Transform.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/TransformChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedAlgorithm = "urn:string:foo";
        expectedTotalChildren = 5;
        expectedXPathChildren = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Transform transform = (Transform) unmarshallElement(singleElementFile);
        
        assertNotNull("Transform", transform);
        assertEquals("Algorithm attribute", expectedAlgorithm, transform.getAlgorithm());
        assertEquals("Total children", 0, transform.getAllChildren().size());
        assertEquals("XPath children", 0, transform.getXPaths().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        Transform transform = (Transform) unmarshallElement(childElementsFile);
        
        assertNotNull("Transform", transform);
        assertEquals("Algorithm attribute", expectedAlgorithm, transform.getAlgorithm());
        assertEquals("Total children", expectedTotalChildren, transform.getAllChildren().size());
        assertEquals("XPath children", expectedXPathChildren, transform.getXPaths().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        Transform transform = (Transform) buildXMLObject(Transform.DEFAULT_ELEMENT_NAME);
        
        transform.setAlgorithm(expectedAlgorithm);
        
        assertEquals(expectedDOM, transform);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        Transform transform = (Transform) buildXMLObject(Transform.DEFAULT_ELEMENT_NAME);
        
        transform.setAlgorithm(expectedAlgorithm);
        transform.getAllChildren().add( buildXMLObject(XPath.DEFAULT_ELEMENT_NAME));
        transform.getAllChildren().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        transform.getAllChildren().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        transform.getAllChildren().add( buildXMLObject(XPath.DEFAULT_ELEMENT_NAME));
        transform.getAllChildren().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        // TODO investigate - XMLUnit does not fail the test if the 
        // above children are added in a different order than the control 
        // XML file.  XMLTooling *does* marshall them in the correct order.
        assertEquals(expectedChildElementsDOM, transform);
    }

}
