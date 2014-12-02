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

package org.opensaml.ws;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLConfigurator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base test case for SOAP XMLObject unit tests.
 */
public abstract class BaseTestCase extends XMLTestCase {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(BaseTestCase.class);

    /** Parser pool. */
    protected static BasicParserPool parserPool;

    /** XMLObject builder factory. */
    protected static XMLObjectBuilderFactory builderFactory;

    /** XMLObject marshaller factory. */
    protected static MarshallerFactory marshallerFactory;

    /** XMLObject unmarshaller factory. */
    protected static UnmarshallerFactory unmarshallerFactory;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        XMLUnit.setIgnoreWhitespace(true);
        
        parserPool = new BasicParserPool();
        parserPool.setNamespaceAware(true);

        Class clazz = BaseTestCase.class;
        try {

            // SOAP 1.1 Configuration
            Document soap11Config = parserPool.parse(clazz.getResourceAsStream("/soap11-config.xml"));
            Document defaulfConfig = parserPool.parse(clazz.getResourceAsStream("/default-config.xml"));
            
            XMLConfigurator configurator = new XMLConfigurator();
            configurator.load(soap11Config);
            configurator.load(defaulfConfig);

            builderFactory = Configuration.getBuilderFactory();
            marshallerFactory = Configuration.getMarshallerFactory();
            unmarshallerFactory = Configuration.getUnmarshallerFactory();
        } catch (Exception e) {
            System.err.println("Can not initialize test suite, " + e.getMessage());
        }
    }

    /**
     * Asserts a given XMLObject is equal to an expected DOM. The XMLObject is marshalled and the resulting DOM object
     * is compared against the expected DOM object for equality.
     * 
     * @param expectedDOM the expected DOM
     * @param xmlObject the XMLObject to be marshalled and compared against the expected DOM
     */
    public void assertEquals(Document expectedDOM, XMLObject xmlObject) {
        assertEquals("Marshalled DOM was not the same as the expected DOM", expectedDOM, xmlObject);
    }

    /**
     * Asserts a given XMLObject is equal to an expected DOM. The XMLObject is marshalled and the resulting DOM object
     * is compared against the expected DOM object for equality.
     * 
     * @param failMessage the message to display if the DOMs are not equal
     * @param expectedDOM the expected DOM
     * @param xmlObject the XMLObject to be marshalled and compared against the expected DOM
     */
    public void assertEquals(String failMessage, Document expectedDOM, XMLObject xmlObject) {
        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        if (marshaller == null) {
            fail("Unable to locate marshaller for " + xmlObject.getElementQName()
                    + " can not perform equality check assertion");
        }

        try {
            Element generatedDOM = marshaller.marshall(xmlObject, parserPool.newDocument());
            if (log.isDebugEnabled()) {
                log.debug("Marshalled DOM was " + XMLHelper.nodeToString(generatedDOM));
            }
            assertXMLEqual(failMessage, expectedDOM, generatedDOM.getOwnerDocument());
        } catch (Exception e) {
            fail("Marshalling failed with the following error: " + e);
        }
    }

    /**
     * Builds the requested XMLObject.
     * 
     * @param objectQName name of the XMLObject
     * 
     * @return the build XMLObject
     */
    public XMLObject buildXMLObject(QName objectQName) {
        XMLObjectBuilder builder = Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null) {
            fail("Unable to retrieve builder for object QName " + objectQName);
        }
        return builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix());
    }
}