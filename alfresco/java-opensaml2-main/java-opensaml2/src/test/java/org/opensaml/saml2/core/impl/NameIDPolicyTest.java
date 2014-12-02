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
package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.core.impl.NameIDPolicyImpl}.
 */
public class NameIDPolicyTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected Format*/
    private String expectedFormat;

    /** Expected SPNameQualifer */
    private String expectedSPNameQualifer;

    /** Expected AllowCreate */
    private XSBooleanValue expectedAllowCreate;

    /**
     * Constructor
     */
    public NameIDPolicyTest() {
        singleElementFile = "/data/org/opensaml/saml2/core/impl/NameIDPolicy.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/NameIDPolicyOptionalAttributes.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedFormat = "urn:string:format";
        expectedSPNameQualifer = "urn:string:spname";
        expectedAllowCreate = new XSBooleanValue(Boolean.TRUE, false);

    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDPolicy.DEFAULT_ELEMENT_LOCAL_NAME);
        NameIDPolicy policy = (NameIDPolicy) buildXMLObject(qname);
        
        assertEquals(expectedDOM, policy);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, NameIDPolicy.DEFAULT_ELEMENT_LOCAL_NAME);
        NameIDPolicy policy = (NameIDPolicy) buildXMLObject(qname);
        
        policy.setFormat(expectedFormat);
        policy.setSPNameQualifier(expectedSPNameQualifer);
        policy.setAllowCreate(expectedAllowCreate);
        
        assertEquals(expectedOptionalAttributesDOM, policy);
    }
    
    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        NameIDPolicy policy = (NameIDPolicy) unmarshallElement(singleElementFile);
        
        assertNotNull(policy);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        NameIDPolicy policy = (NameIDPolicy) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Unmarshalled name Format URI attribute value was not the expected value", expectedFormat, policy.getFormat());
        assertEquals("Unmarshalled SPNameQualifier URI attribute value was not the expected value", expectedSPNameQualifer, policy.getSPNameQualifier());
        assertEquals("Unmarshalled AllowCreate attribute value was not the expected value", expectedAllowCreate, policy.getAllowCreateXSBoolean());
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        NameIDPolicy policy = (NameIDPolicy) buildXMLObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        
        // AllowCreate attribute
        policy.setAllowCreate(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, policy.getAllowCreate());
        assertNotNull("XSBooleanValue was null", policy.getAllowCreateXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                policy.getAllowCreateXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true", policy.getAllowCreateXSBoolean().toString());
        
        policy.setAllowCreate(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, policy.getAllowCreate());
        assertNotNull("XSBooleanValue was null", policy.getAllowCreateXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                policy.getAllowCreateXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false", policy.getAllowCreateXSBoolean().toString());
        
        policy.setAllowCreate((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, policy.getAllowCreate());
        assertNull("XSBooleanValue was not null", policy.getAllowCreateXSBoolean());
    }
}