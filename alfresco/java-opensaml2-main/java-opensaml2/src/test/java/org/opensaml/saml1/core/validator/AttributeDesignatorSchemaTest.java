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

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AttributeDesignatorSchemaValidator}.
 */
public class AttributeDesignatorSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AttributeDesignatorSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, AttributeDesignator.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AttributeDesignatorSchemaValidator();

    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        AttributeDesignator attributeDesignator = (AttributeDesignator) target;

        attributeDesignator.setAttributeName("Jimmy");
        attributeDesignator.setAttributeNamespace("Glaswegan");
    }
    
    public void testName() {
        AttributeDesignator attributeDesignator = (AttributeDesignator) target;

        attributeDesignator.setAttributeName("");
        assertValidationFail("AttributeName attribute is empty - should throw");

        attributeDesignator.setAttributeName(null);
        assertValidationFail("AttributeName attribute is null - should throw");
        

        attributeDesignator.setAttributeName("  ");
        assertValidationFail("AttributeName attribute is invalid  - should throw");
    }

    public void testNameSpace() {
        AttributeDesignator attributeDesignator = (AttributeDesignator) target;

        attributeDesignator.setAttributeNamespace(null);
        assertValidationFail("AttributeNamespace attribute is null - should throw");

        attributeDesignator.setAttributeNamespace("");
        assertValidationFail("AttributeNamespace attribute is empty - should throw");

        attributeDesignator.setAttributeNamespace("  ");
        assertValidationFail("AttributeNamespace attribute is invalid - should throw");
    }
}