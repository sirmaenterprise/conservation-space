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

import javax.xml.namespace.QName;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;

/**
 * A thread-safe unmarshaller for {@link org.opensaml.saml2.metadata.Endpoint} objects.
 */
public class EndpointUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    public EndpointUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        Endpoint endpoint = (Endpoint) samlObject;
        
        if (attribute.getLocalName().equals(Endpoint.BINDING_ATTRIB_NAME)) {
            endpoint.setBinding(attribute.getValue());
        } else if (attribute.getLocalName().equals(Endpoint.LOCATION_ATTRIB_NAME)) {
            endpoint.setLocation(attribute.getValue());
        } else if (attribute.getLocalName().equals(Endpoint.RESPONSE_LOCATION_ATTRIB_NAME)) {
            endpoint.setResponseLocation(attribute.getValue());
        } else {
            QName attribQName = XMLHelper.getNodeQName(attribute);
            if (attribute.isId()) {
               endpoint.getUnknownAttributes().registerID(attribQName);
            }
            endpoint.getUnknownAttributes().put(attribQName, attribute.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        Endpoint endpoint = (Endpoint) parentSAMLObject;

        endpoint.getUnknownXMLObjects().add(childSAMLObject);
    }
}