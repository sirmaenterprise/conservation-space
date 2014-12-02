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
import org.opensaml.saml1.core.RequestAbstractType;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.RequestSchemaValidator}.
 */
public abstract class RequestAbstractTypeSchemaTestBase extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public RequestAbstractTypeSchemaTestBase() {
        super();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        target = buildXMLObject(targetQName);
        RequestAbstractType request = (RequestAbstractType) target;
        request.setID("Ident");
        request.setIssueInstant(new DateTime());
    }
    

    public void testMissingID() {
        RequestAbstractType request = (RequestAbstractType) target;
        
        request.setID(null);
        assertValidationFail("RequestID is null, should raise a Validation Exception");
        
        request.setID("");
        assertValidationFail("RequestID is empty, should raise a Validation Exception");
        
        request.setID(" ");
        assertValidationFail("RequestID is invalid, should raise a Validation Exception");
    }

    public void testMissingIssueInstant() {
        RequestAbstractType request = (RequestAbstractType) target;
        request.setIssueInstant(null);
        assertValidationFail("Both IssueInstant attribute present, should raise a Validation Exception");
    }
    
}