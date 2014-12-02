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

package org.opensaml.ws.soap.soap11;

import javax.xml.namespace.QName;

import org.opensaml.ws.BaseTestCase;
import org.opensaml.ws.soap.util.SOAPConstants;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests marshalling and unmarshalling SOAP messages.
 */
public class SOAPTest extends BaseTestCase {
    
    /** Path, on classpath, to SOAP message test document. */
    private String soapMessage;
    
    /** Path, on classpath, to SOAP fault test document. */
    private String soapFault;
    
    /** Path, on classpath, to SOAP fault test document. */
    private String soapFaultMarshall;
    
    private QName expectedFaultCode;
    
    private String expectedFaultString;
    
    private String expectedFaultActor;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        soapMessage = "/data/org/opensaml/ws/soap/soap11/SOAP.xml";
        soapFault = "/data/org/opensaml/ws/soap/soap11/SOAPFault.xml";
        soapFaultMarshall = "/data/org/opensaml/ws/soap/soap11/SOAPFaultMarshall.xml";
        
        expectedFaultCode = new QName(SOAPConstants.SOAP11_NS, "Server", SOAPConstants.SOAP11_PREFIX);
        expectedFaultString = "Server Error";
        expectedFaultActor = "http://ws.example.org/someActor";
    }
    
    /**
     * Test unmarshalling a SOAP message, dropping its DOM representation and then remarshalling it.
     * 
     * @throws XMLParserException thrown if the XML document can not be located or parsed into a DOM 
     * @throws UnmarshallingException thrown if the DOM can not be unmarshalled
     */
    public void testSOAPMessage() throws XMLParserException, UnmarshallingException{
        Document soapDoc = parserPool.parse(SOAPTest.class.getResourceAsStream(soapMessage));
        Element envelopeElem = soapDoc.getDocumentElement();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(envelopeElem);
        
        Envelope envelope = (Envelope) unmarshaller.unmarshall(envelopeElem);
        
        // Check to make sure everything unmarshalled okay
        QName encodingStyleName = new QName("http://schemas.xmlsoap.org/soap/envelope/", "encodingStyle");
        String encodingStyleValue = envelope.getUnknownAttributes().get(encodingStyleName);
        assertNotNull("Encoding style was null", encodingStyleValue);
        assertEquals("Encoding style had unexpected value", 
                "http://schemas.xmlsoap.org/soap/encoding/", encodingStyleValue);
        
        Header header = envelope.getHeader();
        assertNotNull("Header was null", header);
        assertEquals("Unexpected number of Header children", 1, header.getUnknownXMLObjects().size());
        
        Body body = envelope.getBody();
        assertNotNull("Body was null", body);
        assertEquals("Unexpected number of Body children", 1, body.getUnknownXMLObjects().size());
        
        // Drop the DOM and remarshall, hopefully we get the same document back
        envelope.releaseDOM();
        envelope.releaseChildrenDOM(true);
        assertEquals("Marshalled DOM was not the same as control DOM", soapDoc, envelope);
    }
    
    /**
     * Test unmarshalling a SOAP fault, dropping its DOM representation and then remarshalling it.
     * @throws XMLParserException thrown if the XML document can not be located or parsed into a DOM 
     * @throws UnmarshallingException thrown if the DOM can not be unmarshalled
     */
    public void testSOAPFault() throws XMLParserException, UnmarshallingException{
        Document soapFaultDoc = parserPool.parse(SOAPTest.class.getResourceAsStream(soapFault));
        Element envelopeElem = soapFaultDoc.getDocumentElement();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(envelopeElem);
        
        Envelope envelope = (Envelope) unmarshaller.unmarshall(envelopeElem);
        
        // Check to make sure everything unmarshalled okay
        Header header = envelope.getHeader();
        assertNull("Header was not null", header);
        
        Body body = envelope.getBody();
        assertNotNull("Body was null", body);
        assertEquals("Unexpected number of Body children", 1, body.getUnknownXMLObjects().size());
        
        Fault fault = (Fault) body.getUnknownXMLObjects().get(0);
        assertNotNull("Fault was null", fault);
        
        FaultActor actor = fault.getActor();
        assertNotNull("FaultActor was null", actor);
        assertEquals("FaultActor had unexpected value", expectedFaultActor, actor.getValue());
        
        FaultCode code = fault.getCode();
        assertNotNull("FaultCode was null", code);
        assertEquals("FaultCode had unexpected value", expectedFaultCode, code.getValue());
        
        FaultString message = fault.getMessage();
        assertNotNull("FaultString was null", message);
        assertEquals("FaultString had unexpected value", expectedFaultString, message.getValue());
        
        Detail detail = fault.getDetail();
        assertNotNull("Detail was null", detail);
        assertEquals("Unexpected number of Body children", 1, detail.getUnknownXMLObjects().size());
        
        // Drop the DOM and remarshall, hopefully we get the same document back
        envelope.releaseDOM();
        envelope.releaseChildrenDOM(true);
        assertEquals("Marshalled DOM was not the same as control DOM", soapFaultDoc, envelope);
    }
    
    /**
     * Test constructing and marshalling a SOAP fault message.
     * 
     * @throws MarshallingException  if the DOM can not b marshalled
     * @throws XMLParserException 
     */
    public void testSOAPFaultConstructAndMarshall() throws MarshallingException, XMLParserException {
        Document soapDoc = parserPool.parse(SOAPTest.class.getResourceAsStream(soapFaultMarshall));
        Element envelopeElem = soapDoc.getDocumentElement();
        
        Envelope envelope = (Envelope) buildXMLObject(Envelope.DEFAULT_ELEMENT_NAME);
        
        Body body = (Body) buildXMLObject(Body.DEFAULT_ELEMENT_NAME);
        envelope.setBody(body);
        
        Fault fault = (Fault) buildXMLObject(Fault.DEFAULT_ELEMENT_NAME);
        body.getUnknownXMLObjects().add(fault);
        
        FaultCode faultCode = (FaultCode) buildXMLObject(FaultCode.DEFAULT_ELEMENT_NAME);
        faultCode.setValue(expectedFaultCode);
        fault.setCode(faultCode);
        
        FaultString faultString = (FaultString) buildXMLObject(FaultString.DEFAULT_ELEMENT_NAME);
        faultString.setValue(expectedFaultString);
        fault.setMessage(faultString);
        
        FaultActor faultActor = (FaultActor) buildXMLObject(FaultActor.DEFAULT_ELEMENT_NAME);
        faultActor.setValue(expectedFaultActor);
        fault.setActor(faultActor);
        
        Detail detail = (Detail) buildXMLObject(Detail.DEFAULT_ELEMENT_NAME);
        fault.setDetail(detail);
        
        Element marshalledEnvelope = marshallerFactory.getMarshaller(envelope).marshall(envelope);
        assertEquals("Marshalled DOM was not the same as control DOM", soapDoc, envelope);
        
    }
}