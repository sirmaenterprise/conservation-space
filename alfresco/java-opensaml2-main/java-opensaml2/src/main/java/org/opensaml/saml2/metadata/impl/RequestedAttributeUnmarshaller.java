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
import org.opensaml.saml2.core.impl.AttributeUnmarshaller;
import org.opensaml.saml2.metadata.RequestedAttribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBooleanValue;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.metadata.RequestedAttribute} objects.
 */
public class RequestedAttributeUnmarshaller extends AttributeUnmarshaller {

    public RequestedAttributeUnmarshaller() {
        super(SAMLConstants.SAML20MD_NS, RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     * @throws IllegalArgumentException
     */
    protected RequestedAttributeUnmarshaller(String targetNamespaceURI, String targetLocalName)
            throws IllegalArgumentException {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        RequestedAttribute requestedAttribute = (RequestedAttribute) samlObject;
        if (attribute.getLocalName().equals(RequestedAttribute.IS_REQUIRED_ATTRIB_NAME)) {
            requestedAttribute.setIsRequired(XSBooleanValue.valueOf(attribute.getValue()));
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}