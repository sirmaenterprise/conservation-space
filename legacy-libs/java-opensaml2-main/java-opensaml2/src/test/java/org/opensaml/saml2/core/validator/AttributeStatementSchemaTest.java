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
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.AttributeStatementSchemaValidator}.
 */
public class AttributeStatementSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public AttributeStatementSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new AttributeStatementSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        AttributeStatement attributeStatement = (AttributeStatement) target;
        Attribute attribute = (Attribute) buildXMLObject(new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20_PREFIX));
        attributeStatement.getAttributes().add(attribute);
    }

    /**
     * Tests absent Attribute failure.
     * 
     * @throws ValidationException
     */
    public void testAttributeFailure() throws ValidationException {
        AttributeStatement attributeStatement = (AttributeStatement) target;

        attributeStatement.getAttributes().clear();
        assertValidationFail("Attribute list empty, should raise a Validation Exception");
    }
}