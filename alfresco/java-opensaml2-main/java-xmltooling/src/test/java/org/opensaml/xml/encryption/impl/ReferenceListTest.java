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
import org.opensaml.xml.encryption.DataReference;
import org.opensaml.xml.encryption.KeyReference;
import org.opensaml.xml.encryption.ReferenceList;

/**
 *
 */
public class ReferenceListTest extends XMLObjectProviderBaseTestCase {
    
    private int expectedNumDataRefs;
    private int expectedNumKeyRefs;
    
    /**
     * Constructor
     *
     */
    public ReferenceListTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/ReferenceList.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/ReferenceListChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedNumDataRefs = 2;
        expectedNumKeyRefs = 1;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ReferenceList rl = (ReferenceList) unmarshallElement(singleElementFile);
        
        assertNotNull("ReferenceList", rl);
        assertEquals("# of DataReference children", 0, rl.getDataReferences().size());
        assertEquals("# of KeyReference children", 0, rl.getKeyReferences().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        ReferenceList rl = (ReferenceList) unmarshallElement(childElementsFile);
        
        assertNotNull("ReferenceList", rl);
        assertEquals("# of DataReference children", expectedNumDataRefs, rl.getDataReferences().size());
        assertEquals("# of KeyReference children", expectedNumKeyRefs, rl.getKeyReferences().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        ReferenceList rl = (ReferenceList) buildXMLObject(ReferenceList.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, rl);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        ReferenceList rl = (ReferenceList) buildXMLObject(ReferenceList.DEFAULT_ELEMENT_NAME);
        
        rl.getReferences().add( (DataReference) buildXMLObject(DataReference.DEFAULT_ELEMENT_NAME));
        rl.getReferences().add( (KeyReference) buildXMLObject(KeyReference.DEFAULT_ELEMENT_NAME));
        rl.getReferences().add( (DataReference) buildXMLObject(DataReference.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, rl);
    }

}
