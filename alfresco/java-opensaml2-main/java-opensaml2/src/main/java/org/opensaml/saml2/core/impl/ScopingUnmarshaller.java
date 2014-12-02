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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.IDPList;
import org.opensaml.saml2.core.RequesterID;
import org.opensaml.saml2.core.Scoping;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.Scoping} objects.
 */
public class ScopingUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor.
     */
    public ScopingUnmarshaller() {
        super(SAMLConstants.SAML20P_NS, Scoping.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected ScopingUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        Scoping scoping = (Scoping) samlObject;

        if (attribute.getLocalName().equals(Scoping.PROXY_COUNT_ATTRIB_NAME)) {
            scoping.setProxyCount(Integer.valueOf(attribute.getValue()));
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        Scoping scoping = (Scoping) parentSAMLObject;
        if (childSAMLObject instanceof IDPList) {
            scoping.setIDPList((IDPList) childSAMLObject);
        } else if (childSAMLObject instanceof RequesterID) {
            scoping.getRequesterIDs().add((RequesterID) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}