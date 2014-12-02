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
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.OrganizationURL;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.Organization}.
 */
public class OrganizationSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public OrganizationSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, Organization.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        validator = new OrganizationSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        Organization organization = (Organization) target;
        OrganizationName organizationName = (OrganizationName) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS,
                OrganizationName.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        OrganizationDisplayName organizationDisplayName = (OrganizationDisplayName) buildXMLObject(new QName(
                SAMLConstants.SAML20MD_NS, OrganizationDisplayName.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        OrganizationURL organizationURL = (OrganizationURL) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS,
                OrganizationURL.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        organization.getOrganizationNames().add(organizationName);
        organization.getDisplayNames().add(organizationDisplayName);
        organization.getURLs().add(organizationURL);
    }

    /**
     * Tests for Organization Name failure.
     * 
     * @throws ValidationException
     */
    public void testNameFailure() throws ValidationException {
        Organization organization = (Organization) target;

        organization.getOrganizationNames().clear();
        assertValidationFail("Organization Names list was empty, should raise a Validation Exception.");
    }

    /**
     * Tests for Organization Display Name failure.
     * 
     * @throws ValidationException
     */
    public void testDisplayNameFailure() throws ValidationException {
        Organization organization = (Organization) target;

        organization.getDisplayNames().clear();
        assertValidationFail("Organization Display Names list was empty, should raise a Validation Exception.");
    }

    /**
     * Tests for Organization URL failure.
     * 
     * @throws ValidationException
     */
    public void testURLFailure() throws ValidationException {
        Organization organization = (Organization) target;

        organization.getURLs().clear();
        assertValidationFail("Organization URLs list was empty, should raise a Validation Exception.");
    }
}