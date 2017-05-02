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

package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AssertionIDRefSchemaValidator}.
 */
public class AssertionIDRefSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AssertionIDRefSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, AssertionIDRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AssertionIDRefSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AssertionIDRef assertionIDRef = (AssertionIDRef) target;
        assertionIDRef.setAssertionID("id");
    }

    /**
     * Tests absent ID Reference failure.
     * 
     * @throws ValidationException
     */
    public void testIDFailure() throws ValidationException {
        AssertionIDRef assertionIDRef = (AssertionIDRef) target;

        assertionIDRef.setAssertionID(null);
        assertValidationFail("ID Ref was null, should raise a Validation Exception");

        assertionIDRef.setAssertionID("");
        assertValidationFail("ID Ref was empty string, should raise a Validation Exception");
        
        assertionIDRef.setAssertionID("    ");
        assertValidationFail("ID Ref was white space, should raise a Validation Exception");
    }
}