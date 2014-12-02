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

package org.opensaml.saml2.metadata.impl;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.metadata.OrganizationName} objects.
 */
public class OrganizationNameUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor
     */
    public OrganizationNameUnmarshaller() {
        super(SAMLConstants.SAML20MD_NS, OrganizationName.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor
     * 
     * @param namespaceURI
     * @param elementLocalName
     */
    protected OrganizationNameUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /**
     * {@inheritDoc}
     */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        if (attribute.getLocalName().equals(OrganizationName.LANG_ATTRIB_NAME)
                && attribute.getNamespaceURI().equals(SAMLConstants.XML_NS)) {
            OrganizationName name = (OrganizationName) samlObject;

            LocalizedString nameStr = name.getName();
            if (nameStr == null) {
                nameStr = new LocalizedString();
            }

            nameStr.setLanguage(attribute.getValue());
            name.setName(nameStr);
        }
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        OrganizationName name = (OrganizationName) samlObject;

        LocalizedString nameStr = name.getName();
        if (nameStr == null) {
            nameStr = new LocalizedString();
        }

        nameStr.setLocalizedString(elementContent);
        name.setName(nameStr);
    }
}