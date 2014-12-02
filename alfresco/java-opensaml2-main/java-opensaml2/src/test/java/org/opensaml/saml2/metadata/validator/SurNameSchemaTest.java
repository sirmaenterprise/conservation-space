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
import org.opensaml.saml2.metadata.SurName;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.SurName}.
 */
public class SurNameSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public SurNameSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, SurName.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        validator = new SurNameSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        SurName surName = (SurName) target;
        surName.setName("name");
    }

    /**
     * Tests for Name failure.
     * 
     * @throws ValidationException
     */
    public void testNameFailure() throws ValidationException {
        SurName surName = (SurName) target;

        surName.setName(null);
        assertValidationFail("Name was null, should raise a Validation Exception.");

        surName.setName("");
        assertValidationFail("Name was empty string, should raise a Validation Exception.");

        surName.setName("   ");
        assertValidationFail("Name was white space, should raise a Validation Exception.");
    }
}