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
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.saml2.core.LogoutRequest}.
 */
public class LogoutRequestMarshaller extends RequestAbstractTypeMarshaller {

    /**
     * Constructor.
     */
    public LogoutRequestMarshaller() {
        super(SAMLConstants.SAML20P_NS, LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected LogoutRequestMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        LogoutRequest req = (LogoutRequest) samlObject;

        if (req.getReason() != null){
            domElement.setAttributeNS(null, LogoutRequest.REASON_ATTRIB_NAME, req.getReason());
        }

        if (req.getNotOnOrAfter() != null) {
            String noaStr = Configuration.getSAMLDateFormatter().print(req.getNotOnOrAfter());
            domElement.setAttributeNS(null, LogoutRequest.NOT_ON_OR_AFTER_ATTRIB_NAME, noaStr);
        }

        super.marshallAttributes(samlObject, domElement);
    }

}
