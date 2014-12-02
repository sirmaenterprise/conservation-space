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

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.ArtifactResponse;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Status;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.signature.Signature;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.ArtifactResponse}.
 */
public class ArtifactResponseUnmarshaller extends StatusResponseTypeUnmarshaller {

    /** Constructor. */
    public ArtifactResponseUnmarshaller() {
        super(SAMLConstants.SAML20P_NS, ArtifactResponse.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected ArtifactResponseUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        ArtifactResponse artifactResponse = (ArtifactResponse) parentSAMLObject;

        if (childSAMLObject instanceof Issuer) {
            artifactResponse.setIssuer((Issuer) childSAMLObject);
        } else if (childSAMLObject instanceof Signature) {
            artifactResponse.setSignature((Signature) childSAMLObject);
        } else if (childSAMLObject instanceof Extensions) {
            artifactResponse.setExtensions((Extensions) childSAMLObject);
        } else if (childSAMLObject instanceof Status) {
            artifactResponse.setStatus((Status) childSAMLObject);
        } else {
            artifactResponse.setMessage((SAMLObject) childSAMLObject);
        }
    }
}