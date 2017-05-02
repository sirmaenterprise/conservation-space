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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Attribute;
import org.opensaml.saml1.core.AttributeStatement;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AttributeStatementSchemaValidator}.
 */
public class AttributeStatementSchemaTest extends SubjectStatementSchemaTestBase {

    /** Constructor */
    public AttributeStatementSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AttributeStatementSchemaValidator();

    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        AttributeStatement attributeStatement = (AttributeStatement) target;
 
        QName qname = new QName(SAMLConstants.SAML1_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        attributeStatement.getAttributes().add((Attribute)buildXMLObject(qname));
    }
    
    public void testEmptyAttributeList() {
        AttributeStatement attributeStatement = (AttributeStatement) target;

        attributeStatement.getAttributes().clear();
        assertValidationFail("No Attribute elements - should fail");
    }

}