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
import org.opensaml.saml2.core.impl.AttributeMarshaller;
import org.opensaml.saml2.metadata.RequestedAttribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.saml2.metadata.RequestedAttribute} objects.
 */
public class RequestedAttributeMarshaller extends AttributeMarshaller {

    /** Constructor */
    public RequestedAttributeMarshaller() {
        super(SAMLConstants.SAML20MD_NS, RequestedAttribute.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     * @throws NullPointerException
     */
    protected RequestedAttributeMarshaller(String targetNamespaceURI, String targetLocalName)
            throws NullPointerException {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        RequestedAttribute requestedAttribute = (RequestedAttribute) samlObject;

        if (requestedAttribute.isRequiredXSBoolean() != null) {
            domElement.setAttributeNS(null, RequestedAttribute.IS_REQUIRED_ATTRIB_NAME,
                    requestedAttribute.isRequiredXSBoolean().toString());
        }

        super.marshallAttributes(samlObject, domElement);
    }
}