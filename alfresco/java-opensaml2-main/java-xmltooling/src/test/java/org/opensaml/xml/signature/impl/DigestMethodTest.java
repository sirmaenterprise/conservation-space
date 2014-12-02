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
import org.opensaml.xml.signature.DigestMethod;

public class DigestMethodTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedAlgorithm;
    private int expectedTotalChildren;
    
    /**
     * Constructor.
     *
     */
    public DigestMethodTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/DigestMethod.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/DigestMethodChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedAlgorithm = "urn:string:foo";
        expectedTotalChildren = 3;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        DigestMethod digestMethod = (DigestMethod) unmarshallElement(singleElementFile);
        
        assertNotNull("DigestMethod", digestMethod);
        assertEquals("Algorithm attribute", expectedAlgorithm, digestMethod.getAlgorithm());
        assertEquals("Total children", 0, digestMethod.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        DigestMethod digestMethod = (DigestMethod) unmarshallElement(childElementsFile);
        
        assertNotNull("DigestMethod", digestMethod);
        assertEquals("Algorithm attribute", expectedAlgorithm, digestMethod.getAlgorithm());
        assertEquals("Total children", expectedTotalChildren, digestMethod.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        DigestMethod digestMethod = (DigestMethod) buildXMLObject(DigestMethod.DEFAULT_ELEMENT_NAME);
        
        digestMethod.setAlgorithm(expectedAlgorithm);
        
        assertEquals(expectedDOM, digestMethod);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        DigestMethod digestMethod = (DigestMethod) buildXMLObject(DigestMethod.DEFAULT_ELEMENT_NAME);
        
        digestMethod.setAlgorithm(expectedAlgorithm);
        digestMethod.getUnknownXMLObjects().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        digestMethod.getUnknownXMLObjects().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        digestMethod.getUnknownXMLObjects().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, digestMethod);
    }

}
