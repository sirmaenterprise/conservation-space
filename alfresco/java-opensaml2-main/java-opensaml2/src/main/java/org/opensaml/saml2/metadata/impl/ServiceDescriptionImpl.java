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

import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.ServiceDescription;
import org.opensaml.xml.XMLObject;

/**
 * Concrete implementation of {@link org.opensaml.saml2.metadata.ServiceDescription}.
 */
public class ServiceDescriptionImpl extends AbstractSAMLObject implements ServiceDescription {

    /** Service description */
    private LocalizedString description;

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     * @param namespacePrefix
     */
    protected ServiceDescriptionImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public LocalizedString getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    public void setDescription(LocalizedString newDescription) {
        description = prepareForAssignment(description, newDescription);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}