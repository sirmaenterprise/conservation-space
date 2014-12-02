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
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for {@link XSString}
 */
public class XSStringTest extends XMLObjectBaseTestCase {
    
    private String testDocumentLocation;
    private QName expectedXMLObjectQName;
    private String expectedValue;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception{
        super.setUp();
        testDocumentLocation = "/data/org/opensaml/xml/schema/xsString.xml";
        expectedXMLObjectQName = new QName("urn:example.org:foo", "bar", "foo");
        expectedValue = "test";
    }

    /**
     * Tests Marshalling a string type.
     * @throws MarshallingException 
     * @throws XMLParserException 
     */
    public void testMarshall() throws MarshallingException, XMLParserException{
        XSStringBuilder xssBuilder = (XSStringBuilder) builderFactory.getBuilder(XSString.TYPE_NAME);
        XSString xsString = xssBuilder.buildObject(expectedXMLObjectQName, XSString.TYPE_NAME);
        xsString.setValue(expectedValue);
        
        Marshaller marshaller = marshallerFactory.getMarshaller(xsString);
        Element result = marshaller.marshall(xsString);
        
        Document document = parserPool.parse(XSStringTest.class.getResourceAsStream(testDocumentLocation));
        assertEquals("Marshalled XSString does not match example document", document, xsString);
    }
    
    /**
     * Tests Marshalling a string type.
     * 
     * @throws XMLParserException 
     * @throws UnmarshallingException 
     */
    public void testUnmarshall() throws XMLParserException, UnmarshallingException{
        Document document = parserPool.parse(XSStringTest.class.getResourceAsStream(testDocumentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        XSString xsString = (XSString) unmarshaller.unmarshall(document.getDocumentElement());
        
        assertEquals("Unexpected XSString QName", expectedXMLObjectQName, xsString.getElementQName());
        assertEquals("Unexpected XSString schema type", XSString.TYPE_NAME, xsString.getSchemaType());
        assertEquals("Unexpected value of XSString", xsString.getValue(), expectedValue);
    }
}