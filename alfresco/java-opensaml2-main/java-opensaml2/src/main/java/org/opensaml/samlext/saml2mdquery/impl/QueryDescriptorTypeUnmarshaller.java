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

import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.impl.RoleDescriptorUnmarshaller;
import org.opensaml.samlext.saml2mdquery.QueryDescriptorType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBooleanValue;
import org.w3c.dom.Attr;

public class QueryDescriptorTypeUnmarshaller extends RoleDescriptorUnmarshaller {

    /**
     * Constructor.
     * 
     * @param targetNamespaceURI the namespaceURI of the SAMLObject this unmarshaller operates on
     * @param targetLocalName the local name of the SAMLObject this unmarshaller operates on
     */
    protected QueryDescriptorTypeUnmarshaller(String targetNamespaceURI, String targetLocalName) {
        super(targetNamespaceURI, targetLocalName);
    }

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {
        QueryDescriptorType descriptor = (QueryDescriptorType) parentSAMLObject;

        if (childSAMLObject instanceof NameIDFormat) {
            descriptor.getNameIDFormat().add((NameIDFormat) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        QueryDescriptorType descriptor = (QueryDescriptorType) samlObject;

        if (attribute.getLocalName().equals(QueryDescriptorType.WANT_ASSERTIONS_SIGNED_ATTRIB_NAME)) {
            descriptor.setWantAssertionsSigned(XSBooleanValue.valueOf(attribute.getValue()));
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}