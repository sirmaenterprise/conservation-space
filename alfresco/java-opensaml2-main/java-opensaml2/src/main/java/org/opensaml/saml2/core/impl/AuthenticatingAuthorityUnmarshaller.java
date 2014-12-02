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
import org.opensaml.saml2.core.AuthenticatingAuthority;
import org.opensaml.xml.XMLObject;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.AuthenticatingAuthority}.
 */
public class AuthenticatingAuthorityUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    public AuthenticatingAuthorityUnmarshaller() {
        super(SAMLConstants.SAML20_NS, AuthenticatingAuthority.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected AuthenticatingAuthorityUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        AuthenticatingAuthority authenticatingAuthority = (AuthenticatingAuthority) samlObject;
        authenticatingAuthority.setURI(elementContent);
    }
}