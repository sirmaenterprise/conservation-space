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
import org.opensaml.xml.signature.RetrievalMethod;
import org.opensaml.xml.signature.Transforms;

/**
 *
 */
public class RetrievalMethodTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedURI;
    private String expectedType;
    
    /**
     * Constructor
     *
     */
    public RetrievalMethodTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/RetrievalMethod.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/RetrievalMethodChildElements.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/signature/impl/RetrievalMethodOptionalAttributes.xml"; 
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedURI = "urn:string:foo";
        expectedType = "someType";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        RetrievalMethod rm = (RetrievalMethod) unmarshallElement(singleElementFile);
        
        assertNotNull("RetrievalMethod", rm);
        assertEquals("URI attribute", expectedURI, rm.getURI());
        assertNull("Transforms child element", rm.getTransforms());
    }
    
    

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        RetrievalMethod rm = (RetrievalMethod) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("RetrievalMethod", rm);
        assertEquals("URI attribute", expectedURI, rm.getURI());
        assertEquals("Type attribute", expectedType, rm.getType());
        assertNull("Transforms child element", rm.getTransforms());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        RetrievalMethod rm = (RetrievalMethod) unmarshallElement(childElementsFile);
        
        assertNotNull("RetrievalMethod", rm);
        assertEquals("URI attribute", expectedURI, rm.getURI());
        assertNotNull("Transforms child element", rm.getTransforms());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        RetrievalMethod rm = (RetrievalMethod) buildXMLObject(RetrievalMethod.DEFAULT_ELEMENT_NAME);
        
        rm.setURI(expectedURI);
        
        assertEquals(expectedDOM, rm);
    }
    
    

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        RetrievalMethod rm = (RetrievalMethod) buildXMLObject(RetrievalMethod.DEFAULT_ELEMENT_NAME);
        
        rm.setURI(expectedURI);
        rm.setType(expectedType);
        
        assertEquals(expectedOptionalAttributesDOM, rm);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        RetrievalMethod rm = (RetrievalMethod) buildXMLObject(RetrievalMethod.DEFAULT_ELEMENT_NAME);
        
        rm.setURI(expectedURI);
        rm.setTransforms((Transforms) buildXMLObject(Transforms.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, rm);
    }

}
