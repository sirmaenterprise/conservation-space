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

package org.opensaml.saml2.metadata.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AffiliationDescriptor;
import org.opensaml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.EntityDescriptor}.
 */
public class EntityDescriptorSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public EntityDescriptorSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        validator = new EntityDescriptorSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        EntityDescriptor entityDescriptor = (EntityDescriptor) target;
        AttributeAuthorityDescriptor attributeAuthorityDescriptor = (AttributeAuthorityDescriptor) buildXMLObject(new QName(
                SAMLConstants.SAML20MD_NS, AttributeAuthorityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        entityDescriptor.getRoleDescriptors(AttributeAuthorityDescriptor.DEFAULT_ELEMENT_NAME).add(attributeAuthorityDescriptor);
        entityDescriptor.setEntityID("entity id");
    }

    /**
     * Tests for Entity ID failure.
     * 
     * @throws ValidationException
     */
    public void testEntityIDFailure() throws ValidationException {
        EntityDescriptor entityDescriptor = (EntityDescriptor) target;

        entityDescriptor.setEntityID(null);
        assertValidationFail("Entity ID was null, should raise a Validation Exception.");

        entityDescriptor.setEntityID("");
        assertValidationFail("Entity ID was empty string, should raise a Validation Exception.");

        entityDescriptor.setEntityID("   ");
        assertValidationFail("Entity ID was white space, should raise a Validation Exception.");
    }

    /**
     * Tests for Descriptor failure.
     * 
     * @throws ValidationException
     */
    public void testDescriptorFailure() throws ValidationException {
        EntityDescriptor entityDescriptor = (EntityDescriptor) target;

        AffiliationDescriptor affiliationDescriptor = (AffiliationDescriptor) buildXMLObject(new QName(
                SAMLConstants.SAML20MD_NS, AffiliationDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        entityDescriptor.setAffiliationDescriptor(affiliationDescriptor);
        assertValidationFail("Contains RoleDescriptor and AffiliationDescriptor, should raise Validation Exception.");

        entityDescriptor.getRoleDescriptors().clear();
        entityDescriptor.setAffiliationDescriptor(null);
        assertValidationFail("RoleDescriptors list was empty and AffiliationDescriptor was null, should raise a Validation Exception.");
    }
}