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
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.EntitiesDescriptorImpl}.
 */
public class EntitiesDescriptorTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Name attribute value */
    protected String expectedName;

    /** Expected ID attribute value */
    protected String expectedID;

    /** Expected cacheDuration value in miliseconds */
    protected long expectedCacheDuration;

    /** Expected validUntil value */
    protected DateTime expectedValidUntil;

    /** Expected number of child EntitiesDescriptors */
    protected int expectedEntitiesDescriptorsCount;

    /** Expected number of child EntityDescriptors */
    protected int expectedEntityDescriptorsCount;

    /**
     * Constructor
     */
    public EntitiesDescriptorTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/EntitiesDescriptor.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/EntitiesDescriptorOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/EntitiesDescriptorChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedID = "id";
        expectedName = "eDescName";
        expectedCacheDuration = 90000;
        expectedValidUntil = new DateTime(2005, 12, 7, 10, 21, 0, 0, ISOChronology.getInstanceUTC());
        expectedEntitiesDescriptorsCount = 3;
        expectedEntityDescriptorsCount = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EntitiesDescriptor entitiesDescriptorObj = (EntitiesDescriptor) unmarshallElement(singleElementFile);

        String name = entitiesDescriptorObj.getName();
        assertNull("Name attribute has a value of " + name + ", expected no value", name);

        Long duration = entitiesDescriptorObj.getCacheDuration();
        assertNull("cacheDuration attribute has a value of " + duration + ", expected no value", duration);

        DateTime validUntil = entitiesDescriptorObj.getValidUntil();
        assertNull("validUntil attribute has a value of " + validUntil + ", expected no value", validUntil);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        EntitiesDescriptor entitiesDescriptorObj = (EntitiesDescriptor) unmarshallElement(singleElementOptionalAttributesFile);

        String name = entitiesDescriptorObj.getName();
        assertEquals("Name attribute has a value of " + name + ", expected a value of " + expectedName, expectedName,
                name);

        String id = entitiesDescriptorObj.getID();
        assertEquals("ID attriubte has a value of " + id + ", expected a value of " + expectedID, expectedID, id);

        long duration = entitiesDescriptorObj.getCacheDuration().longValue();
        assertEquals("cacheDuration attribute has a value of " + duration + ", expected a value of "
                + expectedCacheDuration, expectedCacheDuration, duration);

        DateTime validUntil = entitiesDescriptorObj.getValidUntil();
        assertEquals("validUntil attribute value did not match expected value", 0, expectedValidUntil
                .compareTo(validUntil));
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) unmarshallElement(childElementsFile);

        assertNotNull("Signature", entitiesDescriptor.getSignature());
        assertNotNull("Extensions", entitiesDescriptor.getExtensions());
        assertEquals("Entities Descriptor child elements", expectedEntitiesDescriptorsCount, entitiesDescriptor
                .getEntitiesDescriptors().size());
        assertEquals("Entity Descriptor child elements", expectedEntityDescriptorsCount, entitiesDescriptor
                .getEntityDescriptors().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntitiesDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) buildXMLObject(qname);

        assertEquals(expectedDOM, entitiesDescriptor);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntitiesDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) buildXMLObject(qname);

        entitiesDescriptor.setName(expectedName);
        entitiesDescriptor.setID(expectedID);
        entitiesDescriptor.setCacheDuration(new Long(expectedCacheDuration));
        entitiesDescriptor.setValidUntil(expectedValidUntil);

        assertEquals(expectedOptionalAttributesDOM, entitiesDescriptor);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EntitiesDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) buildXMLObject(qname);
        entitiesDescriptor.setID(expectedID);
        
        entitiesDescriptor.setSignature( buildSignatureSkeleton() );

        QName extensionsQName = new QName(SAMLConstants.SAML20MD_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        entitiesDescriptor.setExtensions((Extensions) buildXMLObject(extensionsQName));
        
        QName entitiesDescriptorQName = new QName(SAMLConstants.SAML20MD_NS, EntitiesDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        QName entityDescriptorQName = new QName(SAMLConstants.SAML20MD_NS, EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        entitiesDescriptor.getEntitiesDescriptors().add((EntitiesDescriptor) buildXMLObject(entitiesDescriptorQName));
        entitiesDescriptor.getEntityDescriptors().add((EntityDescriptor) buildXMLObject(entityDescriptorQName));
        entitiesDescriptor.getEntitiesDescriptors().add((EntitiesDescriptor) buildXMLObject(entitiesDescriptorQName));
        entitiesDescriptor.getEntityDescriptors().add((EntityDescriptor) buildXMLObject(entityDescriptorQName));
        entitiesDescriptor.getEntitiesDescriptors().add((EntitiesDescriptor) buildXMLObject(entitiesDescriptorQName));
        assertEquals(expectedChildElementsDOM, entitiesDescriptor);
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