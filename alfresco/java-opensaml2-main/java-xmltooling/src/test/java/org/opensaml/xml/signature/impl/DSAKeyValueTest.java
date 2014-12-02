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
import org.opensaml.xml.signature.DSAKeyValue;
import org.opensaml.xml.signature.G;
import org.opensaml.xml.signature.J;
import org.opensaml.xml.signature.P;
import org.opensaml.xml.signature.PgenCounter;
import org.opensaml.xml.signature.Q;
import org.opensaml.xml.signature.Seed;
import org.opensaml.xml.signature.Y;

/**
 *
 */
public class DSAKeyValueTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public DSAKeyValueTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/DSAKeyValue.xml";
        childElementsFile = "/data/org/opensaml/xml/signature/impl/DSAKeyValueChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        DSAKeyValue keyValue = (DSAKeyValue) unmarshallElement(singleElementFile);
        
        assertNotNull("DSAKeyValue", keyValue);
        assertNull("P child element", keyValue.getP());
        assertNull("Q child element", keyValue.getQ());
        assertNull("G child element", keyValue.getG());
        assertNull("Y child element", keyValue.getY());
        assertNull("J child element", keyValue.getJ());
        assertNull("Seed element", keyValue.getSeed());
        assertNull("PgenCounter element", keyValue.getPgenCounter());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        DSAKeyValue keyValue = (DSAKeyValue) unmarshallElement(childElementsFile);
        
        assertNotNull("DSAKeyValue", keyValue);
        assertNotNull("P child element", keyValue.getP());
        assertNotNull("Q child element", keyValue.getQ());
        assertNotNull("G child element", keyValue.getG());
        assertNotNull("Y child element", keyValue.getY());
        assertNotNull("J child element", keyValue.getJ());
        assertNotNull("Seed element", keyValue.getSeed());
        assertNotNull("PgenCounter element", keyValue.getPgenCounter());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        DSAKeyValue keyValue = (DSAKeyValue) buildXMLObject(DSAKeyValue.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, keyValue);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        DSAKeyValue keyValue = (DSAKeyValue) buildXMLObject(DSAKeyValue.DEFAULT_ELEMENT_NAME);
        
        keyValue.setP((P) buildXMLObject(P.DEFAULT_ELEMENT_NAME));
        keyValue.setQ((Q) buildXMLObject(Q.DEFAULT_ELEMENT_NAME));
        keyValue.setG((G) buildXMLObject(G.DEFAULT_ELEMENT_NAME));
        keyValue.setY((Y) buildXMLObject(Y.DEFAULT_ELEMENT_NAME));
        keyValue.setJ((J) buildXMLObject(J.DEFAULT_ELEMENT_NAME));
        keyValue.setSeed((Seed) buildXMLObject(Seed.DEFAULT_ELEMENT_NAME));
        keyValue.setPgenCounter((PgenCounter) buildXMLObject(PgenCounter.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, keyValue);
    }

}
