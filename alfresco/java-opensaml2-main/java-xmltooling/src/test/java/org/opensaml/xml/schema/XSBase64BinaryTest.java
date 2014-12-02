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

package org.opensaml.xml.schema;

import javax.xml.namespace.QName;

import org.opensaml.xml.XMLObjectBaseTestCase;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.impl.XSBase64BinaryBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for {@link XSBase64Binary}
 */
public class XSBase64BinaryTest extends XMLObjectBaseTestCase {
    
    private String testDocumentLocation;
    private QName expectedXMLObjectQName;
    private String expectedValue;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception{
        super.setUp();
        testDocumentLocation = "/data/org/opensaml/xml/schema/xsBase64Binary.xml";
        expectedXMLObjectQName = new QName("urn:example.org:foo", "bar", "foo");
        expectedValue = "abcdABCDE===";
    }

    /**
     * Tests Marshalling a base64Binary type.
     * @throws MarshallingException 
     * @throws XMLParserException 
     */
    public void testMarshall() throws MarshallingException, XMLParserException{
        XSBase64BinaryBuilder xsb64bBuilder = (XSBase64BinaryBuilder) builderFactory.getBuilder(XSBase64Binary.TYPE_NAME);
        XSBase64Binary xsb64b = xsb64bBuilder.buildObject(expectedXMLObjectQName, XSBase64Binary.TYPE_NAME);
        xsb64b.setValue(expectedValue);
        
        Marshaller marshaller = marshallerFactory.getMarshaller(xsb64b);
        Element result = marshaller.marshall(xsb64b);
        
        Document document = parserPool.parse(XSBase64BinaryTest.class.getResourceAsStream(testDocumentLocation));
        assertEquals("Marshalled XSBase64Binary does not match example document", document, xsb64b);
    }
    
    /**
     * Tests Marshalling a base64Binary type.
     * 
     * @throws XMLParserException 
     * @throws UnmarshallingException 
     */
    public void testUnmarshall() throws XMLParserException, UnmarshallingException{
        Document document = parserPool.parse(XSBase64BinaryTest.class.getResourceAsStream(testDocumentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        XSBase64Binary xsb64b = (XSBase64Binary) unmarshaller.unmarshall(document.getDocumentElement());
        
        assertEquals("Unexpected XSBase64Binary QName", expectedXMLObjectQName, xsb64b.getElementQName());
        assertEquals("Unexpected XSBase64Binary schema type", XSBase64Binary.TYPE_NAME, xsb64b.getSchemaType());
        assertEquals("Unexpected value of XSBase64Binary", xsb64b.getValue(), expectedValue);
    }
}