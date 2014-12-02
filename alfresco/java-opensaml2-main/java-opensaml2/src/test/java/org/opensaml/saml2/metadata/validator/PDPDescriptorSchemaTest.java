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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.PDPDescriptor}.
 */
public class PDPDescriptorSchemaTest extends RoleDescriptorSchemaTestBase {

    /** Constructor */
    public PDPDescriptorSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, PDPDescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        validator = new PDPDescriptorSchemaValidator();
    }
    
    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        PDPDescriptor pdpDescriptor = (PDPDescriptor) target;
        AuthzService authzService = (AuthzService) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS,
                AuthzService.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        pdpDescriptor.getAuthzServices().add(authzService);
    }
    
    /**
     * Tests for AuthzService failure.
     * 
     * @throws ValidationException
     */
    public void testAuthzServiceFailure() throws ValidationException {
        PDPDescriptor pdpDescriptor = (PDPDescriptor) target;

        pdpDescriptor.getAuthzServices().clear();
        assertValidationFail("Authz Services list was empty, should raise Validation Exception");
    }
}