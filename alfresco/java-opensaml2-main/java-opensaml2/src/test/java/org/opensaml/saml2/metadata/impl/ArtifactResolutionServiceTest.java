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
package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.xml.schema.XSBooleanValue;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.ArtifactResolutionServiceImpl}.
 */
public class ArtifactResolutionServiceTest extends BaseSAMLObjectProviderTestCase {
    
    protected String expectedBinding;
    protected String expectedLocation;
    protected String expectedResponseLocation;
    protected Integer expectedIndex;
    protected XSBooleanValue expectedIsDefault;
    
    /**
     * Constructor
     */
    public ArtifactResolutionServiceTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/ArtifactResolutionService.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/metadata/impl/ArtifactResolutionServiceOptionalAttributes.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedBinding = "urn:binding:foo";
        expectedLocation = "example.org";
        expectedResponseLocation = "example.org/response";
        expectedIndex = new Integer(3);
        expectedIsDefault = new XSBooleanValue(Boolean.TRUE, false);
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ArtifactResolutionService service = (ArtifactResolutionService) unmarshallElement(singleElementFile);
        
        assertEquals("Binding URI was not expected value", expectedBinding, service.getBinding());
        assertEquals("Location was not expected value", expectedLocation, service.getLocation());
        assertEquals("Index was not expected value", expectedIndex, service.getIndex());
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        ArtifactResolutionService service = (ArtifactResolutionService) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertEquals("Binding URI was not expected value", expectedBinding, service.getBinding());
        assertEquals("Location was not expected value", expectedLocation, service.getLocation());
        assertEquals("Index was not expected value", expectedIndex, service.getIndex());
        assertEquals("ResponseLocation was not expected value", expectedResponseLocation, service.getResponseLocation());
        assertEquals("isDefault was not expected value", expectedIsDefault, service.isDefaultXSBoolean());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, ArtifactResolutionService.DEFAULT_ELEMENT_LOCAL_NAME);
        ArtifactResolutionService service = (ArtifactResolutionService) buildXMLObject(qname);
        
        service.setBinding(expectedBinding);
        service.setLocation(expectedLocation);
        service.setIndex(expectedIndex);

        assertEquals(expectedDOM, service);
    }

    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, ArtifactResolutionService.DEFAULT_ELEMENT_LOCAL_NAME);
        ArtifactResolutionService service = (ArtifactResolutionService) buildXMLObject(qname);
        
        service.setBinding(expectedBinding);
        service.setLocation(expectedLocation);
        service.setIndex(expectedIndex);
        service.setResponseLocation(expectedResponseLocation);
        service.setIsDefault(expectedIsDefault);

        assertEquals(expectedOptionalAttributesDOM, service);
    }
    
    /**
     * Test the proper behavior of the XSBooleanValue attributes.
     */
    public void testXSBooleanAttributes() {
        ArtifactResolutionService ars = 
            (ArtifactResolutionService) buildXMLObject(ArtifactResolutionService.DEFAULT_ELEMENT_NAME);
        
        // isDefault attribute
        ars.setIsDefault(Boolean.TRUE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.TRUE, ars.isDefault());
        assertNotNull("XSBooleanValue was null", ars.isDefaultXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.TRUE, false),
                ars.isDefaultXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "true", ars.isDefaultXSBoolean().toString());
        
        ars.setIsDefault(Boolean.FALSE);
        assertEquals("Unexpected value for boolean attribute found", Boolean.FALSE, ars.isDefault());
        assertNotNull("XSBooleanValue was null", ars.isDefaultXSBoolean());
        assertEquals("XSBooleanValue was unexpected value", new XSBooleanValue(Boolean.FALSE, false),
                ars.isDefaultXSBoolean());
        assertEquals("XSBooleanValue string was unexpected value", "false", ars.isDefaultXSBoolean().toString());
        
        ars.setIsDefault((Boolean) null);
        assertEquals("Unexpected default value for boolean attribute found", Boolean.FALSE, ars.isDefault());
        assertNull("XSBooleanValue was not null", ars.isDefaultXSBoolean());
    }
}