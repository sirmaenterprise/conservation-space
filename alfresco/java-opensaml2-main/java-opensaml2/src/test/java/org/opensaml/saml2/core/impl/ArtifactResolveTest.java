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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;

/**
 *
 */
public class ArtifactResolveTest extends RequestTestBase {

    /**
     * Constructor
     *
     */
    public ArtifactResolveTest() {
        super();
        singleElementFile = "/data/org/opensaml/saml2/core/impl/ArtifactResolve.xml";
        singleElementOptionalAttributesFile = "/data/org/opensaml/saml2/core/impl/ArtifactResolveOptionalAttributes.xml";
        childElementsFile = "/data/org/opensaml/saml2/core/impl/ArtifactResolveChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ArtifactResolve.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ArtifactResolve ar = (ArtifactResolve) buildXMLObject(qname);
        
        super.populateRequiredAttributes(ar);
        
        assertEquals(expectedDOM, ar);
    }
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ArtifactResolve.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ArtifactResolve ar = (ArtifactResolve) buildXMLObject(qname);
        
        super.populateRequiredAttributes(ar);
        super.populateOptionalAttributes(ar);
        
        assertEquals(expectedOptionalAttributesDOM, ar);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        QName qname = new QName(SAMLConstants.SAML20P_NS, ArtifactResolve.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ArtifactResolve ar = (ArtifactResolve) buildXMLObject(qname);
        
        super.populateChildElements(ar);
        
        QName artifactQName = new QName(SAMLConstants.SAML20P_NS, Artifact.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        ar.setArtifact((Artifact) buildXMLObject(artifactQName));
        
        assertEquals(expectedChildElementsDOM, ar);
    }
 

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        ArtifactResolve ar = (ArtifactResolve) unmarshallElement(singleElementFile);
        
        assertNotNull("ArtifactResolve was null", ar);
        super.helperTestSingleElementUnmarshall(ar);
    }
    
    /** {@inheritDoc} */
    public void testSingleElementOptionalAttributesUnmarshall() {
        ArtifactResolve ar = (ArtifactResolve) unmarshallElement(singleElementOptionalAttributesFile);
        
        assertNotNull("ArtifactResolve was null", ar);
        super.helperTestSingleElementOptionalAttributesUnmarshall(ar);
    }


    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        ArtifactResolve ar = (ArtifactResolve) unmarshallElement(childElementsFile);
        
        assertNotNull("Artifact was null", ar.getArtifact());
        super.helperTestChildElementsUnmarshall(ar);
    }
}