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

import org.opensaml.Configuration;
import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.saml2.core.AuthnStatement}.
 */
public class AuthnStatementMarshaller extends AbstractSAMLObjectMarshaller {

    /** Constructor. */
    public AuthnStatementMarshaller() {
        super(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected AuthnStatementMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        AuthnStatement authnStatement = (AuthnStatement) samlObject;

        if (authnStatement.getAuthnInstant() != null) {
            String authnInstantStr = Configuration.getSAMLDateFormatter().print(authnStatement.getAuthnInstant());
            domElement.setAttributeNS(null, AuthnStatement.AUTHN_INSTANT_ATTRIB_NAME, authnInstantStr);
        }

        if (authnStatement.getSessionIndex() != null) {
            domElement.setAttributeNS(null, AuthnStatement.SESSION_INDEX_ATTRIB_NAME, authnStatement.getSessionIndex());
        }

        if (authnStatement.getSessionNotOnOrAfter() != null) {
            String sessionNotOnOrAfterStr = Configuration.getSAMLDateFormatter().print(
                    authnStatement.getSessionNotOnOrAfter());
            domElement.setAttributeNS(null, AuthnStatement.SESSION_NOT_ON_OR_AFTER_ATTRIB_NAME, sessionNotOnOrAfterStr);
        }
    }
}