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

package org.opensaml.saml2.core.impl;

import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.saml2.core.Attribute} objects.
 */
public class AttributeMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Constructor.
     */
    public AttributeMarshaller() {
        super(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            marshaller operates on
     */
    protected AttributeMarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlElement, Element domElement) throws MarshallingException {
        Attribute attribute = (Attribute) samlElement;

        if (attribute.getName() != null) {
            domElement.setAttributeNS(null, Attribute.NAME_ATTTRIB_NAME, attribute.getName());
        }

        if (attribute.getNameFormat() != null) {
            domElement.setAttributeNS(null, Attribute.NAME_FORMAT_ATTRIB_NAME, attribute.getNameFormat());
        }

        if (attribute.getFriendlyName() != null) {
            domElement.setAttributeNS(null, Attribute.FRIENDLY_NAME_ATTRIB_NAME, attribute.getFriendlyName());
        }

        Attr attr;
        for (Entry<QName, String> entry : attribute.getUnknownAttributes().entrySet()) {
            attr = XMLHelper.constructAttribute(domElement.getOwnerDocument(), entry.getKey());
            attr.setValue(entry.getValue());
            domElement.setAttributeNodeNS(attr);
            if (Configuration.isIDAttribute(entry.getKey())
                    || attribute.getUnknownAttributes().isIDAttribute(entry.getKey())) {
                attr.getOwnerElement().setIdAttributeNode(attr, true);
            }
        }
    }
}