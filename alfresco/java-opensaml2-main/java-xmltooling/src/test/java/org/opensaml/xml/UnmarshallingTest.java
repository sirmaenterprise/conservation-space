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

package org.opensaml.xml;

import java.util.List;

import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.mock.SimpleXMLObject;
import org.opensaml.xml.parse.XMLParserException;
import org.w3c.dom.Document;

/**
 * Unit test for unmarshalling functions.
 */
public class UnmarshallingTest extends XMLObjectBaseTestCase {

    /**
     * Constructor
     */
    public UnmarshallingTest() {
        super();
    }

    /**
     * Tests unmarshalling an element that has attributes.
     * 
     * @throws XMLParserException
     * @throws UnmarshallingException
     */
    public void testUnmarshallingWithAttributes() throws XMLParserException, UnmarshallingException {
        String expectedId = "Firefly";
        String documentLocation = "/data/org/opensaml/xml/SimpleXMLObjectWithAttribute.xml";
        Document document = parserPool.parse(UnmarshallingTest.class.getResourceAsStream(documentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        SimpleXMLObject sxObject = (SimpleXMLObject) unmarshaller.unmarshall(document.getDocumentElement());

        assertNotNull("DOM was not cached after unmarshalling", sxObject.getDOM());
        assertEquals("ID was not expected value", expectedId, sxObject.getId());
    }

    /**
     * Tests unmarshalling an element with content.
     * 
     * @throws XMLParserException
     * @throws UnmarshallingException
     */
    public void testUnmarshallingWithElementContent() throws XMLParserException, UnmarshallingException {
        String documentLocation = "/data/org/opensaml/xml/SimpleXMLObjectWithContent.xml";
        Document document = parserPool.parse(UnmarshallingTest.class.getResourceAsStream(documentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        SimpleXMLObject sxObject = (SimpleXMLObject) unmarshaller.unmarshall(document.getDocumentElement());
        
        assertNotNull("DOM was not cached after unmarshalling", sxObject.getDOM());
        
        List<SimpleXMLObject> children = sxObject.getSimpleXMLObjects();
        assertEquals("Unexpected number of children", 3, children.size());
        
        SimpleXMLObject child1 = children.get(0);
        assertEquals("Unexpected value (text content) for child 1", "Content1", child1.getValue());
        
        SimpleXMLObject child2 = children.get(1);
        assertEquals("Unexpected value (text content) for child 2", "Content2", child2.getValue());
        
        SimpleXMLObject child3 = children.get(2);
        assertNull("Child had text content when it should not", child3.getValue());
        
        List<SimpleXMLObject> grandChildren = child3.getSimpleXMLObjects();
        assertEquals("Unexpected number of grandchildren (children for child 3)", 1, grandChildren.size());
        
        SimpleXMLObject grandChild1 = grandChildren.get(0);
        assertEquals("Unexpected value (text content) for grandchild 1", "Content3", grandChild1.getValue());
    }

    /**
     * Tests unmarshalling an element with child elements.
     * 
     * @throws XMLParserException
     * @throws MarshallingException
     */
    public void testUnmarshallingWithChildElements() throws XMLParserException, UnmarshallingException {
        String documentLocation = "/data/org/opensaml/xml/SimpleXMLObjectWithChildren.xml";
        Document document = parserPool.parse(UnmarshallingTest.class.getResourceAsStream(documentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        SimpleXMLObject sxObject = (SimpleXMLObject) unmarshaller.unmarshall(document.getDocumentElement());

        assertNotNull("DOM was not cached after unmarshalling", sxObject.getDOM());
        
        assertEquals("Number of children elements was not expected value", 2, sxObject.getSimpleXMLObjects().size());
    }
}