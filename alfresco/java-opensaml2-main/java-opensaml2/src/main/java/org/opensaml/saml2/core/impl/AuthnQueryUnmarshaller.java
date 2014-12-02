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

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnQuery;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.AuthnQuery} objects.
 */
public class AuthnQueryUnmarshaller extends SubjectQueryUnmarshaller {

    /**
     * Constructor.
     */
    public AuthnQueryUnmarshaller() {
        super(SAMLConstants.SAML20P_NS, AuthnQuery.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected AuthnQueryUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        AuthnQuery query = (AuthnQuery) samlObject;

        if (attribute.getLocalName().equals(AuthnQuery.SESSION_INDEX_ATTRIB_NAME)) {
            query.setSessionIndex(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        AuthnQuery query = (AuthnQuery) parentSAMLObject;

        if (childSAMLObject instanceof RequestedAuthnContext) {
            query.setRequestedAuthnContext((RequestedAuthnContext) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}