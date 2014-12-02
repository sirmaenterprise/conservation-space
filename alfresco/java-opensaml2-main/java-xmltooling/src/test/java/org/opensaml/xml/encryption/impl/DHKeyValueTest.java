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
import org.opensaml.xml.encryption.DHKeyValue;
import org.opensaml.xml.encryption.Generator;
import org.opensaml.xml.encryption.P;
import org.opensaml.xml.encryption.PgenCounter;
import org.opensaml.xml.encryption.Public;
import org.opensaml.xml.encryption.Q;
import org.opensaml.xml.encryption.Seed;

/**
 *
 */
public class DHKeyValueTest extends XMLObjectProviderBaseTestCase {
    
    /**
     * Constructor
     *
     */
    public DHKeyValueTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/DHKeyValue.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/DHKeyValueChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        DHKeyValue keyValue = (DHKeyValue) unmarshallElement(singleElementFile);
        
        assertNotNull("DHKeyValue", keyValue);
        assertNull("P child element", keyValue.getP());
        assertNull("Q child element", keyValue.getQ());
        assertNull("Generator child element", keyValue.getGenerator());
        assertNull("Public child element", keyValue.getPublic());
        assertNull("seed element", keyValue.getSeed());
        assertNull("pgenCounter element", keyValue.getPgenCounter());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        DHKeyValue keyValue = (DHKeyValue) unmarshallElement(childElementsFile);
        
        assertNotNull("DHKeyValue", keyValue);
        assertNotNull("P child element", keyValue.getP());
        assertNotNull("Q child element", keyValue.getQ());
        assertNotNull("Generator child element", keyValue.getGenerator());
        assertNotNull("Public child element", keyValue.getPublic());
        assertNotNull("seed element", keyValue.getSeed());
        assertNotNull("pgenCounter element", keyValue.getPgenCounter());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        DHKeyValue keyValue = (DHKeyValue) buildXMLObject(DHKeyValue.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, keyValue);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        DHKeyValue keyValue = (DHKeyValue) buildXMLObject(DHKeyValue.DEFAULT_ELEMENT_NAME);
        
        keyValue.setP((P) buildXMLObject(P.DEFAULT_ELEMENT_NAME));
        keyValue.setQ((Q) buildXMLObject(Q.DEFAULT_ELEMENT_NAME));
        keyValue.setGenerator((Generator) buildXMLObject(Generator.DEFAULT_ELEMENT_NAME));
        keyValue.setPublic((Public) buildXMLObject(Public.DEFAULT_ELEMENT_NAME));
        keyValue.setSeed((Seed) buildXMLObject(Seed.DEFAULT_ELEMENT_NAME));
        keyValue.setPgenCounter((PgenCounter) buildXMLObject(PgenCounter.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, keyValue);
    }

}
