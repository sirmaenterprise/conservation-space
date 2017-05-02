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

package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml2.metadata.RequestedAttribute;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.RequestedAttributeImpl}.
 */
public class RequestedAttributeTest extends BaseSAMLObjectProviderTestCase {

    /** Expected Name attribute value */
    protected String expectedName;

    /** Expected NameFormat attribute value */
    protected String expectedNameFormat;

    /** Expected FriendlyName attribute value */
    protected String expectedFriendlyName;

    /** Excpected isRequired attribute value */
    protected XSBooleanValue expectedIsRequired;

    /**
     * Constructor
     */
    public RequestedAttributeTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/RequestedAttribute.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/RequestedAttributeOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        expectedName = "attribName";
        expectedNameFormat = "urn:string";
        expectedFriendlyName = "Attribute Name";
        expectedIsRequired = new XSBooleanValue(Boolean.TRUE, false);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        RequestedAttribute attribute = (RequestedAttribute) unmarshallElement(singleElementFile);

        String name = attribute.getName();
        assertEquals("Name was " + name + ", expected " + expectedName, expectedName, name);

    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        RequestedAttribute requestedAttribute = (RequestedAttribute) unmarshallElement(singleElementOptionalAttributesFile);

        String name = requestedAttribute.getName();
        assertEquals("Name was " + name + ", expected " + expectedName, expectedName, name);

        String nameFormat = requestedAttribute.getNameFormat();
        assertEquals("NameFormat was " + nameFormat + ", expected " + expectedNameFormat, expectedNameFormat,
                nameFormat);

        String friendlyName = requestedAttribute.getFriendlyName();
        assertEquals("FriendlyName was " + friendlyName + ", expected " + expectedFriendlyName, expectedFriendlyName,
                friendlyName);

        boolean isRequired = requestedAttribute.isRequired().booleanValue();
        assertEquals("Is Required was " + isRequired + ", expected " + expectedIsRequired, expectedIsRequired,
                requestedAttribute.isRequiredXSBoolean());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        RequestedAttribute requestedAttribute = (RequestedAttribute) buildXMLObject(qname);

        requestedAttribute.setName(expectedName);

        assertEquals(expectedDOM, requestedAttribute);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20MD_PREFIX);
        RequestedAttribute requestedAttribute = (RequestedAttribute) buildXMLObject(qname);

        requestedAttribute.setName(expectedName);
        requestedAttribute.setNameFormat(expectedNameFormat);
        requestedAttribute.setFriendlyName(expectedFriendlyName);
        requestedAttribute.setIsRequired(expectedIsRequired);

        assertEquals(expectedOptionalAttributesDOM, requestedAttribute);
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        RequestedAttribute attrib = 
            (RequestedAttribute) buildXMLObject(RequestedAttribute.DEFAULT_ELEMENT_NAME);
        
        // isRequired attribute
        attrib.setIsRequired(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, attrib.isRequired());
        assertNotNull("XSBooleanValue was null", attrib.isRequiredXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                attrib.isRequiredXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true", attrib.isRequiredXSBoolean().toString());
        
        attrib.setIsRequired(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, attrib.isRequired());
        assertNotNull("XSBooleanValue was null", attrib.isRequiredXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                attrib.isRequiredXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false", attrib.isRequiredXSBoolean().toString());
        
        attrib.setIsRequired((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, attrib.isRequired());
        assertNull("XSBooleanValue was not null", attrib.isRequiredXSBoolean());
    }
}