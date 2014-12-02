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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

/**
 * A thread safe Unmarshaller for {@link org.opensaml.saml1.core.NameIdentifier} objects.
 */
public class NameIdentifierUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /**
     * Constructor
     */
    public NameIdentifierUnmarshaller() {
        super(SAMLConstants.SAML1_NS, NameIdentifier.DEFAULT_ELEMENT_LOCAL_NAME);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        NameIdentifier nameIdentifier = (NameIdentifier) samlObject;

        if (NameIdentifier.FORMAT_ATTRIB_NAME.equals(attribute.getLocalName())) {
            nameIdentifier.setFormat(attribute.getValue());
        } else if (NameIdentifier.NAMEQUALIFIER_ATTRIB_NAME.equals(attribute.getLocalName())) {
            nameIdentifier.setNameQualifier(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject samlObject, String elementContent) {
        NameIdentifier nameIdentifier = (NameIdentifier) samlObject;
        nameIdentifier.setNameIdentifier(elementContent);
    }
}