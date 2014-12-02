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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.saml1.core.AttributeQuery;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml1.core.AttributeQuery} objects.
 */
public class AttributeQueryUnmarshaller extends SubjectQueryUnmarshaller {

    /** Constructor */
    public AttributeQueryUnmarshaller() {
        super(SAMLConstants.SAML10P_NS, AttributeQuery.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {

        AttributeQuery attributeQuery = (AttributeQuery) parentSAMLObject;

        if (childSAMLObject instanceof AttributeDesignator) {
            attributeQuery.getAttributeDesignators().add((AttributeDesignator) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {

        AttributeQuery attributeQuery = (AttributeQuery) samlObject;

        if (attribute.getLocalName().equals(AttributeQuery.RESOURCE_ATTRIB_NAME)) {
            attributeQuery.setResource(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}