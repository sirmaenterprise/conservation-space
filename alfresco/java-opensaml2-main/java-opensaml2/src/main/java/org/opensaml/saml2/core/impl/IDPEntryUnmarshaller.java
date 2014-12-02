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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.IDPEntry;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.IDPEntry} objects.
 */
public class IDPEntryUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor.
     */
    public IDPEntryUnmarshaller() {
        super(SAMLConstants.SAML20P_NS, IDPEntry.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace URI of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     * @param elementLocalName the local name of either the schema type QName or element QName of the elements this
     *            unmarshaller operates on
     */
    protected IDPEntryUnmarshaller(String namespaceURI, String elementLocalName) {
        super(namespaceURI, elementLocalName);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        IDPEntry entry = (IDPEntry) samlObject;

        if (attribute.getLocalName().equals(IDPEntry.PROVIDER_ID_ATTRIB_NAME)) {
            entry.setProviderID(attribute.getValue());
        } else if (attribute.getLocalName().equals(IDPEntry.NAME_ATTRIB_NAME)) {
            entry.setName(attribute.getValue());
        } else if (attribute.getLocalName().equals(IDPEntry.LOC_ATTRIB_NAME)) {
            entry.setLoc(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}