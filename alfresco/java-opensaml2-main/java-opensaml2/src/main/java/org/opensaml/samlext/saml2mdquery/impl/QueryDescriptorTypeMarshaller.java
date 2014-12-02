/*
 * Copyright [2006] [University Corporation for Advanced Internet Development, Inc.]
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

package org.opensaml.samlext.saml2mdquery.impl;

import org.opensaml.saml2.metadata.impl.RoleDescriptorMarshaller;
import org.opensaml.samlext.saml2mdquery.QueryDescriptorType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/**
 * Marshaller for {@link QueryDescriptorType} objects.
 */
public abstract class QueryDescriptorTypeMarshaller extends RoleDescriptorMarshaller {
    
    /** Constructor. */
    protected QueryDescriptorTypeMarshaller(){
        super();
    }

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * 
     * @throws NullPointerException if any of the arguments are null (or empty in the case of String parameters)
     */
    protected QueryDescriptorTypeMarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject xmlObject, Element domElement) throws MarshallingException {
        QueryDescriptorType descriptor = (QueryDescriptorType) xmlObject;

        if (descriptor.getWantAssertionsSignedXSBoolean() != null) {
            domElement.setAttributeNS(null, QueryDescriptorType.WANT_ASSERTIONS_SIGNED_ATTRIB_NAME,
                    descriptor.getWantAssertionsSignedXSBoolean().toString());
        }
        
        super.marshallAttributes(xmlObject, domElement);
    }
}