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

import org.opensaml.saml2.metadata.IndexedEndpoint;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBooleanValue;
import org.w3c.dom.Attr;

/**
 * A thread-safe unmarshaller for {@link org.opensaml.saml2.metadata.IndexedEndpoint}
 * objects.
 */
public class IndexedEndpointUnmarshaller extends EndpointUnmarshaller {

    /**
     * Constructor
     * 
     * @param targetNamespaceURI
     * @param targetLocalName
     */
    public IndexedEndpointUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        IndexedEndpoint iEndpoint = (IndexedEndpoint) samlObject;

        if (attribute.getLocalName().equals(IndexedEndpoint.INDEX_ATTRIB_NAME)) {
            iEndpoint.setIndex(Integer.valueOf(attribute.getValue()));
        } else if (attribute.getLocalName().equals(IndexedEndpoint.IS_DEFAULT_ATTRIB_NAME)) {
            iEndpoint.setIsDefault(XSBooleanValue.valueOf(attribute.getValue()));
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}