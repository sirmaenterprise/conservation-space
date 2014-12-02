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
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml2.metadata.ServiceName;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.metadata.AttributeConsumingService}.
 */
public class AttributeConsumingServiceSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AttributeConsumingServiceSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20MD_NS, AttributeConsumingService.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX);
        validator = new AttributeConsumingServiceSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AttributeConsumingService attributeConsumingService = (AttributeConsumingService) target;
        ServiceName name = (ServiceName) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS, ServiceName.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20MD_PREFIX));
        RequestedAttribute attribute = (RequestedAttribute) buildXMLObject(new QName(SAMLConstants.SAML20MD_NS,
                RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX));
        attributeConsumingService.setIndex(5);
        attributeConsumingService.getNames().add(name);
        attributeConsumingService.getRequestAttributes().add(attribute);
    }

    /**
     * Tests for Index failure.
     * 
     * @throws ValidationException
     */
    public void testIndexFailure() throws ValidationException {
        AttributeConsumingService attributeConsumingService = (AttributeConsumingService) target;

        attributeConsumingService.setIndex(-3);
        assertValidationFail("Index was negative, should raise a Validation Exception.");
    }

    /**
     * Tests for Service Name Failure
     * 
     * @throws ValidationException
     */
    public void testServiceNameFailure() throws ValidationException {
        AttributeConsumingService attributeConsumingService = (AttributeConsumingService) target;

        attributeConsumingService.getNames().clear();
        assertValidationFail("Service Names list was empty, should raise a Validation Exception");
    }

    public void testRequestedAttributeFailure() throws ValidationException {
        AttributeConsumingService attributeConsumingService = (AttributeConsumingService) target;

        attributeConsumingService.getRequestAttributes().clear();
        assertValidationFail("Requested Attributes list was empty, should raise a Validation Exception");
    }
}