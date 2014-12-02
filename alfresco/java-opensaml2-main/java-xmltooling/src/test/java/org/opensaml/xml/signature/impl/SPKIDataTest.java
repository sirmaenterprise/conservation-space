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
import org.opensaml.xml.signature.SPKIData;
import org.opensaml.xml.signature.SPKISexp;

/**
 *
 */
public class SPKIDataTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public SPKIDataTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/SPKIData.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/SPKIDataChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SPKIData spkiData = (SPKIData) unmarshallElement(singleElementFile);
        
        assertNotNull("SPKIData", spkiData);
        assertEquals("Total # of XMLObject child elements", 0, spkiData.getXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        SPKIData spkiData = (SPKIData) unmarshallElement(childElementsFile);
        
        assertNotNull("SPKIData", spkiData);
        assertEquals("Total # of XMLObject child elements", 4, spkiData.getXMLObjects().size());
        assertEquals("# of SPKISexp child elements", 2, spkiData.getSPKISexps().size());
        assertEquals("# of SimpleElement child elements", 2, spkiData.getXMLObjects(SimpleXMLObject.ELEMENT_NAME).size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        SPKIData spkiData = (SPKIData) buildXMLObject(SPKIData.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, spkiData);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        SPKIData spkiData = (SPKIData) buildXMLObject(SPKIData.DEFAULT_ELEMENT_NAME);
        
        spkiData.getXMLObjects().add(buildXMLObject(SPKISexp.DEFAULT_ELEMENT_NAME));
        spkiData.getXMLObjects().add(buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        spkiData.getXMLObjects().add(buildXMLObject(SPKISexp.DEFAULT_ELEMENT_NAME));
        spkiData.getXMLObjects().add(buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, spkiData);
    }

}
