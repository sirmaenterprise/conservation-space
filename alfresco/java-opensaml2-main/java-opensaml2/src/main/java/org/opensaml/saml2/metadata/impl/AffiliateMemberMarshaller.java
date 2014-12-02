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

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.AffiliateMember;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link org.opensaml.saml2.metadata.AffiliateMember} objects.
 */
public class AffiliateMemberMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor
     */
    public AffiliateMemberMarshaller() {
        super(SAMLConstants.SAML20MD_NS, AffiliateMember.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     */
    protected AffiliateMemberMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /**
     * {@inheritDoc}
     */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) throws MarshallingException {
        super.marshallElementContent(samlObject, domElement);

        AffiliateMember member = (AffiliateMember) samlObject;
        if (member.getID() != null) {
            domElement.appendChild(domElement.getOwnerDocument().createTextNode(member.getID()));
        }
    }
}