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

import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

/**
 * Checks {@link org.opensaml.saml2.metadata.OrganizationDisplayName} for Schema compliance.
 */
public class OrganizationDisplayNameSchemaValidator implements Validator<OrganizationDisplayName> {

    /** Constructor */
    public OrganizationDisplayNameSchemaValidator() {

    }

    /** {@inheritDoc} */
    public void validate(OrganizationDisplayName name) throws ValidationException {
        validateName(name);
    }

    /**
     * Checks that Name is present.
     * 
     * @param organizationDisplayName
     * @throws ValidationException
     */
    protected void validateName(OrganizationDisplayName organizationDisplayName) throws ValidationException {
        if (organizationDisplayName.getName() == null) {
            throw new ValidationException("Name required");
        }
    }
}