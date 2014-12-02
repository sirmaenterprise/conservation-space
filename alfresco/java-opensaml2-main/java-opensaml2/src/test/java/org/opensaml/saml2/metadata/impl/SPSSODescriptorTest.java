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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.ManageNameIDService;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * 
 */
public class SPSSODescriptorTest extends BaseSAMLObjectProviderTestCase {

    /** expected value for AuthnRequestSigned attribute */
    protected XSBooleanValue expectedAuthnRequestSigned;

    /** expected value for WantAssertionsSigned attribute */
    protected XSBooleanValue expectedWantAssertionsSigned;

    /** List of expected supported protocols */
    protected ArrayList<String> expectedSupportedProtocol;

    /** Expected cacheDuration value in miliseconds */
    protected long expectedCacheDuration;

    /** Expected validUntil value */
    protected DateTime expectedValidUntil;

    /**
     * Constructor
     */
    public SPSSODescriptorTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/SPSSODescriptor.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/SPSSODescriptorOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/SPSSODescriptorChildElements.xml";
    }

    protected void setUp() throws Exception {
        super.setUp();

        expectedAuthnRequestSigned = new XSBooleanValue(Boolean.TRUE, false);
        expectedWantAssertionsSigned = new XSBooleanValue(Boolean.TRUE, false);

        expectedSupportedProtocol = new ArrayList<String>();
        expectedSupportedProtocol.add("urn:foo:bar");
        expectedSupportedProtocol.add("urn:fooz:baz");

        expectedCacheDuration = 90000;
        expectedValidUntil = new DateTime(2005, 12, 7, 10, 21, 0, 0, ISOChronology.getInstanceUTC());
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        SPSSODescriptor descriptor = (SPSSODescriptor) unmarshallElement(singleElementFile);

        assertEquals("Supported protocols not equal to expected value", expectedSupportedProtocol, descriptor
                .getSupportedProtocols());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        SPSSODescriptor descriptor = (SPSSODescriptor) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("Supported protocols not equal to expected value", expectedSupportedProtocol, descriptor
                .getSupportedProtocols());
        assertEquals("AuthnRequestsSigned attribute was not expected value", expectedAuthnRequestSigned, descriptor
                .isAuthnRequestsSignedXSBoolean());
        assertEquals("WantAssertionsSigned attribute was not expected value", expectedWantAssertionsSigned, descriptor
                .getWantAssertionsSignedXSBoolean());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        SPSSODescriptor descriptor = (SPSSODescriptor) unmarshallElement(childElementsFile);

        assertNotNull("Extensions", descriptor.getExtensions());
        assertEquals("KeyDescriptor", 0, descriptor.getKeyDescriptors().size());
        assertNotNull("Organization child", descriptor.getOrganization());
        assertEquals("ContactPerson count", 2, descriptor.getContactPersons().size());

        assertEquals("ArtifactResolutionService count", 1, descriptor.getArtifactResolutionServices().size());
        assertEquals("SingleLogoutService count", 2, descriptor.getSingleLogoutServices().size());
        assertEquals("ManageNameIDService count", 4, descriptor.getManageNameIDServices().size());
        assertEquals("NameIDFormat count", 1, descriptor.getNameIDFormats().size());

        assertEquals("AssertionConsumerService count", 2, descriptor.getAssertionConsumerServices().size());
        assertEquals("AttributeConsumingService", 1, descriptor.getAttributeConsumingServices().size());
    }

    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        SPSSODescriptor descriptor = (SPSSODescriptor) buildXMLObject(qname);

        for (String protocol : expectedSupportedProtocol) {
            descriptor.addSupportedProtocol(protocol);
        }

        assertEquals(expectedDOM, descriptor);
    }

    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        SPSSODescriptor descriptor = (SPSSODescriptor) buildXMLObject(qname);

        descriptor.setAuthnRequestsSigned(expectedAuthnRequestSigned);
        descriptor.setWantAssertionsSigned(expectedWantAssertionsSigned);

        for (String protocol : expectedSupportedProtocol) {
            descriptor.addSupportedProtocol(protocol);
        }

        descriptor.setCacheDuration(expectedCacheDuration);
        descriptor.setValidUntil(expectedValidUntil);

        assertEquals(expectedOptionalAttributesDOM, descriptor);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        SPSSODescriptor descriptor = (SPSSODescriptor) buildXMLObject(qname);

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        descriptor.setExtensions((Extensions) buildXMLObject(extensionsQName));

        QName orgQName = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        descriptor.setOrganization((Organization) buildXMLObject(orgQName));

        QName contactQName = new QName(SAMLConstants.SAML20MD_NS, ContactPerson.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 2; i++) {
            descriptor.getContactPersons().add((ContactPerson) buildXMLObject(contactQName));
        }

        QName artResQName = new QName(SAMLConstants.SAML20MD_NS, ArtifactResolutionService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        descriptor.getArtifactResolutionServices().add((ArtifactResolutionService) buildXMLObject(artResQName));

        QName sloQName = new QName(SAMLConstants.SAML20MD_NS, SingleLogoutService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 2; i++) {
            descriptor.getSingleLogoutServices().add((SingleLogoutService) buildXMLObject(sloQName));
        }

        QName mngNameIDQName = new QName(SAMLConstants.SAML20MD_NS, ManageNameIDService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 4; i++) {
            descriptor.getManageNameIDServices().add((ManageNameIDService) buildXMLObject(mngNameIDQName));
        }

        QName nameIDFormatQName = new QName(SAMLConstants.SAML20MD_NS, NameIDFormat.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        descriptor.getNameIDFormats().add((NameIDFormat) buildXMLObject(nameIDFormatQName));

        QName assertConsumeQName = new QName(SAMLConstants.SAML20MD_NS,
                AssertionConsumerService.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 2; i++) {
            descriptor.getAssertionConsumerServices()
                    .add((AssertionConsumerService) buildXMLObject(assertConsumeQName));
        }

        QName attribConsumeQName = new QName(SAMLConstants.SAML20MD_NS,
                AttributeConsumingService.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        descriptor.getAttributeConsumingServices().add((AttributeConsumingService) buildXMLObject(attribConsumeQName));

        assertEquals(expectedChildElementsDOM, descriptor);
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        SPSSODescriptor descriptor = (SPSSODescriptor) buildXMLObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        
        // AuthnRequestsSigned
        descriptor.setAuthnRequestsSigned(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, descriptor.isAuthnRequestsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.isAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                descriptor.isAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true",
                descriptor.isAuthnRequestsSignedXSBoolean().toString());
        
        descriptor.setAuthnRequestsSigned(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, descriptor.isAuthnRequestsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.isAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                descriptor.isAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false",
                descriptor.isAuthnRequestsSignedXSBoolean().toString());
        
        descriptor.setAuthnRequestsSigned((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, descriptor.isAuthnRequestsSigned());
        assertNull("XSBooleanValue was not null", descriptor.isAuthnRequestsSignedXSBoolean());
        
        
        
        // WantAssertionsSigned
        descriptor.setWantAssertionsSigned(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, descriptor.getWantAssertionsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.getWantAssertionsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                descriptor.getWantAssertionsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true",
                descriptor.getWantAssertionsSignedXSBoolean().toString());
        
        descriptor.setWantAssertionsSigned(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, descriptor.getWantAssertionsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.getWantAssertionsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                descriptor.getWantAssertionsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false",
                descriptor.getWantAssertionsSignedXSBoolean().toString());
        
        descriptor.setWantAssertionsSigned((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, descriptor.getWantAssertionsSigned());
        assertNull("XSBooleanValue was not null", descriptor.getWantAssertionsSignedXSBoolean());
    }

}