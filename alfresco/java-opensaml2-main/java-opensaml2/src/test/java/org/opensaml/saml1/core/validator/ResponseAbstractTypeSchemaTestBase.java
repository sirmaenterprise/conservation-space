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

package org.opensaml.saml1.core.validator;

import org.joda.time.DateTime;
import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.saml1.core.ResponseAbstractType;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.ResponseAbstractTypeSchemaValidator}.
 */
public abstract class ResponseAbstractTypeSchemaTestBase extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public ResponseAbstractTypeSchemaTestBase() {
        super();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        ResponseAbstractType response = (ResponseAbstractType) target;
        response.setID("Ident");
        response.setIssueInstant(new DateTime());
    }
    
    public void testMissingID() {
        ResponseAbstractType response = (ResponseAbstractType) target;

        response.setID(null);
        assertValidationFail("RequestID is null, should raise a Validation Exception");

        response.setID("");
        assertValidationFail("RequestID is empty, should raise a Validation Exception");

        response.setID(" ");
        assertValidationFail("RequestID is whitespace, should raise a Validation Exception");
    }

    public void testMissingIssueInstant() {
        ResponseAbstractType response = (ResponseAbstractType) target;
        response.setIssueInstant(null);
        assertValidationFail("No IssueInstant attribute present, should raise a Validation Exception");
    }
}