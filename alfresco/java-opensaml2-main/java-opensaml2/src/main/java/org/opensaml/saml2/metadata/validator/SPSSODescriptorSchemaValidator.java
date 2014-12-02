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

package org.opensaml.saml2.metadata.validator;

import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xml.validation.ValidationException;

/**
 * Checks {@link org.opensaml.saml2.metadata.SPSSODescriptor} for Schema compliance.
 */
public class SPSSODescriptorSchemaValidator extends SSODescriptorSchemaValidator<SPSSODescriptor> {

    /** Constructor */
    public SPSSODescriptorSchemaValidator() {

    }

    /** {@inheritDoc} */
    public void validate(SPSSODescriptor spssoDescriptor) throws ValidationException {
        super.validate(spssoDescriptor);
        validateAttributeConsumingServices(spssoDescriptor);
    }

    /**
     * Checks that at least one Attribute Consuming Service is present.
     * 
     * @param spssoDescriptor
     * @throws ValidationException
     */
    protected void validateAttributeConsumingServices(SPSSODescriptor spssoDescriptor) throws ValidationException {
        if (spssoDescriptor.getAttributeConsumingServices() == null || spssoDescriptor.getAttributeConsumingServices().size() < 1) {
            throw new ValidationException("Must have one or more AttributeConsumingServices.");
        }
    }
}