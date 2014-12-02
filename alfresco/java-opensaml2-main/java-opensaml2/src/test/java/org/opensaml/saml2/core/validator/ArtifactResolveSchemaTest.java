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
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Artifact;
import org.opensaml.saml2.core.ArtifactResolve;

/**
 *
 */
public class ArtifactResolveSchemaTest extends RequestSchemaTestBase {

    /**
     * Constructor
     *
     */
    public ArtifactResolveSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, ArtifactResolve.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new ArtifactResolveSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        ArtifactResolve ar = (ArtifactResolve) target;
        Artifact artifact = (Artifact) buildXMLObject(new QName(SAMLConstants.SAML20P_NS, Artifact.DEFAULT_ELEMENT_LOCAL_NAME));
        ar.setArtifact(artifact);
    }
    
    
    /**
     *  Tests invalid Artifact child element.
     */
    public void testArtifactFailure() {
        ArtifactResolve ar = (ArtifactResolve) target;
        
        ar.setArtifact(null);
        assertValidationFail("Artifact child was null");
    }

}
