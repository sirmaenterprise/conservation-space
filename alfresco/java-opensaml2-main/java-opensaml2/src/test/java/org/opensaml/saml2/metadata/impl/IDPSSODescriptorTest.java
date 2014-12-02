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
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.AssertionIDRequestService;
import org.opensaml.saml2.metadata.AttributeProfile;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.ManageNameIDService;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.NameIDMappingService;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * 
 */
public class IDPSSODescriptorTest extends BaseSAMLObjectProviderTestCase {

    /** List of expected supported protocols */
    protected ArrayList<String> expectedSupportedProtocol;

    /** Expected cacheDuration value in miliseconds */
    protected long expectedCacheDuration;

    /** Expected validUntil value */
    protected DateTime expectedValidUntil;

    /** Expected error url */
    protected String expectedErrorURL;

    /** expected value for WantAuthnRequestSigned attribute */
    protected XSBooleanValue expectedWantAuthnReqSigned;

    /**
     * Constructor
     */
    public IDPSSODescriptorTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/IDPSSODescriptor.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/IDPSSODescriptorOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/IDPSSODescriptorChildElements.xml";
    }

    protected void setUp() throws Exception {
        super.setUp();

        expectedSupportedProtocol = new ArrayList<String>();
        expectedSupportedProtocol.add("urn:foo:bar");
        expectedSupportedProtocol.add("urn:fooz:baz");

        expectedCacheDuration = 90000;
        expectedValidUntil = new DateTime(2005, 12, 7, 10, 21, 0, 0, ISOChronology.getInstanceUTC());

        expectedErrorURL = "http://example.org";

        expectedWantAuthnReqSigned = new XSBooleanValue(Boolean.TRUE, false);
    }

    public void testSingleElementUnmarshall() {
        IDPSSODescriptor descriptor = (IDPSSODescriptor) unmarshallElement(singleElementFile);

        assertEquals("Supported protocols not equal to expected value", expectedSupportedProtocol, descriptor
                .getSupportedProtocols());
    }

    public void testSingleElementOptionalAttributesUnmarshall() {
        IDPSSODescriptor descriptor = (IDPSSODescriptor) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("Cache duration was not expected value", expectedCacheDuration, descriptor.getCacheDuration()
                .longValue());
        assertEquals("ValidUntil was not expected value", expectedValidUntil, descriptor.getValidUntil());
        assertEquals("WantAuthnRequestsSigned attribute was not expected value", expectedWantAuthnReqSigned, descriptor
                .getWantAuthnRequestsSignedXSBoolean());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        IDPSSODescriptor descriptor = (IDPSSODescriptor) unmarshallElement(childElementsFile);

        assertNotNull("Extensions", descriptor.getExtensions());
        assertNotNull("Organization child", descriptor.getOrganization());
        assertEquals("ContactPerson count", 2, descriptor.getContactPersons().size());

        assertEquals("ArtifactResolutionService count", 1, descriptor.getArtifactResolutionServices().size());
        assertEquals("SingleLogoutService count", 2, descriptor.getSingleLogoutServices().size());
        assertEquals("ManageNameIDService count", 4, descriptor.getManageNameIDServices().size());
        assertEquals("NameIDFormat count", 1, descriptor.getNameIDFormats().size());

        assertEquals("SingleSignOnService count", 3, descriptor.getSingleSignOnServices().size());
        assertEquals("NameIDMappingService count", 2, descriptor.getNameIDMappingServices().size());
        assertEquals("AssertionIDRequestService count", 3, descriptor.getAssertionIDRequestServices().size());
        assertEquals("AttributeProfile count", 3, descriptor.getAttributeProfiles().size());
    }

    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        IDPSSODescriptor descriptor = (IDPSSODescriptor) buildXMLObject(qname);

        for (String protocol : expectedSupportedProtocol) {
            descriptor.addSupportedProtocol(protocol);
        }
        descriptor.setWantAuthnRequestsSigned(expectedWantAuthnReqSigned);

        assertEquals(expectedDOM, descriptor);
    }

    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        IDPSSODescriptor descriptor = (IDPSSODescriptor) buildXMLObject(qname);

        for (String protocol : expectedSupportedProtocol) {
            descriptor.addSupportedProtocol(protocol);
        }

        descriptor.setCacheDuration(expectedCacheDuration);
        descriptor.setValidUntil(expectedValidUntil);
        descriptor.setErrorURL(expectedErrorURL);
        descriptor.setWantAuthnRequestsSigned(expectedWantAuthnReqSigned);

        assertEquals(expectedOptionalAttributesDOM, descriptor);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        IDPSSODescriptor descriptor = (IDPSSODescriptor) buildXMLObject(qname);

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        descriptor.setExtensions((Extensions) buildXMLObject(extensionsQName));

        QName orgQName = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
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

        QName ssoQName = new QName(SAMLConstants.SAML20MD_NS, SingleSignOnService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 3; i++) {
            descriptor.getSingleSignOnServices().add((SingleSignOnService) buildXMLObject(ssoQName));
        }

        QName nameIDMapQName = new QName(SAMLConstants.SAML20MD_NS, NameIDMappingService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 2; i++) {
            descriptor.getNameIDMappingServices().add((NameIDMappingService) buildXMLObject(nameIDMapQName));
        }

        QName assertIDReqQName = new QName(SAMLConstants.SAML20MD_NS, AssertionIDRequestService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 3; i++) {
            descriptor.getAssertionIDRequestServices()
                    .add((AssertionIDRequestService) buildXMLObject(assertIDReqQName));
        }

        QName attributeProlfileQName = new QName(SAMLConstants.SAML20MD_NS, AttributeProfile.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 3; i++) {
            descriptor.getAttributeProfiles().add((AttributeProfile) buildXMLObject(attributeProlfileQName));
        }
        assertEquals(expectedChildElementsDOM, descriptor);
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        IDPSSODescriptor descriptor = (IDPSSODescriptor) buildXMLObject(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        
        descriptor.setWantAuthnRequestsSigned(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, descriptor.getWantAuthnRequestsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.getWantAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                descriptor.getWantAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true",
                descriptor.getWantAuthnRequestsSignedXSBoolean().toString());
        
        descriptor.setWantAuthnRequestsSigned(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, descriptor.getWantAuthnRequestsSigned());
        assertNotNull("XSBooleanValue was null", descriptor.getWantAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                descriptor.getWantAuthnRequestsSignedXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false",
                descriptor.getWantAuthnRequestsSignedXSBoolean().toString());
        
        descriptor.setWantAuthnRequestsSigned((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, descriptor.getWantAuthnRequestsSigned());
        assertNull("XSBooleanValue was not null", descriptor.getWantAuthnRequestsSignedXSBoolean());
    }

}