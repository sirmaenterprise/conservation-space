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

package org.opensaml.saml2.core.impl;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.EncryptedAttribute;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AttributeStatementImpl}.
 */
public class AttributeStatementTest extends BaseSAMLObjectProviderTestCase {

    /** Count of Attribute subelements. */
    private int expectedAttributeCount = 3;
    
    /** Count of EncryptedAttribute subelements. */
    private int expectedEncryptedAttributeCount = 3;


    /** Constructor. */
    public AttributeStatementTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/AttributeStatement.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/AttributeStatementChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AttributeStatement attributeStatement = (AttributeStatement) unmarshallElement(singleElementFile);

        assertNotNull(attributeStatement);
    }
    
    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        AttributeStatement attributeStatement = (AttributeStatement) unmarshallElement(childElementsFile);
        assertEquals("Attribute Count", expectedAttributeCount, attributeStatement.getAttributes().size());
        assertEquals("EncryptedAttribute Count", 
                expectedEncryptedAttributeCount, attributeStatement.getEncryptedAttributes().size());
    }


    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        AttributeStatement attributeStatement = 
            (AttributeStatement) buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);

        assertEquals(expectedDOM, attributeStatement);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        AttributeStatement attributeStatement = 
            (AttributeStatement) buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);

        attributeStatement.getAttributes()
            .add((Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME));
        attributeStatement.getEncryptedAttributes()
            .add((EncryptedAttribute) buildXMLObject(EncryptedAttribute.DEFAULT_ELEMENT_NAME));
        attributeStatement.getAttributes()
            .add((Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME));
        attributeStatement.getEncryptedAttributes()
            .add((EncryptedAttribute) buildXMLObject(EncryptedAttribute.DEFAULT_ELEMENT_NAME));
        attributeStatement.getEncryptedAttributes()
            .add((EncryptedAttribute) buildXMLObject(EncryptedAttribute.DEFAULT_ELEMENT_NAME));
        attributeStatement.getAttributes()
            .add((Attribute) buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME));

        assertEquals(expectedChildElementsDOM, attributeStatement);
    }
}