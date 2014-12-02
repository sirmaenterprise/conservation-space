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
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.SubjectConfirmationData;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.SubjectConfirmationDataImpl}.
 */
public class SubjectConfirmationDataTest extends BaseSAMLObjectProviderTestCase {

    /** Expected NotBefore value */
    private DateTime expectedNotBefore;

    /** Expected NotOnOrAfter value */
    private DateTime expectedNotOnOrAfter;

    /** Expected Recipient value */
    private String expectedRecipient;

    /** Expected InResponseTo value */
    private String expectedInResponseTo;

    /** Expected Address value */
    private String expectedAddress;

    /** Constructor */
    public SubjectConfirmationDataTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/SubjectConfirmationData.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/SubjectConfirmationDataOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        expectedNotBefore = new DateTime(1984, 8, 26, 10, 01, 30, 43, ISOChronology.getInstanceUTC());
        expectedNotOnOrAfter = new DateTime(1984, 8, 26, 10, 11, 30, 43, ISOChronology.getInstanceUTC());
        expectedRecipient = "recipient";
        expectedInResponseTo = "inresponse";
        expectedAddress = "address";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SubjectConfirmationData subjectCD = (SubjectConfirmationData) unmarshallElement(singleElementFile);

        DateTime notBefore = subjectCD.getNotBefore();
        assertEquals("NotBefore was " + notBefore + ", expected " + expectedNotBefore, expectedNotBefore, notBefore);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        SubjectConfirmationData subjectCD = (SubjectConfirmationData) unmarshallElement(singleElementOptionalAttributesFile);

        DateTime notBefore = subjectCD.getNotBefore();
        assertEquals("NotBefore was " + notBefore + ", expected " + expectedNotBefore, expectedNotBefore, notBefore);

        DateTime notOnOrAfter = subjectCD.getNotOnOrAfter();
        assertEquals("NotOnOrAfter was " + notOnOrAfter + ", expected " + expectedNotOnOrAfter, expectedNotOnOrAfter,
                notOnOrAfter);

        String recipient = subjectCD.getRecipient();
        assertEquals("Recipient was " + recipient + ", expected " + expectedRecipient, expectedRecipient, recipient);

        String inResponseTo = subjectCD.getInResponseTo();
        assertEquals("InResponseTo was " + inResponseTo + ", expected " + expectedInResponseTo, expectedInResponseTo,
                inResponseTo);

        String address = subjectCD.getAddress();
        assertEquals("Address was " + address + ", expected " + expectedAddress, expectedAddress, address);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX);
        SubjectConfirmationData subjectCD = (SubjectConfirmationData) buildXMLObject(qname);

        subjectCD.setNotBefore(expectedNotBefore);
        assertEquals(expectedDOM, subjectCD);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX);
        SubjectConfirmationData subjectCD = (SubjectConfirmationData) buildXMLObject(qname);

        subjectCD.setNotBefore(expectedNotBefore);
        subjectCD.setNotOnOrAfter(expectedNotOnOrAfter);
        subjectCD.setRecipient(expectedRecipient);
        subjectCD.setInResponseTo(expectedInResponseTo);
        subjectCD.setAddress(expectedAddress);

        assertEquals(expectedOptionalAttributesDOM, subjectCD);
    }
}