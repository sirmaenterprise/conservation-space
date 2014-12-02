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

/**
 * 
 */

package org.opensaml.saml1.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;

/**
 * 
 */
public class AttributeDesignatorTest extends BaseSAMLObjectProviderTestCase {

    /** name used to generate objects */
    private final QName qname;

    /** Value from test file */
    private final String expectedAttributeName;

    /** Value from test file */
    private final String expectedAttributeNamespace;

    /**
     * Constructor
     */
    public AttributeDesignatorTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml1/impl/singleAttributeDesignator.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml1/impl/singleAttributeDesignatorAttributes.xml";
        expectedAttributeName = "AttributeName";
        expectedAttributeNamespace = "namespace";
        qname = new QName(SAMLConstants.SAML1_NS, AttributeDesignator.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        AttributeDesignator ad = (AttributeDesignator) unmarshallElement(singleElementFile);

        assertNull("AttributeName", ad.getAttributeName());
        assertNull("AttributeNamespace", ad.getAttributeNamespace());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        AttributeDesignator ad = (AttributeDesignator) unmarshallElement(singleElementOptionalAttributesFile);

        assertEquals("AttributeName", expectedAttributeName, ad.getAttributeName());
        assertEquals("AttributeNamespace", expectedAttributeNamespace, ad.getAttributeNamespace());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        assertEquals(expectedDOM, buildXMLObject(qname));
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        AttributeDesignator ad = (AttributeDesignator) buildXMLObject(qname);

        ad.setAttributeName(expectedAttributeName);
        ad.setAttributeNamespace(expectedAttributeNamespace);
        assertEquals(expectedOptionalAttributesDOM, ad);
    }
   
}
