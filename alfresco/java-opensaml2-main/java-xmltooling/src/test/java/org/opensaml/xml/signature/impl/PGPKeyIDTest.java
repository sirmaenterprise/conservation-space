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
import org.opensaml.xml.signature.PGPKeyID;

/**
 *
 */
public class PGPKeyIDTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedStringContent;

    /**
     * Constructor
     *
     */
    public PGPKeyIDTest() {
        singleElementFile = "/data/org/opensaml/xml/signature/impl/PGPKeyID.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedStringContent = "somePGPKeyID";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        PGPKeyID pgpElement = (PGPKeyID) unmarshallElement(singleElementFile);
        
        assertNotNull("PGPKeyID", pgpElement);
        assertEquals("PGPKeyID value", pgpElement.getValue(), expectedStringContent);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        PGPKeyID pgpElement = (PGPKeyID) buildXMLObject(PGPKeyID.DEFAULT_ELEMENT_NAME);
        pgpElement.setValue(expectedStringContent);
        
        assertEquals(expectedDOM, pgpElement);
    }

}
