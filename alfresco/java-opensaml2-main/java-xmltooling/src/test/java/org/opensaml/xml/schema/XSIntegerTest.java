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
import org.opensaml.xml.schema.impl.XSIntegerBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for {@link XSInteger}
 */
public class XSIntegerTest extends XMLObjectBaseTestCase {
    
    private String testDocumentLocation;
    private QName expectedXMLObjectQName;
    private Integer expectedValue;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception{
        super.setUp();
        testDocumentLocation = "/data/org/opensaml/xml/schema/xsInteger.xml";
        expectedXMLObjectQName = new QName("urn:example.org:foo", "bar", "foo");
        expectedValue = 1967;
    }

    /**
     * Tests Marshalling a integer type.
     * @throws MarshallingException 
     * @throws XMLParserException 
     */
    public void testMarshall() throws MarshallingException, XMLParserException{
        XSIntegerBuilder xsintBuilder = (XSIntegerBuilder) builderFactory.getBuilder(XSInteger.TYPE_NAME);
        XSInteger xsInteger = xsintBuilder.buildObject(expectedXMLObjectQName, XSInteger.TYPE_NAME);
        xsInteger.setValue(expectedValue);
        
        Marshaller marshaller = marshallerFactory.getMarshaller(xsInteger);
        Element result = marshaller.marshall(xsInteger);
        
        Document document = parserPool.parse(XSIntegerTest.class.getResourceAsStream(testDocumentLocation));
        assertEquals("Marshalled XSInteger does not match example document", document, xsInteger);
    }
    
    /**
     * Tests Marshalling a integer type.
     * 
     * @throws XMLParserException 
     * @throws UnmarshallingException 
     */
    public void testUnmarshall() throws XMLParserException, UnmarshallingException{
        Document document = parserPool.parse(XSIntegerTest.class.getResourceAsStream(testDocumentLocation));

        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
        XSInteger xsInteger = (XSInteger) unmarshaller.unmarshall(document.getDocumentElement());
        
        assertEquals("Unexpected XSInteger QName", expectedXMLObjectQName, xsInteger.getElementQName());
        assertEquals("Unexpected XSInteger schema type", XSInteger.TYPE_NAME, xsInteger.getSchemaType());
        assertEquals("Unexpected value of XSInteger", xsInteger.getValue(), expectedValue);
    }
}