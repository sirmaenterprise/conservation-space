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

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.metadata.AdditionalMetadataLocation;
import org.opensaml.saml2.metadata.AffiliationDescriptor;
import org.opensaml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml2.metadata.AuthnAuthorityDescriptor;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.util.XMLHelper;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.metadata.impl.EntityDescriptorImpl}.
 */
public class EntityDescriptorTest extends BaseSAMLObjectProviderTestCase {

    /** Expected entityID value */
    protected String expectedEntityID;

    /** Expected ID value */
    protected String expectedID;

    /** Expected cacheDuration value in miliseconds */
    protected long expectedCacheDuration;

    /** Expected validUntil value */
    protected DateTime expectedValidUntil;

    /**
     * Constructor
     */
    public EntityDescriptorTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/EntityDescriptor.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/EntityDescriptorOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/EntityDescriptorChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedID = "id";
        expectedEntityID = "99ff33";
        expectedCacheDuration = 90000;
        expectedValidUntil = new DateTime(2005, 12, 7, 10, 21, 0, 0, ISOChronology.getInstanceUTC());
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EntityDescriptor descriptor = (EntityDescriptor) unmarshallElement(singleElementFile);

        String entityID = descriptor.getEntityID();
        assertEquals("entityID attribute has a value of " + entityID + ", expected a value of " + expectedEntityID,
                expectedEntityID, entityID);

        Long duration = descriptor.getCacheDuration();
        assertNull("cacheDuration attribute has a value of " + duration + ", expected no value", duration);

        DateTime validUntil = descriptor.getValidUntil();
        assertNull("validUntil attribute has a value of " + validUntil + ", expected no value", validUntil);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EntityDescriptor descriptor = (EntityDescriptor) unmarshallElement(singleElementOptionalAttributesFile);

        String entityID = descriptor.getEntityID();
        assertEquals("entityID attribute has a value of " + entityID + ", expected a value of " + expectedEntityID,
                expectedEntityID, entityID);

        String id = descriptor.getID();
        assertEquals("ID attribute has a value of " + id + ", expected a value of " + expectedID, expectedID, id);

        long duration = descriptor.getCacheDuration().longValue();
        assertEquals("cacheDuration attribute has a value of " + duration + ", expected a value of "
                + expectedCacheDuration, expectedCacheDuration, duration);

        DateTime validUntil = descriptor.getValidUntil();
        assertEquals("validUntil attribute value did not match expected value", 0, expectedValidUntil
                .compareTo(validUntil));
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EntityDescriptor descriptor = (EntityDescriptor) unmarshallElement(childElementsFile);

        assertNotNull("Extensions child", descriptor.getExtensions());
        assertNotNull("Signature child", descriptor.getSignature());
        assertEquals("IDPSSODescriptor count", 2, descriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).size());
        assertEquals("SPSSODescriptor count", 3, descriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).size());
        assertEquals("AuthnAuthorityDescriptor count", 2, descriptor.getRoleDescriptors(AuthnAuthorityDescriptor.DEFAULT_ELEMENT_NAME).size());
        assertEquals("AttributeAuthorityDescriptor count", 1, descriptor.getRoleDescriptors(AttributeAuthorityDescriptor.DEFAULT_ELEMENT_NAME).size());
        assertEquals("PDPDescriptor count", 2, descriptor.getRoleDescriptors(PDPDescriptor.DEFAULT_ELEMENT_NAME).size());
        assertNotNull("AffiliationDescriptor ", descriptor.getAffiliationDescriptor());
        assertNotNull("Organization ", descriptor.getOrganization());
        assertEquals("ContactPerson count", 1, descriptor.getContactPersons().size());
        assertEquals("AdditionalMetadataLocation count", 3, descriptor.getAdditionalMetadataLocations().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntityDescriptor descriptor = (EntityDescriptor) buildXMLObject(qname);

        descriptor.setEntityID(expectedEntityID);

        assertEquals(expectedDOM, descriptor);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntityDescriptor descriptor = (EntityDescriptor) buildXMLObject(qname);

        descriptor.setEntityID(expectedEntityID);
        descriptor.setID(expectedID);
        descriptor.setValidUntil(expectedValidUntil);
        descriptor.setCacheDuration(expectedCacheDuration);

        assertEquals(expectedOptionalAttributesDOM, descriptor);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntityDescriptor descriptor = (EntityDescriptor) buildXMLObject(qname);
        descriptor.setID(expectedID);
        descriptor.setEntityID(expectedEntityID);

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        descriptor.setExtensions((Extensions) buildXMLObject(extensionsQName));
        
        descriptor.setSignature( buildSignatureSkeleton() );
        
        QName idpSSOQName = new QName(SAMLConstants.SAML20MD_NS, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        QName spSSOQName = new QName(SAMLConstants.SAML20MD_NS, SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        QName authnAuthQName = new QName(SAMLConstants.SAML20MD_NS, AuthnAuthorityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        QName pdpQName = new QName(SAMLConstants.SAML20MD_NS, PDPDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        QName affilQName = new QName(SAMLConstants.SAML20MD_NS, AffiliationDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        descriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).add((IDPSSODescriptor) buildXMLObject(idpSSOQName));
        descriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).add((SPSSODescriptor) buildXMLObject(spSSOQName));
        descriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).add((SPSSODescriptor) buildXMLObject(spSSOQName));
        descriptor.getRoleDescriptors(AuthnAuthorityDescriptor.DEFAULT_ELEMENT_NAME).add((AuthnAuthorityDescriptor) buildXMLObject(authnAuthQName));
        descriptor.getRoleDescriptors(PDPDescriptor.DEFAULT_ELEMENT_NAME).add((PDPDescriptor) buildXMLObject(pdpQName));
        descriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).add((IDPSSODescriptor) buildXMLObject(idpSSOQName));
        descriptor.getRoleDescriptors(AttributeAuthorityDescriptor.DEFAULT_ELEMENT_NAME).add((AttributeAuthorityDescriptor) buildXMLObject(AttributeAuthorityDescriptor.DEFAULT_ELEMENT_NAME));
        descriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).add((SPSSODescriptor) buildXMLObject(spSSOQName));
        descriptor.getRoleDescriptors(AuthnAuthorityDescriptor.DEFAULT_ELEMENT_NAME).add((AuthnAuthorityDescriptor) buildXMLObject(authnAuthQName));
        descriptor.getRoleDescriptors(PDPDescriptor.DEFAULT_ELEMENT_NAME).add((PDPDescriptor) buildXMLObject(pdpQName));
        descriptor.setAffiliationDescriptor((AffiliationDescriptor) buildXMLObject(affilQName));
        
        QName orgQName = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        descriptor.setOrganization((Organization) buildXMLObject(orgQName));
        
        QName contactQName = new QName(SAMLConstants.SAML20MD_NS, ContactPerson.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        descriptor.getContactPersons().add((ContactPerson) buildXMLObject(contactQName));
        
        QName addMDQName = new QName(SAMLConstants.SAML20MD_NS, AdditionalMetadataLocation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        for (int i = 0; i < 3; i++) {
            descriptor.getAdditionalMetadataLocations().add((AdditionalMetadataLocation) buildXMLObject(addMDQName));
        }

        assertEquals(expectedChildElementsDOM, descriptor);
    }

    /**
     * Build a Signature skeleton to use in marshalling unit tests.
     * 
     * @return minimally populated Signature element
     */
    private Signature buildSignatureSkeleton() {
        Signature signature = (Signature) buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        return signature;
    }

}