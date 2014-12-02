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
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;

/**
 *
 */
public class LogoutRequestTest extends RequestTestBase {
    
    /** Expected Reason attribute value */
    private String expectedReason;
    
    /** Expected NotOnOrAfter attribute value */
    private DateTime expectedNotOnOrAfter;
    
    /** Expected number of SessionIndex child elements */
    private int expectedNumSessionIndexes;

    /**
     * Constructor
     *
     */
    public LogoutRequestTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/LogoutRequest.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/LogoutRequestOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/LogoutRequestChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedReason = "urn:string:reason";
        expectedNotOnOrAfter = new DateTime(2006, 2, 21, 20, 45, 0, 0, ISOChronology.getInstanceUTC());
        expectedNumSessionIndexes = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        LogoutRequest req = (LogoutRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        
        assertEquals(expectedDOM, req);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        LogoutRequest req = (LogoutRequest) buildXMLObject(qname);
        
        super.populateRequiredAttributes(req);
        super.populateOptionalAttributes(req);
        req.setReason(expectedReason);
        req.setNotOnOrAfter(expectedNotOnOrAfter);
        
        assertEquals(expectedOptionalAttributesDOM, req);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        LogoutRequest req = (LogoutRequest) buildXMLObject(qname);
        
        super.populateChildElements(req);
        
        QName nameIDQName = new QName(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        req.setNameID((NameID) buildXMLObject(nameIDQName));
        
        QName sessionIndexQName = new QName(SAMLConstants.SAML20P_NS, SessionIndex.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        for (int i=0; i<expectedNumSessionIndexes; i++){
            req.getSessionIndexes().add((SessionIndex) buildXMLObject(sessionIndexQName));
        }
        
        assertEquals(expectedChildElementsDOM, req);
    }
    
    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        LogoutRequest req = (LogoutRequest) unmarshallElement(singleElementFile);
        
        assertNotNull("LogoutRequest was null", req);
        assertNull("Reason was not null", req.getReason());
        assertNull("NotOnOrAfter was not null", req.getNotOnOrAfter());
        super.helperTestSingleElementUnmarshall(req);
    }
 
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        LogoutRequest req = (LogoutRequest) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Unmarshalled Reason attribute was not the expectecd value", expectedReason, req.getReason());
        assertEquals("Unmarshalled NotOnOrAfter attribute was not the expectecd value", 0, expectedNotOnOrAfter.compareTo(req.getNotOnOrAfter()));
        super.helperTestSingleElementOptionalAttributesUnmarshall(req);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        LogoutRequest req = (LogoutRequest) unmarshallElement(childElementsFile);
        
        assertNotNull("Identifier was null", req.getNameID());
        assertEquals("Number of unmarshalled SessionIndexes was not the expected value", expectedNumSessionIndexes, req.getSessionIndexes().size());
        super.helperTestChildElementsUnmarshall(req);
    }
    
}
