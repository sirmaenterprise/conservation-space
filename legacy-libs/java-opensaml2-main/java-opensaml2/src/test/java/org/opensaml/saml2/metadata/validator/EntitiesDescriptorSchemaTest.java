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
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.EntitiesDescriptor}.
 */
public class EntitiesDescriptorSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public EntitiesDescriptorSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, EntitiesDescriptor.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        validator = new EntitiesDescriptorSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) target;
        EntityDescriptor entityDescriptor = (EntityDescriptor) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS,
                EntityDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        entitiesDescriptor.getEntityDescriptors().add(entityDescriptor);
    }

    /**
     * Tests for EntityDescriptor failure.
     * 
     * @throws ValidationException
     */
    public void testMemberFailure() throws ValidationException {
        EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) target;

        entitiesDescriptor.getEntityDescriptors().clear();
        entitiesDescriptor.getEntitiesDescriptors().clear();
        assertValidationFail("EntityDescriptors and EntitiesDescriptors lists were empty, should raise a Validation Exception.");
    }
}