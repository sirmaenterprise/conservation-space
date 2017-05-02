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

package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.Company;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml2.metadata.EmailAddress;
import org.opensaml.saml2.metadata.GivenName;
import org.opensaml.saml2.metadata.SurName;
import org.opensaml.saml2.metadata.TelephoneNumber;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.ContactPersonImpl}.
 */
public class ContactPersonTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected company name */
    protected ContactPersonTypeEnumeration expectedPersonType;
    
    /** Count of EmailAddress subelements */
    protected int emailAddressCount = 2;
    
    /** Count of TelephoneNumber subelements */
    protected int telephoneNumberCount = 3;

    /**
     * Constructor
     */
    public ContactPersonTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/ContactPerson.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/ContactPersonChildElements.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedPersonType = ContactPersonTypeEnumeration.TECHNICAL;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ContactPerson person = (ContactPerson) unmarshallElement(singleElementFile);
        
        assertEquals("Contact type was not expected value", expectedPersonType, person.getType());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall()
    {
        ContactPerson person = (ContactPerson) unmarshallElement(childElementsFile);
        
        assertNotNull("Extension Element not present", person.getExtensions());
        assertNotNull("Company Element not present", person.getCompany());
        assertNotNull("GivenName not present", person.getGivenName());
        assertEquals("Email address count", emailAddressCount, person.getEmailAddresses().size());
        assertEquals("Telephone Number count", telephoneNumberCount, person.getTelephoneNumbers().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, ContactPerson.DEFAULT_ELEMENT_LOCAL_NAME);
        ContactPerson person = (ContactPerson) buildXMLObject(qname);
        
        person.setType(expectedPersonType);

        assertEquals(expectedDOM, person);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall()
    {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, ContactPerson.DEFAULT_ELEMENT_LOCAL_NAME);
        ContactPerson person = (ContactPerson) buildXMLObject(qname);
        
        person.setType(expectedPersonType);

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        person.setExtensions((Extensions) buildXMLObject(extensionsQName));
        
        QName companuQName = new QName(SAMLConstants.SAML20MD_NS, Company.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        person.setCompany((Company) buildXMLObject(companuQName));
        
        QName givenNameQName = new QName(SAMLConstants.SAML20MD_NS, GivenName.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        person.setGivenName((GivenName) buildXMLObject(givenNameQName));
        
        QName surnameQName = new QName(SAMLConstants.SAML20MD_NS, SurName.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        person.setSurName((SurName) buildXMLObject(surnameQName));
        
        QName teleQName = new QName(SAMLConstants.SAML20MD_NS, TelephoneNumber.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < telephoneNumberCount; i++) {
            person.getTelephoneNumbers().add((TelephoneNumber) buildXMLObject(teleQName));
        }
        
        QName emailQName = new QName(SAMLConstants.SAML20MD_NS, EmailAddress.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < emailAddressCount; i++) {
            person.getEmailAddresses().add((EmailAddress) buildXMLObject(emailQName));
        }
        
        assertEquals(expectedChildElementsDOM, person);
    }
}