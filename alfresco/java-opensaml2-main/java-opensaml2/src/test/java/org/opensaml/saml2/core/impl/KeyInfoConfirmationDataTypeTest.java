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

package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.Configuration;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.KeyInfoConfirmationDataType;
import org.opensaml.xml.signature.KeyInfo;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.KeyInfoConfirmationDataTypeImpl}.
 */
public class KeyInfoConfirmationDataTypeTest extends BaseSAMLObjectProviderTestCase {

    /** Expected NotBefore value. */
    private DateTime expectedNotBefore;

    /** Expected NotOnOrAfter value. */
    private DateTime expectedNotOnOrAfter;

    /** Expected Recipient value. */
    private String expectedRecipient;

    /** Expected InResponseTo value. */
    private String expectedInResponseTo;

    /** Expected Address value. */
    private String expectedAddress;
    
    /** Expected xsi:type value. */
    private QName expectedType;
    
    /** Expected number of KeyInfo child elements. */
    private int expectedNumKeyInfoChildren;

    /** Constructor. */
    public KeyInfoConfirmationDataTypeTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/KeyInfoConfirmationDataType.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/KeyInfoConfirmationDataTypeOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/KeyInfoConfirmationDataTypeChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedNotBefore = new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC());
        expectedNotOnOrAfter = new DateTime(1984, 8, 26, 10, 11, 30, 43, ISOChronology.getInstanceUTC());
        expectedRecipient = "recipient";
        expectedInResponseTo = "inresponse";
        expectedAddress = "address";
        expectedType = new QName(SAMLConstants.SAML20_NS, "KeyInfoConfirmationDataType", SAMLConstants.SAML20_PREFIX);
        expectedNumKeyInfoChildren = 3;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        KeyInfoConfirmationDataType kicd = (KeyInfoConfirmationDataType) unmarshallElement(singleElementFile);
        assertNotNull("Object was null", kicd);
        
        assertEquals("Object xsi:type was not the expected value", expectedType, kicd.getSchemaType());

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        KeyInfoConfirmationDataType kicd = (KeyInfoConfirmationDataType) unmarshallElement(singleElementOptionalAttributesFile);

        DateTime notBefore = kicd.getNotBefore();
        assertEquals("NotBefore was " + notBefore + ", expected " + expectedNotBefore, expectedNotBefore, notBefore);

        DateTime notOnOrAfter = kicd.getNotOnOrAfter();
        assertEquals("NotOnOrAfter was " + notOnOrAfter + ", expected " + expectedNotOnOrAfter, expectedNotOnOrAfter,
                notOnOrAfter);

        String recipient = kicd.getRecipient();
        assertEquals("Recipient was " + recipient + ", expected " + expectedRecipient, expectedRecipient, recipient);

        String inResponseTo = kicd.getInResponseTo();
        assertEquals("InResponseTo was " + inResponseTo + ", expected " + expectedInResponseTo, expectedInResponseTo,
                inResponseTo);

        String address = kicd.getAddress();
        assertEquals("Address was " + address + ", expected " + expectedAddress, expectedAddress, address);
        
        assertEquals("Object xsi:type was not the expected value", expectedType, kicd.getSchemaType());
    }
    
    public void testChildElementsUnmarshall() {
        KeyInfoConfirmationDataType kicd = (KeyInfoConfirmationDataType) unmarshallElement(childElementsFile);
        
        assertEquals("Unexpected number of KeyInfo children", 3, kicd.getKeyInfos().size());
        assertEquals("Unexpected number of KeyInfo children", 3, kicd.getUnknownXMLObjects(KeyInfo.DEFAULT_ELEMENT_NAME).size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        KeyInfoConfirmationDataType kicd = buildXMLObject();

        assertEquals(expectedDOM, kicd);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        KeyInfoConfirmationDataType kicd = buildXMLObject();

        kicd.setNotBefore(expectedNotBefore);
        kicd.setNotOnOrAfter(expectedNotOnOrAfter);
        kicd.setRecipient(expectedRecipient);
        kicd.setInResponseTo(expectedInResponseTo);
        kicd.setAddress(expectedAddress);

        assertEquals(expectedOptionalAttributesDOM, kicd);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        KeyInfoConfirmationDataType kicd = buildXMLObject();
        
        for (int i=0; i<expectedNumKeyInfoChildren; i++) {
            KeyInfo keyinfo = (KeyInfo) buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
            kicd.getKeyInfos().add(keyinfo);
        }
        
        assertEquals(expectedChildElementsDOM, kicd);
    }
    
    /** {@inheritDoc} */
    public KeyInfoConfirmationDataType buildXMLObject() {
        SAMLObjectBuilder builder = 
            (SAMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(KeyInfoConfirmationDataType.TYPE_NAME);
        
        if(builder == null){
            fail("Unable to retrieve builder for object QName " + KeyInfoConfirmationDataType.TYPE_NAME);
        }
        return (KeyInfoConfirmationDataType) builder.buildObject();
    }

}