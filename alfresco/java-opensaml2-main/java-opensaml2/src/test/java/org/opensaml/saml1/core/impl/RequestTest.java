/*
 * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
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

/**
 * 
 */
package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml1.core.AssertionArtifact;
import org.opensaml.saml1.core.AssertionIDReference;
import org.opensaml.saml1.core.AttributeQuery;
import org.opensaml.saml1.core.Request;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test in and around the {@link org.opensaml.saml1.core.Request} interface
 */
public class RequestTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    private final String expectedID;
    
    private final DateTime expectedIssueInstant;

    private final int expectedMinorVersion;
    
    public RequestTest() {
        expectedID = "ident";
        singleElementFile = "/data/org/opensaml/saml1/impl/singleRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleRequestAttributes.xml";
        expectedIssueInstant = new DateTime(1970, 1, 1, 0, 0, 0, 100, ISOChronology.getInstanceUTC());
        expectedMinorVersion = 1;
        qname = Request.DEFAULT_ELEMENT_NAME;
    }
    
    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Request request = (Request) unmarshallElement(singleElementFile);

        String id = request.getID();
        assertNull("ID attribute has value " + id + "expected no value", id);
        
        DateTime date = request.getIssueInstant();
        assertNull("IssueInstant attribute has a value of " + date + ", expected no value", date);

        assertNull("Query has value", request.getQuery());
        assertEquals("AssertionArtifact present", 0, request.getAssertionArtifacts().size());
        assertEquals("AssertionIDReferences present", 0, request.getAssertionIDReferences().size());
        assertNull("IssueInstance has value", request.getIssueInstant());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Request request = (Request) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("ID", expectedID, request.getID());
        assertEquals("MinorVersion", expectedMinorVersion, request.getMinorVersion());
        assertEquals("IssueInstant", expectedIssueInstant, request.getIssueInstant());
        
    }
    
    /**
     * Test a few Requests with children 
     */
    public void testSingleElementChildrenUnmarshall() {
        Request request; 
        
        request = (Request) unmarshallElement("/data/org/opensaml/saml1/impl/RequestWithAssertionArtifact.xml");
        
        assertNull("Query is not null", request.getQuery());
        assertEquals("AssertionId count", 0, request.getAssertionIDReferences().size());
        assertEquals("AssertionArtifact count", 2, request.getAssertionArtifacts().size());
        
        request = (Request) unmarshallElement("/data/org/opensaml/saml1/impl/RequestWithQuery.xml");
        
        assertNotNull("Query is null", request.getQuery());
        assertEquals("AssertionId count", 0, request.getAssertionIDReferences().size());
        assertEquals("AssertionArtifact count", 0, request.getAssertionArtifacts().size());
        
        request = (Request) unmarshallElement("/data/org/opensaml/saml1/impl/RequestWithAssertionIDReference.xml");
        assertNull("Query is not null", request.getQuery());
        assertNotNull("AssertionId", request.getAssertionIDReferences());
        assertEquals("AssertionId count", 3, request.getAssertionIDReferences().size());
        assertEquals("AssertionArtifact count", 0, request.getAssertionArtifacts().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        
        Request request = (Request) buildXMLObject(qname);

        request.setID(expectedID);
        request.setIssueInstant(expectedIssueInstant);
        assertEquals(expectedOptionalAttributesDOM, request);
    }

    /**
     * Test a few Requests with children 
     */
    public void testSingleElementChildrenMarshall() {
        QName oqname;
        Request request; 
        Document dom;
                
        
        try {
            dom = parser.parse(BaseSAMLObjectProviderTestCase.class
                        .getResourceAsStream("/data/org/opensaml/saml1/impl/RequestWithAssertionArtifact.xml"));
            request = (Request) buildXMLObject(qname); 
            oqname = AssertionArtifact.DEFAULT_ELEMENT_NAME;
            request.getAssertionArtifacts().add((AssertionArtifact) buildXMLObject(oqname));
            request.getAssertionArtifacts().add((AssertionArtifact) buildXMLObject(oqname));
            assertEquals(dom, request);
          
            dom = parser.parse(BaseSAMLObjectProviderTestCase.class
                    .getResourceAsStream("/data/org/opensaml/saml1/impl/RequestWithAssertionIDReference.xml"));
            request = (Request) buildXMLObject(qname); 
            oqname = AssertionIDReference.DEFAULT_ELEMENT_NAME;
            request.getAssertionIDReferences().add((AssertionIDReference) buildXMLObject(oqname));
            request.getAssertionIDReferences().add((AssertionIDReference) buildXMLObject(oqname));
            request.getAssertionIDReferences().add((AssertionIDReference) buildXMLObject(oqname));
            assertEquals(dom, request);

            dom = parser.parse(BaseSAMLObjectProviderTestCase.class
                    .getResourceAsStream("/data/org/opensaml/saml1/impl/RequestWithQuery.xml"));
            request = (Request) buildXMLObject(qname); 
            oqname = AttributeQuery.DEFAULT_ELEMENT_NAME;
            request.setQuery((AttributeQuery) buildXMLObject(oqname));
            assertEquals(dom, request);

        } catch (XMLParserException e) {
            fail(e.toString());
        }
    }
    
    public void testSignatureUnmarshall() {
        Request request = (Request) unmarshallElement("/data/org/opensaml/saml1/impl/RequestWithSignature.xml");
        
        assertNotNull("Request was null", request);
        assertNotNull("Signature was null", request.getSignature());
        assertNotNull("KeyInfo was null", request.getSignature().getKeyInfo());
    }
    
    public void testDOMIDResolutionUnmarshall() {
        Request request = (Request) unmarshallElement("/data/org/opensaml/saml1/impl/RequestWithSignature.xml");
        
        assertNotNull("Request was null", request);
        assertNotNull("Signature was null", request.getSignature());
        Document document = request.getSignature().getDOM().getOwnerDocument();
        Element idElem = request.getDOM();
        
        assertNotNull("DOM ID resolution returned null", document.getElementById(expectedID));
        assertTrue("DOM elements were not equal", idElem.isSameNode(document.getElementById(expectedID)));
    }

    public void testDOMIDResolutionMarshall() throws MarshallingException {
        Request request = (Request) buildXMLObject(Request.DEFAULT_ELEMENT_NAME);
        request.setID(expectedID);
        request.setQuery((AttributeQuery) buildXMLObject(AttributeQuery.DEFAULT_ELEMENT_NAME));
        
        marshallerFactory.getMarshaller(request).marshall(request);
        
        Document document = request.getQuery().getDOM().getOwnerDocument();
        Element idElem = request.getDOM();
        
        assertNotNull("DOM ID resolution returned null", document.getElementById(expectedID));
        assertTrue("DOM elements were not equal", idElem.isSameNode(document.getElementById(expectedID)));
    }

}
