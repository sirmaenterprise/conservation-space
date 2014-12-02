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


import javax.xml.namespace.QName;

import org.opensaml.xml.XMLObjectProviderBaseTestCase;
import org.opensaml.xml.encryption.EncryptionProperty;
import org.opensaml.xml.mock.SimpleXMLObject;

/**
 *
 */
public class EncryptionPropertyTest extends XMLObjectProviderBaseTestCase {
    
    private String expectedTarget;
    private String expectedID;
    private int expectedNumUnknownChildren;
    
    private QName expectedAttribName1;
    private QName expectedAttribName2;
    
    private String expectedAttribValue1;
    private String expectedAttribValue2;
    
    
    /**
     * Constructor
     *
     */
    public EncryptionPropertyTest() {
        singleElementFile = "/data/org/opensaml/xml/encryption/impl/EncryptionProperty.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/xml/encryption/impl/EncryptionPropertyOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/xml/encryption/impl/EncryptionPropertyChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedTarget = "urn:string:foo";
        expectedID = "someID";
        expectedNumUnknownChildren = 2;
        
        expectedAttribName1 = new QName("urn:namespace:foo", "bar", "foo");
        expectedAttribValue1 = "abc";
        
        expectedAttribName2 = new QName("urn:namespace:foo", "baz", "foo");
        expectedAttribValue2 = "123";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptionProperty ep = (EncryptionProperty) unmarshallElement(singleElementFile);
        
        assertNotNull("EncryptionProperty", ep);
        assertNull("Target attribute", ep.getTarget());
        assertNull("Id attribute", ep.getID());
        assertEquals("Unknown children", 0, ep.getUnknownXMLObjects().size());
        assertNull("Unknown attribute 1", ep.getUnknownAttributes().get(expectedAttribName1));
        assertNull("Unknown attribute 2", ep.getUnknownAttributes().get(expectedAttribName2));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EncryptionProperty ep = (EncryptionProperty) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("EncryptionProperty", ep);
        assertEquals("Target attribute", expectedTarget, ep.getTarget());
        assertEquals("Id attribute", expectedID, ep.getID());
        assertEquals("Unknown children", 0, ep.getUnknownXMLObjects().size());
        assertEquals("Unknown attribute 1", expectedAttribValue1, ep.getUnknownAttributes().get(expectedAttribName1));
        assertEquals("Unknown attribute 2", expectedAttribValue2, ep.getUnknownAttributes().get(expectedAttribName2));
        
        assertEquals("ID lookup failed", ep, ep.resolveID(expectedID));
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptionProperty ep = (EncryptionProperty) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptionProperty", ep);
        assertNull("Target attribute", ep.getTarget());
        assertNull("Id attribute", ep.getID());
        assertEquals("Unknown children", expectedNumUnknownChildren, ep.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptionProperty ep = (EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME);
        
        assertEquals(expectedDOM, ep);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        EncryptionProperty ep = (EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME);
        
        ep.setTarget(expectedTarget);
        ep.setID(expectedID);
        ep.getUnknownAttributes().put(expectedAttribName1, expectedAttribValue1);
        ep.getUnknownAttributes().put(expectedAttribName2, expectedAttribValue2);
        
        assertEquals(expectedOptionalAttributesDOM, ep);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptionProperty ep = (EncryptionProperty) buildXMLObject(EncryptionProperty.DEFAULT_ELEMENT_NAME);
        
        ep.getUnknownXMLObjects().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        ep.getUnknownXMLObjects().add( buildXMLObject(SimpleXMLObject.ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, ep);
    }

}
