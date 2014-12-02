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
import org.opensaml.xml.schema.impl.XSQNameBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for {@link XSQName}
 */
public class XSQNameTest extends XMLObjectBaseTestCase {
    
    private String testDocumentLocation;
    private QName expectedXMLObjectQName;
    private QName expectedValue;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception{
        super.setUp();
        testDocumentLocation = "/data/org/opensaml/xml/schema/xsQName.xml";
        expectedXMLObjectQName = new QName("urn:example.org:foo", "bar", "foo");
        expectedValue = new QName("urn:example.org:baz", "SomeValue", "baz");
    }

    /**
     * Tests Marshalling a QName type.
     * @throws MarshallingException 
     * @throws XMLParserException 
     */
    public void testMarshall() throws MarshallingException, XMLParserException{
        XSQNameBuilder xsQNameBuilder = (XSQNameBuilder) builderFactory.getBuilder(XSQName.TYPE_NAME);
        XSQName xsQName = xsQNameBuilder.buildObject(expectedXMLObjectQName, XSQName.TYPE_NAME);
        xsQName.setValue(expectedValue);
        
        Marshaller marshaller = marshallerFactory.getMarshaller(xsQName);
        Element result = marshaller.marshall(xsQName);
        
        Document document = parserPool.parse(XSQNameTest.class.getResourceAsStream(testDocumentLocation));
        assertEquals("Marshalled XSQName does not match example document", document, xsQName);
    }
    
    /**
     * Tests Unmarshalling a QName type.
     * 
     * @throws XMLParserException 
     * @throws UnmarshallingException 
     */
    public void testUnmarshall() throws XMLParserException, UnmarshallingException{
        Document document = parserPool.parse(XSQNameTest.class.getResourceAsStream(testDocumentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        XSQName xsQName = (XSQName) unmarshaller.unmarshall(document.getDocumentElement());
        
        assertEquals("Unexpected XSQName QName", expectedXMLObjectQName, xsQName.getElementQName());
        assertEquals("Unexpected XSQName schema type", XSQName.TYPE_NAME, xsQName.getSchemaType());
        assertEquals("Unexpected value of XSQName", expectedValue, xsQName.getValue());
    }
}