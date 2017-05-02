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
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.NameIDMappingService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.IDPSSODescriptor}.
 */
public class IDPSSODescriptorSpecTest extends SSODescriptorSpecTestBase {

    /** Constructor */
    public IDPSSODescriptorSpecTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        validator = new IDPSSODescriptorSpecValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) target;
        SingleSignOnService singleSignOnService = (SingleSignOnService) buildXMLObject(new QName(
                SAMLConstants.SAML20MD_NS, SingleSignOnService.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        idpssoDescriptor.getSingleSignOnServices().add(singleSignOnService);
        NameIDMappingService nameIDMappingService = (NameIDMappingService) buildXMLObject(new QName(
                SAMLConstants.SAML20MD_NS, NameIDMappingService.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        idpssoDescriptor.getNameIDMappingServices().add(nameIDMappingService);
    }

    /**
     * Tests for Single Sign On Service Failure.
     * 
     * @throws ValidationException
     */
    public void testSingleSignOnFailure() throws ValidationException {
        IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) target;

        idpssoDescriptor.getSingleSignOnServices().get(0).setResponseLocation("location");
        assertValidationFail("ResponseLocation was present in SingleSignOnService, should raise Validation Exception.");
    }

    /**
     * Tests for Name ID Mapping Service Failure.
     * 
     * @throws ValidationException
     */
    public void testNameIDMappingFailure() throws ValidationException {
        IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) target;

        idpssoDescriptor.getNameIDMappingServices().get(0).setResponseLocation("location");
        assertValidationFail("ResponseLocation was present in NameIDMappingService, should raise Validation Exception.");
    }
}