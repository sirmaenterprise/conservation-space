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
import org.opensaml.xml.encryption.OAEPparams;

/**
 *
 */
public class OAEPparamsTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedBase64Content;

    /**
     * Constructor
     *
     */
    public OAEPparamsTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/OAEPparams.xml";
        
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedBase64Content = "someBase64==";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        OAEPparams oaep = (OAEPparams) unmarshallElement(singleElementFile);
        
        assertNotNull("OAEPparams", oaep);
        assertEquals("OAEPparams value", oaep.getValue(), expectedBase64Content);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        OAEPparams oaep = (OAEPparams) buildXMLObject(OAEPparams.DEFAULT_ELEMENT_NAME);
        oaep.setValue(expectedBase64Content);
        
        assertEquals(expectedDOM, oaep);
    }

}
