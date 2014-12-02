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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AssertionIDRequestService;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread safe Unmarshaller for {@link org.opensaml.saml2.metadata.PDPDescriptor} objects.
 */
public class PDPDescriptorUnmarshaller extends RoleDescriptorUnmarshaller {

    /**
     * Constructor
     */
    public PDPDescriptorUnmarshaller() {
        super(SAMLConstants.SAML20MD_NS, PDPDescriptor.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     */
    protected PDPDescriptorUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        PDPDescriptor descriptor = (PDPDescriptor) parentSAMLObject;

        if (childSAMLObject instanceof AuthzService) {
            descriptor.getAuthzServices().add((AuthzService) childSAMLObject);
        } else if (childSAMLObject instanceof AssertionIDRequestService) {
            descriptor.getAssertionIDRequestServices().add((AssertionIDRequestService) childSAMLObject);
        } else if (childSAMLObject instanceof NameIDFormat) {
            descriptor.getNameIDFormats().add((NameIDFormat) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}