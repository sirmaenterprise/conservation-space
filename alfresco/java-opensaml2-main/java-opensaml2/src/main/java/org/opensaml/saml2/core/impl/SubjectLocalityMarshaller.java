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

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.SubjectLocality;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaler for {@link org.opensaml.saml2.core.SubjectLocality}.
 */
public class SubjectLocalityMarshaller extends AbstractSAMLObjectMarshaller {

    /** Constructor. */
    public SubjectLocalityMarshaller() {
        super(SAMLConstants.SAML20_NS, SubjectLocality.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected SubjectLocalityMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        SubjectLocality subjectLocality = (SubjectLocality) samlObject;
        if (subjectLocality.getAddress() != null) {
            domElement.setAttributeNS(null, SubjectLocality.ADDRESS_ATTRIB_NAME, subjectLocality.getAddress());
        }

        if (subjectLocality.getDNSName() != null) {
            domElement.setAttributeNS(null, SubjectLocality.DNS_NAME_ATTRIB_NAME, subjectLocality.getDNSName());
        }
    }
}