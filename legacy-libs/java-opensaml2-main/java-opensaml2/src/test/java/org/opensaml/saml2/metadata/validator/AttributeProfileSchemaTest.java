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
import org.opensaml.saml2.metadata.AttributeProfile;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.AttributeProfile}.
 */
public class AttributeProfileSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AttributeProfileSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, AttributeProfile.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        validator = new AttributeProfileSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AttributeProfile attributeProfile = (AttributeProfile) target;
        attributeProfile.setProfileURI("profile uri");
    }

    /**
     * Tests for ProfileURI failure.
     * 
     * @throws ValidationException
     */
    public void testProfileURIFailure() throws ValidationException {
        AttributeProfile attributeProfile = (AttributeProfile) target;

        attributeProfile.setProfileURI(null);
        assertValidationFail("ProfileURI was null, should raise a Validation Exception.");

        attributeProfile.setProfileURI("");
        assertValidationFail("ProfileURI was empty string, should raise a Validation Exception.");

        attributeProfile.setProfileURI("   ");
        assertValidationFail("ProfileURI was white space, should raise a Validation Exception.");
    }
}