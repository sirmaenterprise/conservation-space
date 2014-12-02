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
import org.opensaml.saml1.core.AttributeValue;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.AttributeSchemaValidator}.
 */
public class AttributeSchemaTest extends AttributeDesignatorSchemaTest {

    /** Constructor */
    public AttributeSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new AttributeSchemaValidator();

    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();

        Attribute attribute = (Attribute) target;
        
        XSStringBuilder attributeValueBuilder = (XSStringBuilder) builderFactory.getBuilder(XSString.TYPE_NAME);
        attribute.getAttributeValues().add(attributeValueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME));
    }
    
    public void testMissingValue() {
        Attribute attribute = (Attribute) target;
        attribute.getAttributeValues().clear();
        
        assertValidationFail("No AttributeValue elements");
    }
}