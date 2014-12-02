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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.Response} objects.
 */
public class ResponseUnmarshaller extends StatusResponseTypeUnmarshaller {

    /**
     * Constructor.
     * 
     */
    public ResponseUnmarshaller() {
        super(SAMLConstants.SAML20P_NS, Response.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected ResponseUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        Response resp = (Response) parentSAMLObject;

        if (childSAMLObject instanceof Assertion) {
            resp.getAssertions().add((Assertion) childSAMLObject);
        } else if (childSAMLObject instanceof EncryptedAssertion) {
            resp.getEncryptedAssertions().add((EncryptedAssertion) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

}
