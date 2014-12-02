/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Attribute;

/**
 * Test case for creating, marshalling, and unmarshalling {@link org.opensaml.saml2.core.impl.AttributeImpl}.
 */
public class AttributeTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Name attribute value */
    protected String expectedName;

    /** Expected NameFormat attribute value */
    protected String expectedNameFormat;

    /** Expected FriendlyName attribute value */
    protected String expectedFriendlyName;

    /**
     * Constructor
     */
    public AttributeTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/Attribute.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/AttributeOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedName = "attribName";
        expectedNameFormat = "urn:string";
        expectedFriendlyName = "Attribute Name";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        Attribute attribute = (Attribute) unmarshallElement(singleElementFile);

        String name = attribute.getName();
        assertEquals("Name was " + name + ", expected " + expectedName, expectedName, name);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        Attribute attribute = (Attribute) unmarshallElement(singleElementOptionalAttributesFile);

        String name = attribute.getName();
        assertEquals("Name was " + name + ", expected " + expectedName, expectedName, name);

        String nameFormat = attribute.getNameFormat();
        assertEquals("NameFormat was " + nameFormat + ", expected " + expectedNameFormat, expectedNameFormat,
                nameFormat);

        String friendlyName = attribute.getFriendlyName();
        assertEquals("FriendlyName was " + friendlyName + ", expected " + expectedFriendlyName, expectedFriendlyName,
                friendlyName);
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Attribute attribute = (Attribute) buildXMLObject(qname);

        attribute.setName(expectedName);

        assertEquals(expectedDOM, attribute);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Attribute attribute = (Attribute) buildXMLObject(qname);

        attribute.setName(expectedName);
        attribute.setNameFormat(expectedNameFormat);
        attribute.setFriendlyName(expectedFriendlyName);

        assertEquals(expectedOptionalAttributesDOM, attribute);
    }
}