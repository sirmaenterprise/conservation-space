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
import org.opensaml.xml.encryption.OriginatorKeyInfo;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.signature.KeyName;
import org.opensaml.xml.signature.KeyValue;
import org.opensaml.xml.signature.MgmtData;
import org.opensaml.xml.signature.PGPData;
import org.opensaml.xml.signature.RetrievalMethod;
import org.opensaml.xml.signature.SPKIData;
import org.opensaml.xml.signature.X509Data;

/**
 *
 */
public class OriginatorKeyInfoTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedID;
    
    /**
     * Constructor
     *
     */
    public OriginatorKeyInfoTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/OriginatorKeyInfo.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/encryption/impl/OriginatorKeyInfoOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/OriginatorKeyInfoChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedID = "abc123";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) unmarshallElement(singleElementFile);
        
        assertNotNull("OriginatorKeyInfo", keyInfo);
        assertNull("Id attribute", keyInfo.getID());
        assertEquals("Total # of XMLObject child elements", 0, keyInfo.getXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("OriginatorKeyInfo", keyInfo);
        assertEquals("Id attribute", expectedID, keyInfo.getID());
        assertEquals("Total # of XMLObject child elements", 0, keyInfo.getXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) unmarshallElement(childElementsFile);
        
        assertNotNull("OriginatorKeyInfo", keyInfo);
        assertEquals("Total # of XMLObject child elements", 11, keyInfo.getXMLObjects().size());
        assertEquals("# of KeyName child elements", 2, keyInfo.getKeyNames().size());
        assertEquals("# of KeyValue child elements", 2, keyInfo.getKeyValues().size());
        assertEquals("# of RetrievalMethod child elements", 1, keyInfo.getRetrievalMethods().size());
        assertEquals("# of X509Data child elements", 2, keyInfo.getX509Datas().size());
        assertEquals("# of PGPData child elements", 1, keyInfo.getPGPDatas().size());
        assertEquals("# of SPKIData child elements", 1, keyInfo.getSPKIDatas().size());
        assertEquals("# of MgmtData child elements", 1, keyInfo.getMgmtDatas().size());
        assertEquals("# of SimpleElement child elements", 1, keyInfo.getXMLObjects(SimpleXMLObject.ELEMENT_NAME).size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) buildXMLObject(OriginatorKeyInfo.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, keyInfo);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) buildXMLObject(OriginatorKeyInfo.DEFAULT_ELEMENT_NAME);
        
        keyInfo.setID(expectedID);
        
        assertEquals(expectedOptionalAttributesDOM, keyInfo);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        OriginatorKeyInfo keyInfo = (OriginatorKeyInfo) buildXMLObject(OriginatorKeyInfo.DEFAULT_ELEMENT_NAME);
        
        keyInfo.getXMLObjects().add(buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(KeyValue.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME));
        
        keyInfo.getXMLObjects().add(buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(KeyValue.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME));
        
        keyInfo.getXMLObjects().add(buildXMLObject(RetrievalMethod.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(PGPData.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(SPKIData.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(MgmtData.DEFAULT_ELEMENT_NAME));
        keyInfo.getXMLObjects().add(buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, keyInfo);
    }

}
