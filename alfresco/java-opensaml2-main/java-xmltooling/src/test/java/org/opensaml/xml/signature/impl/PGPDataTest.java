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
import org.opensaml.xml.signature.PGPData;
import org.opensaml.xml.signature.PGPKeyID;
import org.opensaml.xml.signature.PGPKeyPacket;

/**
 *
 */
public class PGPDataTest extends XMLObjectProviderBaseTestCase {
    
    
    /**
     * Constructor
     *
     */
    public PGPDataTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/PGPData.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/PGPDataChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        PGPData pgpData = (PGPData) unmarshallElement(singleElementFile);
        
        assertNotNull("PGPData", pgpData);
        assertNull("PGPKeyID child element", pgpData.getPGPKeyID());
        assertNull("PGPKeyPacket child element", pgpData.getPGPKeyPacket());
        assertEquals("# of other XMLObject children", 0, pgpData.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        PGPData pgpData = (PGPData) unmarshallElement(childElementsFile);
        
        assertNotNull("PGPData", pgpData);
        assertNotNull("PGPKeyID child element", pgpData.getPGPKeyID());
        assertNotNull("PGPKeyPacket child element", pgpData.getPGPKeyPacket());
        assertEquals("# of other XMLObject children", 2, pgpData.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        PGPData pgpData = (PGPData) buildXMLObject(PGPData.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, pgpData);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        PGPData pgpData = (PGPData) buildXMLObject(PGPData.DEFAULT_ELEMENT_NAME);
        
        pgpData.setPGPKeyID((PGPKeyID) buildXMLObject(PGPKeyID.DEFAULT_ELEMENT_NAME));
        pgpData.setPGPKeyPacket((PGPKeyPacket) buildXMLObject(PGPKeyPacket.DEFAULT_ELEMENT_NAME));
        pgpData.getUnknownXMLObjects().add(buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        pgpData.getUnknownXMLObjects().add(buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, pgpData);
    }

}
