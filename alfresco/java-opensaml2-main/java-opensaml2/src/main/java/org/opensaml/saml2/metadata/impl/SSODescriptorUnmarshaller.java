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

package org.opensaml.saml2.metadata.impl;

import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.ManageNameIDService;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.SSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread safe Unmarshaller for {@link org.opensaml.saml2.metadata.SSODescriptor} objects.
 */
public abstract class SSODescriptorUnmarshaller extends RoleDescriptorUnmarshaller {

    /**
     * Constructor
     * 
     * @param targetNamespaceURI the namespaceURI of the SAMLObject this unmarshaller operates on
     * @param targetLocalName the local name of the SAMLObject this unmarshaller operates on
     */
    protected SSODescriptorUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentElement, XMLObject childElement) throws UnmarshallingException {
        SSODescriptor descriptor = (SSODescriptor) parentElement;
        if (childElement instanceof ArtifactResolutionService) {
            descriptor.getArtifactResolutionServices().add((ArtifactResolutionService) childElement);
        } else if (childElement instanceof SingleLogoutService) {
            descriptor.getSingleLogoutServices().add((SingleLogoutService) childElement);
        } else if (childElement instanceof ManageNameIDService) {
            descriptor.getManageNameIDServices().add((ManageNameIDService) childElement);
        } else if (childElement instanceof NameIDFormat) {
            descriptor.getNameIDFormats().add((NameIDFormat) childElement);
        } else {
            super.processChildElement(parentElement, childElement);
        }
    }
}