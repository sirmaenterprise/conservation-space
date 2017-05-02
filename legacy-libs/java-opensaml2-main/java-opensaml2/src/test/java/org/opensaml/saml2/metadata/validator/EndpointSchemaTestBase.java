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

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.AddtionalMetadataLocation}.
 */
public abstract class EndpointSchemaTestBase extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public EndpointSchemaTestBase() {

    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        Endpoint endpoint = (Endpoint) target;
        
        endpoint.setBinding("binding");
        endpoint.setLocation("location");
    }

    /**
     * Tests for absent Binding.
     * 
     * @throws ValidationException
     */
    public void testBindingFailure() throws ValidationException {
        Endpoint endpoint = (Endpoint) target;

        endpoint.setBinding(null);
        assertValidationFail("Binding was null, should raise a Validation Exception.");

        endpoint.setBinding("");
        assertValidationFail("Binding was empty string, should raise a Validation Exception.");

        endpoint.setBinding("   ");
        assertValidationFail("Binding was white space, should raise a Validation Exception.");
    }
    
    /**
     * Tests for absent Location.
     * 
     * @throws ValidationException
     */
    public void testLocationFailure() throws ValidationException {
        Endpoint endpoint = (Endpoint) target;

        endpoint.setLocation(null);
        assertValidationFail("Location was null, should raise a Validation Exception.");

        endpoint.setLocation("");
        assertValidationFail("Location was empty string, should raise a Validation Exception.");

        endpoint.setLocation("   ");
        assertValidationFail("Location was white space, should raise a Validation Exception.");
    }
}