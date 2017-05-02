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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.NameIDType} objects.
 */
public abstract class AbstractNameIDTypeUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** Constructor. */
    protected AbstractNameIDTypeUnmarshaller() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * 
     * @throws NullPointerException if any of the arguments are null (or empty in the case of String parameters)
     */
    protected AbstractNameIDTypeUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        NameIDType nameID = (NameIDType) samlObject;
        if (attribute.getLocalName().equals(NameID.NAME_QUALIFIER_ATTRIB_NAME)) {
            nameID.setNameQualifier(attribute.getValue());
        } else if (attribute.getLocalName().equals(NameID.SP_NAME_QUALIFIER_ATTRIB_NAME)) {
            nameID.setSPNameQualifier(attribute.getValue());
        } else if (attribute.getLocalName().equals(NameID.FORMAT_ATTRIB_NAME)) {
            nameID.setFormat(attribute.getValue());
        } else if (attribute.getLocalName().equals(NameID.SPPROVIDED_ID_ATTRIB_NAME)) {
            nameID.setSPProvidedID(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        NameIDType nameID = (NameIDType) samlObject;
        nameID.setValue(elementContent);
    }
}